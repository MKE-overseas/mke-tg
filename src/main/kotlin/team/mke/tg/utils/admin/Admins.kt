package team.mke.tg.utils.admin

import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.base.CommandHandler
import ru.raysmith.tgbot.core.handler.base.isCommand
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.Pagination
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.toChatId
import team.mke.tg.BaseTgUser
import team.mke.tg.getFullName
import team.mke.tg.suspendTransaction

val BotCommand.Companion.ADMIN_MENU get() = "admin"
val CallbackQuery.Companion.ADMINS_PAGE_PREFIX get() = "admins_pages_"
val CallbackQuery.Companion.ADMINS_PROVIDE_PREFIX get() = "admins_provide_"

val BotCommand.Companion.ADMINS get() = "admins"

suspend fun EventHandler.sendAdminsMessage(users: Iterable<BaseTgUser<*>>, action: MessageAction, page: Long = Pagination.PAGE_FIRST) = message(action) {
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

suspend fun CommandHandler.setupAdmins(user: BaseTgUser<*>, users: Iterable<BaseTgUser<*>>) {
    if (!user.isAdmin) return
    
    isCommand(BotCommand.ADMINS) {
        sendAdminsMessage(users, MessageAction.SEND)
    }
}

suspend fun CallbackQueryHandler.setupAdmins(tgUser: BaseTgUser<*>, usersSelector: () -> Iterable<BaseTgUser<*>>, userSelector: (userId: Long) -> BaseTgUser<*>?) {
    if (!tgUser.isAdmin) return

    isPage(CallbackQuery.ADMINS_PAGE_PREFIX) {
        sendAdminsMessage(usersSelector(), MessageAction.EDIT, it)
    }
    isDataStartWith(CallbackQuery.ADMINS_PROVIDE_PREFIX) { userId ->
        suspendTransaction {
            if (userId.toLong() == tgUser.id.value) {
                alert("Нельзя снять с себя роль администратора")
                return@suspendTransaction
            }
            userSelector(userId.toLong())?.apply {
                isAdmin = !isAdmin
                if (isAdmin) {
                    send(userId.toLong().toChatId()) {
                        textWithEntities {
                            italic("Вы стали администратором")
                        }
                    }
                }
                provideCommands()
            }
            sendAdminsMessage(usersSelector(), MessageAction.EDIT, getPreviousPage(CallbackQuery.ADMINS_PAGE_PREFIX))
        }
    }
}