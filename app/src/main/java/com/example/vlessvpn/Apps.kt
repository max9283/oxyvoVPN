package com.example.vlessvpn

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

class AppInfo(val label: String, val pkg: String, val icon: ImageBitmap)

object Apps {
    /** Список приложений с иконкой запуска (кроме самого Oxyvo VPN). */
    fun load(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != ctx.packageName }
            .map {
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.activityInfo.packageName,
                    it.loadIcon(pm).toBitmap(96, 96).asImageBitmap(),
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
