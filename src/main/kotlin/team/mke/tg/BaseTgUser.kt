package team.mke.tg

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.LongEntity
import ru.raysmith.tgbot.network.API
import kotlin.reflect.KClass

abstract class BaseTgUserTable<L : Enum<L>>(
    locationClass: KClass<L>, defaultLocation: L, tableName: String = "tg_users", columnName: String = "id"
) : LongIdTable(tableName, columnName) {
    open val location = enumerationByName("location", 255, locationClass).default(defaultLocation)
    open val phone = varchar("phone", 255).nullable()
    open val isBan = bool("is_ban").default(false)
    open val isRegistered = bool("is_registered").default(false)
    open val isAdmin = bool("is_admin").default(false)

    /** SQL оператор для фильтрации валидных пользователей (не заблокированные и зарегистрированные) */
    open fun valid() = isBan.eq(false) and isRegistered.eq(true)
}

abstract class BaseTgUser<L : Enum<L>>(table: BaseTgUserTable<L>, id: EntityID<Long>) : LongEntity(id) {
    open var location by table.location
    open var phone by table.phone
    open var isBan by table.isBan
    open var isRegistered by table.isRegistered
    open var isAdmin by table.isAdmin

    context(api: API)
    abstract suspend fun provideCommands()

    open fun ban() {
        isBan = true
    }

    open fun unban() {
        isBan = false
    }
}
