package com.example.feed_project.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.CircleShape

@Composable
fun SkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)  // 减小 padding 从 8.dp 到 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {  // 减小内边距从 16.dp 到 12.dp
            // 头像占位符
            Box(
                modifier = Modifier
                    .size(32.dp)  // 减小头像尺寸从 40.dp 到 32.dp
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))  // 减小间隔从 8.dp 到 6.dp

            // 文本占位符
            repeat(2) {  // 减少文本行数从 3 行到 2 行
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)  // 减小文本高度从 16.dp 到 12.dp
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(3.dp))  // 减小间隔从 4.dp 到 3.dp
            }

            // 图片占位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)  // 减小图片高度从 150.dp 到 100.dp
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
