package com.example.vlessvpn

import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/** Проверка доступности сервера по TCP (время установки соединения). */
object Pinger {
    private suspend fun tcpPing(p: Profile, timeoutMs: Int): Int = withContext(Dispatchers.IO) {
        try {
            val t0 = System.nanoTime()
            Socket().use { it.connect(InetSocketAddress(p.address, p.port), timeoutMs) }
            ((System.nanoTime() - t0) / 1_000_000).toInt().coerceAtLeast(1)
        } catch (e: Exception) {
            -1
        }
    }

    /** Возвращает тот же список (в том же порядке), у каждого сервера заполнен ping (-1 = не отвечает). */
    suspend fun pingAll(
        list: List<Profile>,
        concurrency: Int = 60,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ): List<Profile> = coroutineScope {
        val sem = Semaphore(concurrency)
        val done = AtomicInteger(0)
        list.map { p ->
            async {
                val ms = sem.withPermit { tcpPing(p, 3000) }
                onProgress(done.incrementAndGet(), list.size)
                p.copy(ping = ms)
            }
        }.awaitAll()
    }
}
