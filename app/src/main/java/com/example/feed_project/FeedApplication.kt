package com.example.feed_project

import android.app.Application
import com.example.feed_project.core.PluginManager
import com.example.feed_project.ui.components.CarouselCardPlugin

class FeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 注册插件
        PluginManager.register(CarouselCardPlugin())
        // 后续可以继续添加更多插件...

    }
}
