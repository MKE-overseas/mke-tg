package team.mke.tg.utils.updatecommands

import ru.raysmith.tgbot.core.send
import ru.raysmith.tgbot.model.bot.ChatId
import team.mke.tg.BaseTgUser
import team.mke.tg.TgUserSelector

fun <T : BaseTgUser<*>>BaseUpdateCommandsImpl(userSelector: TgUserSelector<ChatId.ID, T>) =
    UpdateCommands { chatId, silently ->
        userSelector.select(chatId)?.provideCommands()?.also {
            if (!silently) {
                send("Успешно")
            }
        }
    }