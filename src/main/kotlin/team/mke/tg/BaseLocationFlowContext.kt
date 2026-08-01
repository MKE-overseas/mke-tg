package team.mke.tg

import ru.raysmith.tgbot.utils.locations.LocationFlowContext
import ru.raysmith.tgbot.utils.message.MessageAction

abstract class BaseLocationFlowContext : LocationFlowContext {
    var backData: String? = null
    var toLocationMessageAction: MessageAction? = null

    fun actionOr(action: MessageAction) = toLocationMessageAction ?: action
}