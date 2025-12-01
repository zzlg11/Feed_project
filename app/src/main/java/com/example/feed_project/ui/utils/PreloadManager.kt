package com.example.feed_project.ui.utils

import android.content.Context
import com.example.feed_project.ui.utils.ImagePreloader


class PreloadManager private constructor(context: Context) {
    private val imagePreloader = ImagePreloader(context.applicationContext)

    companion object {
        @Volatile
        private var INSTANCE: PreloadManager? = null

        fun getInstance(): PreloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: throw IllegalStateException("PreloadManager not initialized")
            }
        }

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = PreloadManager(context)
                    }
                }
            }
        }
    }

    fun preloadImages(imageUrls: List<String>) {
        imagePreloader.preloadImages(imageUrls)
    }
}

