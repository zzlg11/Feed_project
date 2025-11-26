package com.example.feed_project.repository

import com.example.feed_project.model.CardType
import com.example.feed_project.model.FeedItem
import com.example.feed_project.model.LayoutType
import kotlinx.coroutines.delay

class FeedRepository {
    companion object {
        private const val INITIAL_PAGE_SIZE = 10  // 初始10条数据
        private const val REFRESH_SIZE = 5        // 刷新5条数据
    }

    //挂起函数suspend在后台线程模拟网络请求
    suspend fun fetchFeeds(page: Int): Result<List<FeedItem>> {
        return try {
            delay(1500) // 模拟网络延迟
            // 模拟网络错误情况（第5页模拟网络错误）
            if (page == 4) {
                return Result.failure(Exception("Network error"))
            }
            val startIndex = page * INITIAL_PAGE_SIZE
            val endIndex = startIndex + INITIAL_PAGE_SIZE

            val items = (startIndex until endIndex).map { index ->
                FeedItem(
                    id = "item_$index",
                    title = "动态 ${index+1}",
                    content = "这是初始动态内容${index+1}.",
                    imageUrl =  "https://picsum.photos/seed/${index+1}/400/300" ,
                    cardType = when (index % 2) {
                        1 -> CardType.IMAGE_TOP
                        else -> CardType.IMAGE_BOTTOM
                    },
                    //layoutType = if (index % 7 == 5 || index % 7 == 6)
                    LayoutType.SINGLE_COLUMN

                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshFeeds(): Result<List<FeedItem>> {
        val timeSuffix = System.currentTimeMillis().toString().takeLast(3)
        return try {
            delay(1500) // 模拟网络延迟

            // 刷新时加载REFRESH_SIZE条新数据，编号从1开始
            val items = (1..REFRESH_SIZE).map { index ->
                FeedItem(
                    id = "refresh_item_${timeSuffix}_$index",
                    title = "动态 $index",
                    content = "这是一条新刷新的动态内容。",
                    imageUrl = if (index % 3 != 0) "https://picsum.photos/seed/refresh${System.currentTimeMillis()}_$index/400/300" else null,
                    cardType = when (index % 3) {
                        0 -> CardType.TEXT_ONLY
                        1 -> CardType.IMAGE_TOP
                        else -> CardType.IMAGE_BOTTOM
                    },
                    layoutType = if (index % 5 == 3 || index % 5 == 4)
                         LayoutType.DOUBLE_COLUMN
                    else
                        LayoutType.SINGLE_COLUMN

                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
