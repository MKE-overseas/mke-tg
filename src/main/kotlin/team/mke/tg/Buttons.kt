package team.mke.tg

import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.model.network.keyboard.KeyboardButton

val KeyboardButton.Companion.BACK: String get() = "« Назад"
val KeyboardButton.Companion.CANCEL: String get() = "« Отмена"

val CallbackQuery.Companion.BACK: String get() = "back"
val CallbackQuery.Companion.BACK_PREFIX: String get() = "back_"
val CallbackQuery.Companion.YES: String get() = "YES"
val CallbackQuery.Companion.NO: String get() = "NO"
val CallbackQuery.Companion.CANCEL: String get() = "cancel"