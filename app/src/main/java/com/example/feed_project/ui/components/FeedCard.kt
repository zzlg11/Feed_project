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
import com.example.feed_project.model.CardType
import com.example.feed_project.model.FeedItem
import androidx.compose.ui.composed
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyListState
import com.example.feed_project.model.ExposureLog
import com.example.feed_project.ui.utils.AdvancedExposureTracker
import com.example.feed_project.ui.components.ExposureTestTool

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
    onToggleZoom: (Boolean) -> Unit
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
                        .height(400.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
    onCardClick: (String) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isImageZoomed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 325.dp)
            .padding(4.dp)
            .clickAndLongClick(
                onClick = { onCardClick(feedItem.id) },
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
                            onToggleZoom = { isImageZoomed = it }
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
                            onToggleZoom = { isImageZoomed = it }
                        )
                    }
                }

                CardType.TEXT_ONLY -> {
                    FeedCardContent(feedItem)
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