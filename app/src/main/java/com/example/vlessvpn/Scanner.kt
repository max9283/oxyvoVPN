package com.example.vlessvpn

import android.content.Context
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Сканер обхода блокировок: поднимает ядро с одним сервером и делает реальные запросы через него:
 * нейтральный сайт (сервер вообще работает — для Б.С. это значит, что он проходит белые списки),
 * YouTube и Telegram (обход чёрных списков).
 */
object Scanner {
    private const val URL_NEUTRAL = "https://www.gstatic.com/generate_204"
    private const val URL_YT = "https://www.youtube.com/generate_204"
    private const val URL_TG = "https://telegram.org/"
    private const val TIMEOUT_MS = 8000L

    private val pool = Executors.newCachedThreadPool()

    fun available(): Boolean = CoreBridge.measureAvailable()

    private fun submit(ctx: Context, cfg: String, url: String): Future<Long> =
        pool.submit(Callable<Long> { CoreBridge.measure(ctx, cfg, url) })

    private fun result(f: Future<Long>): Int = try {
        f.get(TIMEOUT_MS, TimeUnit.MILLISECONDS).toInt()
    } catch (e: Exception) {
        f.cancel(true)
        -1
    }

    private fun scanOne(ctx: Context, p: Profile): Profile {
        val cfg = ConfigBuilder.buildTest(p)
        val real = result(submit(ctx, cfg, URL_NEUTRAL))
        if (real < 0) return p.copy(scanned = true, real = -1, yt = false, tg = false)
        val ytF = submit(ctx, cfg, URL_YT)
        val tgF = submit(ctx, cfg, URL_TG)
        return p.copy(scanned = true, real = real, yt = result(ytF) >= 0, tg = result(tgF) >= 0)
    }

    suspend fun scanAll(
        ctx: Context,
        list: List<Profile>,
        concurrency: Int = 4,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ): List<Profile> = coroutineScope {
        val sem = Semaphore(concurrency)
        val done = AtomicInteger(0)
        list.map { p ->
            async(Dispatchers.IO) {
                val r = sem.withPermit { scanOne(ctx, p) }
                onProgress(done.incrementAndGet(), list.size)
                r
            }
        }.awaitAll()
    }
}
