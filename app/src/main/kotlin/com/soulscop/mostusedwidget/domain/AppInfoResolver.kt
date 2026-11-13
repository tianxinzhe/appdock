package com.soulscop.mostusedwidget.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build

class AppInfoResolver(private val context: Context) {
    private val pm = context.packageManager

    fun resolveLabel(packageName: String): String = runCatching {
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    }.getOrDefault(packageName)

    @Suppress("DEPRECATION")
    fun resolveIcon(packageName: String): Drawable? = runCatching {
        pm.getApplicationIcon(packageName)
    }.getOrNull()

    fun isSystemApp(packageName: String): Boolean = runCatching {
        val info = pm.getApplicationInfo(packageName, 0)
        (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }.getOrDefault(false)

    fun isGame(packageName: String): Boolean = runCatching {
        val info = pm.getApplicationInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.category == ApplicationInfo.CATEGORY_GAME
        } else {
            val label = pm.getApplicationLabel(info).toString().lowercase()
            label.contains("game") || label.contains("游戏")
        }
    }.getOrDefault(false)
}
