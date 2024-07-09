package team.mke.tg.utils

import ru.raysmith.tgbot.core.BotConfig
import ru.raysmith.tgbot.utils.datepicker.DatePicker
import ru.raysmith.utils.atEndOfDay
import team.mke.tg.incomingBotDateFormat
import team.mke.tg.incomingBotDateTimeFormat
import team.mke.tg.incomingBotTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

private val defaultDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

sealed class IncomingBotDateResult {
    data class Date(val date: LocalDateTime, val parseTime: Boolean) : IncomingBotDateResult()
    data class WrongRange(val date: LocalDateTime, val allowStart: LocalDateTime, val allowEnd: LocalDateTime, val message: String) : IncomingBotDateResult()
    data object Failed : IncomingBotDateResult()
}

fun String.incomingBotDate(botConfig: BotConfig, datePicker: DatePicker, dateFormat: DateTimeFormatter = defaultDateFormat, throwIfWrong: Boolean = false): IncomingBotDateResult {
    val dates = datePicker.dates(botConfig, null)

    return if (this.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$".toRegex())) {
        try {
            val parsedDate = LocalDate.parse(this, incomingBotDateFormat).atStartOfDay()
            if (dates.contains(parsedDate.toLocalDate())) IncomingBotDateResult.Date(parsedDate, false)
            else {
                val rangeString = "${dates.start.atStartOfDay().format(dateFormat)} — ${dates.endInclusive.atEndOfDay().format(dateFormat)}"
                val message = "Эта дата не доступна для выбора. Доступный промежуток: $rangeString"
                if (throwIfWrong) {
                    error(message)
                } else {
                    IncomingBotDateResult.WrongRange(
                        parsedDate, dates.start.atStartOfDay(), dates.endInclusive.atEndOfDay(), message
                    )
                }
            }
        } catch (e: Exception) { if (throwIfWrong) throw e else IncomingBotDateResult.Failed }
    } else {
        try {
            val parsedDate = LocalDateTime.parse(this, incomingBotDateTimeFormat)
            if (dates.contains(parsedDate.toLocalDate())) IncomingBotDateResult.Date(parsedDate, true)
            else {
                val rangeString = "${dates.start.atStartOfDay().format(incomingBotDateTimeFormat)} — ${dates.endInclusive.atEndOfDay().format(incomingBotDateTimeFormat)}"
                val message = "Эта дата не доступна для выбора. Доступный промежуток: $rangeString"
                if (throwIfWrong) {
                    error(message)
                } else {
                    IncomingBotDateResult.WrongRange(
                        parsedDate, dates.start.atStartOfDay(), dates.endInclusive.atEndOfDay(), message
                    )
                }
            }
        } catch (e: Exception) { if (throwIfWrong) throw e else IncomingBotDateResult.Failed }
    }
}

fun String.incomingBotTime(throwIfWrong: Boolean = false) = if (this.matches("^\\d{2}:\\d{2}$".toRegex())) {
    try {
        LocalTime.parse(this, incomingBotTimeFormat)
    } catch (e: Exception) { if (throwIfWrong) throw e else null }
} else null