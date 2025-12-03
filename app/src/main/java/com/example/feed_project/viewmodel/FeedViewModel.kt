
package com.example.feed_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feed_project.domain.model.ExposureLog
import com.example.feed_project.domain.model.FeedItem
import com.example.feed_project.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.feed_project.ui.utils.PreloadManager
import com.example.feed_project.domain.model.CardType
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import androidx.compose.ui.platform.ComposeView
import android.view.View
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.compose.material3.Text
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.feed_project.ui.components.FeedCard
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ImageView
import android.view.ViewGroup
import androidx.core.graphics.createBitmap


class FeedViewModel() : ViewModel() {
    private val repository = FeedRepository()

    private val _feeds = MutableStateFlow<List<FeedItem>>(emptyList())
    val feeds: StateFlow<List<FeedItem>> = _feeds

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore

    private val _exposureLogs = MutableStateFlow<List<ExposureLog>>(emptyList())
    val exposureLogs: StateFlow<List<ExposureLog>> = _exposureLogs

    private var currentPage = 0
    private var pendingRetryPage: Int? = null

    private var isUsingCacheData = false


    // 添加防抖控制
    private var refreshJob: Job? = null
    private var loadJob: Job? = null
    private var prefetchJob: Job? = null

    private val PRELOAD_THRESHOLD = 3

    // 预渲染缓存
    private val preRenderedCards = mutableMapOf<String, View>()
    private var composeView: ComposeView? = null
    private var context: Context? = null
    private var lifecycleOwner: LifecycleOwner? = null

    // 预渲染队列
    private val preRenderQueue = mutableListOf<FeedItem>()
    private var isPreRendering = false

    init {
        loadFeeds()
    }


    fun loadFeeds() {
        if (_isLoading.value || !_canLoadMore.value) return

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false

            try {
                val pageToLoad = pendingRetryPage ?: currentPage
                val result = repository.fetchFeeds(pageToLoad)
                if (result.isSuccess) {
                    val newFeeds = result.getOrNull() ?: emptyList()

                    // 异步预加载图片，不阻塞主线程
                    preloadImagesAsync(newFeeds)

                    if (newFeeds.isEmpty()) {
                        _canLoadMore.value = false
                    } else {
                        val currentSize = _feeds.value.size
                        val numberedFeeds = newFeeds.mapIndexed { index, feedItem ->
                            feedItem.copy(
                                title = "动态 ${currentSize + index + 1}"
                            )
                        }
                        _feeds.value = _feeds.value + numberedFeeds
                        currentPage++
                    }
                    pendingRetryPage = null
                } else {
                    _hasError.value = true
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
                    pendingRetryPage = pageToLoad
                }
            } catch (e: Exception) {
                _hasError.value = true
                _errorMessage.value = e.message ?: "Network error"
                pendingRetryPage = currentPage
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 异步预加载图片
    private fun preloadImagesAsync(feeds: List<FeedItem>) {
        viewModelScope.launch {
            try {
                val imageUrls = feeds.flatMap { feedItem ->
                    mutableListOf<String>().apply {
                        feedItem.imageUrl?.let { add(it) }
                        if (feedItem.cardType == CardType.CAROUSEL) {
                            addAll(feedItem.content.split(",").map { it.trim() })
                        }
                    }
                }

                if (imageUrls.isNotEmpty()) {
                    PreloadManager.getInstance().preloadImages(imageUrls)
                }

                // 同时触发预渲染
                if (context != null && lifecycleOwner != null) {
                    preRenderCards(feeds)
                }
            } catch (e: Exception) {
                // 静默处理预加载错误
            }
        }
    }



    // 添加预加载方法
    fun prefetchRefreshData(visibleItemCount: Int = 0) {
        // 根据可见项目数决定是否需要预加载
        if (visibleItemCount < PRELOAD_THRESHOLD) return

        prefetchJob?.cancel()

        prefetchJob = viewModelScope.launch {
            try {
                val prefetchResult = repository.refreshFeeds()
                if (prefetchResult.isSuccess) {
                    val prefetchedFeeds = prefetchResult.getOrNull() ?: emptyList()
                    preloadImagesAsync(prefetchedFeeds)
                }
            } catch (e: Exception) {
                // 静默处理预加载错误
            }
        }
    }


    fun refreshFeeds() {
    // 防止并发刷新
    if (_isRefreshing.value) return
    refreshJob?.cancel()
    refreshJob=viewModelScope.launch {
        _isRefreshing.value = true
        _hasError.value = false

        try {
            val result = repository.refreshFeeds()
            if (result.isSuccess) {
                val refreshedFeeds = result.getOrNull() ?: emptyList()
                val existingFeeds = _feeds.value ?: emptyList()

                // 新数据放在顶部，旧数据保持原有顺序在后面
                val mergedFeeds = refreshedFeeds + existingFeeds

                // 重新编号：从顶部开始为 1, 2, 3...
                val renumberedFeeds = mergedFeeds.mapIndexed { index, feedItem ->
                    feedItem.copy(
                        title = "动态 ${index + 1}"
                    )
                }

                _feeds.value = renumberedFeeds
                // 重置分页状态，因为现在数据已经重新排列
                currentPage = 0
                pendingRetryPage = null

                // 延迟执行图片预加载，让用户先看到内容
                launch {
                    delay(100) // 短暂延迟
                    // 图片预加载 - 下拉刷新时也执行
                    val imageUrls = refreshedFeeds.flatMap { feedItem ->
                        mutableListOf<String>().apply {
                            feedItem.imageUrl?.let { add(it) }
                            // 轮播图图片预加载
                            if (feedItem.cardType == CardType.CAROUSEL) {
                                addAll(feedItem.content.split(",").map { it.trim() })
                            }
                        }
                    }

                    // 执行图片预加载
                    if (imageUrls.isNotEmpty()) {
                        PreloadManager.getInstance().preloadImages(imageUrls)
                    }
                }
            } else {
                _hasError.value = true
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            _hasError.value = true
            _errorMessage.value = e.message ?: "Refresh failed"
        } finally {
            _isRefreshing.value = false
            }
        }
    }

    fun deleteFeed(id: String) {
        _feeds.value = _feeds.value.filter { it.id != id }
    }

    fun retry() {
        if (_hasError.value) {
            if (_feeds.value.isEmpty()) {
                _hasError.value = false
                _errorMessage.value = ""
                loadFeeds()
            } else {
                // 如果已有数据，则尝试加载更多
                loadFeeds()
            }
        }
    }

    fun addExposureLog(log: ExposureLog) {
        val currentLogs = _exposureLogs.value.toMutableList()
        currentLogs.add(log)
        _exposureLogs.value = currentLogs
    }

    fun clearExposureLogs() {
        _exposureLogs.value = emptyList()
    }

    fun loadMoreFeeds() {
        loadFeeds()
    }


    fun setupPreRenderContext(context: Context, lifecycleOwner: LifecycleOwner) {
        this.context = context
        this.lifecycleOwner = lifecycleOwner
        this.composeView = ComposeView(context).apply {
        setContent {
            Text("PreRender ComposeView")
        }
    }

    }

    fun preRenderCards(feeds: List<FeedItem>) {
        if (isPreRendering || feeds.isEmpty()) return

        viewModelScope.launch {
            isPreRendering = true
            try {
                // 清理过期的预渲染视图
                cleanupOldPreRenders()

                // 预渲染可见区域附近的卡片
                val itemsToPreRender = feeds.take(5) // 预渲染前5个卡片

                itemsToPreRender.forEach { feedItem ->
                    if (!preRenderedCards.containsKey(feedItem.id)) {
                        preRenderSingleCard(feedItem)
                    }
                }
            } catch (e: Exception) {
                // 静默处理预渲染错误
            } finally {
                isPreRendering = false
            }
        }
    }

    private suspend fun preRenderSingleCard(feedItem: FeedItem) {
        withContext(Dispatchers.Main.immediate) {
            try {
                composeView?.setContent {
                    FeedCard(
                        feedItem = feedItem,
                        onDeleteRequest = {},
                        onCardClick = {}
                    )
                }

                // 触发测量和布局
                composeView?.measure(
                    View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )

                composeView?.layout(0, 0, composeView!!.measuredWidth, composeView!!.measuredHeight)

                // 创建 Bitmap 并绘制当前视图内容
                val bitmap = createBitmap(
                    composeView!!.width,
                    composeView!!.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                composeView?.draw(canvas)

                // 可以选择将 bitmap 包装成 ImageView 或其他容器进行复用
                val imageView = ImageView(context)
                imageView.setImageBitmap(bitmap)

                preRenderedCards[feedItem.id] = imageView
            } catch (e: Exception) {
                // 静默处理单个卡片预渲染错误
            }
        }
    }


    private fun cleanupOldPreRenders() {
        // 保留最近使用的预渲染卡片，清理旧的
        if (preRenderedCards.size > 20) {
            val keysToRemove = preRenderedCards.keys.take(preRenderedCards.size - 15)
            keysToRemove.forEach { key ->
                preRenderedCards.remove(key)?.let { view ->
                    // 清理视图资源
                    cleanupView(view)
                }
            }
        }
    }

    private fun cleanupView(view: View) {
        // 清理视图相关资源
        try {
            // 移除视图引用
            if (view.parent != null) {
                (view.parent as? ViewGroup)?.removeView(view)
            }
        } catch (e: Exception) {
            // 静默处理清理错误
        }
    }

    fun getPreRenderedCard(id: String): View? {
        return preRenderedCards[id]?.also {
            // 使用后从缓存中移除，避免重复使用
            preRenderedCards.remove(id)
        }
    }

    fun clearPreRenderCache() {
        preRenderedCards.values.forEach { view ->
            cleanupView(view)
        }
        preRenderedCards.clear()
        preRenderQueue.clear()
    }

    // 在 ViewModel 清理时调用
    override fun onCleared() {
        clearPreRenderCache()
        composeView = null
        context = null
        lifecycleOwner = null
        super.onCleared()
    }


}
