package com.autoswipe.helper.config

/**
 * 应用配置常量
 */
object AppConfig {
    /** 应用名称 */
    const val APP_NAME = "自动上滑助手"

    /** 通知渠道 ID - 服务保活 */
    const val CHANNEL_KEEP_ALIVE = "keep_alive_channel"

    /** 通知渠道 ID - 服务状态 */
    const val CHANNEL_SERVICE_STATUS = "service_status_channel"

    /** 通知 ID - 前台服务保活 */
    const val NOTIFY_ID_KEEP_ALIVE = 1001

    /** 通知 ID - 服务运行状态 */
    const val NOTIFY_ID_SERVICE_STATUS = 1002

    /** 通知 ID - 悬浮窗 */
    const val NOTIFY_ID_FLOATING = 1003

    /** 默认配置值 */
    object Defaults {
        /** 默认滑动延迟 (ms) — 检测到广告后等待再滑 */
        const val SWIPE_DELAY_MS = 200L

        /** 默认滑动持续时间 (ms) — 手指从起点滑到终点的时间 */
        const val SWIPE_DURATION_MS = 200L

        /** 默认最小滑动间隔 (ms) */
        const val MIN_SWIPE_INTERVAL_MS = 3000L

        /** 默认滑动起始位置（屏幕高度比例） */
        const val SWIPE_START_RATIO = 0.90f

        /** 默认滑动结束位置（屏幕高度比例） */
        const val SWIPE_END_RATIO = 0.10f

        /** 默认固定广告时长 (ms) */
        const val FIXED_AD_DURATION_MS = 10_000L

        /** 默认是否开启悬浮窗 */
        const val FLOATING_WINDOW_ENABLED = true

        /** 默认是否开启通知 */
        const val NOTIFICATION_ENABLED = true

        /** 默认是否开机自启 */
        const val AUTO_START_ENABLED = false

        /** 默认检测灵敏度 (1=低, 2=中, 3=高) */
        const val DETECTION_SENSITIVITY = 2
    }
}