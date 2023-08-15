package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.core.view.size
import com.android.apphelper2.R
import com.android.apphelper2.utils.CustomViewUtil
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/**
 * 1：小于等于4个，就占据全部的区域
 * 2：大于4，就加上间隔，然后自动延伸
 */
class TabLayout(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private val mItemTitleArray: Array<String> = arrayOf("线索", "需求", "商场", "我的")
    private val mInterval = 30
    private val mTabIndicatorTag = "indicator"
    private val mRootView = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        tag = "root"
    }
    private val mTitleArrayView = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        tag = "title-array"
    }

    private val mMaxItemCount = 4
    private val mTabIndicator = View(context).also {
        it.setBackgroundColor(Color.RED)
        it.tag = mTabIndicatorTag
    }
    private var mTabIndicatorHeight = ResourcesUtil.toDp(2F)
    private val mTitleMap = mutableMapOf<Int, Point>()
    private var mDefaultItem = 0
    private val mAnimationMaxDuration = 1000
    private val mAnimationSpeed: Long by lazy {
        return@lazy (mAnimationMaxDuration / mItemTitleArray.size).toLong()
    }

    fun initData() {
        removeAllViews()

        // add root
        addView(mRootView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // add title array
        mRootView.addView(mTitleArrayView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        // add item
        mItemTitleArray.forEachIndexed { index, s ->
            addItem(index, s)
        }

        // add tabIndicator
        addView(mTabIndicator, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
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
        // textView.setBackgroundColor(Color.rgb(r, g, b))

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
        textView.setOnClickListener {
            it as TextView
            LogUtil.e("item:" + it.text)
            clickItem(index)
        }
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

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        getChildAt(1)?.let { indicator ->
            if (TextUtils.equals(indicator.tag.toString(), mTabIndicatorTag)) {
                getChildAt(0)?.let { root ->
                    if (root is LinearLayout) {
                        root.getChildAt(0)
                            ?.let { titleArray ->
                                if (titleArray is LinearLayout) {
                                    // mTitleWidthMap[index] = Point(itemWidth.toInt(), itemHeight, itemView)
                                    for (index in 0..titleArray.size) {
                                        titleArray.getChildAt(index)
                                            ?.let { item ->
                                                if (item is TextView) {
                                                    val itemWidth = CustomViewUtil.getTextWidth(item.paint!!, item.text.toString())
                                                    val itemHeight = item.height
                                                    mTitleMap[index] =
                                                        Point(item.width, itemWidth.toInt(), itemHeight, item.left, item.right)
                                                }
                                            }
                                    }
                                }
                            }

                        // tabIndicator layout
                        mTitleMap[mDefaultItem]?.let { item ->
                            val left = (((item.right - item.left) - item.width) / 2)
                            val top = (item.height - mTabIndicatorHeight).toInt()
                            val right = (left + item.width)
                            indicator.layout(left, top, right, item.height)
                        }
                    }
                }
            }
        }
    }

    private fun clickItem(clickIndex: Int) {
        val defaultPoint = mTitleMap[mDefaultItem]
        val clickPoint = mTitleMap[clickIndex]
        if (defaultPoint != null && clickPoint != null) {
            var fromLeft = (defaultPoint.right - defaultPoint.left - defaultPoint.width) / 2
            for (index in 0 until mDefaultItem) {
                mTitleMap[index]?.let {
                    fromLeft += it.measureWidth
                }
            }

            var toLeft = (clickPoint.right - clickPoint.left - clickPoint.width) / 2
            for (index in 0 until clickIndex) {
                mTitleMap[index]?.let {
                    toLeft += it.measureWidth
                }
            }

            LogUtil.e(
                "mDefaultItem: $mDefaultItem clickIndex:  $clickIndex default:$defaultPoint click: $clickPoint from: $fromLeft to: $toLeft")
            ValueAnimator.ofInt(fromLeft, toLeft)
                .apply {
                    duration = abs((mAnimationSpeed * (clickIndex - mDefaultItem)))
                    addUpdateListener {
                        val left = it.animatedValue as Int
                        val top = clickPoint.height - mTabIndicatorHeight
                        val right = left + clickPoint.width
                        val bottom = clickPoint.height
                        mTabIndicator.layout(left, top.toInt(), right, bottom)
                    }
                    addListener(onEnd = {
                        mDefaultItem = clickIndex
                    })
                    start()
                }
        }
    }

    data class Point(var measureWidth: Int, var width: Int, var height: Int, var left: Int, var right: Int)
}