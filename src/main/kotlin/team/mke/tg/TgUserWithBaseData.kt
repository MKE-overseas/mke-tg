package team.mke.tg

import org.jetbrains.exposed.dao.id.EntityID
import ru.raysmith.tgbot.model.network.User
import ru.raysmith.tgbot.utils.toChatId
import team.mke.utils.db.COLLATE_UTF8MB4_UNICODE_CI
import kotlin.reflect.KClass

abstract class TgUserWithBaseDataTable<L : Enum<L>>(
    locationClass: KClass<L>, defaultLocation: L, tableName: String = "tg_users", columnName: String = "id"
) : BaseTgUserTable<L>(locationClass, defaultLocation, tableName, columnName) {
    val firstName = varchar("first_name", 255, COLLATE_UTF8MB4_UNICODE_CI)
    val lastName = varchar("last_name", 255, COLLATE_UTF8MB4_UNICODE_CI).nullable()
    val username = varchar("username", 255, COLLATE_UTF8MB4_UNICODE_CI).nullable()
}

abstract class TgUserWithBaseData<L : Enum<L>>(table: TgUserWithBaseDataTable<L>, id: EntityID<Long>) : BaseTgUser<L>(table, id) {
    var firstName by table.firstName
    var lastName by table.lastName
    var username by table.username

    fun updateBaseData(user: User) {
        firstName = user.firstName
        lastName = user.lastName
        username = user.username
    }

    fun fullname() = "$firstName${lastName?.let { " $it" } ?: ""}"

    fun toTgUser() = User(
        id = id.value.toChatId(),
        isBot = false,
        firstName, lastName, username
    )
}