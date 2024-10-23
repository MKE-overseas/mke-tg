package team.mke.tg.utils.ban

import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.base.CommandHandler
import ru.raysmith.tgbot.core.handler.base.isCommand
import ru.raysmith.tgbot.core.send
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.pagination.Pagination
import ru.raysmith.tgbot.utils.toChatId
import team.mke.tg.BaseTgUser
import team.mke.tg.getFullName
import team.mke.tg.utils.admin.sendAdminsMessage
import team.mke.utils.db.suspendTransaction

val BotCommand.Companion.BAN get() = "ban"
val BotCommand.Companion.UNBAN get() = "unban"

val BotCommand.Companion.BANS_MENU get() = "bans"
val CallbackQuery.Companion.BANS_PAGE_PREFIX get() = "bans_pages_"
val CallbackQuery.Companion.BANS_BAN_PREFIX get() = "bans_ban_"

suspend fun <U : BaseTgUser<*>> CommandHandler.setupBans(banCommandsImpl: BanCommands<U>) {
    if (banCommandsImpl.hasAccess(getChatIdOrThrow())) {
        isCommand(BotCommand.BAN) {
            val tgUser = banCommandsImpl.getRestrictedTgUser(getChatIdOrThrow(), command) ?: return@isCommand
            if (tgUser.isBan) {
                send("Пользователь уже заблокирован")
            } else {
                banCommandsImpl.ban(tgUser)
                send("Успешно")
            }
        }
        isCommand(BotCommand.UNBAN) {
            val tgUser = banCommandsImpl.getRestrictedTgUser(getChatIdOrThrow(), command) ?: return@isCommand
            if (!tgUser.isBan) {
                send("Пользователь разблокирован")
            } else {
                banCommandsImpl.unban(tgUser)
                send("Успешно")
            }
        }
    }
}

suspend fun <U : BaseTgUser<*>> EventHandler.sendBansMessage(users: Iterable<U>, action: MessageAction, page: Int = Pagination.PAGE_FIRST) = message(action) {
    suspendTransaction {
        text = "Отмеченные пользователи галочками — заблокированы"
        inlineKeyboard {
            pagination(users, CallbackQuery.BANS_PAGE_PREFIX, page) { user ->
                val prefix = if (user.isBan) "☑ " else "🔲 "
                val name = this@sendBansMessage.getChat(user.id.value.toChatId()).getFullName()
                button(prefix + name, CallbackQuery.BANS_BAN_PREFIX + user.id)
            }
        }
    }
}

suspend fun <U : BaseTgUser<*>> CommandHandler.setupBans(user: BaseTgUser<*>, users: Iterable<U>) {
    if (!user.isAdmin || user.isBan) return

    isCommand(BotCommand.BANS_MENU) {
        sendBansMessage(users, MessageAction.SEND)
    }
}

suspend fun <U : BaseTgUser<*>> CallbackQueryHandler.setupBans(
    tgUser: U,
    usersSelector: suspend () -> Iterable<U>,
    userSelector: suspend (userId: Long) -> U?
) {
    if (!tgUser.isAdmin || tgUser.isBan) return

    isPage(CallbackQuery.BANS_PAGE_PREFIX) {
        sendAdminsMessage(usersSelector(), MessageAction.EDIT, it)
    }
    isDataStartWith(CallbackQuery.BANS_BAN_PREFIX) { userId ->
        suspendTransaction {
            if (userId.toLong() == tgUser.id.value) {
                alert("Нельзя заблокировать себя")
                return@suspendTransaction
            }
            userSelector(userId.toLong())?.apply {
                isBan = !isBan
                if (!isBan) {
                    send(chatId = userId.toLong().toChatId()) {
                        textWithEntities {
                            italic("Вы были разблокированы")
                        }
                    }
                }
                provideCommands()
            }
            sendBansMessage(usersSelector(), MessageAction.EDIT, getPreviousPage(CallbackQuery.BANS_PAGE_PREFIX))
        }
    }
}