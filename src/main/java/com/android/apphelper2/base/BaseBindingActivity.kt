package com.android.apphelper2.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.app.AppHelper2
import com.android.apphelper2.interfaces.UiInterface
import com.android.apphelper2.utils.ActivityManager
import com.android.common.utils.statusBar.StatusBarUtil
import com.android.common.utils.LogUtil

open abstract class BaseBindingActivity<T : ViewBinding> : AppCompatActivity(), UiInterface<T> {

    lateinit var mBinding: T
    lateinit var mActivity: FragmentActivity
    var statusBar: Int = 0

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

        // add the activity to the Stack
        ActivityManager.addActivity(this)
    }

    override fun initStatusBar() {
        super.initStatusBar()
        if (statusBar == 0) {
            AppHelper2.getBuilder()?.statusBarColor?.let {
                statusBar = it
            }
        }
        if (statusBar != 0) {
            StatusBarUtil.getInstance(this)
                .setStatusColor(statusBar)
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