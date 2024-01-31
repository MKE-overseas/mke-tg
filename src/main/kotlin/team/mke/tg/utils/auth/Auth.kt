package team.mke.tg.utils.auth

import ru.raysmith.google.sheets.GoogleSheetsService
import ru.raysmith.google.sheets.Range
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.MessageHandler
import ru.raysmith.tgbot.model.bot.message.MessageText
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.n
import team.mke.tg.BaseTgUser

suspend fun MessageHandler.setupAuth(
    tgUser: BaseTgUser<*>,
    sheetService: GoogleSheetsService,
    phonesSpreadsheetId: String,
    sendAuthMessage: suspend MessageHandler.() -> Unit,
    rowCheck: (row: List<Any>) -> Boolean,
    onAuthNotAvailable: suspend MessageHandler.(e: Exception) -> Unit = { sendAuthTemporaryNotAvailableMessage() },
    onFail: suspend MessageHandler.() -> Unit,
    onSuccess: suspend MessageHandler.(row: List<Any>?) -> Unit
) {
    if (!tgUser.isRegistered) {
        if (message.contact != null) {
            try {
                val values = sheetService.SpreadSheets.values(phonesSpreadsheetId, Range())

                var result = false
                var currentRow: List<Any>? = null
                for (row in values) {
                    result = rowCheck(row)
                    currentRow = row
                    if (result) {
                        break
                    }
                }

                onSuccess(currentRow)
            } catch (e: Exception) {
                onAuthNotAvailable(e)
            }

            onFail()
        } else sendAuthMessage()
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

internal suspend fun EventHandler.sendAuthTemporaryNotAvailableMessage(
    additionalInfo: MessageText.() -> MessageText = { this }
) = send {
    textWithEntities {
        text("Авторизация временно недоступна.").n()
        additionalInfo()
    }
}