package team.mke.tg.utils

import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.raysmith.tgbot.utils.pagination.DefaultPaginationFetcherFactory
import ru.raysmith.tgbot.utils.pagination.PaginationFetcher
import ru.raysmith.tgbot.utils.pagination.PaginationFetcherFactory

class ExposedPaginationFetcherFactory : PaginationFetcherFactory {
    companion object {
        private val defaultInstance = DefaultPaginationFetcherFactory()
    }

    override fun <T> getFetcher(): PaginationFetcher<T> {
        return object : PaginationFetcher<T> {
            override fun getCount(data: Iterable<T>): Int {
                return if (data is SizedIterable<T>) transaction { data.count().toInt() } else data.count()
            }
            override fun fetchData(data: Iterable<T>, page: Int, offset: Int, count: Int, rows: Int, columns: Int): Iterable<T> {
                return if (data is SizedIterable<T>) transaction { data.limit(count).offset(offset.toLong()).toList() }
                else defaultInstance.getFetcher<T>().fetchData(data, page, offset, count, rows, columns)
            }
        }
    }
}