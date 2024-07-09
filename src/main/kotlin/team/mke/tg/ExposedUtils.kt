package team.mke.tg

import org.jetbrains.exposed.sql.Table
import ru.raysmith.tgbot.model.bot.message.IMessage
import team.mke.utils.db.COLLATE_UTF8MB4_UNICODE_CI

fun Table.botComment(name: String = "comment") = varchar(name, IMessage.MAX_TEXT_LENGTH, COLLATE_UTF8MB4_UNICODE_CI)