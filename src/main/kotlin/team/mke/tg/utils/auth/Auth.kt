package team.mke.tg.utils.auth

import ru.raysmith.google.sheets.service.GoogleSheetsService
import ru.raysmith.google.sheets.service.get
import ru.raysmith.google.sheets.utils.Range
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.MessageHandler
import ru.raysmith.tgbot.model.bot.message.MessageText
import ru.raysmith.tgbot.utils.BotFeature
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.n
import team.mke.tg.BaseTgUser
import team.mke.tg.phoneFormatted

/**
 * Добавляет систему авторизации по номеру телефона и таблицы Google Sheets
 *
 * @param tgUser текущий пользователь контекста
 * @param sheetService сервис Google Sheets
 * @param phonesSpreadsheetId id таблицы Google Sheets. Если null авторизация будет не доступна для пользователя
 * @param range диапазон ячеек для поиска телефонов
 * @param sendAuthMessage сообщение отправляемое с инструкцией авторизации и кнопкой отправки номера телефона
 * @param getPhone возвращает отформатированный номер телефона из полученного сообщения пользователя
 * или null если телефон не был передан/ не корректный
 * @param rowCheck проверяет есть ли в строке таблицы телефон полученный из [getPhone]
 * @param onAuthNotAvailable вызывается, когда авторизация не доступна ([phonesSpreadsheetId] == null)
 * @param onFail вызывается, когда телефон не найден в таблице
 * @param onSuccess вызывается, когда телефон найден в таблице
 * */
data class AuthGoogleSheetsFeature<U : BaseTgUser<*>>(
    val tgUser: U,
    val sheetService: GoogleSheetsService,
    val phonesSpreadsheetId: String?,
    val range: Range,
    val sendAuthMessage: suspend MessageHandler.() -> Unit = { sendAuthMessage(MessageAction.SEND) },
    val getPhone: suspend MessageHandler.() -> String? = { message.contact?.phoneFormatted() },
    val rowCheck: suspend MessageHandler.(phone: String, row: List<Any>) -> Boolean = { phone, row -> phone in row  },
    val onAuthNotAvailable: suspend MessageHandler.(e: Exception?) -> Unit = { sendAuthTemporaryNotAvailableMessage() },
    val onFail: suspend MessageHandler.() -> Unit,
    val onSuccess: suspend MessageHandler.(phone: String, row: List<Any>) -> Unit
) : BotFeature {
    override suspend fun handle(handler: EventHandler, handled: Boolean) {
        check(handler is MessageHandler) { "AuthFeature supports only MessageHandler" }

        with(handler) {
            if (!tgUser.isRegistered) {
                val phone = getPhone() ?: run {
                    this.sendAuthMessage()
                    return
                }

                try {
                    val values = sheetService.Spreadsheets.Values.get(
                        phonesSpreadsheetId ?: run {
                            onAuthNotAvailable(null)
                            return
                        }, range
                    )

                    var result = false
                    var currentRow: List<Any>? = null
                    for (row in values) {
                        result = rowCheck(phone, row)
                        if (result) {
                            currentRow = row
                            break
                        }
                    }

                    if (!result) {
                        onFail()
                    } else {
                        onSuccess(phone, currentRow!!)
                    }
                } catch (e: Exception) {
                    onAuthNotAvailable(e)
                }
            }
        }
    }

}

suspend fun EventHandler.sendAuthMessage(
    action: MessageAction = MessageAction.SEND,
    buttonText: String = "Отправить номер телефона",
    messageText: MessageText.() -> MessageText = { text("Для авторизации отправьте свой номер телефона") }
) = message(action) {
    textWithEntities {
        messageText()
    }
    replyKeyboard {
        resizeKeyboard = true

        row {
            button {
                text = buttonText
                requestContact = true
            }
        }
    }
}

suspend fun EventHandler.sendAuthTemporaryNotAvailableMessage(
    additionalInfo: MessageText.() -> MessageText = { this }
) = send {
    textWithEntities {
        text("Авторизация временно недоступна.").n()
        additionalInfo()
    }
}