package com.soulscop.mostusedwidget.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves app labels / icons / flags with in-memory caching,
 * because PackageManager calls are slow (IPC + package XML parsing).
 */
class AppInfoResolver(private val context: Context) {
    private val pm = context.packageManager

    // Small caches keyed by package name (#6/#7: repeated PM IPC was slow)
    private val labelCache = ConcurrentHashMap<String, String>()
    private val systemCache = ConcurrentHashMap<String, Boolean>()
    private val gameCache = ConcurrentHashMap<String, Boolean>()
    private val iconCache = LruCache<String, Drawable>(64)

    fun resolveLabel(packageName: String): String = labelCache.getOrPut(packageName) {
        runCatching {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    @Suppress("DEPRECATION")
    fun resolveIcon(packageName: String): Drawable? {
        iconCache.get(packageName)?.let { return it }
        return runCatching { pm.getApplicationIcon(packageName) }
            .getOrNull()
            ?.also { iconCache.put(packageName, it) }
    }

    fun isSystemApp(packageName: String): Boolean = systemCache.getOrPut(packageName) {
        runCatching {
            val info = pm.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrDefault(false)
    }

    fun isGame(packageName: String): Boolean = gameCache.getOrPut(packageName) {
        runCatching {
            val info = pm.getApplicationInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.category == ApplicationInfo.CATEGORY_GAME
            } else {
                val label = pm.getApplicationLabel(info).toString().lowercase()
                label.contains("game") || label.contains("游戏")
            }
        }.getOrDefault(false)
    }
}
