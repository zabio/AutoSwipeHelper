package com.autoswipe.helper

import android.app.Application
import com.autoswipe.helper.util.LogUtil

/**
 * Application 类
 *
 * 应用初始化入口
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化日志配置
        LogUtil.isDebugEnabled = true

        LogUtil.d("AutoSwipeHelper application initialized")
    }

    companion object {
        @Volatile
        private var instance: App? = null

        fun getInstance(): App {
            return instance ?: throw IllegalStateException("App not initialized")
        }
    }
}
