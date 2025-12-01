package com.example.feed_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.feed_project.domain.model.CardType
import com.example.feed_project.domain.model.FeedItem
import androidx.compose.ui.composed
import androidx.compose.foundation.*
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import com.example.feed_project.core.PluginManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.clickAndLongClick(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    longPressTimeoutMillis: Long = 500L
) = composed {
    var isLongPressed by remember { mutableStateOf(false) }

    this.combinedClickable(
        onClick = {
            if (!isLongPressed) {
                onClick()
            }
            isLongPressed = false
        },
        onLongClick = {
            isLongPressed = true
            onLongClick()
        }
    )
}



@Composable
fun ZoomableImage(
    imageUrl: String,
    isZoomed: Boolean,
    onToggleZoom: (Boolean) -> Unit,
    isDoubleColumn: Boolean = false // 添加参数
) {
    val painter = rememberImagePainter(data = imageUrl)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .then(
                if (isZoomed) {
                    Modifier
                        .fillMaxWidth()
                        .height(if (isDoubleColumn) 200.dp else 300.dp) // 双栏模式使用更小的高度
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(if (isDoubleColumn) 150.dp else 180.dp) // 双栏模式使用更小的高度
                }
            )
            .clickable { onToggleZoom(!isZoomed) },
        contentScale = if (isZoomed) ContentScale.Fit else ContentScale.Crop
    )
}



@Composable
fun FeedCard(
    feedItem: FeedItem,
    onDeleteRequest: (String) -> Unit,
    onCardClick: (String) -> Unit = {},
    isDoubleColumn: Boolean = false
) {
    val plugin = PluginManager.getPlugin(feedItem.cardType)

    if (plugin != null) {
        // 使用插件渲染卡片
        plugin.Render(feedItem)
    } else{
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isImageZoomed by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(10) } // 10秒倒计时
    var isLiked by remember { mutableStateOf(false) } // 添加点赞状态
    val coroutineScope = rememberCoroutineScope()

    // 倒计时逻辑
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (remainingTime > 0) {
                delay(1000)
                remainingTime--
            }
            isPlaying = false
            remainingTime = 10
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 325.dp)
            .padding(4.dp)
            .clickAndLongClick(
                onClick = {
                    if (feedItem.cardType == CardType.VIDEO) {
                        isPlaying = !isPlaying
                        if (!isPlaying) {
                            remainingTime = 10 // 重置倒计时
                        }
                    } else {
                        onCardClick(feedItem.id)
                    }
                },
                onLongClick = { showDeleteDialog = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f)
        ) {
            when (feedItem.cardType) {
                CardType.IMAGE_TOP -> {
                    feedItem.imageUrl?.let {
                        ZoomableImage(
                            imageUrl = it,
                            isZoomed = isImageZoomed,
                            onToggleZoom = { isImageZoomed = it },
                            isDoubleColumn = isDoubleColumn
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    FeedCardContent(feedItem)
                }

                CardType.IMAGE_BOTTOM -> {
                    FeedCardContent(feedItem)
                    feedItem.imageUrl?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        ZoomableImage(
                            imageUrl = it,
                            isZoomed = isImageZoomed,
                            onToggleZoom = { isImageZoomed = it },
                            isDoubleColumn = isDoubleColumn
                        )
                    }
                }

                CardType.TEXT_ONLY -> {
                    FeedCardContent(feedItem)
                }

                CardType.VIDEO -> {
                    // 视频播放器模拟
                    VideoPlayerSimulator(
                        isPlaying = isPlaying,
                        remainingTime = remainingTime,
                        onPlayPauseToggle = {
                           //播放完成后重置状态
                            if (remainingTime == 0) {
                                remainingTime = 10 // 重置倒计时
                            }
                            isPlaying = !isPlaying
                        },
                        isDoubleColumn = isDoubleColumn
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeedCardContent(feedItem)
                }

                CardType.CAROUSEL -> {

                    Text("Carousel 卡片待实现", style = MaterialTheme.typography.bodyMedium)
                    FeedCardContent(feedItem)
                }

            }
            // 修改 FeedCard 中的点赞按钮部分
Box(
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
) {
    // 使用 Box 替代 IconButton 来避免内部尺寸限制
    Box(
        modifier = Modifier
            .size(24.dp)
            .align(Alignment.CenterEnd) // 右对齐
            .clickable { isLiked = !isLiked },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
            contentDescription = if (isLiked) "取消点赞" else "点赞",
            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp) // 适当大小的图标
        )
    }
}


        }
    }

    // 长按删除对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除确认") },
            text = { Text("确定要删除 \"${feedItem.title}\" 这个卡片吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRequest(feedItem.id)
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
}

@Composable
fun FeedCardContent(feedItem: FeedItem) {
    Column {
        Text(
            text = feedItem.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        //Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedItem.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun VideoPlayerSimulator(
    isPlaying: Boolean,
    remainingTime: Int,
    onPlayPauseToggle: () -> Unit,
    isDoubleColumn: Boolean = false
) {
    val videoHeight = if (isDoubleColumn) 150.dp else 180.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(videoHeight)
            .background(Color.Black)
    ) {
        // 视频缩略图占位符
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Text(
                text = "视频卡片",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 播放控制按钮
        if (!isPlaying && remainingTime > 0) {
            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                )
            }
        }

        // 暂停按钮 - 仅在播放时显示
        if (isPlaying && remainingTime > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onPlayPauseToggle() }
            )
        }


        // 倒计时显示
        Text(
            text = "$remainingTime 秒",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}