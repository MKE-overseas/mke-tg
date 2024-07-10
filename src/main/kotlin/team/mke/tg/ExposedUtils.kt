package team.mke.tg

import org.jetbrains.exposed.sql.Table
import team.mke.utils.db.COLLATE_UTF8MB4_UNICODE_CI

fun Table.botComment(name: String = "comment") = text(name, COLLATE_UTF8MB4_UNICODE_CI)