package com.android.apphelper2.app

import android.app.Application
import com.android.apphelper2.BuildConfig
import com.android.apphelper2.R
import com.android.common.app.CommonManager

object AppHelper2 {

    lateinit var application: Application

    private var builder: Builder? = null
    var isDebug = BuildConfig.DEBUG

    val mPackageName: String by lazy {
        return@lazy application.packageName
    }

    @JvmStatic
    fun init(app: Application, builder: Builder) {
        this.builder = builder
        application = app
        if (!this::application.isInitialized) {
            throw java.lang.NullPointerException("context is not initialized !")
        }

        // init common
        CommonManager.init(app, isDebug, mPackageName)
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
        var placeHolderRecycleErrorView: Int = R.layout.base_recycle_error

        var httpBaseUrl: String = ""
    }
}
