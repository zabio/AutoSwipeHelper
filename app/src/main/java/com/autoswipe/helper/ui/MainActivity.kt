package com.autoswipe.helper.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.autoswipe.helper.R
import com.autoswipe.helper.config.AppConfig
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.databinding.ActivityMainBinding
import com.autoswipe.helper.keepalive.KeepAliveForegroundService
import com.autoswipe.helper.permission.PermissionHelper
import com.autoswipe.helper.util.LogUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionHelper = PermissionHelper(this)
        preferencesManager = PreferencesManager(this)

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        refreshServiceStatus()

        val ext = intent.getStringExtra("EXTRA_EXIT")

        if (ext=="LOGOUT"){
            finish()
            finishAffinity()
            return
        }
        // 无障碍已开启时自动启动辅助服务
        if (permissionHelper.isAccessibilityServiceEnabled() && !preferencesManager.isPaused()) {
            startKeepAliveService()
            startFloatingWindow()
        }
    }


    private fun setupClickListeners() {
        // 开启无障碍服务
        binding.btnToggleService.setOnClickListener {
            permissionHelper.openAccessibilitySettings()
            Toast.makeText(this, "请在列表中找到「${AppConfig.APP_NAME}」并开启", Toast.LENGTH_LONG).show()
        }

        // 权限项点击
        binding.itemAccessibility.setOnClickListener { permissionHelper.openAccessibilitySettings() }
        binding.chipAccessibility.setOnClickListener { permissionHelper.openAccessibilitySettings() }
        binding.itemOverlay.setOnClickListener { permissionHelper.openOverlaySettings() }
        binding.chipOverlay.setOnClickListener { permissionHelper.openOverlaySettings() }
        binding.itemNotification.setOnClickListener { permissionHelper.openAppSettings() }
        binding.chipNotification.setOnClickListener { permissionHelper.openAppSettings() }
        binding.itemBattery.setOnClickListener { permissionHelper.requestIgnoreBatteryOptimizations() }
        binding.chipBattery.setOnClickListener { permissionHelper.requestIgnoreBatteryOptimizations() }

        // 设置
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 启动/停止
        binding.btnStartService.setOnClickListener {
            if (permissionHelper.isAccessibilityServiceEnabled()) {
                startKeepAliveService()
                startFloatingWindow()
                preferencesManager.setPaused(false)
                Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
                permissionHelper.openAccessibilitySettings()
            }
        }
    }

    private fun refreshPermissionStatus() {
        // 无障碍服务
        val accessibilityGranted = permissionHelper.isAccessibilityServiceEnabled()
        updateChipStatus(binding.chipAccessibility, accessibilityGranted)

        // 悬浮窗权限
        val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
        updateChipStatus(binding.chipOverlay, overlayGranted)

        // 通知权限 (仅 Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            updateChipStatus(binding.chipNotification, notificationGranted)
            binding.itemNotification.visibility = View.VISIBLE
        } else {
            updateChipStatus(binding.chipNotification, true)
            binding.itemNotification.visibility = View.GONE
        }

        // 电池优化
        val batteryGranted = permissionHelper.isIgnoringBatteryOptimizations()
        updateChipStatus(binding.chipBattery, batteryGranted)
    }

    private fun updateChipStatus(chip: com.google.android.material.chip.Chip, granted: Boolean) {
        if (granted) {
            chip.text = getString(R.string.permission_granted)
            chip.setChipIconResource(android.R.drawable.presence_online)
            chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.status_running)
        } else {
            chip.text = getString(R.string.permission_not_granted)
            chip.setChipIconResource(android.R.drawable.presence_offline)
            chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.status_paused)
        }
    }

    private fun refreshServiceStatus() {
        val isAccessibilityOn = permissionHelper.isAccessibilityServiceEnabled()
        val isPaused = preferencesManager.isPaused()
        val statusIndicator = binding.statusIndicator.background as? GradientDrawable

        when {
            !isAccessibilityOn -> {
                binding.tvServiceStatus.text = getString(R.string.service_stopped)
                statusIndicator?.setColor(ContextCompat.getColor(this, R.color.status_stopped))
                binding.btnToggleService.text = getString(R.string.open_accessibility)
                binding.btnStartService.text = "▶ 开始"
                binding.tvSwipeCount.visibility = View.GONE
            }
            isPaused -> {
                binding.tvServiceStatus.text = getString(R.string.service_paused)
                statusIndicator?.setColor(ContextCompat.getColor(this, R.color.status_paused))
                binding.btnToggleService.text = "恢复服务"
                binding.btnStartService.text = "▶ 恢复"
            }
            else -> {
                binding.tvServiceStatus.text = getString(R.string.service_running)
                statusIndicator?.setColor(ContextCompat.getColor(this, R.color.status_running))
                binding.btnToggleService.text = "暂停服务"
                binding.btnStartService.text = "■ 停止"
                val count = preferencesManager.getTotalSwipeCount()
                binding.tvSwipeCount.text = getString(R.string.swipe_count, count)
                binding.tvSwipeCount.visibility = View.VISIBLE
            }
        }
    }

    private fun startKeepAliveService() {
        try {
            val intent = Intent(this, KeepAliveForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            LogUtil.d("KeepAliveForegroundService started")
        } catch (e: Exception) {
            LogUtil.e("Failed to start KeepAliveForegroundService", e)
        }
    }

    private fun startFloatingWindow() {
        if (!preferencesManager.isFloatingWindowEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(this, FloatingWindowService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            LogUtil.d("FloatingWindowService started")
        } catch (e: Exception) {
            LogUtil.e("Failed to start FloatingWindowService", e)
        }
    }
}