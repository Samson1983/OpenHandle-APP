package net.hid.util


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build

object NetworkUtils {

    /**
     * 获取设备的局域网 IPv4 地址127.0.0.1
     * @param context 上下文
     * @return 当前设备的 IP 地址，如果获取失败则返回空字符串
     */
    @SuppressLint("MissingPermission")
    fun getWifiIP(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.let { manager ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查权限（Android 6.0+需要运行时权限）
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return null
                    }
                }

                val wifiInfo = manager.connectionInfo
                val ipAddress = wifiInfo.ipAddress

                // 更直观的IP地址转换方式
                (ipAddress and 0xFF).toString() + "." +
                        (ipAddress shr 8 and 0xFF) + "." +
                        (ipAddress shr 16 and 0xFF) + "." +
                        (ipAddress shr 24 and 0xFF)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}