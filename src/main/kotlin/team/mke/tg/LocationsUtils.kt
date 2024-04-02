package team.mke.tg

import ru.raysmith.tgbot.core.BotContext
import ru.raysmith.tgbot.core.BotHolder
import ru.raysmith.tgbot.core.handler.LocationHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.location.LocationCallbackQueryHandler
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.locations.LocationConfig
import ru.raysmith.tgbot.utils.locations.LocationsWrapper
import ru.raysmith.tgbot.utils.message.MessageAction

context(BotHolder)
suspend fun <T : LocationConfig> LocationsWrapper<T>.location(location: ILocation, newLocation: suspend ru.raysmith.tgbot.utils.locations.Location<T>.() -> Unit) {
    location(location.name, newLocation)
}

context(BotContext<*>)
suspend fun <T : BaseLocationConfig> LocationHandler<T>.toLocation(location: ILocation, toLocationMessageAction: MessageAction? = null) {
    if (config.toLocationMessageAction == null) {
        config.toLocationMessageAction = toLocationMessageAction
    }
    toLocation(location.name)
}

suspend fun <T : BaseLocationConfig> LocationCallbackQueryHandler<T>.back(location: ILocation) {
    config.toLocationMessageAction = MessageAction.EDIT
    toLocation(location.name)
}

context(LocationCallbackQueryHandler<out BaseLocationConfig>)
suspend fun setupBack(location: ILocation, ignoreData: Boolean = false) {
    isDataEqual(CallbackQuery.BACK) { back(location) }

    if (!ignoreData) {
        isDataStartWith(CallbackQuery.BACK_PREFIX) {
            config.backData = it
            back(location)
        }
    }
}

suspend fun CallbackQueryHandler.isBack(handler: suspend (data: String?) -> Unit) {
    isDataEqual(CallbackQuery.BACK) {
        handler(null)
    }
    isDataStartWith(CallbackQuery.BACK_PREFIX) {
        handler(it)
    }
}

fun BaseLocationConfig.actionOr(action: MessageAction) = toLocationMessageAction ?: action