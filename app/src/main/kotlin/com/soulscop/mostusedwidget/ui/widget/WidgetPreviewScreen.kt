package com.soulscop.mostusedwidget.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soulscop.mostusedwidget.R

@Composable
fun WidgetPreviewScreen(isPremium: Boolean) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.widget_preview), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(120.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(44.dp).background(Color(0xFF555555), RoundedCornerShape(10.dp)))
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.width(40.dp).height(6.dp).background(Color(0xFF888888), RoundedCornerShape(3.dp)))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.widget_add_hint), style = MaterialTheme.typography.bodyMedium)
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
