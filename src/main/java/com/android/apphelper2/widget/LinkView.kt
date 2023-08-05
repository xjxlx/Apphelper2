package com.android.apphelper2.widget

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import com.android.apphelper2.R

/**
 * 拓展 TextView，让他可以自定义变换颜色，并增加点击事件
 */
class LinkView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attributeSet) {

    private val mSpannableBuilder = SpannableStringBuilder()

    /**
     * 注意，空格也算是一个字符，这里的设置规则是包前不包后，所以endIndex要比实际的角标大1，从第0个开始计算
     */
    fun setColors(vararg params: Colors) {
        if (params.isNotEmpty()) {
            // Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括)、
            // Spanned.SPAN_INCLUSIVE_EXCLUSIVE(前面包括，后面不包括)、
            // Spanned.SPAN_EXCLUSIVE_INCLUSIVE(前面不包括，后面包括)、
            // Spanned.SPAN_INCLUSIVE_INCLUSIVE(前后都包括)。

            if (!TextUtils.isEmpty(text)) {
                mSpannableBuilder.append(text)
                params.forEach { colors ->
                    mSpannableBuilder.setSpan(SpanClick(colors.color, colors.listener), colors.startIndex, colors.endIndex,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }

                movementMethod = LinkMovementMethod.getInstance() // 这个一定要记得设置，不然点击不生效
                highlightColor = resources.getColor(R.color.transparent, null) //不设置会有背景色
                text = mSpannableBuilder// 设置文字
            }
        }
    }

    data class Colors(var color: Int = 0, var startIndex: Int = 0, var endIndex: Int = 0, var listener: OnClickListener? = null)

    class SpanClick(val color: Int, val listener: OnClickListener?) : ClickableSpan() {
        override fun onClick(widget: View) {
            listener?.onClick(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            // super.updateDrawState(ds) // 移除下划线
            ds.color = color
        }
    }
}