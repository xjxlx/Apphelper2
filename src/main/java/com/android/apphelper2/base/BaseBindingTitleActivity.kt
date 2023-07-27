package com.android.apphelper2.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.databinding.BaseTitlePageBinding
import com.android.apphelper2.interfaces.TitleInterface

open abstract class BaseBindingTitleActivity<T : ViewBinding> : BaseBindingActivity<T>(), TitleInterface {

    private var mBaseTitlePageBinding: BaseTitlePageBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // init title page binding
        mBaseTitlePageBinding = BaseTitlePageBinding.inflate(layoutInflater, null, false)
        // init mBinding
        mBaseTitlePageBinding?.let {
            // attach root view
            mBinding = getBinding(layoutInflater, it.root, true)
        }
        super.onCreate(savedInstanceState)

        // init title
        mBaseTitlePageBinding?.let {
            initTitle(it)
        }
    }

    override fun getContentView(): View {
        mBaseTitlePageBinding?.let {
            return it.root
        }
        return super.getContentView()
    }

    private fun initTitle(binding: BaseTitlePageBinding) {
        val titleContent = getTitleContent()
        binding.baseTitle.tvBaseTitle.text = titleContent ?: ""
    }
}