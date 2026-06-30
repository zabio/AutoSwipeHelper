package com.autoswipe.helper.accessibility

/**
 * 无障碍服务配置常量
 * 红果短剧专项优化
 */
object AccessibilityConfig {

    /** 目标应用包名 */
    val TARGET_PACKAGES = setOf(
        "com.redfruit.drama",       // 红果短剧
        "com.phoenix.read",       // 红果短剧
        "com.ss.android.ugc.aweme", // 抖音
        "com.kuaishou.nebula",      // 快手
    )

    /** 事件处理防抖间隔（毫秒） */
    const val EVENT_DEBOUNCE_MS = 300L

    /** 默认两次滑动最小间隔（毫秒） */
    const val DEFAULT_SWIPE_INTERVAL_MS = 2000L

    /** 默认检测到广告后延迟执行上滑（毫秒） */
    const val DEFAULT_SWIPE_DELAY_MS = 500L

    /** 默认滑动持续时间（毫秒） */
    const val DEFAULT_SWIPE_DURATION_MS = 300L

    /** 红果短剧专属 - 广告结束标识文本 */
    val HONGGUO_AD_FINISHED_PATTERNS = listOf(
        "上滑继续观看短剧",
        "上滑继续观看",
        "上滑观看下一集",
        "向上滑动继续",
        "滑动继续观看",
        "继续观看",
        "短剧",
    )

    /** 通用广告结束标识文本 */
    val GENERAL_AD_FINISHED_PATTERNS = listOf(
        "上滑继续观看",
        "上滑观看",
        "滑动继续",
        "向上滑动",
        "继续观看",
        "观看下集",
        "下一集",
        "跳过广告",
        "广告结束",
        "上滑",
        "滑动",
    )

    /** 倒计时结束文本模式 */
    val COUNTDOWN_END_PATTERNS = listOf(
        "0s", "0秒", "即将播放", "播放中"
    )

    /** 广告相关文本（辅助判断） */
    val AD_LABEL_PATTERNS = listOf(
        "广告", "推广", "赞助", "sponsored", "ad"
    )
}