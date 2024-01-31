package team.mke.tg.utils.updatecommands

import ru.raysmith.tgbot.core.BotContext
import ru.raysmith.tgbot.model.bot.ChatId

fun interface UpdateCommands {

    context(BotContext<*>)
    suspend fun updateCommands(chatId: ChatId.ID, silently: Boolean)
}