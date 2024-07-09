package team.mke.tg

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.CallbackQueryHandler
import ru.raysmith.tgbot.core.send
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.bot.message.IMessage
import ru.raysmith.tgbot.model.bot.message.MessageText
import ru.raysmith.tgbot.model.bot.message.keyboard.MessageInlineKeyboard
import ru.raysmith.tgbot.model.network.message.MessageEntity
import java.time.format.DateTimeFormatter
import java.util.*

val incomingBotDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))
val incomingBotTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"))
val incomingBotDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))

typealias BotCommandModel = ru.raysmith.tgbot.model.network.command.BotCommand
typealias InlineRow = MessageInlineKeyboard.Row

// ---------------------------------------------------------------------------------------------------------------------

fun <U : BaseTgUser<*>> LongEntityClass<U>.findOrAdd(userId: Long) = transaction { findById(userId) ?: new(userId) {} }

suspend fun EventHandler.handleEntityNotAvailable(message: String = recordNotAvailableMessage) {
    if (this@handleEntityNotAvailable is CallbackQueryHandler) {
        alert(message)
    } else send(message)
}

fun MessageText.applyEntities(entities: List<MessageEntity>?, text: String? = null, commentMaxLength: Int = IMessage.MAX_TEXT_LENGTH) {
    val startLength = currentTextLength
    if (text != null) {
        text(text)
    }
    val filteredEntities = entities?.filter { it.offset < commentMaxLength }
    filteredEntities?.mapIndexed { i, entity ->
        if (i == filteredEntities.lastIndex && entity.offset + entity.length > commentMaxLength) {
            entity.copy(length = commentMaxLength - entity.offset)
        } else entity
    }?.forEach {
        entity(it.type) {
            val offset = it.offset + startLength

            this.language = it.language
            this.url = it.url
            this.user = it.user
            this.length = it.length
            this.offset = offset
        }
    }
}

fun MessageText.appendClearFilterHint() = italic("Отправьте другой запрос или /${BotCommand.CLEAR} для очистки фильтра")
fun MessageText.appendNameFilterHint(hasFilter: Boolean) {
    if (hasFilter) {
        appendClearFilterHint()
    } else {
        italic("Отправьте сообщение для фильтрации по наименованию")
    }
}