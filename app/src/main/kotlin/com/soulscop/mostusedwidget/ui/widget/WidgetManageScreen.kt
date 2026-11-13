package com.soulscop.mostusedwidget.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.ui.settings.SettingsViewModel

@Composable
fun WidgetManageScreen(viewModel: SettingsViewModel = viewModel()) {
    val isPremium by viewModel.premium.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.widget_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        WidgetPreviewScreen(isPremium = isPremium)
    }
}
