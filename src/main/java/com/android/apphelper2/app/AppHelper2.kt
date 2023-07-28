package com.android.apphelper2.app

import android.app.Application
import com.android.apphelper2.BuildConfig
import com.android.apphelper2.R
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object AppHelper2 {

    lateinit var application: Application

    private var builder: Builder? = null
    var isDebug = BuildConfig.DEBUG

    val mPackageName: String by lazy {
        return@lazy application.packageName
    }

    @JvmOverloads
    @JvmStatic
    fun init(app: Application, repeat: Boolean = false, builder: Builder) {
        this.builder = builder
        application = app
        if (!this::application.isInitialized) {
            throw java.lang.NullPointerException("context is not initialized !")
        }

        // 避免和appHelper 重复初始化
        if (!repeat) {
            initLogger()
        }
    }

    private fun initLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false) // （可选）是否显示线程信息。
            // 默认值为true
            .methodCount(0) // （可选）要显示的方法行数。 默认2
            .methodOffset(0) // （可选）设置调用堆栈的函数偏移值，0的话则从打印该Log的函数开始输出堆栈信息，默认是0
            .tag(mPackageName) // （可选）每个日志的全局标记。 默认PRETTY_LOGGER（如上图）
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return isDebug // 只有在 Debug模式下才会打印
            }
        })
    }

    fun checkRegister() {
        if (!this::application.isInitialized) {
            throw java.lang.NullPointerException("application is not register !")
        }
    }

    fun getBuilder(): Builder? {
        return builder
    }

    class Builder {
        /**
         * status bar color
         */
        var statusBarColor: Int = 0

        /**
         * title bar view resource
         */
        var titleBarLayout: Int = R.layout.base_title_item

        var placeHolderRecycleTempView: Int = R.layout.base_recycle_empty
        var PlaceHolderRecycleErrorView: Int = R.layout.base_recycle_error
    }
}
