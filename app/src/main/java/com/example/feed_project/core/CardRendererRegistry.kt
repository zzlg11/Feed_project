// 文件路径: src/main/java/com/example/feed_project/core/CardRendererRegistry.kt
package com.example.feed_project.core

import com.example.feed_project.core.CardRenderer
import com.example.feed_project.domain.model.CardType

object CardRendererRegistry {
    private val renderers = mutableListOf<CardRenderer>()

    fun register(renderer: CardRenderer) {
        renderers.add(renderer)
    }

    fun getRendererFor(cardType: CardType): CardRenderer? {
        return renderers.find { it.supports(cardType) }
    }
}
