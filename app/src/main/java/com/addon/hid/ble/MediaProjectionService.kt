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
 * MediaProjectionService
 * 专用于屏幕录制的前台服务
 */
class MediaProjectionService : Service() {

    // 前台服务绑定方式，这里不支持绑定，直接返回 null
    override fun onBind(intent: Intent?): IBinder? = null

    // 通知渠道 ID（Android 8.0+ 需要使用通知渠道）
    private val notificationChannelId by lazy {
        val id = "media_projection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道
            val channel = NotificationChannel(
                id,
                "屏幕录制服务",  // 用户在系统设置中看到的名称
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
        setContentTitle("屏幕录制服务运行中") // 通知标题
        setSmallIcon(android.R.drawable.ic_input_get) // 状态栏图标
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏也显示通知
    }.build()

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        // 启动前台服务，通知 ID 可随意设置（唯一即可）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1002, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1002, buildNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { 
            when (it.action) {
                ACTION_START -> {
                    val resultCode = it.getIntExtra("resultCode", 0)
                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra("resultData", Intent::class.java)
                    } else {
                        it.getParcelableExtra<Intent>("resultData")
                    }
                    if (resultCode != 0 && resultData != null) {
                        // 在这里创建 MediaProjection，因为服务已经是前台服务
                        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
                        try {
                            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
                            // 注册回调
                            mediaProjection?.registerCallback(object : android.media.projection.MediaProjection.Callback() {
                                override fun onStop() {
                                    super.onStop()
                                    stopSelf()
                                }
                            }, null)
                            // 保存 MediaProjection 实例到 ScreenCaptureManager
                            ScreenCaptureManager.setMediaProjection(mediaProjection)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                ACTION_STOP -> {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
    }
}