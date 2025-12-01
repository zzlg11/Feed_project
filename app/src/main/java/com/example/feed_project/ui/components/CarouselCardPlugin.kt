package com.example.feed_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.feed_project.core.CardPlugin
import com.example.feed_project.domain.model.CardType
import com.example.feed_project.domain.model.FeedItem

class CarouselCardPlugin : CardPlugin {
    override val cardType = CardType.CAROUSEL
    override val name = "Carousel Card"

    @Composable
    override fun Render(feedItem: FeedItem, modifier: Modifier) {
        var currentIndex by remember { mutableStateOf(0) }
        val images = feedItem.content.split(",") // 假设内容是逗号分隔的图片URL

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                // 显示标题
                Text(
                    text = feedItem.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                // 轮播图区域
                Box(modifier = Modifier.fillMaxWidth()) {
                    // 显示当前图片
                    if (images.isNotEmpty()) {
                        Image(
                            painter = rememberImagePainter(images[currentIndex].trim()),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    // 左右导航按钮
                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex > 0) currentIndex - 1
                                         else images.size - 1
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                    }

                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex < images.size - 1) currentIndex + 1
                                         else 0
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                // 指示器
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (index == currentIndex) Color.Blue else Color.Gray,
                                    CircleShape
                                )
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}


