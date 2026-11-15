package com.soulscop.mostusedwidget.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import java.io.File

object CsvExporter {

    /**
     * Escapes a CSV field per RFC 4180 and neutralizes spreadsheet formula
     * injection (= + - @ leading chars) with a leading apostrophe.
     */
    private fun escape(value: String): String {
        val sanitized = if (value.isEmpty()) value else {
            val first = value.first()
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                "'$value"
            } else {
                value
            }
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\""
    }

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
            sb.appendLine(
                listOf(
                    escape(it.packageName),
                    escape(it.appLabel),
                    it.launchCount7d.toString(),
                    it.launchCount30d.toString(),
                    it.foregroundTime7d.toString(),
                    it.foregroundTime30d.toString(),
                    it.lastUsedTime.toString(),
                    it.score.toString()
                ).joinToString(",")
            )
        }
        return sb.toString()
    }
}
