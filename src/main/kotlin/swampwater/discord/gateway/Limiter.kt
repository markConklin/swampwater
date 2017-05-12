package swampwater.discord.gateway

import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit


open class Limiter(val rate: Long, limit: Long, val unit: TimeUnit) {
    private val window = unit.toMillis(limit)
    private var available: Long = rate
    private var mark: Long = currentTimeMillis()

    @Synchronized
    open fun submit(runnable: () -> Unit) {
        if (available == 0L) {
            if (currentTimeMillis() - mark < window) {
                unit.sleep((mark + window) - currentTimeMillis())
            }
            mark += window
            available = rate
        }
        available--
        runnable.invoke()
    }
}