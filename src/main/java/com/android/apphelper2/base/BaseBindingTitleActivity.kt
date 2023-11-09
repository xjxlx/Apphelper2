package com.android.apphelper2.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.apphelper2.databinding.BaseTitlePageBinding
import com.android.apphelper2.interfaces.TitleInterface

abstract class BaseBindingTitleActivity<T : ViewBinding> : BaseBindingActivity<T>(), TitleInterface {

    private var mBaseTitlePageBinding: BaseTitlePageBinding? = null
    var autoFinish = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // init title page binding
        mBaseTitlePageBinding = BaseTitlePageBinding.inflate(layoutInflater, null, false)
        // init mBinding
        mBaseTitlePageBinding?.let {
            // attach root view
            mBinding = getBinding(layoutInflater, it.root, true)
        }
        super.onCreate(savedInstanceState)

        mBaseTitlePageBinding?.let {
            // init title
            initTitle(it)

            if (autoFinish) {
                goBack()
            }
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
        binding.baseTitle.tvBaseTitle.text = titleContent
    }

    fun goBack(listener: View.OnClickListener? = null) {
        mBaseTitlePageBinding?.let {
            if (autoFinish) {
                it.baseTitle.llBaseTitleBack.setOnClickListener {
                    finish()
                }
            } else {
                it.baseTitle.llBaseTitleBack.setOnClickListener(listener)
            }
        }
    }
}