package com.example.feed_project.core

import com.example.feed_project.core.CardPlugin
import com.example.feed_project.domain.model.CardType

object PluginManager {
    private val plugins = mutableMapOf<CardType, CardPlugin>()

    fun register(plugin: CardPlugin) {
        plugins[plugin.cardType] = plugin
    }

    fun getPlugin(cardType: CardType): CardPlugin? = plugins[cardType]

    fun getAllPlugins(): List<CardPlugin> = plugins.values.toList()

    fun getSupportedCardTypes(): Set<CardType> = plugins.keys
}
