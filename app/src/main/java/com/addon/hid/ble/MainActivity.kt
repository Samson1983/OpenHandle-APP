package com.addon.hid.ble

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import net.hid.sdk.BleManager

import net.hid.util.NetworkUtils.getWifiIP
import net.hid.util.ScreenInfo
import java.util.concurrent.TimeUnit

const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1
const val REQUEST_SCREEN_CAPTURE = 2
const val REQUEST_FLOATING_WINDOW = 3




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 初始化 USB 管理模块
        BleManager.init(this)
        // 初始化屏幕信息（如分辨率、密度等）
        ScreenInfo.init(this)
        // 启动 HTTP 服务（例如用于远程控制）
        startServer(this,9123)

        // 初始化屏幕捕获管理器
        ScreenCaptureManager.init(this)


        // 延迟 1 秒后启动前台服务（用于提高应用存活率）
        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                println("Android 8.0 及以上使用 startForegroundService 启动前台服务")
                // Android 8.0 及以上使用 startForegroundService 启动前台服务
                startForegroundService(Intent(this, AppForegroundService::class.java))
            } else {
                println("低版本使用普通方式启动服务")
                // 低版本使用普通方式启动服务
                startService(Intent(this, AppForegroundService::class.java))
            }
        }, 1000) // 延迟 1 秒执行


        //初始化UI组件
        initViews()

    }

    private fun requestScreenCapturePermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_SCREEN_CAPTURE)
    }

    // 延迟初始化本地服务器地址（将在initViews中动态获取）
    private lateinit var localServerUrl: String

    private fun initViews() {

        localServerUrl = "http://${getWifiIP(this)}:9123" // 替换为实际地址
        // 设置本地服务器地址显示
        val tvLocalServer = findViewById<TextView>(R.id.tv_local_server)
        tvLocalServer.text = "🌐 本地服务器: $localServerUrl"

        // 设置点击事件
        tvLocalServer.setOnClickListener {
            openUrlInBrowser(localServerUrl, "无法打开本地服务器链接")
        }

        // 设置官网链接点击事件（如果布局中有）
        findViewById<TextView>(R.id.tv_website)?.setOnClickListener {
            openUrlInBrowser("http://jsdevhub.com", "无法打开官网")
        }
        
        // 设置启用屏幕截图按钮点击事件
        val btnEnableScreenshot = findViewById<android.widget.Button>(R.id.btn_enable_screenshot)
        btnEnableScreenshot.setOnClickListener {
            requestScreenCapturePermission()
        }
        
//        // 设置悬浮窗控制按钮点击事件
//        val btnFloatingWindow = findViewById<android.widget.Button>(R.id.btn_floating_window)
//        btnFloatingWindow.setOnClickListener {
//            if (checkFloatingWindowPermission()) {
//                startForegroundServiceCompat()
//                Toast.makeText(this, "悬浮窗已显示", Toast.LENGTH_SHORT).show()
//            } else {
//                requestFloatingWindowPermission()
//            }
//        }
    }
    
    // 检查悬浮窗权限
    private fun checkFloatingWindowPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    // 请求悬浮窗权限
    private fun requestFloatingWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_FLOATING_WINDOW)
        }
    }

    private fun openUrlInBrowser(url: String, errorMessage: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startForegroundServiceCompat() {
        val serviceIntent = Intent(this, AppForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 用户授予权限后继续操作
                println("用户同意")
            } else {
                // 用户拒绝权限，您可以提示用户权限未被授予，无法继续操作
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCREEN_CAPTURE -> {
                val btnEnableScreenshot = findViewById<android.widget.Button>(R.id.btn_enable_screenshot)
                val tvScreenshotStatus = findViewById<TextView>(R.id.tv_screenshot_status)
                
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        // 保存resultCode和data，以便后续创建MediaProjection实例
                        ScreenCaptureManager.setMediaProjectionInstance(resultCode, data)
                        
//                        // 更新UI状态
//                        btnEnableScreenshot.text = "屏幕截图功能已启用"
//                        btnEnableScreenshot.isEnabled = false
//                        btnEnableScreenshot.setBackgroundColor(0xFF4CAF50.toInt())
//                        tvScreenshotStatus.text = "屏幕截图功能已启用，可以截取整个屏幕（包括悬浮窗）"
//                        tvScreenshotStatus.setTextColor(0xFF4CAF50.toInt())
                        
                        Toast.makeText(this, "屏幕截图功能已启用", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 更新UI状态
                        btnEnableScreenshot.text = "重新启用屏幕截图功能"
                        tvScreenshotStatus.text = "屏幕录制权限设置失败: ${e.message}"
                        tvScreenshotStatus.setTextColor(0xFFFF0000.toInt())
                        
                        Toast.makeText(this, "屏幕录制权限设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // 更新UI状态
                    btnEnableScreenshot.text = "重新启用屏幕截图功能"
                    tvScreenshotStatus.text = "屏幕录制权限被拒绝，截图功能将无法使用"
                    tvScreenshotStatus.setTextColor(0xFFFF0000.toInt())
                    
                    Toast.makeText(this, "屏幕录制权限被拒绝，截图功能将无法使用", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_FLOATING_WINDOW -> {
                if (checkFloatingWindowPermission()) {
                    startForegroundServiceCompat()
                    Toast.makeText(this, "悬浮窗权限已授予，悬浮窗已显示", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "悬浮窗权限被拒绝，无法显示悬浮窗", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

/**
 * 启动一个本地 HTTP 服务器
 *
 * @param context 上下文对象，用于获取服务器绑定的 IP 地址
 */
fun startServer(context: Context,port: Int) {
    // 创建并配置 AndServer 服务器实例
    val server = AndServer.webServer(context)
        .port(port) // 设置服务器监听的端口
        .timeout(10, TimeUnit.SECONDS) // 设置请求超时时间
        .listener(object : Server.ServerListener {

            // 服务器启动成功后回调此方法
            override fun onStarted() {
                // 在主线程中显示绑定的服务器地址
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    // 使用 Toast 提示当前服务器绑定的 IP 地址
                    Toast.makeText(
                        context,
                        "服务器绑定地址: http://${getWifiIP(context)}:9123",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // 打印服务器地址，方便调试
                println("服务器绑定地址: http://${getWifiIP(context)}:9123")
            }

            // 服务器停止时回调此方法
            override fun onStopped() {
                // 可在此处理服务器停止后的逻辑
            }

            // 服务器异常时回调此方法
            override fun onException(e: Exception) {
                // 打印异常堆栈
                e.printStackTrace()
            }
        })
        .build()

    // 启动服务器
    server?.startup()
}