package com.addon.hid.ble

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.runBlocking

class FloatingWindowManager(private val service: Service) {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    
    // 视图组件
    private lateinit var toolbarLayout: LinearLayout
    private lateinit var btnToggle: ImageButton
    
    // 状态标志
    private var isExpanded = false
    private var isDragging = false
    private var isAttachedToEdge = true // 默认贴边
    
    // 记录手指按下时的位置
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    
    // 屏幕尺寸
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    
    fun showFloatingWindow() {
        // 检查是否有悬浮窗权限
        if (!checkOverlayPermission()) {
            // 没有权限，跳转到设置页面
            requestOverlayPermission()
            return
        }
        
        // 获取WindowManager
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // 获取屏幕尺寸
        val displayMetrics = android.util.DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        
        // 初始化布局参数 - 默认贴右边
        params = WindowManager.LayoutParams().apply {
            // 设置窗口类型
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            // 设置窗口属性
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            
            // 设置窗口位置和大小
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            // 默认贴右边中间位置
            x = screenWidth - 60
            y = screenHeight / 2 - 100
        }
        
        // 加载悬浮窗布局
        val inflater = service.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window_collapsible, null)
        
        // 初始化视图组件
        toolbarLayout = floatingView.findViewById(R.id.toolbar_layout)
        btnToggle = floatingView.findViewById(R.id.btn_toggle)
        
        // 初始化按钮点击事件
        initButtons()
        
        // 设置小图标的触摸事件，实现拖动和点击功能
        setupToggleButtonTouchListener()
        
        // 添加悬浮窗到窗口管理器
        try {
            windowManager.addView(floatingView, params)
            // 默认收缩状态
            collapseToolbar()
        } catch (e: Exception) {
            e.printStackTrace()
            // 显示错误信息
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(service, "无法显示悬浮窗: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 设置小图标的触摸事件监听器
     * 实现拖动和点击功能
     */
    private fun setupToggleButtonTouchListener() {
        btnToggle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // 如果移动距离超过10像素，认为是拖动
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        isDragging = true
                        isAttachedToEdge = false
                    }
                    
                    if (isDragging) {
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()
                        
                        // 限制在屏幕范围内
                        params.x = params.x.coerceIn(0, screenWidth - 100)
                        params.y = params.y.coerceIn(0, screenHeight - 100)
                        
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // 点击事件 - 切换展开/收缩状态
                        toggleToolbar()
                    } else {
                        // 拖动结束，自动贴边
                        attachToEdge()
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * 自动贴边
     */
    private fun attachToEdge() {
        val centerX = params.x + 20
        val targetX = if (centerX < screenWidth / 2) {
            // 贴左边
            0
        } else {
            // 贴右边
            screenWidth - 60
        }
        
        // 使用动画平滑移动到边缘
        val animator = ValueAnimator.ofInt(params.x, targetX)
        animator.duration = 200
        animator.addUpdateListener { animation ->
            params.x = animation.animatedValue as Int
            windowManager.updateViewLayout(floatingView, params)
        }
        animator.start()
        
        isAttachedToEdge = true
    }
    
    /**
     * 切换工具栏展开/收缩状态
     */
    private fun toggleToolbar() {
        if (isExpanded) {
            collapseToolbar()
        } else {
            expandToolbar()
        }
    }
    
    /**
     * 展开工具栏
     */
    private fun expandToolbar() {
        // 先隐藏小图标
        btnToggle.visibility = View.GONE
        // 再显示工具栏
        toolbarLayout.visibility = View.VISIBLE
     
        isExpanded = true
        
        // 展开时调整位置，确保工具栏完全显示在屏幕内
        adjustPositionForExpansion()
    }
    
    /**
     * 收缩工具栏
     */
    private fun collapseToolbar() {
        toolbarLayout.visibility = View.GONE
        btnToggle.visibility = View.VISIBLE
        isExpanded = false
    }
    
    /**
     * 展开时调整位置，确保工具栏完全显示在屏幕内
     */
    private fun adjustPositionForExpansion() {
        // 获取工具栏宽度（估算）
        val toolbarWidth = 400 // 6个按钮 * 48dp + padding
        
        // 如果贴右边，需要向左移动以显示完整工具栏
        if (params.x > screenWidth / 2) {
            val targetX = screenWidth - toolbarWidth
            if (targetX < params.x) {
                val animator = ValueAnimator.ofInt(params.x, targetX.coerceAtLeast(0))
                animator.duration = 200
                animator.addUpdateListener { animation ->
                    params.x = animation.animatedValue as Int
                    windowManager.updateViewLayout(floatingView, params)
                }
                animator.start()
            }
        }
    }
    
    /**
     * 检查是否有悬浮窗权限
     */
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(service)
        } else {
            true
        }
    }
    
    /**
     * 请求悬浮窗权限
     */
    private fun requestOverlayPermission() {
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = android.net.Uri.parse("package:" + service.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        service.startActivity(intent)
        
        // 显示提示信息
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(service, "请授予悬浮窗权限以显示控制按钮", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun initButtons() {
        // 切换按钮（小图标）的点击事件在 setOnTouchListener 中处理
        
        // 为工具栏添加点击事件，点击工具栏空白处收缩
        toolbarLayout.setOnClickListener {
            collapseToolbar()
        }
        
        // 截图按钮
        floatingView.findViewById<ImageButton>(R.id.btn_screenshot).setOnClickListener {
            // 截图功能
            try {
                // 检查是否有屏幕录制权限
                if (ScreenCaptureManager.isMediaProjectionInitialized()) {
                    // 执行截图操作
                    Toast.makeText(service, "正在截图...", Toast.LENGTH_SHORT).show()
                    
                    // 在后台线程中执行截图
                    Thread {
                        try {
                            val bitmap = runBlocking {
                                ScreenCaptureManager.captureFullScreen()
                            }
                            
                            // 截图成功
                            Toast.makeText(service, "截图成功", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // 截图失败，跳转到MainActivity重新请求权限
                            val intent = Intent(service, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            service.startActivity(intent)
                        }
                    }.start()
                } else {
                    // 没有权限，跳转到MainActivity请求权限
                    val intent = Intent(service, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    service.startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 跳转到MainActivity重新请求权限
                val intent = Intent(service, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                service.startActivity(intent)
            }
        }
        
        // 主页按钮
        floatingView.findViewById<ImageButton>(R.id.btn_home).setOnClickListener {

            Toast.makeText(service, "功能未实现1", Toast.LENGTH_SHORT).show()
//            // 模拟Home键
//            service.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }
        
        // 菜单按钮
        floatingView.findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            // 模拟菜单键
            // 可以通过Intent发送广播或使用其他方式实现
            Toast.makeText(service, "功能未实现2", Toast.LENGTH_SHORT).show()
        }
        
        // 设置按钮
        floatingView.findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            // 打开设置页面
            val intent = Intent(service, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            service.startActivity(intent)
        }
        
        // 关闭按钮 - 退出应用
        floatingView.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            // 退出应用
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }
        
        // 收缩按钮
        floatingView.findViewById<ImageButton>(R.id.btn_collapse).setOnClickListener {
            //隐藏工具栏
            collapseToolbar()
            
            // 贴边
            attachToEdge()

           // 隐藏应用：将应用最小化到后台
           val intent = Intent(Intent.ACTION_MAIN)
           intent.addCategory(Intent.CATEGORY_HOME)
           intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
           service.startActivity(intent)
        }
    }
    
    fun hideFloatingWindow() {
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun isFloatingWindowShowing(): Boolean {
        return ::floatingView.isInitialized && floatingView.parent != null
    }
}
