package team.mke.tg

import ru.raysmith.tgbot.utils.locations.LocationConfig
import ru.raysmith.tgbot.utils.message.MessageAction

abstract class BaseLocationConfig : LocationConfig {
    var backData: String? = null
    var toLocationMessageAction: MessageAction? = null
}