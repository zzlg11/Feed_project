package com.example.feed_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.feed_project.ui.screens.FeedScreen
import com.example.feed_project.ui.theme.Feed_projectTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feed_project.viewmodel.FeedViewModel
import com.example.feed_project.ui.components.SkeletonCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Feed_projectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: FeedViewModel = viewModel()
                    FeedScreenWithSkeleton()
                    //FeedScreen()
                }
            }
        }
    }
}

@Composable
fun FeedScreenWithSkeleton() {
    val viewModel: FeedViewModel = viewModel()
    val feeds by viewModel.feeds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 在数据加载完成前显示骨架屏
    if (feeds.isEmpty() && isLoading) {
        LazyColumn {
            items(8) {  // 显示5个骨架屏
                SkeletonCard()
            }
        }
    } else {
        FeedScreen(viewModel)
    }
}

