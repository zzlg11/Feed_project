package com.example.feed_project

import android.app.Application
import com.example.feed_project.core.PluginManager
import com.example.feed_project.ui.components.CarouselCardPlugin
import com.example.feed_project.ui.utils.PreloadManager

class FeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化预加载管理器
        PreloadManager.initialize(this)

        // 注册插件
        PluginManager.register(CarouselCardPlugin())
        // 后续可以继续添加更多插件...

    }
}
