package com.example.feed_project.ui.utils

import android.content.Context
import coil.Coil
import coil.request.ImageRequest

@Suppress("SpellCheckingInspection")
class ImagePreloader(private val context: Context) {
    fun preloadImages(imageUrls: List<String>) {
        imageUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCacheKey(url)
                .diskCacheKey(url)
                .build()
            Coil.imageLoader(context).enqueue(request)
        }
    }
}
