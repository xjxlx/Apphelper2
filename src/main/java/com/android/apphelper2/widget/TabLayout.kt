package com.android.apphelper2.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.apphelper2.R
import com.android.common.utils.ResourcesUtil
import kotlin.math.max
import kotlin.random.Random

/**
 * 1：小于等于4个，就占据全部的区域
 * 2：大于4，就加上间隔，然后自动延伸
 */
class TabLayout(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private val mItemTitleArray: Array<String> = arrayOf("线索", "需求", "商场", "我的")
    private val mInterval = 30
    private val mRootView = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        tag = "root"
    }
    private val mTitleArrayView = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        tag = "title-array"
    }

    private var mMaxItemCount = 4

    fun initData() {
        removeAllViews()

        // add root
        addView(mRootView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // add title array
        mRootView.addView(mTitleArrayView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        mItemTitleArray.forEachIndexed { index, s ->
            addItem(index, s)
        }
        requestLayout()
    }

    private fun addItem(index: Int, title: String) {
        val textView = TextView(context)
        textView.textSize = 25F
        textView.setTextColor(ResourcesUtil.getColor(context, R.color.black))
        textView.text = title
        textView.tag = "title - index: $index title:$title"
        val r = Random.nextInt(0, 255)
        val g = Random.nextInt(0, 255)
        val b = Random.nextInt(0, 255)

        textView.setBackgroundColor(Color.rgb(r, g, b))
        textView.gravity = Gravity.CENTER

        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .also {
                    if (mItemTitleArray.size <= mMaxItemCount) {
                        it.width = 0
                        it.weight = 1F
                    } else {
                        it.marginEnd = mInterval
                    }
                }
        mTitleArrayView.addView(textView, layoutParams)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var mTotalWidth = 0F
        var mTotalHeight = 0

        // 遍历求出最大的宽和高

        // 第一层view = root
        getChildAt(0)?.let { root ->
            if (root is LinearLayout) {
                // title view
                root.getChildAt(0)
                    ?.let { titleArray ->
                        if (titleArray is LinearLayout) {
                            for (index in 0 until titleArray.childCount) {
                                val itemView = titleArray.getChildAt(index)
                                mTotalWidth += (itemView.measuredWidth)
                                mTotalHeight = max(mTotalHeight, itemView.measuredHeight)
                            }
                            mTotalWidth += (mInterval * (titleArray.childCount - 1))
                        }
                    }
            }
        }

        if (mItemTitleArray.size <= mMaxItemCount) {
            mTotalWidth = measuredWidth.toFloat()
        }
        setMeasuredDimension(mTotalWidth.toInt(), mTotalHeight)
    }

//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        super.onLayout(changed, l, t, r, b)
//        var left = 0
//        var top = 0
//        var right = 0
//        var bottom = 0
//
//        val rootView = getChildAt(0) as LinearLayout
//        val titleArrayView = rootView.getChildAt(0) as LinearLayout
//
//        for (index in 0 until titleArrayView.childCount) {
//            val itemView = titleArrayView.getChildAt(index)
//            val itemWidth = itemView.width
//            val itemHeight = itemView.height
//
//            right = (left + itemWidth)
//            bottom = itemHeight
//            itemView.layout(left, top, right, bottom)
//            LogUtil.e("left: $left top: $top right: $right bottom: $bottom")
////            left += (itemWidth + mInterval)
//            left += (itemWidth)
//        }
//    }
}