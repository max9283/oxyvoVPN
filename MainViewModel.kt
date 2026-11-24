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
    var splitMode by mutableIntStateOf(storage.splitMode); private set
    var preferredList by mutableIntStateOf(storage.preferredList); private set
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

    /** Загружает встроенные (и свои) списки, проверяет пингом и оставляет лучшие серверы. */
    fun autoSetup() {
        if (busy) return
        viewModelScope.launch {
            busy = true
            try {
                progress = "Загружаю списки серверов…"
                val sources = Defaults.SOURCES + storage.customSubs
                val fetched = withContext(Dispatchers.IO) {
                    coroutineScope {
                        sources.map { url ->
                            async { runCatching { Subscription.fetch(url) }.getOrDefault(emptyList()) }
                        }.awaitAll().flatten()
                    }
                }
                val candidates = fetched.distinctBy { it.key() }.shuffled().take(Defaults.MAX_CANDIDATES)
                if (candidates.isEmpty()) {
                    message = "Не удалось загрузить списки. Проверьте интернет (GitHub может быть недоступен)."
                    return@launch
                }
                progress = "Проверяю серверы…"
                val checked = Pinger.pingAll(candidates) { d, t -> progress = "Проверка серверов: $d из $t" }
                val best = checked.filter { it.ping >= 0 }
                    .sortedBy { it.ping }
                    .take(Defaults.KEEP)
                    .map { it.copy(auto = true) }
                if (best.isEmpty()) {
                    message = "Ни один сервер не ответил. Попробуйте позже."
                    return@launch
                }
                val manual = profiles.filter { !it.auto }
                updateProfiles(manual + best)
                select(manual.size)
                storage.autoLoaded = true
                message = "Найдено рабочих серверов: ${best.size}"
            } catch (e: Exception) {
                message = "Ошибка: ${e.message}"
            } finally {
                progress = null
                busy = false
            }
        }
    }

    /** Повторная проверка пинга всех серверов с сортировкой. */
    fun retest() {
        if (busy || profiles.isEmpty()) return
        viewModelScope.launch {
            busy = true
            try {
                val current = profiles.getOrNull(selected)?.key()
                val res = Pinger.pingAll(profiles) { d, t -> progress = "Проверка серверов: $d из $t" }
                val sorted = res.sortedBy { if (it.ping < 0) Int.MAX_VALUE else it.ping }
                updateProfiles(sorted)
                select(sorted.indexOfFirst { it.key() == current }.coerceAtLeast(0))
                message = "Рабочих серверов: ${sorted.count { it.ping >= 0 }} из ${sorted.size}"
            } finally {
                progress = null
                busy = false
            }
        }
    }

    /** Ссылка подписки (http...) добавляется к списку источников, vless:// vmess:// trojan:// — как свой сервер. */
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

    /** Выбирает сервер с наименьшим известным пингом из предпочтительной группы. */
    fun selectAuto(): Boolean {
        val preferred = profiles.withIndex()
            .filter { it.value.listGroup == preferredList && it.value.ping >= 0 }
            .minByOrNull { it.value.ping }
        if (preferred == null) {
            message = "Нет проверенных серверов. Обновите список серверов."
            return false
        }
        select(preferred.index)
        return true
    }

    fun setPreferredList(group: Int) {
        preferredList = if (group == 2) 2 else 1
        storage.preferredList = preferredList
    }

    fun setProfileListGroup(index: Int, group: Int) {
        if (index !in profiles.indices) return
        updateProfiles(profiles.toMutableList().also {
            it[index] = it[index].copy(listGroup = if (group == 2) 2 else 1)
        })
    }

    fun delete(i: Int) {
        val list = profiles.toMutableList().also { it.removeAt(i) }
        updateProfiles(list)
        select(selected.coerceIn(0, (list.size - 1).coerceAtLeast(0)))
    }

    /** 0 = выкл, 1 = белый список, 2 = чёрный список. */
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
