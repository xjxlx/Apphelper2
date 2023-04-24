package com.android.apphelper2.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationUtil(private val context: Context) {

    private val mManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId: String = context.packageName // 需要保持唯一
    private val mNotificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)// notification builder
    private var mCustomListener: CustomViewCallBackListener? = null
    private var mLoopListener: LoopCallBackListener? = null
    private var mScope = CoroutineScope(Dispatchers.IO)
    private val notificationId = 100
    private var paddingRequestCode = 100

    var notificationAutoCancel: Boolean = true // 设置是否点击取消，true:自动取消，false：侧滑不会删除
    var paddingActivity: Intent? = null // 点击notification 跳转的页面
    var contentTitle: String = "" // title
    var contentContent: String = ""// content
    var smallIcon: Int = 0 // small icon

    var isNotificationSound: Boolean = true // 是否有通知声音、灯光、震动
    var notificationNumber: Int = 0 // 是否显示通知的数量
    var notificationLevel: Int = NotificationCompat.PRIORITY_DEFAULT // 消息的等级
    var notificationWhen = -1L // 通知的时间戳
    var notificationRemoteLayout: Int = 0 // 自定义布局id

    var channelName: String = context.packageName // 渠道名字
    var channelDescription: String = context.packageName + " 的描述" // 渠道的描述

    /**
     *  NotificationManager # IMPORTANCE_NONE 关闭通知
     *                   IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
     *                   IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
     *                   IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
     *                   IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
     */
    var channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT // 渠道的优先级

    var notification: Notification? = null

    class Builder {
        fun build(context: Context): NotificationUtil {
            return NotificationUtil(context)
        }
    }

    init {
        createNotification()
    }

    private fun createNotification() {
        if (smallIcon == 0) {
            LogUtil.e("be must set smallIcon !")
            return
        }
        mNotificationBuilder.setSmallIcon(smallIcon) // icon
        mNotificationBuilder.setContentTitle(contentTitle) // title
        mNotificationBuilder.setContentText(contentContent) // content

        mNotificationBuilder.priority = notificationLevel // 设置优先级
        if (notificationWhen != -1L) {
            mNotificationBuilder.setWhen(notificationWhen) // 消息时间戳
        }
        mNotificationBuilder.setAutoCancel(notificationAutoCancel) // 自动取消

        if (isNotificationSound) { // 是否设置声音、震动、灯光
            mNotificationBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
        }

        if (notificationNumber > 0) {  // 消息的数量
            mNotificationBuilder.setNumber(notificationNumber)
        }

        paddingActivity?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pendingIntent = PendingIntent.getActivity(context, paddingRequestCode, it, PendingIntent.FLAG_UPDATE_CURRENT)
            mNotificationBuilder.setContentIntent(pendingIntent)  // 跳转的activity
            mNotificationBuilder.setFullScreenIntent(pendingIntent, true)// 横幅通知的跳转
        }

        // 锁屏可见
        mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (notificationRemoteLayout != 0) {
            val remoteViews = RemoteViews(context.packageName, notificationRemoteLayout)
            mNotificationBuilder.setCustomContentView(remoteViews)
            mCustomListener?.onCallBack(remoteViews)
        }

        // 8.0 以后需要使用渠道信息
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, channelName, channelImportance)
            mChannel.description = channelDescription // 渠道描述
            if (isNotificationSound) {
                mChannel.enableLights(true) // 设置通知出现时的闪灯（如果 android 设备支持的话）
                mChannel.lightColor = Color.RED
                mChannel.enableVibration(true)// 设置通知出现时的震动（如果 android 设备支持的话）
                mChannel.setShowBadge(true) //显示logo

                mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT)
            }
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC // 锁屏可见

            mManager.createNotificationChannel(mChannel) // 通知Manager去创建渠道
            mNotificationBuilder.setChannelId(channelId) // 设置渠道id
        }

        // create notification
        val build = mNotificationBuilder.build()
        notification = build
    }

    fun startForeground(service: Service, loop: Boolean = false, interval: Long = 0) {
        notification?.let { notification ->
            if (loop) {
                mScope.launch {
                    repeat(Int.MAX_VALUE) {
                        service.startForeground(notificationId, notification)
                        mLoopListener?.onLoop()
                        delay(interval)
                    }
                }
            } else {
                service.startForeground(notificationId, notification)
                mLoopListener?.onLoop()
            }
        }
    }

    fun sendNotification(loop: Boolean = false, interval: Long = 0) {
        notification?.let { notification ->
            if (loop) {
                mScope.launch {
                    repeat(Int.MAX_VALUE) {
                        mManager.notify(notificationId, notification)
                        mLoopListener?.onLoop()
                        delay(interval)
                    }
                }
            } else {
                mManager.notify(notificationId, notification)
                mLoopListener?.onLoop()
            }
        }
    }

    /**
     * @return 检测是否已经打开了通知的权限
     */
    fun isEnableNotification(): Boolean {
        var isOpened = false
        try {
            val from = NotificationManagerCompat.from(context)
            isOpened = from.areNotificationsEnabled()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isOpened
    }

    /**
     * 跳转到通知的页面
     */
    fun openNotificationPage() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.applicationContext.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        context.startActivity(intent)
    }

    /**
     * 打开渠道的页面
     */
    fun openChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val packageName: String = context.packageName
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, packageName)
            context.startActivity(intent)
        }
    }

    interface CustomViewCallBackListener {
        fun onCallBack(view: RemoteViews)
    }

    fun setCustomViewCallBackListener(listener: CustomViewCallBackListener) {
        this.mCustomListener = listener
    }

    interface LoopCallBackListener {
        fun onLoop()
    }

    fun setLoopCallBackListener(listener: LoopCallBackListener) {
        mLoopListener = listener
    }
}