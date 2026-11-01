package com.kourt.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Calls [onRefresh] each time the screen becomes visible again: after coming back from another
 * screen, or when the app returns from the background.
 *
 * The first resume is skipped, because the ViewModel already loads when it's created. That also
 * covers rotation, where the composition (and this observer) starts over but the data is kept.
 */
@Composable
fun RefreshOnReturn(onRefresh: () -> Unit) {
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        var resumedBefore = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (resumedBefore) currentOnRefresh() else resumedBefore = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
