package io.ecosed.libecosed.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.farmerbb.taskbar.lib.Taskbar
import com.kongzue.dialogx.dialogs.PopTip
import io.ecosed.libecosed.BuildConfig
import io.ecosed.libecosed.EcosedFramework
import io.ecosed.libecosed.R
import io.ecosed.libecosed.plugin.LibEcosedPlugin
import io.ecosed.libecosed.utils.ChineseCaleUtils
import io.ecosed.libecosed.utils.EnvironmentUtils
import io.ecosed.plugin.PluginExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

internal class EcosedService : Service(), Shizuku.OnBinderReceivedListener,
    Shizuku.OnBinderDeadListener, Shizuku.OnRequestPermissionResultListener {

    private lateinit var mService: EcosedService
    private var isDebug: Boolean by mutableStateOf(value = false)
    private lateinit var mProductLogo: Drawable
    private lateinit var poem: ArrayList<String>

    override fun onCreate() {
        super.onCreate()
        mService = this@EcosedService



        isDebug = PluginExecutor.execMethodCall(
            service = mService,
            name = LibEcosedPlugin.channel,
            method = LibEcosedPlugin.isDebug,
            null
        ) as Boolean
        mProductLogo = PluginExecutor.execMethodCall(
            service = mService,
            name = LibEcosedPlugin.channel,
            method = LibEcosedPlugin.getProductLogo,
            null
        ) as Drawable

        poem = arrayListOf()
        poem.add("不向焦虑与抑郁投降，这个世界终会有我们存在的地方。")
        poem.add("把喜欢的一切留在身边，这便是努力的意义。")
        poem.add("治愈、温暖，这就是我们最终幸福的结局。")
        poem.add("我有一个梦，也许有一天，灿烂的阳光能照进黑暗森林。")
        poem.add("如果必须要失去，那么不如一开始就不曾拥有。")
        poem.add("我们的终点就是与幸福同在。")
        poem.add("孤独的人不会伤害别人，只会不断地伤害自己罢了。")
        poem.add("如果你能记住我的名字，如果你们都能记住我的名字，也许我或者我们，终有一天能自由地生存着。")
        poem.add("对于所有生命来说，不会死亡的绝望，是最可怕的审判。")
        poem.add("我不曾活着，又何必害怕死亡。")


        val notification = buildNotification()
        startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return object : EcosedFramework.Stub() {
            override fun getFrameworkVersion(): String = frameworkVersion()
            override fun getShizukuVersion(): String = shizukuVersion()
            override fun getChineseCale(): String = chineseCale()
            override fun getOnePoem(): String = onePoem()
            override fun isWatch(): Boolean = watch()
            override fun isUseDynamicColors(): Boolean = dynamicColors()
            override fun openDesktopSettings() = taskbarSettings()
            override fun openEcosedSettings() = ecosedSettings()
        }
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBinderReceived() {

    }

    override fun onBinderDead() {

    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {

    }

    private val mUserServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(AppUtils.getAppPackageName(), UserService().javaClass.name)
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(AppUtils.getAppVersionCode())

    private fun check() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            if (e.javaClass == IllegalStateException().javaClass) {
                PopTip.show("Shizuku未激活")
            }
        }
    }

    private fun frameworkVersion(): String {
        return AppUtils.getAppVersionName()
    }

    private fun shizukuVersion(): String {
        return try {
            "Shizuku ${Shizuku.getVersion()}"
        } catch (e: Exception) {
            Log.getStackTraceString(e)
        }
    }

    private fun chineseCale(): String {
        return ChineseCaleUtils.getChineseCale()
    }

    private fun onePoem(): String {
        return poem[(poem.indices).random()]
    }

    private fun watch(): Boolean {
        return EnvironmentUtils.isWatch(this)
    }

    private fun dynamicColors(): Boolean {
        return true
    }

    private fun taskbarSettings() {
        CoroutineScope(
            context = Dispatchers.Main
        ).launch {
            Taskbar.openSettings(
                this@EcosedService,
                "任务栏设置",
                R.style.Theme_LibEcosed_Taskbar
            )
        }
    }
    private fun ecosedSettings() {
        CoroutineScope(
            context = Dispatchers.Main
        ).launch {

        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notification = NotificationCompat.Builder(
            this@EcosedService,
            LibEcosedPlugin.notificationChannel
        )
            .setContentTitle(AppUtils.getAppName())
            .setContentText("服务正在运行")
            .setSmallIcon(R.drawable.baseline_keyboard_command_key_24)
            .build()

        notification.flags = Notification.FLAG_ONGOING_EVENT

        return notification
    }

    companion object {
        const val notificationId = 1
    }
}