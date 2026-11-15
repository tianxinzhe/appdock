package com.soulscop.mostusedwidget.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity

@Composable
fun WidgetConfigScreen(
    initial: WidgetConfigEntity,
    isPremium: Boolean,
    onSave: (WidgetConfigEntity) -> Unit,
    onCancel: () -> Unit
) {
    var draft by remember(initial) { mutableStateOf(initial) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.widget_config_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.widget_config_preview), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        WidgetMockPreview(draft)

        if (!isPremium) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.widget_config_premium_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionTitle(stringResource(R.string.widget_config_icon_count))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8).forEach { count ->
                val locked = !isPremium && count != 4
                FilterChip(
                    selected = draft.iconCount == count,
                    onClick = { if (!locked) draft = draft.copy(iconCount = count) },
                    enabled = !locked,
                    label = { Text(stringResource(R.string.widget_count_items, count)) }
                )
            }
        }

        SectionTitle(stringResource(R.string.widget_config_appearance))
        ColorPicker(
            label = stringResource(R.string.widget_config_bg_color),
            options = bgColorOptions,
            selected = draft.bgColor,
            enabled = isPremium
        ) { draft = draft.copy(bgColor = it) }
        ColorPicker(
            label = stringResource(R.string.widget_config_text_color),
            options = textColorOptions,
            selected = draft.textColor,
            enabled = isPremium
        ) { draft = draft.copy(textColor = it) }
        ValueSlider(
            label = stringResource(R.string.widget_config_corner),
            value = draft.cornerRadiusDp.toFloat(),
            valueRange = 0f..24f,
            enabled = isPremium
        ) { draft = draft.copy(cornerRadiusDp = it.toInt()) }
        ValueSlider(
            label = stringResource(R.string.widget_config_icon_size),
            value = draft.iconSizeDp.toFloat(),
            valueRange = 28f..64f,
            enabled = isPremium
        ) { draft = draft.copy(iconSizeDp = it.toInt()) }

        SectionTitle(stringResource(R.string.widget_config_template))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = draft.templateId == 0,
                onClick = { draft = draft.copy(templateId = 0) },
                enabled = isPremium,
                label = { Text(stringResource(R.string.widget_template_standard)) }
            )
            FilterChip(
                selected = draft.templateId == 1,
                onClick = { draft = draft.copy(templateId = 1) },
                enabled = isPremium,
                label = { Text(stringResource(R.string.widget_template_compact)) }
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
            Spacer(Modifier.weight(1f))
            Button(onClick = { onSave(draft) }) { Text(stringResource(R.string.widget_config_save)) }
        }
    }
}

private val bgColorOptions = listOf(
    0xFF1E1E1E.toInt() to R.string.color_dark,
    0xFF000000.toInt() to R.string.color_black,
    0xFFFFFFFF.toInt() to R.string.color_light,
    0xFF1A73E8.toInt() to R.string.color_blue,
    0xFF6750A4.toInt() to R.string.color_purple,
    0xFF2E7D32.toInt() to R.string.color_green
)

private val textColorOptions = listOf(
    0xFFFFFFFF.toInt() to R.string.color_light,
    0xFF000000.toInt() to R.string.color_black,
    0xFF1E1E1E.toInt() to R.string.color_dark
)

@Composable
private fun SectionTitle(title: String) {
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ColorPicker(
    label: String,
    options: List<Pair<Int, Int>>,
    selected: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { (color, nameRes) ->
                val optionLabel = stringResource(nameRes)
                val isSelected = selected == color
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                        .clickable(enabled = enabled) { onSelect(color) }
                        .semantics { contentDescription = optionLabel }
                )
            }
        }
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onChange: (Float) -> Unit
) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text("$label: ${value.toInt()}")
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = valueRange,
            enabled = enabled
        )
    }
}