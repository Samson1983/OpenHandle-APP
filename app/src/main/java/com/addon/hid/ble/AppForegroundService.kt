

package com.addon.hid.ble

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * AppForegroundService
 * 专用于启动前台通知的服务，提升存活率
 */
class AppForegroundService : Service() {

    // 前台服务绑定方式，这里不支持绑定，直接返回 null
    override fun onBind(intent: Intent?): IBinder? = null

    // 通知渠道 ID（Android 8.0+ 需要使用通知渠道）
    private val notificationChannelId by lazy {
        val id = "app_foreground_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            val channel = NotificationChannel(
                id,
                "前台通知服务",  // 用户在系统设置中看到的名称
                NotificationManager.IMPORTANCE_HIGH // 通知重要等级
            ).apply {
                setShowBadge(false)        // 不显示桌面角标
                enableVibration(false)     // 不震动
                enableLights(false)        // 不闪灯
            }
            // 注册通知渠道
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        id
    }

    // 创建通知对象（用于前台服务）
    private fun buildNotification() = NotificationCompat.Builder(this, notificationChannelId).apply {
        setContentTitle("前台服务运行中，请勿关闭") // 通知标题
        setSmallIcon(android.R.drawable.ic_input_get) // 状态栏图标
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏也显示通知
        // 可添加 setContentIntent() 指定点击动作
    }.build()

    // 悬浮窗管理器
    private lateinit var floatingWindowManager: FloatingWindowManager

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        // 启动前台服务，通知 ID 可随意设置（唯一即可）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1001, buildNotification())
        }
        
        // 初始化悬浮窗管理器
        floatingWindowManager = FloatingWindowManager(this)
        // 显示悬浮窗
        floatingWindowManager.showFloatingWindow()
    }

    // 服务启动时调用，可响应 intent 的 action
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { handleAction(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    // 处理不同的服务请求动作
    private fun handleAction(action: String) {
        when (action) {
            ACTION_PRINT_LAYOUT -> {
                // 你可以在此处理打印布局或其他业务
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 隐藏悬浮窗
        if (::floatingWindowManager.isInitialized) {
            floatingWindowManager.hideFloatingWindow()
        }
    }

    companion object {
        const val ACTION_PRINT_LAYOUT = "action_print_layout" // 打印布局指令常量
    }
}
