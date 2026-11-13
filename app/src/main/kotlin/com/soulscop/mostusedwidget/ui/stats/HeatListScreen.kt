package com.soulscop.mostusedwidget.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.soulscop.mostusedwidget.ui.components.AppRow
import com.soulscop.mostusedwidget.ui.components.EmptyState
import com.soulscop.mostusedwidget.ui.components.PremiumGate

@Composable
fun HeatListScreen(viewModel: StatsViewModel = viewModel()) {
    val heatItems by viewModel.heatList.collectAsState()
    val isPremium by viewModel.premium.collectAsState()
    var showPremium by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.stats_refresh))
            }
        }
        if (heatItems.isEmpty()) {
            EmptyState(stringResource(R.string.stats_empty))
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(heatItems, key = { it.entity.packageName }) { item ->
                    AppRow(
                        item = item,
                        isPremium = isPremium,
                        onHide = { viewModel.hide(item.entity.packageName) },
                        onPin = { viewModel.pin(item.entity.packageName) },
                        onUnpin = { viewModel.unpin(item.entity.packageName) },
                        onPremiumRequest = { showPremium = true }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showPremium) {
        PremiumGate(
            onPurchase = { viewModel.purchase(); showPremium = false },
            onDismiss = { showPremium = false }
        )
    }
}
