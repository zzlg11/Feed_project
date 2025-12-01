package com.example.feed_project.domain.model


data class FeedItem(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val cardType: CardType,
    val layoutType: LayoutType,
    val doubleColumnPosition: DoubleColumnPosition? = null
)
enum class DoubleColumnPosition {
    LEFT, RIGHT
}

enum class CardType {
    TEXT_ONLY,
    IMAGE_TOP,
    IMAGE_BOTTOM,
    VIDEO,
    CAROUSEL
}

enum class LayoutType {
    SINGLE_COLUMN,
    DOUBLE_COLUMN
}

enum class ExposureEvent {
    VISIBLE,           // 卡片露出
    VISIBLE_50_PERCENT, // 卡片露出超过50%
    FULLY_VISIBLE,     // 卡片完整露出
    INVISIBLE          // 卡片消失
}

data class ExposureLog(
    val itemId: String,
    val event: ExposureEvent,
    val timestamp: Long = System.currentTimeMillis()
)
