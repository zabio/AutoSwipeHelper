package com.autoswipe.helper.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.autoswipe.helper.accessibility.AccessibilityConfig

/**
 * SharedPreferences 配置管理器
 *
 * 管理所有用户可配置的参数，包括：
 * - 手势参数
 * - 目标应用配置
 * - 检测策略参数
 * - UI 偏好
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    // ==================== 目标应用 ====================

    fun getTargetPackages(): Set<String> {
        val saved = prefs.getStringSet(KEY_TARGET_PACKAGES, null)
        return saved ?: AccessibilityConfig.TARGET_PACKAGES
    }

    fun setTargetPackages(packages: Set<String>) {
        prefs.edit { putStringSet(KEY_TARGET_PACKAGES, packages) }
    }

    fun isTargetApp(packageName: String): Boolean {
        return getTargetPackages().contains(packageName)
    }

    fun addTargetPackage(packageName: String) {
        val current = getTargetPackages().toMutableSet()
        current.add(packageName)
        setTargetPackages(current)
    }

    fun removeTargetPackage(packageName: String) {
        val current = getTargetPackages().toMutableSet()
        current.remove(packageName)
        setTargetPackages(current)
    }

    // ==================== 手势参数 ====================

    /** 检测到广告结束到执行上滑的延迟 (ms) */
    fun getSwipeDelay(): Long {
        return prefs.getLong(KEY_SWIPE_DELAY, AppConfig.Defaults.SWIPE_DELAY_MS)
    }

    fun setSwipeDelay(delayMs: Long) {
        prefs.edit { putLong(KEY_SWIPE_DELAY, delayMs.coerceIn(0, 3000)) }
    }

    /** 滑动动画持续时间 (ms) */
    fun getSwipeDuration(): Long {
        return prefs.getLong(KEY_SWIPE_DURATION, AppConfig.Defaults.SWIPE_DURATION_MS)
    }

    fun setSwipeDuration(durationMs: Long) {
        prefs.edit { putLong(KEY_SWIPE_DURATION, durationMs.coerceIn(50, 1000)) }
    }

    /** 两次滑动最小间隔 (ms) */
    fun getMinSwipeInterval(): Long {
        return prefs.getLong(KEY_MIN_SWIPE_INTERVAL, AppConfig.Defaults.MIN_SWIPE_INTERVAL_MS)
    }

    fun setMinSwipeInterval(intervalMs: Long) {
        prefs.edit { putLong(KEY_MIN_SWIPE_INTERVAL, intervalMs.coerceIn(500, 10000)) }
    }

    /** 滑动起始位置（屏幕高度比例，0-1） */
    fun getSwipeStartRatio(): Float {
        return prefs.getFloat(KEY_SWIPE_START_RATIO, AppConfig.Defaults.SWIPE_START_RATIO)
    }

    fun setSwipeStartRatio(ratio: Float) {
        prefs.edit { putFloat(KEY_SWIPE_START_RATIO, ratio.coerceIn(0.3f, 1.0f)) }
    }

    /** 滑动结束位置（屏幕高度比例，0-1） */
    fun getSwipeEndRatio(): Float {
        return prefs.getFloat(KEY_SWIPE_END_RATIO, AppConfig.Defaults.SWIPE_END_RATIO)
    }

    fun setSwipeEndRatio(ratio: Float) {
        prefs.edit { putFloat(KEY_SWIPE_END_RATIO, ratio.coerceIn(0.0f, 0.7f)) }
    }

    // ==================== 检测参数 ====================

    /** 固定广告时长（兜底方案，ms） */
    fun getFixedAdDuration(): Long {
        return prefs.getLong(KEY_FIXED_AD_DURATION, AppConfig.Defaults.FIXED_AD_DURATION_MS)
    }

    fun setFixedAdDuration(durationMs: Long) {
        prefs.edit { putLong(KEY_FIXED_AD_DURATION, durationMs.coerceIn(3000, 30000)) }
    }

    /** 检测灵敏度 (1=低, 2=中, 3=高) */
    fun getDetectionSensitivity(): Int {
        return prefs.getInt(KEY_DETECTION_SENSITIVITY, AppConfig.Defaults.DETECTION_SENSITIVITY)
    }

    fun setDetectionSensitivity(level: Int) {
        prefs.edit { putInt(KEY_DETECTION_SENSITIVITY, level.coerceIn(1, 3)) }
    }

    // ==================== UI 偏好 ====================

    /** 是否显示悬浮窗 */
    fun isFloatingWindowEnabled(): Boolean {
        return prefs.getBoolean(KEY_FLOATING_ENABLED, AppConfig.Defaults.FLOATING_WINDOW_ENABLED)
    }

    fun setFloatingWindowEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_FLOATING_ENABLED, enabled) }
    }

    /** 是否显示通知 */
    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, AppConfig.Defaults.NOTIFICATION_ENABLED)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATION_ENABLED, enabled) }
    }

    /** 是否开机自启 */
    fun isAutoStartEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_START, AppConfig.Defaults.AUTO_START_ENABLED)
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_START, enabled) }
    }

    /** 自动上滑是否暂停 */
    fun isPaused(): Boolean {
        return prefs.getBoolean(KEY_IS_PAUSED, false)
    }

    fun setPaused(paused: Boolean) {
        prefs.edit { putBoolean(KEY_IS_PAUSED, paused) }
    }

    /** 获取上滑总次数 */
    fun getTotalSwipeCount(): Long {
        return prefs.getLong(KEY_TOTAL_SWIPE_COUNT, 0)
    }

    fun incrementSwipeCount() {
        prefs.edit { putLong(KEY_TOTAL_SWIPE_COUNT, getTotalSwipeCount() + 1) }
    }

    companion object {
        private const val PREFS_NAME = "auto_swipe_prefs"

        // Key 常量
        private const val KEY_TARGET_PACKAGES = "target_packages"
        private const val KEY_SWIPE_DELAY = "swipe_delay"
        private const val KEY_SWIPE_DURATION = "swipe_duration"
        private const val KEY_MIN_SWIPE_INTERVAL = "min_swipe_interval"
        private const val KEY_SWIPE_START_RATIO = "swipe_start_ratio"
        private const val KEY_SWIPE_END_RATIO = "swipe_end_ratio"
        private const val KEY_FIXED_AD_DURATION = "fixed_ad_duration"
        private const val KEY_DETECTION_SENSITIVITY = "detection_sensitivity"
        private const val KEY_FLOATING_ENABLED = "floating_enabled"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_IS_PAUSED = "is_paused"
        private const val KEY_TOTAL_SWIPE_COUNT = "total_swipe_count"
    }
}
