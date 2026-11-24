package com.example.vlessvpn

import android.content.Context
import org.json.JSONArray

class Storage(ctx: Context) {
    private val sp = ctx.getSharedPreferences("vless", Context.MODE_PRIVATE)

    fun profiles(): List<Profile> {
        val a = JSONArray(sp.getString("profiles", "[]"))
        return (0 until a.length()).map { Profile.fromJson(a.getJSONObject(it)) }
    }

    fun saveProfiles(list: List<Profile>) {
        val a = JSONArray()
        list.forEach { a.put(it.toJson()) }
        sp.edit().putString("profiles", a.toString()).apply()
    }

    var selected: Int
        get() = sp.getInt("selected", 0)
        set(v) = sp.edit().putInt("selected", v).apply()

    var subUrl: String
        get() = sp.getString("sub", "") ?: ""
        set(v) = sp.edit().putString("sub", v).apply()

    fun selectedProfile(): Profile? = profiles().getOrNull(selected)
}
