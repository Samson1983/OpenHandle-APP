package com.addon.hid.ble

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScreenCaptureManager private constructor() {
    companion object {
        private lateinit var instance: ScreenCaptureManager
        private lateinit var context: Context
        private var mediaProjection: MediaProjection? = null
        private var mediaProjectionManager: MediaProjectionManager? = null
        private var resultCode: Int = 0
        private lateinit var resultData: Intent
        private var virtualDisplay: VirtualDisplay? = null
        private var imageReader: ImageReader? = null
        private var handler: Handler? = null
        private var handlerThread: HandlerThread? = null
        
        // MediaProjection 回调
        private val mediaProjectionCallback = object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                // 当 MediaProjection 停止时，清理资源
                cleanup()
                
                // 停止媒体投影前台服务
                val serviceIntent = Intent(context, MediaProjectionService::class.java)
                serviceIntent.action = MediaProjectionService.ACTION_STOP
                context.stopService(serviceIntent)
            }
        }

        fun init(ctx: Context) {
            context = ctx
            instance = ScreenCaptureManager()
            mediaProjectionManager = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        }

        fun getInstance(): ScreenCaptureManager {
            return instance
        }

        fun setMediaProjectionInstance(code: Int, data: Intent) {
            resultCode = code
            resultData = data
            
            // 启动媒体投影前台服务，并传递必要的数据
            val serviceIntent = Intent(context, MediaProjectionService::class.java)
            serviceIntent.action = MediaProjectionService.ACTION_START
            serviceIntent.putExtra("resultCode", code)
            serviceIntent.putExtra("resultData", data)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
        
        // 从 MediaProjectionService 设置 MediaProjection 实例
        fun setMediaProjection(projection: android.media.projection.MediaProjection?) {
            mediaProjection = projection
            // 注册 MediaProjection 回调
            mediaProjection?.registerCallback(mediaProjectionCallback, null)
        }

        fun isMediaProjectionInitialized(): Boolean {
            return mediaProjection != null
        }

        suspend fun captureFullScreen(): Bitmap {
            return suspendCancellableCoroutine {continuation ->
                var isCompleted = false
                try {
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val displayMetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                    val width = displayMetrics.widthPixels
                    val height = displayMetrics.heightPixels
                    val density = displayMetrics.densityDpi

                    // 确保 MediaProjection 已初始化
                    if (mediaProjection == null) {
                        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
                    }

                    if (mediaProjection == null) {
                        continuation.resumeWithException(Exception("MediaProjection 未初始化，请先授予屏幕录制权限"))
                        return@suspendCancellableCoroutine
                    }

                    // 启动 HandlerThread 用于处理图像（如果尚未启动）
                    if (handlerThread == null || !handlerThread!!.isAlive) {
                        handlerThread = HandlerThread("ScreenCaptureThread")
                        handlerThread?.start()
                        handler = Handler(handlerThread?.looper!!)
                    }

                    // 清理之前的 ImageReader
                    cleanupImageReader()

                    // 创建新的 ImageReader
                    imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)

                    // 创建虚拟显示（如果尚未创建或已释放）
                    if (virtualDisplay == null) {
                        virtualDisplay = mediaProjection?.createVirtualDisplay(
                            "ScreenCapture",
                            width,
                            height,
                            density,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            imageReader?.surface,
                            null,
                            handler
                        )
                    } else {
                        // 如果虚拟显示已存在，更新其 surface
                        virtualDisplay?.surface = imageReader?.surface
                    }

                    // 监听 ImageReader 的新图像
                    imageReader?.setOnImageAvailableListener({ reader ->
                        try {
                            if (isCompleted) return@setOnImageAvailableListener
                            
                            val image: Image? = reader.acquireLatestImage()
                            if (image != null) {
                                val bitmap = imageToBitmap(image)
                                image.close()
                                isCompleted = true
                                // 只清理 ImageReader，不释放虚拟显示和 HandlerThread
                                cleanupImageReader()
                                continuation.resume(bitmap)
                            } else {
                                if (!isCompleted) {
                                    isCompleted = true
                                    cleanupImageReader()
                                    continuation.resumeWithException(Exception("获取图像失败"))
                                }
                            }
                        } catch (e: Exception) {
                            if (!isCompleted) {
                                isCompleted = true
                                cleanupImageReader()
                                continuation.resumeWithException(e)
                            }
                        }
                    }, handler)

                    // 延迟一点时间确保图像已捕获
                    handler?.postDelayed({ 
                        // 如果超时还没有获取到图像，就取消
                        if (!isCompleted && !continuation.isCompleted) {
                            isCompleted = true
                            cleanupImageReader()
                            continuation.resumeWithException(Exception("截图超时"))
                        }
                    }, 3000)

                } catch (e: Exception) {
                    if (!isCompleted) {
                        isCompleted = true
                        cleanupImageReader()
                        continuation.resumeWithException(e)
                    }
                }
            }
        }

        private fun imageToBitmap(image: Image): Bitmap {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                image.width,
                image.height
            )
        }

        private fun cleanup() {
            virtualDisplay?.release()
            imageReader?.close()
            handlerThread?.quitSafely()
            virtualDisplay = null
            imageReader = null
            handler = null
            handlerThread = null
        }

        private fun cleanupImageReader() {
            imageReader?.close()
            imageReader = null
        }
    }
}
