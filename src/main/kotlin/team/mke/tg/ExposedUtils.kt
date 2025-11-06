package team.mke.tg

import com.google.api.services.docs.v1.model.Table
import org.jetbrains.exposed.v1.core.Table.Dual.text
import team.mke.utils.db.Collation

fun Table.botComment(name: String = "comment") = text(name, Collation.utf8mb4_unicode_520_ci)