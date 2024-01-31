package team.mke.tg

import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.bot.message.keyboard.MessageInlineKeyboard
import ru.raysmith.tgbot.model.network.CallbackQuery
import ru.raysmith.tgbot.model.network.keyboard.KeyboardButton

fun InlineRow.back(text: String = KeyboardButton.BACK) = button(text, CallbackQuery.BACK)
fun InlineRow.back(text: String = KeyboardButton.BACK, data: String) = button(text, CallbackQuery.BACK_PREFIX + data)
fun InlineRow.skip(callbackQuery: String) = button("Пропустить »", callbackQuery)

suspend fun MessageInlineKeyboard.back(text: String = KeyboardButton.BACK, data: String? = null) = row { if (data == null) back(text) else back(text, data) }

val BotCommand.Companion.CANCEL get() = "cancel"
val BotCommand.Companion.CLEAR get() = "clear"