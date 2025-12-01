package com.example.feed_project.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun AdvancedExposureTracker(
    lazyListState: LazyListState,
    itemIds: List<String>,
    onExposureEvent: (String, ExposureEvent) -> Unit,
    debounceMs: Long = 100L
) {
    var lastReportedEvents by remember { mutableStateOf<Map<String, ExposureEvent>>(emptyMap()) }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                delay(debounceMs)

                val viewportStart = layoutInfo.viewportStartOffset
                val viewportEnd = layoutInfo.viewportEndOffset
                val currentEvents = mutableMapOf<String, ExposureEvent>()

                for (visibleItem in layoutInfo.visibleItemsInfo) {
                    val index = visibleItem.index
                    if (index !in itemIds.indices) continue

                    val itemId = itemIds[index]
                    val itemTop = visibleItem.offset
                    val itemBottom = itemTop + visibleItem.size
                    val totalHeight = visibleItem.size // 使用实际测量高度

                    val visibleTop = maxOf(itemTop, viewportStart)
                    val visibleBottom = minOf(itemBottom, viewportEnd)
                    val visibleHeight = maxOf(0, visibleBottom - visibleTop)

                    val ratio = if (totalHeight > 0) visibleHeight.toFloat() / totalHeight else 0f

                    val event = when {
                        ratio >= 1.0f -> ExposureEvent.FULLY_VISIBLE
                        ratio >= 0.5f -> ExposureEvent.VISIBLE_50_PERCENT
                        ratio > 0f -> ExposureEvent.VISIBLE
                        else -> ExposureEvent.INVISIBLE
                    }

                    currentEvents[itemId] = event
                }

                val disappeared = lastReportedEvents.keys - currentEvents.keys
                disappeared.forEach { id ->
                    onExposureEvent(id, ExposureEvent.INVISIBLE)
                }

                currentEvents.forEach { (id, event) ->
                    if (lastReportedEvents[id] != event) {
                        onExposureEvent(id, event)
                    }
                }

                lastReportedEvents = currentEvents + disappeared.associateWith { ExposureEvent.INVISIBLE }
            }
    }
}