package com.android.apphelper2.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.R
import com.android.apphelper2.app.BaseApplication
import com.android.apphelper2.interfaces.UiInterface
import com.android.apphelper2.utils.LogUtil
import com.android.apphelper2.utils.statusBar.StatusBarUtil

open abstract class BaseBindingActivity<T : ViewBinding> : AppCompatActivity(), UiInterface<T> {

    lateinit var mBinding: T
    lateinit var mActivity: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        LogUtil.e("当前页面是：Activity: " + javaClass.name)

        initStatusBar()
        onCreateViewBefore()

        val contentView = getContentView()
        setContentView(contentView)

        initView()
        initListener()
        initData(savedInstanceState)
        onCreateViewAfter()
    }

    override fun initStatusBar() {
        super.initStatusBar()

        if (BaseApplication.builder != null) {
            BaseApplication.builder?.let {
                val statusBarColor = it.statusBarColor
                if (statusBarColor > 0) {
                    StatusBarUtil.getInstance(this)
                        .setStatusColor(statusBarColor)
                } else {
                    StatusBarUtil.getInstance(this)
                        .setStatusColor(R.color.base_title_background_color)
                }
            }
        } else {
            StatusBarUtil.getInstance(this)
                .setStatusColor(R.color.base_title_background_color)
        }
    }

    override fun getContentView(): View {
        mBinding = getBinding(layoutInflater, null)
        return getRootView()
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

    override fun getRootView(): View {
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        mActivity = this
    }

    override fun onResume() {
        super.onResume()
        mActivity = this
    }
}