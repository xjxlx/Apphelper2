package com.android.apphelper2.interfaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

interface UiInterface<T : ViewBinding?> {

    /**
     * init statusBar
     */
    fun initStatusBar() {}

    /**
     * on Before create View
     */
    fun onCreateViewBefore() {}

    fun getContentView(): View

    fun initView()

    fun initView(rootView: View?)

    /**
     * init listener
     */
    fun initListener()

    /**
     * @param savedInstanceState save data
     * init data
     */
    fun initData(savedInstanceState: Bundle?)

    /**
     * on create View after
     */
    fun onCreateViewAfter() {}

    /**
     * @param inflater  layout inflater
     * @param container parent rootView
     * @return return an ViewBinding content ,if need required，use XXXBinding.inflate(layoutInflater, container,true),
     * if not required, use XXXBinding.inflate(layoutInflater)
     */
    fun getBinding(inflater: LayoutInflater, container: ViewGroup?, attachToRoot: Boolean = false): T

    /**
     * @return get rootView for the bindingView
     */
    fun getRootView(): View
}