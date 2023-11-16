package com.android.apphelper2.app

import android.app.Application
import com.android.apphelper2.BuildConfig
import com.android.common.app.ApplicationManager

object AppHelper2 {

    lateinit var application: Application

    private var builder: ApplicationManager.Builder? = null
    var isDebug = BuildConfig.DEBUG

    val mPackageName: String by lazy {
        return@lazy application.packageName
    }

    @JvmStatic
    fun init(app: Application, builder: ApplicationManager.Builder) {
        this.builder = builder
        application = app
        if (!this::application.isInitialized) {
            throw java.lang.NullPointerException("context is not initialized !")
        }

        // init common
        ApplicationManager.init(app, builder)
    }

    fun checkRegister() {
        if (!this::application.isInitialized) {
            throw java.lang.NullPointerException("application is not register !")
        }
    }
}
