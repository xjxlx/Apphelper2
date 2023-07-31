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
    var mBinding: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFragment = this
        activity?.let {
            mActivity = it
        }
        LogUtil.e("当前页面是：Fragment: " + javaClass.name)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragment = this
        activity?.let {
            mActivity = it
        }
        return getBinding(inflater, container, false).root
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

        onCreateViewBefore()
        initView(view)
        initListener()
        initData(savedInstanceState)
        onCreateViewAfter()
    }

    override fun getContentView(): View {
        TODO("Not yet implemented")
    }

    override fun initView() {
    }

    override fun initListener() {
    }

    override fun getRootView(): View {
        return mBinding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}