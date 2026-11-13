package com.soulscop.mostusedwidget.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import java.io.File

object CsvExporter {
    fun export(context: Context, items: List<UsageCacheEntity>): Uri? = runCatching {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        val file = File(dir, "mostused_stats.csv")
        file.writeText(buildCsv(items), Charsets.UTF_8)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull()

    private fun buildCsv(items: List<UsageCacheEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("packageName,appLabel,launchCount7d,launchCount30d,foregroundTime7d,foregroundTime30d,lastUsedTime,score")
        for (it in items) {
            val label = it.appLabel.replace("\"", "\"\"")
            sb.appendLine("${it.packageName},\"$label\",${it.launchCount7d},${it.launchCount30d},${it.foregroundTime7d},${it.foregroundTime30d},${it.lastUsedTime},${it.score}")
        }
        return sb.toString()
    }
}
