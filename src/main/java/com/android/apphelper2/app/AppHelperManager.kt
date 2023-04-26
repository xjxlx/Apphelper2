package com.android.apphelper2.app

import android.app.Application
import com.android.apphelper2.BuildConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object AppHelperManager {

    lateinit var context: Application
    var isDebug = BuildConfig.DEBUG
    val packageName: String by lazy {
        return@lazy context.packageName
    }

    @JvmStatic
    fun init(app: Application) {
        context = app
        if (!this::context.isInitialized) {
            throw java.lang.NullPointerException("context is not initialized !")
        }
        initLogger()
    }

    private fun initLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false) // （可选）是否显示线程信息。
            // 默认值为true
            .methodCount(0) // （可选）要显示的方法行数。 默认2
            .methodOffset(0) // （可选）设置调用堆栈的函数偏移值，0的话则从打印该Log的函数开始输出堆栈信息，默认是0
            .tag(packageName) // （可选）每个日志的全局标记。 默认PRETTY_LOGGER（如上图）
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return isDebug // 只有在 Debug模式下才会打印
            }
        })
    }

    fun checkRegister() {
        if (!this::context.isInitialized) {
            throw java.lang.NullPointerException("application is not register !")
        }
    }
}
