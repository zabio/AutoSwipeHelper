package com.autoswipe.helper.detection

/**
 * 固定计时检测器（兜底方案）
 *
 * 当 UI 节点检测和倒计时检测都无法正常工作时，使用固定计时来触发上滑。
 * 假设从检测到广告开始，等待固定时长后自动上滑。
 */
class TimerBasedDetector(
    /** 默认广告时长（毫秒），红果短剧通常 5-15 秒 */
    private val defaultAdDurationMs: Long = 10_000L
) {
    private var adStartTime: Long = 0L
    private var isInAd = false

    /**
     * 记录检测到广告的时间
     */
    fun onAdDetected() {
        if (!isInAd) {
            adStartTime = System.currentTimeMillis()
            isInAd = true
        }
    }

    /**
     * 检查是否应该执行上滑
     * @return true 表示广告时长已到，应上滑
     */
    fun shouldSwipe(): Boolean {
        if (!isInAd) return false
        val elapsed = System.currentTimeMillis() - adStartTime
        if (elapsed >= defaultAdDurationMs) {
            isInAd = false
            return true
        }
        return false
    }

    /** 获取已等待时长（秒） */
    fun getElapsedSeconds(): Int {
        if (!isInAd) return 0
        return ((System.currentTimeMillis() - adStartTime) / 1000).toInt()
    }

    /** 获取剩余时间（秒） */
    fun getRemainingSeconds(): Int {
        if (!isInAd) return 0
        val remaining = ((defaultAdDurationMs - (System.currentTimeMillis() - adStartTime)) / 1000).toInt()
        return remaining.coerceAtLeast(0)
    }

    /** 更新广告时长设置 */
    fun setAdDuration(durationMs: Long) {
        // 允许在下次使用新时长
    }

    /** 重置状态 */
    fun reset() {
        isInAd = false
        adStartTime = 0L
    }

    /** 强制触发上滑（手动触发时使用） */
    fun forceTrigger(): Boolean {
        if (isInAd) {
            isInAd = false
            return true
        }
        return false
    }
}
