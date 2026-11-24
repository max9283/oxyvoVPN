package com.example.vlessvpn

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    var profiles by mutableStateOf(storage.profiles()); private set
    var selected by mutableIntStateOf(storage.selected); private set
    var subUrl by mutableStateOf(storage.subUrl); private set
    var message by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false); private set

    /** Принимает ссылку подписки (http...) либо одну/несколько ссылок vless:// vmess:// trojan://. */
    fun add(input: String) {
        val text = input.trim()
        if (text.isEmpty()) return
        if (text.startsWith("http://") || text.startsWith("https://")) {
            subUrl = text
            storage.subUrl = text
            refresh()
            return
        }
        val parsed = LinkParser.parseMany(text)
        if (parsed.isEmpty()) {
            message = "Не удалось распознать ссылку"
            return
        }
        updateProfiles(profiles + parsed)
        message = "Добавлено серверов: ${parsed.size}"
    }

    fun refresh() {
        if (subUrl.isBlank()) return
        viewModelScope.launch {
            loading = true
            try {
                val list = withContext(Dispatchers.IO) { Subscription.fetch(subUrl) }
                if (list.isEmpty()) {
                    message = "В подписке нет поддерживаемых серверов"
                } else {
                    updateProfiles(list)
                    select(0)
                    message = "Загружено серверов: ${list.size}"
                }
            } catch (e: Exception) {
                message = "Ошибка загрузки: ${e.message}"
            } finally {
                loading = false
            }
        }
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

    private fun updateProfiles(list: List<Profile>) {
        profiles = list
        storage.saveProfiles(list)
    }
}
