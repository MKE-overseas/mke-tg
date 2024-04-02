package team.mke.tg.utils

import org.jetbrains.exposed.sql.SizedCollection
import ru.raysmith.tgbot.utils.pagination.PaginationFetcher
import ru.raysmith.tgbot.utils.pagination.PaginationFetcherFactory

class ExposedPaginationFetcherFactory : PaginationFetcherFactory {
    override fun <T> getFetcher(): PaginationFetcher<T> {
        return object : PaginationFetcher<T> {
            override fun getCount(data: Iterable<T>): Int {
                return if (data is SizedCollection<T>) data.count().toInt() else data.count()
            }
            override fun fetchData(data: Iterable<T>, page: Int, offset: Int, count: Int, rows: Int, columns: Int): Iterable<T> {
                return if (data is SizedCollection<T>) data.limit(count, offset.toLong()) else data.toList().subList((columns * rows) * (page - 1), ((columns * rows) * (page - 1) + (columns * rows)).coerceAtMost(data.count()))
            }
        }
    }
}