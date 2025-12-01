

package com.example.feed_project.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

enum class ExposureEvent {
    VISIBLE,
    VISIBLE_50_PERCENT,
    FULLY_VISIBLE,
    INVISIBLE
}

@Composable
fun ExposureTrackerForCompose(
    lazyListState: LazyListState,
    itemIds: List<String>,
    onExposureEvent: (String, com.example.feed_project.domain.model.ExposureEvent) -> Unit,
    debounceMs: Long = 100L
) {
    var lastVisibleIds by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(lazyListState.layoutInfo.visibleItemsInfo) {
        delay(debounceMs)
        val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
        val currentVisibleIds = visibleItemsInfo.mapNotNull { info ->
            if (info.index in itemIds.indices) itemIds[info.index] else null
        }.toSet()

        // 新出现的 item → 触发 VISIBLE
        (currentVisibleIds - lastVisibleIds).forEach { id ->
            onExposureEvent(id, com.example.feed_project.domain.model.ExposureEvent.VISIBLE)
        }

        // 消失的 item → 触发 INVISIBLE
        (lastVisibleIds - currentVisibleIds).forEach { id ->
            onExposureEvent(id, com.example.feed_project.domain.model.ExposureEvent.INVISIBLE)
        }

        lastVisibleIds = currentVisibleIds
    }
}


