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

// TODO handle error `bot can't initiate conversation with a user`

/** /admins */
val BotCommand.Companion.ADMINS get() = "admins"

val CallbackQuery.Companion.ADMINS_PAGE_PREFIX get() = "admins_pages_"
val CallbackQuery.Companion.ADMINS_PROVIDE_PREFIX get() = "admins_provide_"

context(_: BotContext<*>)
private suspend fun <U : TgUserWithBaseData<*>> EventHandler.sendAdminsMessage(users: Iterable<U>, filter: String?, action: MessageAction, page: Int = Pagination.PAGE_FIRST) = message(action) {
    val filtered = if (filter != null) users.filter {
        it.phone?.contains(filter) == true || it.id.value.toString() == filter || it.fullname(includeUsername = true).contains(filter, ignoreCase = true)
    } else users
    textWithEntities {
        text("Пользователи отмеченные галочками являются администраторами.").n()
        n()
        text("Для фильтрации используйте /${BotCommand.ADMINS} ").code("<filter>")
    }
    inlineKeyboard {
       pagination(filtered, CallbackQuery.ADMINS_PAGE_PREFIX + "${filter ?: ""}_", page, { rows = 10 }) { user ->
           val prefix = if (user.isAdmin) "☑ " else "🔲 "
           val name = user.fullname(includeUsername = true)
           button(prefix + name, CallbackQuery.ADMINS_PROVIDE_PREFIX + "${filter ?: ""}_" + user.id)
           button {
               text = "👤 Профиль"
               url = "tg://user?id=${user.id.value}"
           }
       }
    }
}

enum class AdminsFeatureState {
    COMMAND,
    LIST,
    ADMIN_TOGGLE,
}

/**
 * Добавляет меню по [команде][ADMINS] со списком пользователей для управления статусом администратора.
 * Доступно только администраторам.
 *
 * @param tgUser текущий пользователь контекста
 * @param userSelector лямбда реализующая [TgUserSelector]
 * @param usersSelector лямбда возвращающая список всех [*валидных*][team.mke.tg.TgUserWithBaseDataTable.valid] пользователей
 * */
data class AdminsFeature<U : TgUserWithBaseData<*>>(
    val tgUser: U,
    val userSelector: TgUserSelector<Long, U>,
    val usersSelector: context(Transaction) () -> Iterable<U>,
    val onStateChanged: EventHandler.(state: AdminsFeatureState) -> Unit = {}
) : BotFeature {
    override suspend fun handle(handler: EventHandler, handled: Boolean) {
        if (!tgUser.isAdmin) return
        when(handler) {
            is CallbackQueryHandler -> with(handler) {
                isDataStartWith(CallbackQuery.ADMINS_PAGE_PREFIX) { data ->
                    onStateChanged(AdminsFeatureState.LIST)
                    val (filter, page) = data.split("_")
                    sendAdminsMessage(transaction { usersSelector() }, filter.ifEmpty { null }, MessageAction.EDIT, page.drop(1).toInt())
                }
                isDataStartWith(CallbackQuery.ADMINS_PROVIDE_PREFIX) { data ->
                    suspendTransaction {
                        onStateChanged(AdminsFeatureState.ADMIN_TOGGLE)

                        val (filter, userId) = data.split("_")

                        if (userId.toLong() == tgUser.id.value) {
                            alert("Нельзя снять с себя роль администратора")
                            return@suspendTransaction
                        }
                        userSelector.select(userId.toLong())?.apply {
                            isAdmin = !isAdmin
                            if (isAdmin) {
                                send(chatId = userId.toLong().toChatId()) {
                                    textWithEntities {
                                        italic("Вы стали администратором")
                                    }
                                }
                            }
                            provideCommands()
                            sendAdminsMessage(usersSelector(), filter.ifEmpty { null }, MessageAction.EDIT, getPreviousPage(CallbackQuery.ADMINS_PAGE_PREFIX))
                        }
                    }
                }
            }
            is CommandHandler -> with(handler) {
                isCommand(BotCommand.ADMINS) { filter ->
                    onStateChanged(AdminsFeatureState.COMMAND)

                    if (filter != null && filter.length > 25) {
                        send("Фильтр не должен превышать 25 символов")
                        return@isCommand
                    }

                    sendAdminsMessage(transaction { usersSelector() }, filter, MessageAction.SEND)
                }
            }
            else -> error("AdminsFeature supports only CommandHandler and CallbackQueryHandler")
        }
    }
}
