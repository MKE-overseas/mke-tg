package team.mke.tg

import ru.raysmith.tgbot.core.handler.LocationHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.handler.location.LocationCallbackQueryHandler
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.utils.locations.LocationFlowContext
import ru.raysmith.tgbot.utils.locations.LocationsWrapper
import ru.raysmith.tgbot.utils.locations.loc
import ru.raysmith.tgbot.utils.message.MessageAction

suspend fun <T : LocationFlowContext> LocationsWrapper<T>.location(
    location: ILocation,
    newLocation: suspend ru.raysmith.tgbot.utils.locations.Location<T>.() -> Unit
) {
    location(location.name, newLocation)
}

context(_: T)
suspend fun <T : BaseLocationFlowContext> LocationHandler<T, *>.toLocation(
    location: ILocation,
    toLocationMessageAction: MessageAction? = null
) {
    if (loc.toLocationMessageAction == null) {
        loc.toLocationMessageAction = toLocationMessageAction
    }
    toLocation(location.name)
}

context(_: T)
suspend fun <T : BaseLocationFlowContext> LocationCallbackQueryHandler<T>.back(location: ILocation) {
    loc.toLocationMessageAction = MessageAction.EDIT
    toLocation(location.name)
}

context(ctx: T)
suspend fun <T : BaseLocationFlowContext> LocationCallbackQueryHandler<T>.setupBack(
    location: ILocation,
    ignoreData: Boolean = false
) {
    isDataEqual(CallbackQuery.BACK) { back(location) }

    if (!ignoreData) {
        isDataStartWith(CallbackQuery.BACK_PREFIX) {
            loc.backData = it
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