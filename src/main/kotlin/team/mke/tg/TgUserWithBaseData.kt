package team.mke.tg

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import ru.raysmith.tgbot.model.network.User
import ru.raysmith.tgbot.utils.toChatId
import team.mke.utils.db.Collation
import kotlin.reflect.KClass

abstract class TgUserWithBaseDataTable<L : Enum<L>>(
    locationClass: KClass<L>, defaultLocation: L, tableName: String = "tg_users", columnName: String = "id"
) : BaseTgUserTable<L>(locationClass, defaultLocation, tableName, columnName) {
    open val firstName = varchar("first_name", 255, Collation.utf8mb4_unicode_520_ci)
    open val lastName = varchar("last_name", 255, Collation.utf8mb4_unicode_520_ci).nullable()
    open val username = varchar("username", 255, Collation.utf8mb4_unicode_520_ci).nullable()
}

abstract class TgUserWithBaseData<L : Enum<L>>(table: TgUserWithBaseDataTable<L>, id: EntityID<Long>) : BaseTgUser<L>(table, id) {
    open var firstName by table.firstName
    open var lastName by table.lastName
    open var username by table.username

    open fun updateBaseData(user: User) {
        firstName = user.firstName
        lastName = user.lastName
        username = user.username
    }

    open fun fullname(includeUsername: Boolean = false) = buildString {
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
            append("#$id")
        }
    }

    open fun toTgUser() = User(
        id = id.value.toChatId(),
        isBot = false,
        firstName, lastName, username
    )
}