package com.android.apphelper2.utils.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.apphelper2.utils.LogUtil

class DialogUtil {

    private var typeFrom: Int = 0 // 1：来源于activity，2：来源于fragment
    var activity: FragmentActivity? = null// dialog依赖的activity对象
    var fragment: Fragment? = null; // dialog依赖的fragment对象
    var dialog: Dialog? = null
    var isBlur = true // 是否背景模糊，默认是模糊
    var animation: Int = R.style.base_dialog_animation;// 动画
    var layoutView: View? = null // dialog的布局
    var gravity: Int = Gravity.CENTER;// 默认居中显示
    var canceledOnTouchOutside = true; // 点击dialog外界是否可以取消dialog ，默认可以
    var width = WindowManager.LayoutParams.MATCH_PARENT; // 宽
    var height = WindowManager.LayoutParams.WRAP_CONTENT; // 高
    var offsetX = 0; // 偏移的X轴
    var offsetY = 0; // 偏移的Y轴
    var cancelable: Boolean = true // 按下返回键的时候，是否可以取消dialog,默认可以
    var showListener: DialogInterface.OnShowListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null
    private val listCloseView: HashSet<View> = hashSetOf()// 关闭dialog的对象

    @IdRes
    var closeId: Int = 0
        set(value) {
            layoutView?.let {
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

    fun createDialog(activity: FragmentActivity): DialogUtil {
        typeFrom = 1
        this.activity = activity
        fragment = null
        init()
        return this
    }

    fun createDialog(fragment: Fragment): DialogUtil {
        typeFrom = 2
        this.fragment = fragment
        activity = fragment.activity
        init()
        return this
    }

    private fun init() {
        activity?.let {
            if ((!it.isFinishing) && (!it.isDestroyed)) {
                dialog?.let { dialog ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }

                dialog = if (isBlur) {
                    Dialog(it, R.style.base_dialog_default)
                } else {
                    Dialog(it, R.style.base_dialog_hint)
                }

                layoutView?.let { view ->
                    dialog?.let { dialog ->
                        // 设置布局
                        dialog.setContentView(view)
                        // 按下返回键是否可以取消dialog
                        dialog.setCancelable(cancelable)
                        // dialog点击区域外的时候，是否可以取消dialog
                        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
                        val window = dialog.window
                        if (window != null) {
                            window.setGravity(gravity)
                            val attributes = window.attributes
                            if (attributes != null) {
                                attributes.width = width
                                attributes.height = height
                                attributes.x = offsetX
                                attributes.y = offsetY
                                // 设置动画
                                if (animation != 0) {
                                    attributes.windowAnimations = animation
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

                if (listCloseView.isNotEmpty()) {
                    for (item in listCloseView) {
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
        activity?.let {
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

    class Builder {
        //
//
//        private DialogInterface.OnShowListener mShowListener;
//        private DialogInterface.OnDismissListener mDismissListener;
//
//        /**
//         * 1：来源于activity，2：来源于fragment
//         */
//        private final int mTypeFrom;

        fun build(): DialogUtil {
            return DialogUtil()
        }
    }

}