package com.soulscop.mostusedwidget.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.soulscop.mostusedwidget.R

@Composable
fun PremiumGate(onPurchase: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.premium_title)) },
        text = { Text(stringResource(R.string.premium_description)) },
        confirmButton = {
            TextButton(onClick = onPurchase) { Text(stringResource(R.string.premium_purchase)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
