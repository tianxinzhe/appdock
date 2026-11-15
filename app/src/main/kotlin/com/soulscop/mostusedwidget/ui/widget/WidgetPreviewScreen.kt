package com.soulscop.mostusedwidget.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity

@Composable
fun WidgetPreviewScreen(isPremium: Boolean) {
    val previewConfig = remember { WidgetConfigEntity(widgetId = 0) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.widget_preview), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        WidgetMockPreview(previewConfig)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.widget_add_hint), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.widget_manage_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isPremium) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.widget_customize_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}