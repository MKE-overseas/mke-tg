package team.mke.tg

import ru.raysmith.tgbot.model.network.chat.IChat
import ru.raysmith.tgbot.model.network.media.Contact
import ru.raysmith.utils.letIf
import ru.raysmith.utils.notNull

fun IChat.getFullName(includeUsername: Boolean = false): String = buildString {
    append(firstName ?: "")
    if (firstName notNull lastName) {
        append(" ")
    }
    append(lastName ?: "")
    if (includeUsername && username != null && this.isNotEmpty()) {
        append(" (")
        append(username)
        append(")")
    }

    if (this.isEmpty()) {
        append("[${id.value}] Без имени")
    }
}


fun TgUserWithBaseData<*>.getFullName(includeUsername: Boolean = false): String = buildString {
    append(firstName)
    if (lastName != null) {
        append(" ")
    }
    append(lastName ?: "")
    if (includeUsername && username != null && this.isNotEmpty()) {
        append(" (")
        append(username)
        append(")")
    }

    if (this.isEmpty()) {
        append("[$id] Без имени")
    }
}

fun Contact.phoneFormatted(onlyRuPhones: Boolean = false) = phoneNumber.phoneFormatted(onlyRuPhones)
    .let { "+$it" }
    .letIf({ it.startsWith("+8") && it.length == 12 }) { it.replace("+8", "+7") }

private val ruPhoneCodeRegex = "^7|8\\d{10}".toRegex()
private val phoneRegex = "\\d+".toRegex()
fun String.phoneFormatted(onlyRuPhones: Boolean = false) = replace("\\D".toRegex(), "")
    .let { if (onlyRuPhones && !it.matches(ruPhoneCodeRegex)) null else it }
    ?.let { if (!onlyRuPhones && !it.matches(phoneRegex)) null else it }
    ?.let { "+$it" }
    ?.letIf({ it.startsWith("+8") && it.length == 12 }) { it.replace("+8", "+7") }