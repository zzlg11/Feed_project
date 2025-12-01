package com.example.feed_project.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feed_project.domain.model.FeedItem
import com.example.feed_project.domain.model.CardType
import com.example.feed_project.domain.model.LayoutType

interface CardPlugin {
    val cardType: CardType
    val name: String

    @Composable
    fun Render(feedItem: FeedItem, modifier: Modifier = Modifier)

    fun createFeedItem(id: String, title: String, content: String): FeedItem {
        return FeedItem(
            id = id,
            title = title,
            content = content,
            imageUrl = null,
            cardType = cardType,
            layoutType = LayoutType.SINGLE_COLUMN
        )
    }
}
