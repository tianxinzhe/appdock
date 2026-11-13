package com.soulscop.mostusedwidget.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.util.SortMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val isPremium by viewModel.premium.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()

    var showPrivacy by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showPrivacy) {
        PrivacyPolicyScreen(onBack = { showPrivacy = false })
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        if (isPremium) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.premium_active), color = MaterialTheme.colorScheme.primary)
        } else {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.purchase() }) { Text(stringResource(R.string.settings_purchase)) }
            TextButton(onClick = { viewModel.restore() }) { Text(stringResource(R.string.settings_restore)) }
        }

        SectionTitle(stringResource(R.string.settings_sort_mode))
        val sortMode = runCatching { SortMode.valueOf(settings.sortMode) }.getOrDefault(SortMode.LAUNCH_COUNT)
        SortMode.entries.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = sortMode == mode,
                    onClick = { if (isPremium) viewModel.updateSettings { it.copy(sortMode = mode.name) } },
                    enabled = isPremium
                )
                Text(sortModeLabel(mode))
            }
        }

        SectionTitle(stringResource(R.string.settings_weight))
        SliderSetting(stringResource(R.string.settings_w1), settings.w1, 0f, 10f, isPremium) { v ->
            viewModel.updateSettings { it.copy(w1 = v) }
        }
        SliderSetting(stringResource(R.string.settings_w2), settings.w2, 0f, 10f, isPremium) { v ->
            viewModel.updateSettings { it.copy(w2 = v) }
        }
        SliderSetting(stringResource(R.string.settings_decay), settings.decayFactorDays, 1f, 30f, isPremium) { v ->
            viewModel.updateSettings { it.copy(decayFactorDays = v) }
        }

        SectionTitle(stringResource(R.string.settings_filter))
        SwitchSetting(stringResource(R.string.settings_exclude_system), settings.excludeSystem, isPremium) { v ->
            viewModel.updateSettings { it.copy(excludeSystem = v) }
        }
        SwitchSetting(stringResource(R.string.settings_exclude_games), settings.excludeGames, isPremium) { v ->
            viewModel.updateSettings { it.copy(excludeGames = v) }
        }

        SectionTitle(stringResource(R.string.settings_blacklist))
        if (hiddenApps.isEmpty()) {
            Text(stringResource(R.string.settings_blacklist_empty), style = MaterialTheme.typography.bodySmall)
        } else {
            hiddenApps.forEach { pkg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pkg, modifier = Modifier.weight(1f), maxLines = 1)
                    TextButton(onClick = { viewModel.unhide(pkg) }) { Text(stringResource(R.string.action_unpin)) }
                }
            }
        }

        if (isPremium) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.exportCsv() }) { Text(stringResource(R.string.settings_export_csv)) }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showPrivacy = true }) { Text(stringResource(R.string.settings_privacy)) }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.settings_clear_data))
        }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.settings_about_content), style = MaterialTheme.typography.bodySmall)
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.settings_clear_data)) },
            text = { Text(stringResource(R.string.settings_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearData(); showClearConfirm = false }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun sortModeLabel(mode: SortMode): String = when (mode) {
    SortMode.LAUNCH_COUNT -> stringResource(R.string.sort_launch_count)
    SortMode.FOREGROUND_TIME -> stringResource(R.string.sort_foreground_time)
    SortMode.LAST_USED -> stringResource(R.string.sort_last_used)
}

@Composable
private fun SectionTitle(title: String) {
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    enabled: Boolean,
    onChange: (Float) -> Unit
) {
    Column {
        Text("$label: ${"%.1f".format(value)}")
        Slider(value = value, onValueChange = onChange, valueRange = min..max, enabled = enabled)
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}
