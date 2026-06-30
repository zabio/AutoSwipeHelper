package com.autoswipe.helper.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.autoswipe.helper.accessibility.AutoSwipeService
import com.autoswipe.helper.config.AppConfig
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.databinding.ActivitySettingsBinding
import com.autoswipe.helper.keepalive.KeepAliveForegroundService
import com.autoswipe.helper.util.NotificationHelper
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupSliders()
        setupSwitches()
        setupButtons()
    }

    @SuppressLint("SetTextI18n")
    private fun setupSliders() {
        // 上滑延迟
        val swipeDelay = preferencesManager.getSwipeDelay()
        binding.sliderSwipeDelay.value = swipeDelay.toFloat()
        binding.tvSwipeDelayValue.text = "${swipeDelay}ms"
        binding.sliderSwipeDelay.addOnChangeListener { _, value, _ ->
            val v = value.toLong()
            preferencesManager.setSwipeDelay(v)
            binding.tvSwipeDelayValue.text = "${v}ms"
        }

        // 滑动速度（持续时间）
        val swipeDuration = preferencesManager.getSwipeDuration()
        binding.sliderSwipeDuration.value = swipeDuration.toFloat()
        binding.tvSwipeDurationValue.text = "${swipeDuration}ms"
        binding.sliderSwipeDuration.addOnChangeListener { _, value, _ ->
            val v = value.toLong()
            preferencesManager.setSwipeDuration(v)
            binding.tvSwipeDurationValue.text = "${v}ms"
        }

        // 最小滑动间隔
        val minInterval = preferencesManager.getMinSwipeInterval()
        binding.sliderMinInterval.value = minInterval.toFloat()
        binding.tvMinIntervalValue.text = "${minInterval / 1000.0}s"
        binding.sliderMinInterval.addOnChangeListener { _, value, _ ->
            val v = value.toLong()
            preferencesManager.setMinSwipeInterval(v)
            binding.tvMinIntervalValue.text = "${v / 1000.0}s"
        }

        // 固定广告时长
        val adDuration = preferencesManager.getFixedAdDuration()
        binding.sliderAdDuration.value = (adDuration / 1000).toFloat()
        binding.tvAdDurationValue.text = "${adDuration / 1000}秒"
        binding.sliderAdDuration.addOnChangeListener { _, value, _ ->
            val v = (value * 1000).toLong()
            preferencesManager.setFixedAdDuration(v)
            binding.tvAdDurationValue.text = "${value.toInt()}秒"
        }
    }

    private fun setupSwitches() {
        // 悬浮窗开关
        binding.switchFloating.isChecked = preferencesManager.isFloatingWindowEnabled()
        binding.switchFloating.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setFloatingWindowEnabled(isChecked)
            FloatingWindowService.toggle(this,isChecked)
        }

        // 通知开关
        binding.switchNotification.isChecked = preferencesManager.isNotificationEnabled()
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationEnabled(isChecked)
            if (!isChecked){
                NotificationHelper(this).cancelAll()
            }
        }

        // 开机自启开关
        binding.switchAutoStart.isChecked = preferencesManager.isAutoStartEnabled()
        binding.switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setAutoStartEnabled(isChecked)
        }
    }

    private fun setupButtons() {
        binding.btnResetDefaults.setOnClickListener {
            resetToDefaults()
            Toast.makeText(this, "已恢复默认设置", Toast.LENGTH_SHORT).show()
            recreate()
        }
        binding.btnCloseExit.setOnClickListener {
           exitAll()
        }

    }

    private fun exitAll(){
        runCatching {
        FloatingWindowService.toggle(this,false)
        AutoSwipeService.instance?.stopSelf()
        val intent = Intent(this, KeepAliveForegroundService::class.java)
        stopService(intent)
        finish()

            // 启动你的Launcher Activity（比如MainActivity）
            val it = Intent(this, MainActivity::class.java)
            // 清除目标任务里的所有Activity，同时确保创建新任务或复用现有任务
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            it.putExtra("EXTRA_EXIT", "LOGOUT")
            startActivity(it)
            // 关闭当前Activity所属任务中的所有本应用Activity
            finishAffinity()
        }
    }

    private fun resetToDefaults() {
        preferencesManager.setSwipeDelay(AppConfig.Defaults.SWIPE_DELAY_MS)
        preferencesManager.setSwipeDuration(AppConfig.Defaults.SWIPE_DURATION_MS)
        preferencesManager.setMinSwipeInterval(AppConfig.Defaults.MIN_SWIPE_INTERVAL_MS)
        preferencesManager.setFixedAdDuration(AppConfig.Defaults.FIXED_AD_DURATION_MS)
        preferencesManager.setFloatingWindowEnabled(AppConfig.Defaults.FLOATING_WINDOW_ENABLED)
        preferencesManager.setNotificationEnabled(AppConfig.Defaults.NOTIFICATION_ENABLED)
        preferencesManager.setAutoStartEnabled(AppConfig.Defaults.AUTO_START_ENABLED)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
