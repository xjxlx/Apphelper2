package com.android.apphelper2.utils.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.apphelper2.utils.LogUtil

class DialogUtil private constructor() {
    var dialog: Dialog? = null
    var showListener: DialogInterface.OnShowListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null
    private var builder: Builder? = null

    constructor(builder: Builder) : this() {
        this.builder = builder
        init(builder)
    }

    private fun init(builder: Builder) {
        builder.activity?.let {
            if ((!it.isFinishing) && (!it.isDestroyed)) {
                dialog?.let { dialog ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }

                dialog = if (builder.isBlur) {
                    Dialog(it, R.style.base_dialog_default)
                } else {
                    Dialog(it, R.style.base_dialog_hint)
                }

                builder.rootView?.let { view ->
                    dialog?.let { dialog ->
                        // 设置布局
                        dialog.setContentView(view)
                        // 按下返回键是否可以取消dialog
                        dialog.setCancelable(builder.cancelable)
                        // dialog点击区域外的时候，是否可以取消dialog
                        dialog.setCanceledOnTouchOutside(builder.canceledOnTouchOutside)
                        val window = dialog.window
                        if (window != null) {
                            window.setGravity(builder.gravity)
                            val attributes = window.attributes
                            if (attributes != null) {
                                attributes.width = builder.width
                                attributes.height = builder.height
                                attributes.x = builder.offsetX
                                attributes.y = builder.offsetY
                                // 设置动画
                                if (builder.animation != 0) {
                                    attributes.windowAnimations = builder.animation
                                }
                                // 解决android 9.0水滴屏/刘海屏有黑边的问题
                                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                // attributes.layoutInDisplayCutoutMode =
                                // WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                                // }
                            }
                            window.attributes = attributes  // 设置属性
                        }

                        dismissListener?.let { dismiss ->
                            dialog.setOnDismissListener(dismiss)
                        }

                        showListener?.let { show ->
                            dialog.setOnShowListener(show)
                        }
                    }
                }

                if (builder.listCloseView.isNotEmpty()) {
                    for (item in builder.listCloseView) {
                        item.setOnClickListener {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener): DialogUtil {
        this.dismissListener = listener
        return this
    }

    fun setOnShowListener(listener: DialogInterface.OnShowListener): DialogUtil {
        this.showListener = listener
        return this
    }

    fun show() {
        builder?.activity?.let {
            if (!it.isFinishing && !it.isDestroyed) {
                dialog?.let { dialog ->
                    if (!dialog.isShowing) {
                        dialog.show()
                    }
                }
            }
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun setViewClickListener(id: Int, listener: View.OnClickListener): DialogUtil {
        builder?.rootView?.let {
            val view = it.findViewById<View>(id)
            view.setOnClickListener(listener)
        }
        return this
    }

    class Builder {
        var rootView: View? = null // dialog的布局对象
        var activity: FragmentActivity? = null   // dialog依赖的activity对象
        var fragment: Fragment? = null // dialog依赖的fragment对象
        private var typeFrom: Int = 0 // 1：来源于activity，2：来源于fragment

        var isBlur = true // 是否背景模糊，默认是模糊
        var animation: Int = R.style.base_dialog_animation // 动画
        var gravity: Int = Gravity.CENTER  // 默认居中显示
        var canceledOnTouchOutside = true  // 点击dialog外界是否可以取消dialog ，默认可以
        var width = WindowManager.LayoutParams.MATCH_PARENT  // 宽
        var height = WindowManager.LayoutParams.WRAP_CONTENT  // 高
        var offsetX = 0 // 偏移的X轴
        var offsetY = 0 // 偏移的Y轴
        var cancelable: Boolean = true // 按下返回键的时候，是否可以取消dialog,默认可以
        val listCloseView: HashSet<View> = hashSetOf()// 关闭dialog的对象

        @IdRes
        var closeId: Int = 0
            set(value) {
                rootView?.let {
                    val view = it.findViewById<View>(value)
                    if (view != null) {
                        listCloseView.add(view)
                        LogUtil.e("添加了一个view  --> " + listCloseView.size)
                    }
                }
            }

        var closeView: View? = null
            set(value) {
                if (value != null) {
                    listCloseView.add(value)
                    LogUtil.e("添加了一个view  --> " + listCloseView.size)
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

        fun isBlur(isBlur: Boolean): Builder {
            this.isBlur = isBlur
            return this
        }

        fun setAnimation(animation: Int): Builder {
            this.animation = animation
            return this
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

        fun setViewClickListener(id: Int, listener: View.OnClickListener): Builder {
            rootView?.let {
                val view = it.findViewById<View>(id)
                view.setOnClickListener(listener)
            }
            return this
        }

        fun setViewCreatedListener(listener: View.OnClickListener): Builder {
            listener.onClick(rootView)
            return this
        }

        fun <T : View> getView(id: Int): T? {
            return rootView?.findViewById(id)
        }

        fun build(): DialogUtil {
            return DialogUtil(this)
        }
    }

}