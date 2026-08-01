package team.mke.tg.utils.features

import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.raysmith.tgbot.core.BotContext
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.base.CommandHandler
import ru.raysmith.tgbot.core.handler.base.isCommand
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.BotFeature
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.n
import ru.raysmith.tgbot.utils.pagination.Pagination
import ru.raysmith.tgbot.utils.toChatId
import team.mke.tg.TgUserSelector
import team.mke.tg.TgUserWithBaseData

/** /bans */
val BotCommand.Companion.BANS_MENU get() = "bans"

val CallbackQuery.Companion.BANS_PAGE_PREFIX get() = "bans_pages_"
val CallbackQuery.Companion.BANS_BAN_PREFIX get() = "bans_ban_"

context(_: BotContext<*>)
private suspend fun <U : TgUserWithBaseData<*>> EventHandler.sendBansMessage(users: Iterable<U>, filter: String?, action: MessageAction, page: Int = Pagination.PAGE_FIRST) = message(action) {
    val filtered = if (filter != null) users.filter {
        it.phone?.contains(filter) == true || it.id.value.toString() == filter || it.fullname(includeUsername = true).contains(filter, ignoreCase = true)
    } else users
    textWithEntities {
        text("Пользователи отмеченные галочками являются заблокированными.").n()
        n()
        text("Для фильтрации используйте /${BotCommand.BANS_MENU} ").code("<filter>")
    }
    inlineKeyboard {
        pagination(filtered, CallbackQuery.BANS_PAGE_PREFIX + "${filter ?: ""}_", page, { rows = 10 }) { user ->
            val prefix = if (user.isBan) "☑ " else "🔲 "
            val name = (user.phone?.let { "($it) " } ?: "") + user.fullname(includeUsername = true)
            button(prefix + name, CallbackQuery.BANS_BAN_PREFIX + "${filter ?: ""}_" + user.id)
            button {
                text = "👤 Профиль"
                url = "tg://user?id=${user.id.value}"
            }
        }
    }
}

/** Состояние меню блокировки пользователей */
enum class BansFeatureState {

    /** Вызов команды */
    COMMAND,

    /** Просмотр списка пользователей */
    LIST,

    /** Изменение статуса блокировки пользователя */
    BAN_TOGGLE
}

/**
 * Добавляет меню по [команде][BANS_MENU] со списком пользователей для управления статусом блокировки.
 * Доступно только администраторам.
 *
 * @param tgUser текущий пользователь контекста
 * @param userSelector лямбда реализующая [TgUserSelector]
 * @param usersSelector лямбда возвращающая список всех [*валидных*][team.mke.tg.TgUserWithBaseDataTable.valid]
 *  пользователей
 * */
data class BansFeature<U : TgUserWithBaseData<*>>(
    val tgUser: U,
    val userSelector: TgUserSelector<Long, U>,
    val usersSelector: context(Transaction) () -> Iterable<U>,
    val onStateChanged: EventHandler.(state: BansFeatureState) -> Unit = {}
) : BotFeature {
    override suspend fun handle(handler: EventHandler, handled: Boolean) {
        if (!tgUser.isAdmin) return

        when(handler) {
            is CallbackQueryHandler -> with(handler) {
                isDataStartWith(CallbackQuery.BANS_PAGE_PREFIX) { data ->
                    onStateChanged(BansFeatureState.LIST)

                    val (filter, page) = data.split("_")
                    sendBansMessage(
                        transaction { usersSelector() },
                        filter.ifEmpty { null },
                        MessageAction.EDIT, page.drop(1).toInt()
                    )
                }
                isDataStartWith(CallbackQuery.BANS_BAN_PREFIX) { data ->
                    suspendTransaction {
                        onStateChanged(BansFeatureState.BAN_TOGGLE)

                        val (filter, userId) = data.split("_")

                        if (userId.toLong() == tgUser.id.value) {
                            alert("Нельзя заблокировать себя")
                            return@suspendTransaction
                        }
                        userSelector.select(userId.toLong())?.apply {
                            isBan = !isBan
                            if (!isBan) {
                                send(chatId = userId.toLong().toChatId()) {
                                    textWithEntities {
                                        italic("Вы были разблокированы")
                                    }
                                }
                            }
                            provideCommands()
                            sendBansMessage(usersSelector(), filter.ifEmpty { null }, MessageAction.EDIT, getPreviousPage(CallbackQuery.BANS_PAGE_PREFIX))
                        }
                    }
                }
            }
            is CommandHandler -> with(handler) {
                isCommand(BotCommand.BANS_MENU) { filter ->
                    onStateChanged(BansFeatureState.COMMAND)

                    if (filter != null && filter.length > 25) {
                        send("Фильтр не должен превышать 25 символов")
                        return@isCommand
                    }

                    sendBansMessage(transaction { usersSelector() }, filter, MessageAction.SEND)
                }
            }
            else -> error("BansFeature supports only CommandHandler and CallbackQueryHandler")
        }
    }
}