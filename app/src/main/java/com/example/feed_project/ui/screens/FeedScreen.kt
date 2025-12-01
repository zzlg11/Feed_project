
package com.example.feed_project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feed_project.ui.components.ExposureTestTool
import com.example.feed_project.ui.components.FeedCard
import com.example.feed_project.viewmodel.FeedViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.feed_project.model.LayoutType
import com.example.feed_project.model.FeedItem
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.feed_project.ui.utils.ExposureTrackerForCompose
import com.example.feed_project.model.ExposureLog
import com.example.feed_project.ui.utils.AdvancedExposureTracker
// 👇 在文件顶部（或单独文件）定义辅助类
private sealed interface FeedRenderItem
private data class SingleColumnItem(val feed: FeedItem) : FeedRenderItem
private data class DoubleColumnPair(val left: FeedItem, val right: FeedItem?) : FeedRenderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel()
) {
    val feeds by viewModel.feeds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hasError by viewModel.hasError.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val exposureLogs by viewModel.exposureLogs.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val feedItemIds = remember(feeds) { feeds.map { it.id } }

    val renderItems = remember(feeds) {
        buildList {
            var i = 0
            while (i < feeds.size) {
                val current = feeds[i]
                if (current.layoutType == LayoutType.DOUBLE_COLUMN) {
                    // 尝试找下一个也是 DOUBLE_COLUMN 的项
                    val next = if (i + 1 < feeds.size && feeds[i + 1].layoutType == LayoutType.DOUBLE_COLUMN) {
                        feeds[i + 1]
                    } else {
                        null
                    }
                    add(DoubleColumnPair(current, next))
                    i += if (next != null) 2 else 1
                } else {
                    add(SingleColumnItem(current))
                    i++
                }
            }
        }
    }

    // 监听是否滚动到列表底部，实现上拉刷新
var hasInitiallyScrolled by remember { mutableStateOf(false) }

LaunchedEffect(listState) {
    snapshotFlow {
        val layoutInfo = listState.layoutInfo
        layoutInfo.visibleItemsInfo.lastOrNull()?.index to layoutInfo.totalItemsCount
    }
    .collect { (lastVisibleIndex, totalItemsCount) ->
        // 标记是否已经滚动过
        if (lastVisibleIndex != null && lastVisibleIndex > 0) {
            hasInitiallyScrolled = true
        }

        // 只有在用户主动滚动且接近底部时才触发加载
        if (hasInitiallyScrolled &&
            lastVisibleIndex != null &&
            lastVisibleIndex >= totalItemsCount - 2 &&
            canLoadMore &&
            !isLoading &&
            !isRefreshing) {
            viewModel.loadMoreFeeds()
        }
    }
}



    ExposureTrackerForCompose(
        lazyListState = listState,
        itemIds = feedItemIds,
        onExposureEvent = { itemId, event ->
            viewModel.addExposureLog(ExposureLog(itemId, event))
        }
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshFeeds() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                ExposureTestTool(
                    exposureLogs = exposureLogs,
                    onClearLogs = { viewModel.clearExposureLogs() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (hasError && feeds.isEmpty()) {
                item {
                    ErrorItem(
                        message = errorMessage,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(renderItems) { renderItem ->
                when (renderItem) {
                    is SingleColumnItem -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            FeedCard(
                                feedItem = renderItem.feed,
                                onDeleteRequest = { id ->
                                    itemToDelete = id
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    is DoubleColumnPair -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                FeedCard(
                                    feedItem = renderItem.left,
                                    onDeleteRequest = { id ->
                                        itemToDelete = id
                                        showDeleteDialog = true
                                    },
                                    isDoubleColumn = true
                                )
                            }
                            if (renderItem.right != null) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FeedCard(
                                        feedItem = renderItem.right,
                                        onDeleteRequest = { id ->
                                            itemToDelete = id
                                            showDeleteDialog = true
                                        },
                                        isDoubleColumn = true
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (isLoading && feeds.isNotEmpty()) {
                item {
                    LoadingItem(modifier = Modifier.fillMaxWidth())
                }
            }

            if (hasError && feeds.isNotEmpty()) {
                item {
                    ErrorItem(
                        message = errorMessage,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!canLoadMore && feeds.isNotEmpty() && !hasError) {
                item {
                    NoMoreDataItem(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (isLoading && feeds.isEmpty() && !hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除确认") },
            text = { Text("确定要删除这个卡片吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteFeed(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}




@Composable
fun LoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("加载中...")
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

@Composable
fun NoMoreDataItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "没有更多数据了",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
