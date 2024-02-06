package team.mke.tg.utils.ban

import ru.raysmith.tgbot.core.BotContext
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.bot.ChatId
import ru.raysmith.tgbot.model.bot.message.MessageText
import team.mke.tg.BaseTgUser

interface BanCommands<U : BaseTgUser<*>> {
    fun hasAccess(chatId: ChatId.ID): Boolean
    fun ban(user: U)
    fun unban(user: U)

    context(BotContext<*>)
    suspend fun getRestrictedTgUser(chatId: ChatId.ID, command: BotCommand): U?
    fun banCommandSyntaxAppend(messageText: MessageText)
    fun unbanCommandSyntaxAppend(messageText: MessageText)
}