// src/main/java/com/example/feed_project/ui/components/SwipeRefresh.kt
package com.example.feed_project.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

import kotlin.math.abs

@Composable
fun SwipeRefresh(
    state: SwipeRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: @Composable (SwipeRefreshState) -> Unit = { DefaultIndicator(it) },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = onRefresh

    val nestedScrollConnection = remember(state, coroutineScope) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (state.isRefreshing) return Offset.Zero

                val delta = available.y
                return if (delta > 0 && state.shouldStartRefresh(delta)) {
                    state.dispatchScrollDelta(delta)
                    Offset(0f, delta)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (state.isRefreshing) return Offset.Zero

                val delta = available.y
                return if (delta > 0) {
                    state.dispatchScrollDelta(delta)
                    Offset(0f, delta)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (state.isRefreshing) return Velocity.Zero

                if (state.shouldTriggerRefresh()) {
                    state.startRefreshing()
                    updatedOnRefresh()
                    return available
                }

                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
    ) {
        content()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            indicator(state)
        }
    }
}

@Stable
class SwipeRefreshState internal constructor(
    private val threshold: Float
) {
    val isRefreshing: Boolean
        get() = _isRefreshing

    val progress: Float
        get() = if (isRefreshing) 1f else abs(swipeOffset) / threshold

    private var _isRefreshing by mutableStateOf(false)
    private var swipeOffset by mutableStateOf(0f)

    internal fun shouldStartRefresh(delta: Float): Boolean {
        return swipeOffset == 0f && delta > 0
    }

    internal fun dispatchScrollDelta(delta: Float) {
        if (isRefreshing) return
        swipeOffset = (swipeOffset + delta).coerceAtLeast(0f)
    }

    internal fun shouldTriggerRefresh(): Boolean {
        return !isRefreshing && swipeOffset >= threshold
    }

    internal fun startRefreshing() {
        _isRefreshing = true
        swipeOffset = threshold
    }

    internal fun endRefreshing() {
        _isRefreshing = false
        swipeOffset = 0f
    }
}

@Composable
fun rememberSwipeRefreshState(isRefreshing: Boolean): SwipeRefreshState {
    val density = LocalDensity.current
    val threshold = with(density) { 80.dp.toPx() }
    val state = remember { SwipeRefreshState(threshold) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing != state.isRefreshing) {
            if (isRefreshing) {
                state.startRefreshing()
            } else {
                state.endRefreshing()
            }
        }
    }

    return state
}

@Composable
fun DefaultIndicator(state: SwipeRefreshState) {
    val trigger = state.progress >= 1f
    val scale by animateFloatAsState(
        targetValue = if (trigger) 0.9f else 1f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp)
            .then(
                if (state.isRefreshing) {
                    Modifier
                } else {
                    Modifier.offset(y = with(LocalDensity.current) { (-56).dp + (state.progress * 56).dp })
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (state.isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale),
                strokeWidth = 2.dp
            )
        } else {
            val rotate by animateFloatAsState(
                targetValue = state.progress * 360f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
                label = "rotate"
            )

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = if (trigger) Color.Green else Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotate)
                    .scale(scale)
            )
        }
    }
}
