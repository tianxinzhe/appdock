package com.soulscop.mostusedwidget.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import kotlin.math.roundToInt

object WidgetUtils {
    /**
     * Renders [drawable] into a Bitmap of [targetSizeDp] (converted to px).
     * Keeping icons at a bounded, density-aware size avoids oversized bitmaps
     * in RemoteViews (high overdraw / Binder transaction failures).
     */
    fun drawableToBitmap(drawable: Drawable, targetSizeDp: Int = 44, context: Context? = null): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bitmap = drawable.bitmap
            val targetPx = dpToPx(targetSizeDp, context)
            if (targetPx > 0 && (bitmap.width > targetPx * 2 || bitmap.height > targetPx * 2)) {
                return Bitmap.createScaledBitmap(bitmap, targetPx, targetPx, true)
            }
            return bitmap
        }
        val targetPx = dpToPx(targetSizeDp, context)
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: targetPx
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: targetPx
        // Cap rasterized size at 2x target to preserve sharpness without huge bitmaps
        val scale = (targetPx.toFloat() * 2 / maxOf(width, height)).coerceAtMost(1f)
        val bitmap = Bitmap.createBitmap(
            (width * scale).roundToInt().coerceAtLeast(1),
            (height * scale).roundToInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 生成带圆角的纯色背景位图。位图按 800x400 设计，配合 fitXY 自适应小组件尺寸。
     */
    fun roundedRectBitmap(color: Int, cornerRadiusDp: Int): Bitmap {
        val width = 800
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val radius = cornerRadiusDp * 2.3f
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        return bitmap
    }

    private fun dpToPx(dp: Int, context: Context?): Int {
        if (context == null) return dp // 1dp ~ 1px fallback (used from unit-test context)
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
        ).roundToInt().coerceAtLeast(1)
    }

    fun launchPendingIntent(context: Context, packageName: String, requestCode: Int): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return fallbackPI(context)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * FIX: previously returned an empty ACTION_MAIN activity PI (tapping did nothing).
     * Fallback now opens the app's details page in system settings instead.
     */
    private fun fallbackPI(context: Context): PendingIntent {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(android.net.Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
