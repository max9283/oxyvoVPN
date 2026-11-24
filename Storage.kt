package com.example.vlessvpn

import android.content.Context
import org.json.JSONArray

class Storage(ctx: Context) {
    private val sp = ctx.getSharedPreferences("vless", Context.MODE_PRIVATE)

    fun profiles(): List<Profile> = try {
        val a = JSONArray(sp.getString("profiles", "[]"))
        (0 until a.length()).map { Profile.fromJson(a.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    fun saveProfiles(list: List<Profile>) {
        val a = JSONArray()
        list.forEach { a.put(it.toJson()) }
        sp.edit().putString("profiles", a.toString()).apply()
    }

    var selected: Int
        get() = sp.getInt("selected", 0)
        set(v) = sp.edit().putInt("selected", v).apply()

    /** 1 = предпочитать белый список, 2 = чёрный список. */
    var preferredList: Int
        get() = sp.getInt("preferred_list", 1)
        set(v) = sp.edit().putInt("preferred_list", v).apply()

    var autoLoaded: Boolean
        get() = sp.getBoolean("auto_loaded", false)
        set(v) = sp.edit().putBoolean("auto_loaded", v).apply()

    /** Свои ссылки-подписки пользователя (добавляются к встроенным). */
    var customSubs: Set<String>
        get() = (sp.getStringSet("custom_subs", emptySet()) ?: emptySet()).toSet()
        set(v) = sp.edit().putStringSet("custom_subs", v).apply()

    /** 0 = выключено, 1 = белый список (VPN только для выбранных), 2 = чёрный список (выбранные мимо VPN). */
    var splitMode: Int
        get() = sp.getInt("split_mode", 0)
        set(v) = sp.edit().putInt("split_mode", v).apply()

    var splitApps: Set<String>
        get() = (sp.getStringSet("split_apps", emptySet()) ?: emptySet()).toSet()
        set(v) = sp.edit().putStringSet("split_apps", v).apply()

    fun selectedProfile(): Profile? = profiles().getOrNull(selected)
}
