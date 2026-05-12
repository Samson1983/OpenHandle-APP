package net.hid.ble

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.addon.hid.ble.ScreenCaptureManager
import com.google.gson.Gson
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.ResponseBody
import com.yanzhenjie.andserver.util.MediaType
import kotlinx.coroutines.runBlocking
import net.hid.sdk.BleManager
import net.hid.util.ScreenInfo
import java.io.ByteArrayOutputStream
import java.util.Base64

@RestController
class HttpService {

    private val gson = Gson()

    data class ApiResponse(
        val code: Int,
        val msg: String
    )

    private fun success(): String {
        return gson.toJson(ApiResponse(200, "成功"))
    }

    private fun failure(): String {
        return gson.toJson(ApiResponse(400, "失败"))
    }

    /**
     * 连接设备
     * 示例 URL:
     * - http://192.168.0.115:9123/connect?mac=00:11:22:33:44:55 (指定MAC连接)
     * - http://192.168.0.115:9123/connect (遍历已配对设备查找UUID连接)
     */

    @GetMapping("/connect")
    fun connect(@RequestParam("mac", required = false) mac: String): String {
        val result = if (mac.isNullOrBlank()) {
            BleManager.connect()  // 不带参数时连接所有设备
        } else {
            BleManager.connect(mac)  // 带参数时连接指定设备
        }


        if (result) {
            return success()
        } else {
            return failure()
        }
    }


    /**
     * 状态检查
     * 示例 URL: http://192.168.0.115:9123/state
     */
    @GetMapping("/state")
    fun state(): String {
        val result = BleManager.state()
        if (result) {
            return success()
        } else {
            return failure()
        }
    }



    /**
     * 点击操作
     * 示例 URL: http://192.168.0.115:9123/click?x=100&y=500
     */
    @GetMapping("/click")
    fun click(@RequestParam("x") x: Double, @RequestParam("y") y: Double): String {
        val result = BleManager.click(x, y)
        if (result) {
            return success()
        } else {
            return failure()
        }
    }

    /**
     * 长按操作
     * 示例 URL: http://192.168.0.115:9123/press?x=100&y=200&duration=1000
     */
    @GetMapping("/press")
    fun press(
        @RequestParam("x") x: Double,
        @RequestParam("y") y: Double,
        @RequestParam("duration") duration: Long,
    ): String {
        val result = BleManager.press(x, y, duration)
        if (result) {
            return success()
        } else {
            return failure()
        }
    }


    /**
     * 滑动操作
     * 示例 URL: http://192.168.0.115:9123/swipe?x1=100&y1=100&x2=200&y2=200&duration=1000
     */
    @GetMapping("/swipe")
    fun swipe(
        @RequestParam("x1") x1: Double,
        @RequestParam("y1") y1: Double,
        @RequestParam("x2") x2: Double,
        @RequestParam("y2") y2: Double,
        @RequestParam("duration") duration: Long


    ): String {
        val result = BleManager.swipe(x1, y1, x2, y2,duration)
        if (result) {
            return success()
        } else {
            return failure()
        }
    }
    /**
     * 鼠标滑动带速度控制
     * 示例 URL: http://192.168.0.115:9123/swipe1?x1=100&y1=100&x2=300&y2=300&s=1.5
     */
    @GetMapping("/swipe1")
    fun swipe1(
        @RequestParam("x1") x1: Double,
        @RequestParam("y1") y1: Double,
        @RequestParam("x2") x2: Double,
        @RequestParam("y2") y2: Double,
        @RequestParam("s") s: Double
    ): String {
        return if (BleManager.swipe1(x1, y1, x2, y2, s, 1000)) success() else failure()
    }
    /**
     * 复制
     * 示例 URL: http://192.168.0.115:9123/copy
     */
    @GetMapping("/copy")
    fun copy(): String {
        val result = BleManager.copy()
        if (result) {
            return success()
        } else {
            return failure()
        }
    }

    /**
     * 粘贴
     * 示例 URL: http://192.168.0.115:9123/paste
     */
    @GetMapping("/paste")
    fun paste(): String {
        val result = BleManager.paste()
        if (result) {
            return success()
        } else {
            return failure()
        }
    }

    /**
     * 返回键
     * 示例 URL: http://192.168.0.115:9123/back
     */
    @GetMapping("/back")
    fun back(): String {
        val result = BleManager.back()
//        println("----------back");
//        /*滑动必须是小步、连续、低速（每次 X 轴偏移 - 2~-5，分 20~30 步），单次大偏移会被识别为 "鼠标移动" 而非 "滑动手势"；*/
//        val result = BleManager.swipe1(0.0, ScreenInfo.screenHeight()/2.0, (ScreenInfo.screenWidth()-(ScreenInfo.screenWidth()/3.0)), ScreenInfo.screenHeight()/2.0,20.0,1);

//        val result = true;
//        for(i in 0 until 10){
////            BleManager.swipe1(ScreenInfo.screenWidth()/2.0, ScreenInfo.screenHeight()*1.0+800, ScreenInfo.screenWidth()/2.0, (ScreenInfo.screenHeight()/3.0),30.0,3);
////            BleManager.swipe(ScreenInfo.screenWidth()/2.0, ScreenInfo.screenHeight()*1.0+800, ScreenInfo.screenWidth()/2.0, (ScreenInfo.screenHeight()/3.0),30);
//            BleManager.swipe1(0.0, ScreenInfo.screenHeight()/2.0, (ScreenInfo.screenWidth()-(ScreenInfo.screenWidth()/3.0)), ScreenInfo.screenHeight()/2.0,30.0,3);
//        }
////        BleManager.press(ScreenInfo.screenWidth()/2.0, ScreenInfo.screenHeight()/2.0,3000);
////        val result = BleManager.swipe(0.0, ScreenInfo.screenHeight()/2.0+10, (ScreenInfo.screenWidth()-(ScreenInfo.screenWidth()/3.0)), ScreenInfo.screenHeight()/2.0,500)
////        val result = BleManager.swipe1(0.0, ScreenInfo.screenHeight()/2.0, (ScreenInfo.screenWidth()-(ScreenInfo.screenWidth()/3.0)), ScreenInfo.screenHeight()/2.0,3.0,3000)

        if (result) {
            return success()
        } else {
            return failure()
        }
    }

    /**
     * ikeyboard - 自定义按钮:可实现home\back\最近任务等
     * 示例 URL: http://192.168.2.99:9123/ikeyboard?key1=0x87&key2=0xB0&duration=100
     * ---------组合建-----------
     * key1:
     *  0x87 = 触发这些安卓特殊键的功能前缀（Application 键）
     * key2:
     *  0xB0 = Home（主页）
     *  0xB1 = Back（返回）
     *  0xB2 = Menu（菜单）
     *  0xB3 = Recent Apps（最近任务 / 多任务）
     *---------单键-----------
     * key1:
     *  0x00 = 不触发特殊键的功能
     * key2:
     *  0xB0= 回车；KEY_RETURN
     *  0xB2= 退格键；KEY_BACKSPACE
     *  0xB1= Esc键；KEY_ESC
     *  0xB4= 空格键；KEY_SPACE_BAR
     *  0xD4= KEY_DELETE
     */
    @GetMapping("/ikeyboard")
    fun home(
        @RequestParam("key1") key1: String,
        @RequestParam("key2") key2: String,
        @RequestParam("duration") duration: String,
    ): String {
        println("----------ikeyboard - 自定义按钮");

        val result =  BleManager.ikeyboard(key1,key2,duration)
//
//        // 获取屏幕尺寸
//        val screenWidth = ScreenInfo.screenWidth()
//        val screenHeight = ScreenInfo.screenHeight()
//
//        // 从屏幕底部中央向上滑动到屏幕中央，模拟Home手势
//        val startX = screenWidth / 2.0
//        val startY = screenHeight - 5.0  // 从屏幕底部边缘5像素处开始
//        val endX = screenWidth / 2.0
//        val endY = screenHeight / 2.0  // 到屏幕中央结束
//
//        // 使用快速滑动模拟Home手势
//        val result = BleManager.homeSwipe(startX, startY, endX, endY)

        if (result) {
            return success()
        } else {
            return failure()
        }
    }


    /**
     * Home键
     * 示例 URL: http://192.168.0.115:9123/home
     */
    @GetMapping("/home")
    fun home(): String {
        val result = BleManager.home()
        if (result) {
            return success()
        } else {
            return failure()
        }
    }


    /**
     * 回车键
     * 示例 URL: http://192.168.0.115:9123/enter
     */
    @GetMapping("/enter")
    fun enter(): String {
        val result = BleManager.enter()
        if (result) {
            return success()
        } else {
            return failure()
        }
    }

    /**
     * 任务键 - 实现自然曲线从底部向上滑动
     * 示例 URL: http://192.168.0.115:9123/recents
     */
    @GetMapping("/recents")
    fun recents(): String {
        println("----------recents - 红米Turbo3专用滑动");

        val result =  BleManager.recents()
        return success()
//        // 获取屏幕尺寸
//        val screenWidth = ScreenInfo.screenWidth()
//        val screenHeight = ScreenInfo.screenHeight()
//
//        // MIUI特殊方法：从屏幕底部中央稍微偏右的位置开始，快速向上滑动
//        // 这是MIUI系统识别最近任务手势的关键位置
//        val startX = screenWidth * 0.55  // 稍微偏右（55%位置）
//        val startY = screenHeight - 3.0  // 从屏幕底部边缘3像素处开始
//        val endX = screenWidth * 0.45   // 结束时稍微偏左（45%位置）
//        val endY = screenHeight * 0.15  // 到屏幕15%位置结束
//
//        // 使用MIUI专用的快速滑动方法
//        val result = BleManager.miuiRecentSwipe(startX, startY, endX, endY)
//        return success()



//        if (result) {
//            return success()
//        } else {
//            // 如果MIUI专用方法失败，尝试备用方法
//            println("MIUI专用方法失败，尝试备用方法");
//            Thread.sleep(500)
//
//            // 备用方法：从屏幕底部左侧开始，向右上角滑动
//            val backupStartX = screenWidth * 0.2
//            val backupStartY = screenHeight - 3.0
//            val backupEndX = screenWidth * 0.8
//            val backupEndY = screenHeight * 0.1
//
//            val backupResult = BleManager.miuiRecentSwipe(backupStartX, backupStartY, backupEndX, backupEndY)
//
//            if (backupResult) {
//                return success()
//            } else {
//                // 第三种方法：尝试从屏幕底部左侧向上滑动（MIUI有时需要从特定位置开始）
//                println("备用方法失败，尝试左侧滑动方法");
//                Thread.sleep(500)
//
//                val leftStartX = screenWidth * 0.15
//                val leftStartY = screenHeight - 3.0
//                val leftEndX = screenWidth * 0.15
//                val leftEndY = screenHeight * 0.2
//
//                val leftResult = BleManager.miuiRecentSwipe(leftStartX, leftStartY, leftEndX, leftEndY)
//
//                if (leftResult) {
//                    return success()
//                } else {
//                    // 最后尝试：使用内置的recent命令
//                    println("所有滑动方法失败，尝试内置recent命令");
//                    Thread.sleep(500)
//                    val builtinResult = BleManager.recents()
//
//                    if (builtinResult) {
//                        return success()
//                    } else {
//                        return failure()
//                    }
//                }
//            }
//        }
    }

    /**
     * 执行自然曲线滑动
     * 使用三次贝塞尔曲线实现从底部到顶部的自然滑动效果
     */
    private fun executeNaturalCurveSwipe(
        startX: Double, startY: Double,
        endX: Double, endY: Double,
        controlX1: Double, controlY1: Double,
        controlX2: Double, controlY2: Double
    ): Boolean {
        try {
            // 计算总滑动距离
            val distance = Math.sqrt(Math.pow(endX - startX, 2.0) + Math.pow(endY - startY, 2.0))

            // 根据距离动态调整步数和延迟
            val steps = (distance / 10).toInt().coerceIn(30, 80)  // 增加步数，使滑动更平滑
            val stepDelay = 1L  // 增加每步延迟，使滑动更慢，更容易触发最近任务

            // 使用BleManager的executeSwipeRecents方法执行整个贝塞尔曲线滑动
            // 传递起点、终点和控制点，让BleManager处理整个滑动过程
            return BleManager.executeBezierCurveSwipe(startX, startY, endX, endY, controlX1, controlY1, controlX2, controlY2, steps.toDouble(), stepDelay)

        } catch (e: Exception) {
            println("自然曲线滑动失败: ${e.message}")
            return false
        }
    }

    /**
     * 获取手机系统信息
     * 示例 URL: http://192.168.0.115:9123/systeminfo
     */
    @GetMapping("/systeminfo")
    fun systemInfo(): String {
        try {
            val context = BleManager.getContext()
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            
            // 安全获取设备序列号，避免权限问题
            val serialNumber = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Build.getSerial()
                } else {
                    @Suppress("DEPRECATION")
                    Build.SERIAL
                }
            } catch (e: Exception) {
                "unknown"
            }
            
            // 获取更友好的设备名称
            val deviceName = try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter?.name ?: "${Build.MANUFACTURER} ${Build.MODEL}"
            } catch (e: Exception) {
                "${Build.MANUFACTURER} ${Build.MODEL}"
            }
            
            val systemInfo = mapOf(
                "system" to "Android",
                "version" to Build.VERSION.RELEASE,
                "sdk" to Build.VERSION.SDK_INT,
                "manufacturer" to Build.MANUFACTURER,
                "model" to Build.MODEL,
                "deviceName" to deviceName,
                "device" to Build.DEVICE,
                "product" to Build.PRODUCT,
                "brand" to Build.BRAND,
                "hardware" to Build.HARDWARE,
                "serial" to serialNumber,
                "board" to Build.BOARD,
                "bootloader" to Build.BOOTLOADER,
                "display" to Build.DISPLAY,
                "fingerprint" to Build.FINGERPRINT,
                "host" to Build.HOST,
                "id" to Build.ID,
                "tags" to Build.TAGS,
                "type" to Build.TYPE,
                "user" to Build.USER,
                "screenWidth" to ScreenInfo.getScreenWidth(),
                "screenHeight" to ScreenInfo.getScreenHeight(),
                "screenDensity" to ScreenInfo.screenDensity(),
                "isLandscape" to ScreenInfo.isLandscape(),
                "appVersion" to packageInfo.versionName,
                "appVersionCode" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
            )
            
            return gson.toJson(mapOf("code" to 200, "msg" to "成功", "data" to systemInfo))
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "获取系统信息失败: ${e.message}"))
        }
    }


    /**
     * 获取应用列表
     * 示例 URL: http://192.168.0.115:9123/applist
     */
    @GetMapping("/applist")
    fun appList(): String {
        try {
            val context = BleManager.getContext()
            val packageManager = context.packageManager

            // 检查权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 需要 QUERY_ALL_PACKAGES 权限
                if (context.checkSelfPermission(android.Manifest.permission.QUERY_ALL_PACKAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return gson.toJson(mapOf("code" to 403, "msg" to "缺少查询应用列表权限，请在设置中授予应用权限"))
                }
            }

            // 获取所有已安装的应用
            val installedPackages = packageManager.getInstalledPackages(0)

            val appList = installedPackages.map { packageInfo ->
                val packageName = packageInfo.packageName
//                mapOf(
//                    "package" to packageName
//                )
                packageName
            }

            return gson.toJson(mapOf("code" to 200, "msg" to "成功", "data" to appList))
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "获取应用列表失败: ${e.message}"))
        }
    }


    /**
     * 获取应用列表
     * 示例 URL: http://192.168.0.115:9123/applist?search=微信 (搜索应用名称或包名包含"微信"的应用)
     * 示例 URL: http://192.168.0.115:9123/applist (默认只获取非系统应用)
     * 示例 URL: http://192.168.0.115:9123/applist?system=true (只获取系统应用)
     * 示例 URL: http://192.168.0.115:9123/applist?system=all (获取全部应用)
     * 示例 URL: http://192.168.0.115:9123/applist?system=false&search=QQ (搜索非系统应用中名称或包名包含"QQ"的应用)
     */
    @GetMapping("/applist_v1")
    fun appList(
        @RequestParam("system", required = false) system: String?, 
        @RequestParam("search", required = false) search: String?
    ): String {
        try {
            val context = BleManager.getContext()
            val packageManager = context.packageManager
            
            // 检查权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ 需要 QUERY_ALL_PACKAGES 权限
                if (context.checkSelfPermission(android.Manifest.permission.QUERY_ALL_PACKAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return gson.toJson(mapOf("code" to 403, "msg" to "缺少查询应用列表权限，请在设置中授予应用权限"))
                }
            }
            
            // 获取所有已安装的应用
            val installedPackages = packageManager.getInstalledPackages(0)
            
            val appList = installedPackages.map { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo
                val appName = try {
                    packageManager.getApplicationLabel(applicationInfo).toString()
                } catch (e: Exception) {
                    packageInfo.packageName
                }
                val packageName = packageInfo.packageName
                val versionName = packageInfo.versionName ?: "未知"
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
                
                // 更准确的系统应用判断
                val isSystemApp = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||
                                (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                mapOf(
                    "name" to appName,
                    "packageName" to packageName,
                    // "versionName" to versionName,
                    "versionCode" to versionCode,
                    "isSystemApp" to isSystemApp
                )
            }.filter { app ->
                // 系统应用过滤
                val systemFilter = when (system) {
                    "true" -> app["isSystemApp"] == true
                    "all" -> true
                    else -> app["isSystemApp"] == false // 默认只获取非系统应用
                }
                
                // 搜索过滤
                val searchFilter = search?.let {
                    val appName = app["name"] as? String ?: ""
                    val packageName = app["packageName"] as? String ?: ""
                    appName.contains(search, ignoreCase = true) || packageName.contains(search, ignoreCase = true)
                } ?: true
                
                systemFilter && searchFilter
            }
            
            return gson.toJson(mapOf("code" to 200, "msg" to "成功", "data" to appList))
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "获取应用列表失败: ${e.message}"))
        }
    }

    /**
     * 获取当前屏幕截图（返回json）
     * 示例 URL: http://192.168.0.115:9123/screenshot
     */
    @GetMapping("/screenshot")
    fun screenshot(): String {
        try {
            val context = BleManager.getContext()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)

            // 使用 ScreenCaptureManager 获取全屏幕截图
            val bitmap = runBlocking {
                ScreenCaptureManager.captureFullScreen()
            }

            // 将Bitmap转换为Base64字符串
            val byteArrayOutputStream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            // 使用Base64编码
            val base64String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(byteArray)
            } else {
                @Suppress("DEPRECATION")
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            }
            
            // 清理资源
            bitmap.recycle()
            byteArrayOutputStream.close()
            
            return gson.toJson(mapOf(
                "code" to 200,
                "msg" to "成功",
                "data" to mapOf(
                    "screenshot" to base64String,
                    "width" to displayMetrics.widthPixels,
                    "height" to displayMetrics.heightPixels,
                    "format" to "PNG"
                )
            ))
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "截图失败: ${e.message}"))
        }
    }

    /**
     * 获取当前屏幕截图（直接返回图片）
     * 示例 URL: http://192.168.0.115:9123/screenshot/image
     */
    @GetMapping("/screenshot/image")
    fun screenshotImage(): ResponseBody {
        try {
            val context = BleManager.getContext()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)

            // 使用 ScreenCaptureManager 获取全屏幕截图
            val bitmap = runBlocking {
                ScreenCaptureManager.captureFullScreen()
            }

            // 将Bitmap转换为字节数组
            val byteArrayOutputStream = ByteArrayOutputStream()

            // 使用JPEG格式和中等质量压缩，减小图片大小
//            bitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // 清理资源
            bitmap.recycle()
            byteArrayOutputStream.close()

            // 创建响应体并设置正确的Content-Type
            return object : ResponseBody {
                override fun contentType(): MediaType? = MediaType.valueOf("image/jpeg")
                override fun contentLength() = byteArray.size.toLong()
                override fun writeTo(output: java.io.OutputStream) {
                    output.write(byteArray)
                }
                override fun isRepeatable() = true
                override fun isChunked() = false
            }
        } catch (e: Exception) {
            // 如果出错，返回错误信息的JSON响应
            val errorJson = gson.toJson(mapOf("code" to 400, "msg" to "截图失败: ${e.message}"))
            return object : ResponseBody {
                override fun contentType(): MediaType? = MediaType.valueOf("application/json")
                override fun contentLength() = errorJson.toByteArray().size.toLong()
                override fun writeTo(output: java.io.OutputStream) {
                    output.write(errorJson.toByteArray())
                }
                override fun isRepeatable() = true
                override fun isChunked() = false
            }
        }
    }



    /**
     * 输入中文到手机
     * 示例 URL: http://192.168.0.115:9123/input/text?content=你好世界
     */
    @GetMapping("/input/text")
    fun inputText(@RequestParam("content") content: String): String {
        try {
            val context = BleManager.getContext()

            // 复制内容到剪贴板
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("input", content)
            clipboard.setPrimaryClip(clip)

//            Thread.sleep(100);
//

            // 粘贴内容
            val pasteResult = BleManager.paste()
            if (pasteResult) {
                return success()
            } else {
                return gson.toJson(mapOf("code" to 400, "msg" to "粘贴失败"))
            }
//            Thread.sleep(100);
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "输入失败: ${e.message}"))
        }
    }

    /**
     * 打开指定包名的应用
     * 示例 URL: http://192.168.0.115:9123/openapp?package=com.example.app
     */
    @GetMapping("/openapp")
    fun openApp(@RequestParam("package") packageName: String): String {
        try {
            val context = BleManager.getContext()
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return success()
            } else {
                return gson.toJson(mapOf("code" to 400, "msg" to "应用未安装: $packageName"))
            }
        } catch (e: Exception) {
            return gson.toJson(mapOf("code" to 400, "msg" to "打开应用失败: ${e.message}"))
        }
    }

}