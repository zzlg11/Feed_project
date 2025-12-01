package com.example.feed_project.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.feed_project.domain.model.FeedItem
import com.example.feed_project.domain.model.CardType

interface CardRenderer {
    @Composable
    fun Render(feedItem: FeedItem, modifier: Modifier = Modifier)

    fun supports(cardType: CardType): Boolean
}



