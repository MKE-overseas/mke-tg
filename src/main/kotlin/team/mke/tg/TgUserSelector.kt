package team.mke.tg

fun interface TgUserSelector<T, U : BaseTgUser<*>> {
    fun select(value: T): U?
}