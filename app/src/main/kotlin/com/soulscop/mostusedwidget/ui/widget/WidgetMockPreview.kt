package com.soulscop.mostusedwidget.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity

@Composable
fun WidgetMockPreview(config: WidgetConfigEntity, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(config.bgColor), RoundedCornerShape(config.cornerRadiusDp.dp))
            .padding(12.dp)
    ) {
        val count = config.iconCount.coerceIn(1, 8)
        val cellWidth = maxWidth / count
        val iconSize = minOf(config.iconSizeDp.dp, (cellWidth - 2.dp).coerceAtLeast(12.dp))

        Column(Modifier.fillMaxSize()) {
            if (config.templateId == 0) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.app_name),
                        color = Color(config.textColor),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.widget_refresh),
                        color = Color(config.textColor),
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(count) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(iconSize)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            Modifier
                                .width(minOf(iconSize, 36.dp))
                                .height(5.dp)
                                .background(Color(0x55FFFFFF), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}