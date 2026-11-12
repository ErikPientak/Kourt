package com.kourt.app.ui.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** A resume this soon after the screen appeared or last refreshed is the initial load, not a return. */
internal const val MIN_REFRESH_GAP_MS = 1_500L

/** True when a resume at [nowMs] should refresh, given the last handled resume at [lastHandledMs]. */
internal fun shouldRefreshOnResume(lastHandledMs: Long, nowMs: Long): Boolean =
    nowMs - lastHandledMs !in 0..MIN_REFRESH_GAP_MS

/**
 * Calls [onRefresh] each time the screen becomes visible again: after coming back from another
 * screen, or when the app returns from the background.
 *
 * Navigation removes a screen from composition while another one is on top, so a "skip the first
 * resume" flag kept in plain `remember` starts over on return and would swallow the very resume
 * this exists for. The time of the last handled resume is therefore kept with `rememberSaveable`,
 * which survives that. A resume right after the screen first appears is the ViewModel's own initial
 * load and is skipped by the short gap in [shouldRefreshOnResume]. Rotation reloads once, which is cheap.
 */
@Composable
fun RefreshOnReturn(onRefresh: () -> Unit) {
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastHandledMs by rememberSaveable { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = SystemClock.elapsedRealtime()
                if (shouldRefreshOnResume(lastHandledMs, now)) currentOnRefresh()
                lastHandledMs = now
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
