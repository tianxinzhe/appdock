package com.soulscop.mostusedwidget.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity
import com.soulscop.mostusedwidget.domain.AppInfoResolver
import com.soulscop.mostusedwidget.util.UsageStatsPermission
import com.soulscop.mostusedwidget.util.WidgetUtils

object WidgetRenderer {

    private val containerIds = intArrayOf(
        R.id.widget_item_0, R.id.widget_item_1, R.id.widget_item_2, R.id.widget_item_3,
        R.id.widget_item_4, R.id.widget_item_5, R.id.widget_item_6, R.id.widget_item_7
    )
    private val iconIds = intArrayOf(
        R.id.widget_item_0_icon, R.id.widget_item_1_icon, R.id.widget_item_2_icon, R.id.widget_item_3_icon,
        R.id.widget_item_4_icon, R.id.widget_item_5_icon, R.id.widget_item_6_icon, R.id.widget_item_7_icon
    )
    private val labelIds = intArrayOf(
        R.id.widget_item_0_label, R.id.widget_item_1_label, R.id.widget_item_2_label, R.id.widget_item_3_label,
        R.id.widget_item_4_label, R.id.widget_item_5_label, R.id.widget_item_6_label, R.id.widget_item_7_label
    )

    fun build(context: Context, top: List<UsageCacheEntity>, config: WidgetConfigEntity, widgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_most_used)
        val resolver = AppInfoResolver(context)
        val hasPermission = UsageStatsPermission.hasPermission(context)

        views.setInt(R.id.widget_root, "setBackgroundColor", config.bgColor)

        val refreshIntent = Intent(context, MostUsedWidgetProvider::class.java).apply {
            action = MostUsedWidgetProvider.ACTION_REFRESH
        }
        val refreshPI = PendingIntent.getBroadcast(
            context, widgetId, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPI)

        views.setTextViewText(
            R.id.widget_title,
            when {
                !hasPermission -> context.getString(R.string.widget_no_permission)
                top.isEmpty() -> context.getString(R.string.widget_no_data)
                else -> context.getString(R.string.app_name)
            }
        )

        val count = if (hasPermission) top.size.coerceAtMost(config.iconCount).coerceAtMost(containerIds.size) else 0

        for (i in containerIds.indices) {
            if (i < count) {
                val item = top[i]
                views.setViewVisibility(containerIds[i], View.VISIBLE)
                views.setTextColor(labelIds[i], config.textColor)
                views.setTextViewText(labelIds[i], item.appLabel)
                val drawable = resolver.resolveIcon(item.packageName)
                if (drawable != null) {
                    views.setImageViewBitmap(iconIds[i], WidgetUtils.drawableToBitmap(drawable))
                } else {
                    views.setImageViewResource(iconIds[i], R.drawable.ic_app_placeholder)
                }
                val pi = WidgetUtils.launchPendingIntent(context, item.packageName, widgetId * 100 + i)
                views.setOnClickPendingIntent(containerIds[i], pi)
            } else {
                views.setViewVisibility(containerIds[i], View.GONE)
            }
        }

        return views
    }
}
