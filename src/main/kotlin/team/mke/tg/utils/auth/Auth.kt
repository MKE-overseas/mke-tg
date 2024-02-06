package team.mke.tg.utils.auth

import ru.raysmith.google.sheets.service.GoogleSheetsService
import ru.raysmith.google.sheets.service.Range
import ru.raysmith.google.sheets.service.get
import ru.raysmith.tgbot.core.handler.EventHandler
import ru.raysmith.tgbot.core.handler.base.MessageHandler
import ru.raysmith.tgbot.model.bot.message.MessageText
import ru.raysmith.tgbot.utils.message.MessageAction
import ru.raysmith.tgbot.utils.message.message
import ru.raysmith.tgbot.utils.n
import team.mke.tg.BaseTgUser

suspend fun <U : BaseTgUser<*>> MessageHandler.setupAuth(
    tgUser: U,
    sheetService: GoogleSheetsService,
    phonesSpreadsheetId: String?,
    sendAuthMessage: suspend MessageHandler.() -> Unit = { sendAuthMessage(MessageAction.SEND) },
    getPhone: suspend MessageHandler.() -> String?,
    rowCheck: suspend MessageHandler.(phone: String, row: List<Any>) -> Boolean = { phone, row -> phone in row  },
    onAuthNotAvailable: suspend MessageHandler.(e: Exception?) -> Unit = { sendAuthTemporaryNotAvailableMessage() },
    onFail: suspend MessageHandler.() -> Unit,
    onSuccess: suspend MessageHandler.(phone: String, row: List<Any>?) -> Unit
) {
    if (!tgUser.isRegistered) {
        val phone = getPhone() ?: run {
            sendAuthMessage()
            return
        }

        try {
            val values = sheetService.SpreadSheets.Values.get(
                phonesSpreadsheetId ?: run {
                    onAuthNotAvailable(null)
                    return
                }, Range()
            )

            var result = false
            var currentRow: List<Any>? = null
            for (row in values) {
                result = rowCheck(phone, row)
                currentRow = row
                if (result) {
                    break
                }
            }

            onSuccess(phone, currentRow)
        } catch (e: Exception) {
            onAuthNotAvailable(e)
        }

        onFail()
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