package team.mke.tg.utils.admin

import org.jetbrains.exposed.sql.transactions.transaction
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.base.CommandHandler
import ru.raysmith.tgbot.core.handler.base.isCommand
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.BotFeature
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.pagination.Pagination
import ru.raysmith.tgbot.utils.toChatId
import team.mke.tg.*
import team.mke.utils.exposed.suspendTransaction

/** /admin */
val BotCommand.Companion.ADMIN_MENU get() = "admin"

/** /admins */
val BotCommand.Companion.ADMINS get() = "admins"


val CallbackQuery.Companion.ADMINS_PAGE_PREFIX get() = "admins_pages_"
val CallbackQuery.Companion.ADMINS_PROVIDE_PREFIX get() = "admins_provide_"


suspend fun <U : BaseTgUser<*>> EventHandler.sendAdminsMessage(users: Iterable<U>, action: MessageAction, page: Int = Pagination.PAGE_FIRST) = message(action) {
    suspendTransaction {
        text = "Отмеченные пользователи галочками являются администраторами"
        inlineKeyboard {
            pagination(users, CallbackQuery.ADMINS_PAGE_PREFIX, page) { user ->
                val prefix = if (user.isAdmin) "☑ " else "🔲 "
                val name = (user.phone?.let { "($it) " } ?: "") + this@sendAdminsMessage.getChat(user.id.value.toChatId()).getFullName()
                button(prefix + name, CallbackQuery.ADMINS_PROVIDE_PREFIX + user.id)
            }
        }
    }
}

/**
 * Добавляет меню по [команде][ADMINS] со списком пользователей для управления статусом администратора.
 * Доступно только администраторам.
 *
 * @param tgUser текущий пользователь контекста
 * @param userSelector лямбда реализующая [TgUserSelector]
 * @param usersSelector лямбда возвращающая список всех [*валидных*][BaseTgUserTable.valid] пользователей
 * */
data class AdminsFeature<U : BaseTgUser<*>>(val tgUser: U, val userSelector: TgUserSelector<Long, U>, val usersSelector: () -> Iterable<U>) : BotFeature {
    override suspend fun handle(handler: EventHandler, handled: Boolean) {
        if (!tgUser.isAdmin) return

        when(handler) {
            is CallbackQueryHandler -> with(handler) {
                isPage(CallbackQuery.ADMINS_PAGE_PREFIX) {
                    sendAdminsMessage(usersSelector(), MessageAction.EDIT, it)
                }
                isDataStartWith(CallbackQuery.ADMINS_PROVIDE_PREFIX) { userId ->
                    suspendTransaction {
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
                            sendAdminsMessage(usersSelector(), MessageAction.EDIT, getPreviousPage(CallbackQuery.ADMINS_PAGE_PREFIX))
                        }
                    }
                }
            }
            is CommandHandler -> with(handler) {
                isCommand(BotCommand.ADMINS) {
                    sendAdminsMessage(transaction { usersSelector() }, MessageAction.SEND)
                }
            }
            else -> error("AdminsFeature supports only CommandHandler and CallbackQueryHandler")
        }
    }
}