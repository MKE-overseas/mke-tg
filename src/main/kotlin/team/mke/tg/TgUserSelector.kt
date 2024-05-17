package team.mke.tg

/** Фабрика пользователей telegram */
fun interface TgUserSelector<ID, U : BaseTgUser<*>> {
    fun select(value: ID): U?
}