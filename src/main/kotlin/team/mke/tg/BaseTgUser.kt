package team.mke.tg

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import kotlin.reflect.KClass

abstract class BaseTgUserTable<L : Enum<L>>(
    locationClass: KClass<L>, defaultLocation: L, tableName: String = "tg_users", columnName: String = "id"
) : LongIdTable(tableName, columnName) {
    val location = enumerationByName("location", 255, locationClass).default(defaultLocation)
    val phone = varchar("phone", 255).nullable()
    val isBan = bool("is_ban").default(false)
    val isRegistered = bool("is_registered").default(false)
    val isAdmin = bool("is_admin").default(false)

    fun valid() = isBan.eq(false) and isRegistered.eq(true)
}

abstract class BaseTgUser<L : Enum<L>>(table: BaseTgUserTable<L>, id: EntityID<Long>) : LongEntity(id) {
    var location by table.location
    var phone by table.phone
    var isBan by table.isBan
    var isRegistered by table.isRegistered
    var isAdmin by table.isAdmin

    abstract fun provideCommands()

    fun ban() {
        isBan = true
    }

    fun unban() {
        isBan = false
    }
}
