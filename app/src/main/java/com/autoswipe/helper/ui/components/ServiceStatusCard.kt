package com.autoswipe.helper.ui.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.autoswipe.helper.R

/**
 * 服务状态卡片组件
 *
 * 自定义组合 View，用于展示无障碍服务的运行状态
 * 可直接在 XML 中使用，或代码中动态创建
 */
class ServiceStatusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val statusText: TextView
    private val statusIndicator: View

    init {
        LayoutInflater.from(context).inflate(R.layout.component_service_status, this, true)
        statusText = findViewById(R.id.tv_status)
        statusIndicator = findViewById(R.id.status_dot)
    }

    /**
     * 设置服务状态
     * @param status 状态文本
     * @param isRunning 是否运行中
     */
    fun setStatus(status: String, isRunning: Boolean) {
        statusText.text = status
        statusIndicator.setBackgroundColor(
            if (isRunning) Color.parseColor("#4CAF50") else Color.parseColor("#BDBDBD")
        )
    }
}
