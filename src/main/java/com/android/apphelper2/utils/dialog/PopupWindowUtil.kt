package com.android.apphelper2.utils.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.utils.LogUtil

class PopupWindowUtil {

    var popupWindow: PopupWindow? = null
    var builder: Builder? = null

    constructor(builder: Builder) {
        LogUtil.e("----->###  constructor")
        init(builder)
    }

    class Builder {
        var rootView: View? = null // popupWindow 的布局对象
        var activity: FragmentActivity? = null   // popupWindow 依赖的activity对象
        var fragment: Fragment? = null // popupWindow 依赖的fragment对象
        private var typeFrom: Int = 0 // 1：来源于activity，2：来源于fragment
        var canceledOnTouchOutside = true  // 点击 popupWindow 外界是否可以取消dialog ，默认可以
        var width = WindowManager.LayoutParams.MATCH_PARENT  // 宽
        var height = WindowManager.LayoutParams.MATCH_PARENT  // 高
        var gravity: Int = Gravity.CENTER  // 默认居中显示
        var offsetX = 0 // 偏移的X轴
        var offsetY = 0 // 偏移的Y轴
        var cancelable: Boolean = true // 按下返回键的时候，是否可以取消 popupWindow 默认可以
        val listCloseView: HashSet<View> = hashSetOf()// 关闭 popupWindow 的对象
        var isFocusable = true //  是否拥有焦点
        var isTouchable = true //  是否可以触摸
        var isClippingEnabled = true //  是否超出屏幕

        @IdRes
        var closeId: Int = 0
            set(value) {
                rootView?.let {
                    val view = it.findViewById<View>(value)
                    if (view != null) {
                        listCloseView.add(view)
                    }
                }
            }

        var closeView: View? = null
            set(value) {
                if (value != null) {
                    listCloseView.add(value)
                }
            }

        constructor(activity: FragmentActivity, contentView: Int) {
            typeFrom = 1
            this.activity = activity
            rootView = LayoutInflater.from(activity)
                .inflate(contentView, null, false)
        }

        constructor(fragment: Fragment, contentView: Int) {
            typeFrom = 2
            this.fragment = fragment
            this.activity = fragment.activity
            rootView = LayoutInflater.from(activity)
                .inflate(contentView, null, false)
        }

        fun setGravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        fun canceledOnTouchOutside(canceledOnTouchOutside: Boolean): Builder {
            this.canceledOnTouchOutside = canceledOnTouchOutside
            return this
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun seOffsetX(offsetX: Int): Builder {
            this.offsetX = offsetX
            return this
        }

        fun seOffsetY(offsetY: Int): Builder {
            this.offsetY = offsetY
            return this
        }

        fun cancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun isFocusable(isFocusable: Boolean): Builder {
            this.isFocusable = isFocusable
            return this
        }

        fun isTouchable(isTouchable: Boolean): Builder {
            this.isTouchable = isTouchable
            return this
        }

        fun isClippingEnabled(isClippingEnabled: Boolean): Builder {
            this.isClippingEnabled = isClippingEnabled
            return this
        }

        fun setViewClickListener(id: Int, listener: View.OnClickListener): Builder {
            rootView?.let {
                val view = it.findViewById<View>(id)
                view.setOnClickListener(listener)
            }
            return this
        }

        fun <T : View> getView(id: Int): T? {
            return rootView?.findViewById(id)
        }

        fun build(): PopupWindowUtil {
            LogUtil.e("----->###  build")
            return PopupWindowUtil(this)
        }
    }

    private fun init(builder: Builder) {
        LogUtil.e("----->###  init")

        builder.activity?.let {
            if ((!it.isFinishing) && (!it.isDestroyed)) {

                popupWindow = PopupWindow().apply {
                    this.width = builder.width // 宽度
                    this.height = builder.height // 高度

                    // 焦点
                    this.isFocusable = builder.isFocusable
                    // 设置可以点击pop以外的区域
                    this.isOutsideTouchable = builder.canceledOnTouchOutside
                    // 设置PopupWindow可触摸
                    this.isTouchable = builder.isTouchable
                    // 设置超出屏幕显示，默认为false,代表可以
                    this.isClippingEnabled = builder.isClippingEnabled

                    // 点击外部是否关闭popupWindow
                    if (builder.canceledOnTouchOutside) {
                        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    } else {
                        this.setBackgroundDrawable(null)
                    }

                    // 设置布局
                    if (builder.rootView != null) {
                        this.contentView = builder.rootView
                    }
                }

                // 关闭弹窗
                if (builder.listCloseView.isNotEmpty()) {
                    for (item in builder.listCloseView) {
                        item.setOnClickListener {
                            dismiss()
                        }
                    }
                }

                // 关闭监听
                popupWindow?.let { window ->
                    window.setOnDismissListener { dismissListener?.onDismiss(window) }
                }
            }
        }
    }

    fun show() {
        builder?.let {
            if (it.activity != null) {
                if ((!it.activity!!.isFinishing) && (!it.activity!!.isDestroyed)) {
                    popupWindow?.let { window ->
                        if (!window.isShowing) {
                            if (builder?.rootView != null) {
                                window.showAtLocation(builder?.rootView!!, it.gravity, it.offsetX, it.offsetY)
                                showListener?.onShow(window)
                            }
                        }
                    }
                }
            }
        }
    }

    fun show(view: View) {
        LogUtil.e("----->###  show")

        popupWindow?.let { window ->
            builder?.let {
                window.showAtLocation(view, it.gravity, it.offsetX, it.offsetY)
            }
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    interface OnDismissListener {
        fun onDismiss(popupWindow: PopupWindow)
    }

    private var dismissListener: OnDismissListener? = null
    fun setDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnShowListener {
        fun onShow(popupWindow: PopupWindow)
    }

    private var showListener: OnShowListener? = null
    fun setShowListener(listener: OnShowListener) {
        this.showListener = listener
    }

}