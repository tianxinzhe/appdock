package com.soulscop.mostusedwidget.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soulscop.mostusedwidget.R
import com.soulscop.mostusedwidget.domain.AppInfoResolver
import com.soulscop.mostusedwidget.util.WidgetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            val drawable: Drawable? = AppInfoResolver(context).resolveIcon(packageName)
            drawable?.let { WidgetUtils.drawableToBitmap(it).asImageBitmap() }
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = null, modifier = modifier.size(size))
    } else {
        Image(
            painter = painterResource(R.drawable.ic_app_placeholder),
            contentDescription = null,
            modifier = modifier.size(size)
        )
    }
}
