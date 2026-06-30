package com.autoswipe.helper.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限管理助手
 *
 * 负责检查和引导用户开启所有必需权限：
 * 1. 无障碍服务
 * 2. 悬浮窗权限
 * 3. 通知权限 (Android 13+)
 * 4. 电池优化豁免
 */
class PermissionHelper(private val activity: Activity) {

    /**
     * 权限项定义
     */
    sealed class PermissionItem(
        val name: String,
        val description: String,
        val isRequired: Boolean = true
    ) {
        class Accessibility : PermissionItem("无障碍服务", "用于检测广告状态并自动上滑")
        class Overlay : PermissionItem("悬浮窗权限", "用于显示控制悬浮窗")
        class Notification : PermissionItem("通知权限", "用于显示服务运行状态")
        class BatteryOptimization : PermissionItem("电池优化", "防止服务被系统关闭", isRequired = false)
    }

    /**
     * 检查所有权限状态
     * @return 未授权的权限列表
     */
    fun checkAllPermissions(): List<PermissionItem> {
        val missing = mutableListOf<PermissionItem>()

        // 1. 无障碍服务
        if (!isAccessibilityServiceEnabled()) {
            missing.add(PermissionItem.Accessibility())
        }

        // 2. 悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                missing.add(PermissionItem.Overlay())
            }
        }

        // 3. 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missing.add(PermissionItem.Notification())
            }
        }

        // 4. 电池优化豁免
        if (!isIgnoringBatteryOptimizations()) {
            missing.add(PermissionItem.BatteryOptimization())
        }

        return missing
    }

    /**
     * 检查无障碍服务是否已开启
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        // 尝试多种可能的服务名格式（含 debug 后缀）
        val possibleNames = listOf(
            "${activity.packageName}/.accessibility.AutoSwipeService",
            "${activity.packageName}/${activity.packageName}.accessibility.AutoSwipeService",
        )

        val enabledServices = Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        // 用 : 分隔多个服务，逐个匹配
        val services = enabledServices.split(':')
        return possibleNames.any { name -> services.any { it.equals(name, ignoreCase = true) } }
                || enabledServices.lowercase().contains("autoswipe")
    }

    /**
     * 打开系统无障碍设置页面
     */
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    /**
     * 打开悬浮窗权限设置
     */
    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    /**
     * 请求通知权限
     */
    fun requestNotificationPermission(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
    }

    /**
     * 检查是否已忽略电池优化
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }

    /**
     * 打开电池优化豁免请求页面
     */
    fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${activity.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }

    /**
     * 打开应用详情设置页面
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
}