package com.android.apphelper2.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.apphelper2.R
import com.android.common.utils.ResourcesUtil
import kotlin.math.max

/**
 * 1：小于等于4个，就占据全部的区域
 * 2：大于4，就加上间隔，然后自动延伸
 */
class TabLayout(context: Context, attributeSet: AttributeSet) : HorizontalScrollView(context, attributeSet) {

    private val mItemTitleArray: Array<String> = arrayOf("线索", "需求", "商场", "我的")
    private val mInterval = 100
    private val mRootView = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        tag = "root"
    }
    private val mTitleArrayView = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        tag = "title-array"
    }

    private var mMax = 4

    init {
        if (!isInEditMode) {
            initData()
        }
    }

    fun initData() {
        removeAllViews()

        // add root
        addView(mRootView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // add title array
        mRootView.addView(mTitleArrayView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

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
        textView.tag = "title - index: $title"

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            .also {
                if (mItemTitleArray.size <= 4) {
                    it.width = 0
                    it.height = LinearLayout.LayoutParams.WRAP_CONTENT
                    it.weight = 1F
                    it.gravity = Gravity.CENTER
                } else {
                    if (index < mItemTitleArray.size) {
                        it.marginEnd = mInterval
                    }
                }
            }
        mTitleArrayView.addView(textView, params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var mTotalWidth = 0F
        var mTotalHeight = 0
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


        if (mItemTitleArray.size <= 4) {
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