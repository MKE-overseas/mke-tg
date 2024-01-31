package team.mke.tg.utils.ban

import org.jetbrains.exposed.sql.transactions.transaction
import ru.raysmith.tgbot.core.Bot
import ru.raysmith.tgbot.core.BotContext
import ru.raysmith.tgbot.core.send
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.bot.ChatId
import ru.raysmith.tgbot.model.bot.message.MessageText
import ru.raysmith.tgbot.model.network.message.MessageEntityType
import ru.raysmith.tgbot.utils.botContext
import ru.raysmith.tgbot.utils.n
import team.mke.tg.BaseTgUser
import team.mke.tg.TgUserSelector

/**
 * Базовая реализация для команд /ban и /unban по id или телефону
 *
 * @param bot контекст бота
 * @param userSelectorById селектор пользователя по id
 * @param userSelectorByPhone селектор пользователя по телефону
 * */
fun BaseBanCommandsImpl(
    bot: Bot,
    userSelectorById: TgUserSelector<Long, BaseTgUser<*>>, userSelectorByPhone: TgUserSelector<String, BaseTgUser<*>>
) = object : BanCommands {
    override fun ban(user: BaseTgUser<*>) {
        transaction { user.ban() }
    }

    override fun unban(user: BaseTgUser<*>) {
        transaction { user.unban() }
    }

    override fun hasAccess(chatId: ChatId.ID) = userSelectorById.select(chatId.value)?.isAdmin == true

    context(BotContext<*>)
    suspend fun sendErrorSyntax(isBan: Boolean) {
        send {
            textWithEntities {
                text("Неверное использование команды").n()
                n()
                if (isBan) {
                    banCommandSyntaxAppend(this)
                } else {
                    unbanCommandSyntaxAppend(this)
                }
            }
        }
    }

    context(BotContext<*>)
    override suspend fun getRestrictedTgUser(chatId: ChatId.ID, command: BotCommand): BaseTgUser<*>? {
        suspend fun error() = sendErrorSyntax(isBan = command.body == BotCommand.BAN)

        if (command.argsString == null) {
            error()
        } else {
            val args = command.argsString!!.split(" ")
            val idArg = args.contains("-id")
            if (idArg && args.size <= 1 || args.size == 1 && args.first().isEmpty() || args.isEmpty()) {
                error()
            } else {
                val user = if (idArg) userSelectorById.select(args.last().toLongOrNull() ?: -1)
                else userSelectorByPhone.select(args.last())

                if (user == null) {
                    botContext(bot, chatId) { send("Пользователь не найден") }
                } else {
                    if (chatId.value == user.id.value) {
                        botContext(bot, chatId) { send("Нельзя заблокировать себя") }
                        return null
                    }

                    return user
                }
            }
        }

        return null
    }

    override fun banCommandSyntaxAppend(messageText: MessageText) {
        with(messageText) {
            code("/${BotCommand.BAN} [-id] ").mix("user", MessageEntityType.CODE, MessageEntityType.ITALIC).n()
            code("[-id]").italic(" — необязательный параметр для блокировки по id").n()
            code("user").italic(" — телефон пользователя или id если указан параметр")
        }
    }

    override fun unbanCommandSyntaxAppend(messageText: MessageText) {
        with(messageText) {
            code("/${BotCommand.UNBAN} [-id] ").mix("user", MessageEntityType.CODE, MessageEntityType.ITALIC).n()
            code("[-id]").italic(" — необязательный параметр для раблокировки по id").n()
            code("user").italic(" — телефон пользователя или id если указан параметр")
        }
    }

}