package team.mke.tg.utils.updatecommands

import ru.raysmith.tgbot.core.handler.base.CommandHandler
import ru.raysmith.tgbot.core.handler.base.isCommand
import ru.raysmith.tgbot.model.bot.BotCommand

// TODO create BotFeature

suspend fun CommandHandler.setupUpdateCommands(updateCommandsImpl: UpdateCommands, silently: Boolean) {
    isCommand(BotCommand.UPDATE_COMMANDS) {
        updateCommandsImpl.updateCommands(getChatId(), silently)
    }
}

val BotCommand.Companion.UPDATE_COMMANDS get() = "updatecommands"