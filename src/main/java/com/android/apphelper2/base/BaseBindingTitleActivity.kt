package com.android.apphelper2.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseBindingTitleActivity<T : ViewBinding> : BaseBindingActivity<T>() {

    override fun initData(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): T {
        TODO("Not yet implemented")
    }

    override fun getRootView(): View {
        TODO("Not yet implemented")
    }
}