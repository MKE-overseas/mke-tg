package team.mke.tg

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnTransformer
import org.jetbrains.exposed.v1.core.Table
import ru.raysmith.tgbot.model.network.message.MessageEntity
import ru.raysmith.tgbot.network.TelegramApi

private val MessageEntitiesTransformerNullable = object : ColumnTransformer<String?, List<MessageEntity>?> {
    override fun unwrap(value: List<MessageEntity>?) = value?.let { TelegramApi.json.encodeToString(it) }
    override fun wrap(value: String?) = value?.let { TelegramApi.json.decodeFromString<List<MessageEntity>>(it) }
}

val MessageEntitiesTransformer = object : ColumnTransformer<String, List<MessageEntity>> {
    override fun unwrap(value: List<MessageEntity>) = TelegramApi.json.encodeToString(value)
    override fun wrap(value: String) = TelegramApi.json.decodeFromString<List<MessageEntity>>(value)
}

context(table: Table)
@JvmName("transformMessageEntitiesNotNull")
fun Column<String>.transformMessageEntities() = with(table) { transform(MessageEntitiesTransformer) }

context(table: Table)
fun Column<String?>.transformMessageEntities() = with(table) { transform(MessageEntitiesTransformerNullable) }