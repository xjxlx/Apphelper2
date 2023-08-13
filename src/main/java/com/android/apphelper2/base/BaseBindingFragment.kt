package com.android.apphelper2.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.interfaces.UiInterface
import com.android.common.utils.LogUtil

open abstract class BaseBindingFragment<T : ViewBinding> : Fragment(), UiInterface<T> {

    lateinit var mFragment: Fragment
    lateinit var mActivity: FragmentActivity
    lateinit var mBinding: T
    var mArguments: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFragment = this
        activity?.let {
            mActivity = it
        }
        mArguments = arguments
        LogUtil.e("当前页面是：Fragment: " + javaClass.name)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragment = this
        activity?.let {
            mActivity = it
        }
        mBinding = getBinding(inflater, container, false)
        return getRootView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mFragment = this
        if (context is FragmentActivity) {
            mActivity = context
        }
    }

    override fun onStart() {
        super.onStart()
        mFragment = this
        activity?.let {
            mActivity = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initStatusBar()
        onCreateViewBefore()
        initView(view)
        initListener()
        initData(savedInstanceState)
        onCreateViewAfter()
    }

    override fun initView() {
    }

    override fun initView(rootView: View?) {
    }

    override fun initListener() {
    }

    override fun initStatusBar() {
    }

    override fun getRootView(): View {
        return mBinding.root
    }

}