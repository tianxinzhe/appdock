package com.soulscop.mostusedwidget.domain

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.soulscop.mostusedwidget.util.UsageStatsPermission

data class RawUsage(
    val launchCount7d: Long = 0,
    val launchCount30d: Long = 0,
    val foregroundTime7d: Long = 0,
    val foregroundTime30d: Long = 0,
    val lastUsedTime: Long = 0
)

class UsageStatsProvider(private val context: Context) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val dayMs = 24L * 60 * 60 * 1000

    fun hasPermission(): Boolean = UsageStatsPermission.hasPermission(context)

    fun collect(): Map<String, RawUsage> {
        val now = System.currentTimeMillis()
        val start7 = now - 7 * dayMs
        val start30 = now - 30 * dayMs

        val acc = mutableMapOf<String, MutableRawUsage>()

        aggregateForeground(start7, now) { pkg, time, last ->
            val u = acc.getOrPut(pkg) { MutableRawUsage() }
            u.foregroundTime7d += time
            if (last > u.lastUsedTime) u.lastUsedTime = last
        }
        aggregateForeground(start30, now) { pkg, time, last ->
            val u = acc.getOrPut(pkg) { MutableRawUsage() }
            u.foregroundTime30d += time
            if (last > u.lastUsedTime) u.lastUsedTime = last
        }
        countLaunches(start7, now) { pkg ->
            acc.getOrPut(pkg) { MutableRawUsage() }.launchCount7d++
        }
        countLaunches(start30, now) { pkg ->
            acc.getOrPut(pkg) { MutableRawUsage() }.launchCount30d++
        }

        return acc.mapValues { (_, v) -> v.toRaw() }
    }

    private fun aggregateForeground(start: Long, end: Long, onResult: (String, Long, Long) -> Unit) {
        val list = runCatching { usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) }.getOrNull() ?: return
        for (s in list) {
            onResult(s.packageName, s.totalTimeInForeground, s.lastTimeUsed)
        }
    }

    private fun countLaunches(start: Long, end: Long, onEvent: (String) -> Unit) {
        val events = runCatching { usm.queryEvents(start, end) }.getOrNull() ?: return
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                onEvent(e.packageName)
            }
        }
    }

    private class MutableRawUsage {
        var launchCount7d = 0L
        var launchCount30d = 0L
        var foregroundTime7d = 0L
        var foregroundTime30d = 0L
        var lastUsedTime = 0L

        fun toRaw() = RawUsage(launchCount7d, launchCount30d, foregroundTime7d, foregroundTime30d, lastUsedTime)
    }
}
