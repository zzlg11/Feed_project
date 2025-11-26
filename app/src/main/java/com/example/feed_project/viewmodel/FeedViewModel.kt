
package com.example.feed_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feed_project.model.ExposureEvent
import com.example.feed_project.model.ExposureLog
import com.example.feed_project.model.FeedItem
import com.example.feed_project.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class FeedViewModel : ViewModel() {
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

    init {
        loadFeeds()
    }

    fun loadFeeds() {
        if (_isLoading.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false

            try {
                val result = repository.fetchFeeds(currentPage)
                if (result.isSuccess) {
                    val newFeeds = result.getOrNull() ?: emptyList()
                    _feeds.value = _feeds.value + newFeeds

                    if (newFeeds.isEmpty()) {
                        _canLoadMore.value = false
                    } else {
                        currentPage++
                    }
                } else {
                    _hasError.value = true
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _hasError.value = true
                _errorMessage.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshFeeds() {
        viewModelScope.launch {
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
                    // 不重置 currentPage，保持分页状态（如适用）
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
    if (_isLoading.value || !_canLoadMore.value) return

    viewModelScope.launch {
        _isLoading.value = true
        _hasError.value = false

        try {
            delay(1500)
            val result = repository.fetchFeeds(currentPage)
            if (result.isSuccess) {
                val newFeeds = result.getOrNull() ?: emptyList()
                if (newFeeds.isEmpty()) {
                    _canLoadMore.value = false
                } else {
                    _feeds.value = _feeds.value + newFeeds
                    currentPage++
                }
            } else {
                _hasError.value = true
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        } catch (e: Exception) {
            _hasError.value = true
            _errorMessage.value = e.message ?: "Network error"
        } finally {
            _isLoading.value = false
        }
    }
}


}
