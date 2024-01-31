package team.mke.tg.utils.ban

import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.bot.ChatId
import ru.raysmith.tgbot.model.bot.message.MessageText
import team.mke.tg.BaseTgUser

interface BanCommands {
    fun hasAccess(chatId: ChatId.ID): Boolean
    fun ban(user: BaseTgUser<*>)
    fun unban(user: BaseTgUser<*>)
    suspend fun getRestrictedTgUser(chatId: ChatId.ID, command: BotCommand): BaseTgUser<*>?
    fun banCommandSyntaxAppend(messageText: MessageText)
    fun unbanCommandSyntaxAppend(messageText: MessageText)
}