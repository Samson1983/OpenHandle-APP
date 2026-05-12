# openHandle-APP



[Readme in English](README_en.md)

<div align="center">
<img src=README.assets/openHandle-logo.png width="20%"/>
</div>
<p align="center">
    👋 加入我们的 <a href="resources/WECHAT.md" target="_blank">微信:hzfaic</a> 
</p>



## 项目介绍

OpenHandle-APP是一个控制器，提供HTTP接口、SDK接口，可供第三方ai大模型使用；

esp32c3的固件可以替换ADB(Android Debug Bridge)的能力来控制设备;

app通过截图为大模型提供屏幕感知。

Agent 即可自动解析意图、理解当前界面、规划下一步动作并完成整个流程。系统还内置敏感操作确认机制，并支持在登录或验证码场景下进行人工接管。

> ⚠️
> 本项目仅供研究和学习使用。严禁用于非法获取信息、干扰系统或任何违法活动。请仔细审阅 [使用条款](resources/privacy_policy.txt)。





## 设计原理图解

<img src="README.assets/openHand-by-openAutoGLM%E9%80%BB%E8%BE%91%E5%9B%BE.png" alt="openHand-by-openAutoGLM逻辑图" style="zoom: 80%;" />



## 验证：

1.手上有esp32c3硬件的，下载sdk，并烧录sdk或直接购买这个硬件；
2.手机安装apk（开源)；
3.直接在PC的浏览器访问http接口即可。**（注意：必须先调用“连接设备”接口，才能调其他接口）**



## 使用步骤：

1.首先，下载openHandle-By-Open-AutoGLM（开源）。
2.手上有esp32c3硬件的，下载sdk，并烧录sdk或直接购买这个硬件；
3.手机安装apk（开源)；
4.连接控制即可：演示；



### 源码及固件：


OpenHandle-By-Open-AutoGLM：https://github.com/Samson1983/OpenHandle-By-Open-AutoGLM

OpenHandle-APP：https://github.com/Samson1983/OpenHandle-APP

app及固件下载：https://github.com/Samson1983/OpenHandle-APP/releases





 ## 快速编释

### 1. 用IDE打开项目:如Android Studio

编释输出apk就好

![image-20260512174922065](README.assets/image-20260512174922065.png)



### Http接口(见HttpService.kt)

```kotlin
 
    /**
     * 连接设备
     * 示例 URL:
     * - http://192.168.0.115:9123/connect?mac=00:11:22:33:44:55 (指定MAC连接)
     * - http://192.168.0.115:9123/connect (遍历已配对设备查找UUID连接)
     */

     

    /**
     * 状态检查
     * 示例 URL: http://192.168.0.115:9123/state
     */
    

    /**
     * 点击操作
     * 示例 URL: http://192.168.0.115:9123/click?x=100&y=500
     */
     

    /**
     * 长按操作
     * 示例 URL: http://192.168.0.115:9123/press?x=100&y=200&duration=1000
     */
     

    /**
     * 滑动操作
     * 示例 URL: http://192.168.0.115:9123/swipe?x1=100&y1=100&x2=200&y2=200&duration=1000
     */
    
	
    /**
     * 鼠标滑动带速度控制
     * 示例 URL: http://192.168.0.115:9123/swipe1?x1=100&y1=100&x2=300&y2=300&s=1.5
     */
    
	
    /**
     * 复制
     * 示例 URL: http://192.168.0.115:9123/copy
     */
     

    /**
     * 粘贴
     * 示例 URL: http://192.168.0.115:9123/paste
     */ 

    /**
     * 返回键
     * 示例 URL: http://192.168.0.115:9123/back
     */
    @GetMapping("/back")
     

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
     


    /**
     * Home键
     * 示例 URL: http://192.168.0.115:9123/home
     */
     

    /**
     * 回车键
     * 示例 URL: http://192.168.0.115:9123/enter
     */
     

    /**
     * 任务键 - 实现自然曲线从底部向上滑动
     * 示例 URL: http://192.168.0.115:9123/recents
     */
     


    /**
     * 获取手机系统信息
     * 示例 URL: http://192.168.0.115:9123/systeminfo
     */
     


    /**
     * 获取应用列表
     * 示例 URL: http://192.168.0.115:9123/applist
     */
     

    /**
     * 获取当前屏幕截图（返回json）
     * 示例 URL: http://192.168.0.115:9123/screenshot
     */
    

    /**
     * 获取当前屏幕截图（直接返回图片）
     * 示例 URL: http://192.168.0.115:9123/screenshot/image
     */
     


    /**
     * 输入中文到手机
     * 示例 URL: http://192.168.0.115:9123/input/text?content=你好世界
     */
    

    /**
     * 打开指定包名的应用
     * 示例 URL: http://192.168.0.115:9123/openapp?package=com.example.app
     */

```





## 常见问题

我们列举了一些常见的问题，以及对应的解决方案：



### 截图失败(报错)

通过是截图权限关闭了；

### 截图失败(黑屏)

这通常意味着应用正在显示敏感页面(支付、密码、银行类应用)。Agent 会自动检测并请求人工接管。





 
