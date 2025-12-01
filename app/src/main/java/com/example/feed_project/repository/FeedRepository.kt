package com.example.feed_project.repository

import com.example.feed_project.model.CardType
import com.example.feed_project.model.FeedItem
import com.example.feed_project.model.LayoutType
import kotlinx.coroutines.delay

class FeedRepository {
    companion object {
        private const val INITIAL_PAGE_SIZE = 10  // 初始10条数据
        private const val REFRESH_SIZE = 6        // 刷新5条数据
    }

    //添加内存缓存
    private val feedCache = mutableMapOf<Int, List<FeedItem>>()
    private var refreshCache: List<FeedItem> = emptyList()
    private val pageRetryCount = mutableMapOf<Int, Int>()
    //挂起函数suspend在后台线程模拟网络请求
    suspend fun fetchFeeds(page: Int): Result<List<FeedItem>> {
        return try {
            // 模拟网络延迟
            delay(1500)
            // 模拟网络错误情况（每三页报错）
            if (page %3 == 2) {
                val retryCount = pageRetryCount.getOrDefault(page, 0)
                if (retryCount == 0) {
                    pageRetryCount[page] = retryCount + 1
                    //网络错误时尝试使用缓存数据
                    if (feedCache.containsKey(page)) {
                        return Result.success(feedCache[page]!!)
                    }
                    return Result.failure(Exception("Network error"))
                } else {
                    // 重试时清除重试计数
                    pageRetryCount.remove(page)
                }
            }
            val startIndex = page * INITIAL_PAGE_SIZE
            val endIndex = startIndex + INITIAL_PAGE_SIZE

            val items = (startIndex until endIndex).map { index ->
                FeedItem(
                    id = "item_$index",
                    title = "动态 ${index+1}",
                    content = "这是动态卡片内容${index+1}.",
                    imageUrl =  "https://picsum.photos/seed/${index+1}/400/300" ,
                    cardType = when (index % 3) {
                        1 -> CardType.IMAGE_TOP
                        2 -> CardType.VIDEO
                        else -> CardType.IMAGE_BOTTOM
                    },
                    layoutType = if (index < startIndex + 2)
                        LayoutType.SINGLE_COLUMN
                    else
                        LayoutType.DOUBLE_COLUMN
                )
            }
            // 缓存当前页面数据
            feedCache[page] = items
            Result.success(items)
        } catch (e: Exception) {
            if (feedCache.containsKey(page)) {
                Result.success(feedCache[page]!!)
            } else {
                Result.failure(e)
            }
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
                        0 -> CardType.VIDEO
                        1 -> CardType.IMAGE_TOP
                        else -> CardType.IMAGE_BOTTOM
                    },
                    layoutType = if (index <= 2)
                        LayoutType.SINGLE_COLUMN
                    else
                        LayoutType.DOUBLE_COLUMN
                )
            }
            // 更新刷新缓存
            refreshCache = items
            Result.success(items)
        } catch (e: Exception) {
            if (refreshCache.isNotEmpty()) {
                Result.success(refreshCache)
            } else {
                Result.failure(e)
            }
        }
    }

    //清除缓存功能
    fun clearCache() {
        feedCache.clear()
        refreshCache = emptyList()
        pageRetryCount.clear()
    }
}
