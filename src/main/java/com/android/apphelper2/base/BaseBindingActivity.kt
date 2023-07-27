package com.android.apphelper2.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.R
import com.android.apphelper2.app.BaseApplication
import com.android.apphelper2.interfaces.UiInterface
import com.android.apphelper2.utils.statusBar.StatusBarUtil

open abstract class BaseBindingActivity<T : ViewBinding> : AppCompatActivity(), UiInterface<T> {

    lateinit var mBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStatusBar()
        onCreateViewBefore()

        mBinding = getBinding(layoutInflater, null)
        setContentView(mBinding.root)

        initView()
        initListener()
        initData(savedInstanceState)
        onCreateViewAfter()
    }

    override fun initStatusBar() {
        super.initStatusBar()

        val statusBarColor = BaseApplication.builder.statusBarColor
        if (statusBarColor != 0) {
            StatusBarUtil.getInstance(this)
                .setStatusColor(statusBarColor)
        } else {
            StatusBarUtil.getInstance(this)
                .setStatusColor(R.color.statusBar)
        }
    }

    override fun onCreateViewBefore() {
        super.onCreateViewBefore()
    }

    override fun initView() {
    }

    override fun initView(rootView: View?) {
    }

    override fun initListener() {
    }

    override fun onCreateViewAfter() {
        super.onCreateViewAfter()
    }
}