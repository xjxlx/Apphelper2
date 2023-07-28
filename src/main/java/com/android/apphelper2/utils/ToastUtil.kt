package com.android.apphelper2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.apphelper2.R
import com.android.apphelper2.app.AppHelper2

@SuppressLint("StaticFieldLeak")
object ToastUtil {

    private const val TAG = "ToastUtil"
    private var mContext: Context = AppHelper2.application
    private val mToast: Toast by lazy {
        return@lazy Toast(mContext)
    }
    private val mView: View by lazy {
        return@lazy LayoutInflater.from(mContext)
            .inflate(R.layout.widget_toast, null)
    }
    private val mTextView: TextView by lazy {
        return@lazy mView.findViewById<TextView>(R.id.message)
    }
    private var yOffset = 0

    private var mLeft = 0
    private var mTop = 0
    private var mRight = 0
    private var mBottom = 0
    private var mTextSize = 0
    private var mIsBOLD = false
    private var isPadding = false

    /**
     * 强大的吐司，能够连续弹的吐司,默认弹出在屏幕底部5分之一的位置
     *
     * @param text 内容
     */
    fun show(text: String?) {
        if (yOffset <= 0) {
            val screenHeight: Int = ScreenUtil.getScreenHeight(mContext)
            yOffset = screenHeight / 5
        }
        show(text, Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, yOffset)
    }

    fun show(context: Context, text: String?) {
        mContext = context
        if (yOffset <= 0) {
            val screenHeight: Int = ScreenUtil.getScreenHeight(context)
            yOffset = screenHeight / 5
        }
        show(text, Toast.LENGTH_SHORT, Gravity.BOTTOM, 0, yOffset)
    }

    /**
     * @param text     内容
     * @param duration 时间
     * @param gravity  位置
     * @param xOffset  默认居中
     * @param yOffset  偏移，如果为-1的话，就使用默认屏幕5分之一的高度
     */
    @SuppressLint("InflateParams")
    fun show(text: String?, duration: Int, gravity: Int, xOffset: Int, yOffset: Int) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        mTextView.text = text

        if (isPadding) {
            mTextView.setPadding(mLeft, mTop, mRight, mBottom)
        }
        if (mTextSize != 0) {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
        }
        if (mIsBOLD) {
            mTextView.typeface = Typeface.DEFAULT_BOLD
        }

        // 5:设置Toast的参数
        mToast.setGravity(gravity, xOffset, yOffset)
        mToast.view = mView
        mToast.duration = duration
        mToast.show()
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mLeft = left
        mTop = top
        mRight = right
        mBottom = bottom
        isPadding = true
    }

    fun setTextSize(size: Int) {
        mTextSize = size
    }

    fun isBOLD(isBOLD: Boolean) {
        mIsBOLD = isBOLD
    }
}