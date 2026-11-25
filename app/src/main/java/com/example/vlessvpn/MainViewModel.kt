package com.example.vlessvpn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    var profiles by mutableStateOf(storage.profiles()); private set
    var selected by mutableIntStateOf(storage.selected); private set
    var busy by mutableStateOf(false); private set
    var progress by mutableStateOf<String?>(null); private set
    var message by mutableStateOf<String?>(null)
    var catFilter by mutableIntStateOf(storage.catFilter); private set
    var splitMode by mutableIntStateOf(storage.splitMode); private set
    var splitApps by mutableStateOf(storage.splitApps); private set
    var apps by mutableStateOf<List<AppInfo>>(emptyList()); private set

    init {
        viewModelScope.launch {
            apps = withContext(Dispatchers.IO) {
                try { Apps.load(getApplication()) } catch (e: Exception) { emptyList() }
            }
        }
        if (!storage.autoLoaded) autoSetup()
    }

    private fun poolOf(): List<Profile> = profiles.filter { catFilter == 0 || it.cat == catFilter }

    private fun bestIndex(): Int {
        val inCat = profiles.withIndex().filter { catFilter == 0 || it.value.cat == catFilter }
        return inCat.filter { it.value.ping >= 0 }.minByOrNull { it.value.ping }?.index
            ?: inCat.firstOrNull()?.index
            ?: 0
    }

    /** 0 = все, 1 = против Б.С., 2 = против Ч.С. */
    fun chooseCat(cat: Int) {
        catFilter = cat
        storage.catFilter = cat
        val cur = profiles.getOrNull(selected)
        if (cat != 0 && cur?.cat != cat) select(bestIndex())
    }

    /** Загружает все источники, проверяет пингом и оставляет лучшие серверы каждой категории. */
    private suspend fun loadServers(): Boolean {
        progress = "Загружаю списки серверов…"
        val sources = Defaults.SOURCES + storage.customSubs.map { Src(it, Defaults.CAT_BL) }
        val fetched = withContext(Dispatchers.IO) {
            coroutineScope {
                sources.map { s ->
                    async {
                        runCatching {
                            Subscription.fetch(s.url).map { it.copy(cat = s.cat, auto = true) }
                        }.getOrDefault(emptyList())
                    }
                }.awaitAll().flatten()
            }
        }
        val usable = fetched.filter { it.supported() }.distinctBy { it.key() }
        if (usable.isEmpty()) {
            message = "Не удалось загрузить списки. Проверьте интернет (GitHub может быть недоступен)."
            return false
        }
        val candidates = usable.groupBy { it.cat }
            .flatMap { (_, l) -> l.shuffled().take(Defaults.MAX_PER_CAT) }
        progress = "Проверяю серверы…"
        val checked = Pinger.pingAll(candidates) { d, t -> progress = "Проверка серверов: $d из $t" }
        val alive = checked.filter { it.ping >= 0 }
        if (alive.isEmpty()) {
            message = "Ни один сервер не ответил. Попробуйте позже."
            return false
        }
        val best = alive.groupBy { it.cat }
            .flatMap { (_, l) -> l.sortedBy { it.ping }.take(Defaults.KEEP_PER_CAT) }
            .sortedWith(compareBy({ it.cat }, { it.ping }))
        val manual = profiles.filter { !it.auto }
        updateProfiles(manual + best)
        storage.autoLoaded = true
        val wl = best.count { it.cat == Defaults.CAT_WL }
        message = "Найдено рабочих: против Б.С. — $wl, против Ч.С. — ${best.size - wl}"
        return true
    }

    fun autoSetup() {
        if (busy) return
        viewModelScope.launch {
            busy = true
            try {
                if (loadServers()) select(bestIndex())
            } catch (e: Exception) {
                message = "Ошибка: ${e.message}"
            } finally {
                progress = null
                busy = false
            }
        }
    }

    /** Главная кнопка: проверяет серверы выбранной категории, выбирает самый быстрый и вызывает onReady. */
    fun autoConnect(onReady: () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            var go = false
            try {
                val pool = poolOf()
                var checked: List<Profile>
                if (pool.isEmpty()) {
                    if (!loadServers()) return@launch
                    checked = poolOf()
                } else {
                    progress = "Ищу самый быстрый сервер…"
                    checked = Pinger.pingAll(pool) { d, t -> progress = "Проверка серверов: $d из $t" }
                    if (checked.none { it.ping >= 0 }) {
                        if (!loadServers()) return@launch
                        checked = poolOf()
                    } else {
                        mergePings(checked)
                    }
                }
                val best = checked.filter { it.ping >= 0 }.minByOrNull { it.ping }
                if (best == null) {
                    message = "В этой категории нет работающих серверов."
                    return@launch
                }
                select(profiles.indexOfFirst { it.key() == best.key() }.coerceAtLeast(0))
                message = "Выбран: ${best.name} (${best.ping} мс)"
                go = true
            } catch (e: Exception) {
                message = "Ошибка: ${e.message}"
            } finally {
                progress = null
                busy = false
            }
            if (go) onReady()
        }
    }

    private fun mergePings(checked: List<Profile>) {
        val m = checked.associate { it.key() to it.ping }
        val cur = profiles.getOrNull(selected)?.key()
        updateProfiles(profiles.map { p -> m[p.key()]?.let { p.copy(ping = it) } ?: p })
        select(profiles.indexOfFirst { it.key() == cur }.coerceAtLeast(0))
    }

    /** Повторная проверка пинга всех серверов с сортировкой. */
    fun retest() {
        if (busy || profiles.isEmpty()) return
        viewModelScope.launch {
            busy = true
            try {
                val current = profiles.getOrNull(selected)?.key()
                val res = Pinger.pingAll(profiles) { d, t -> progress = "Проверка серверов: $d из $t" }
                val sorted = res.sortedWith(
                    compareBy({ it.cat }, { if (it.ping < 0) Int.MAX_VALUE else it.ping }),
                )
                updateProfiles(sorted)
                select(sorted.indexOfFirst { it.key() == current }.coerceAtLeast(0))
                message = "Рабочих серверов: ${sorted.count { it.ping >= 0 }} из ${sorted.size}"
            } finally {
                progress = null
                busy = false
            }
        }
    }

    /** Ссылка подписки (http...) добавляется к источникам, vless:// vmess:// trojan:// — как свой сервер. */
    fun add(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        if (text.startsWith("http://") || text.startsWith("https://")) {
            storage.customSubs = storage.customSubs + text
            message = "Подписка добавлена"
            autoSetup()
            return
        }
        val parsed = LinkParser.parseMany(text)
        if (parsed.isEmpty()) {
            message = "Не удалось распознать ссылку"
            return
        }
        val known = profiles.map { it.key() }.toSet()
        val fresh = parsed.filter { it.key() !in known }
        updateProfiles(fresh + profiles)
        select(0)
        message = "Добавлено серверов: ${fresh.size}"
    }

    fun select(i: Int) {
        selected = i
        storage.selected = i
    }

    fun delete(i: Int) {
        val list = profiles.toMutableList().also { it.removeAt(i) }
        updateProfiles(list)
        select(selected.coerceIn(0, (list.size - 1).coerceAtLeast(0)))
    }

    /** 0 = выкл, 1 = белый список приложений, 2 = чёрный список приложений. */
    fun chooseMode(mode: Int) {
        splitMode = mode
        storage.splitMode = mode
    }

    fun toggleApp(pkg: String) {
        val next = if (pkg in splitApps) splitApps - pkg else splitApps + pkg
        splitApps = next
        storage.splitApps = next
    }

    fun clearApps() {
        splitApps = emptySet()
        storage.splitApps = emptySet()
    }

    private fun updateProfiles(list: List<Profile>) {
        profiles = list
        storage.saveProfiles(list)
    }
}
