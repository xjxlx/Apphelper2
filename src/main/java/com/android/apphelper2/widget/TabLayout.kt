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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
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

    private val mItemTitleArray: Array<String> = arrayOf("线索", "需求", "商场111111", "我的")
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
    private var mTabIndicatorHeight = ResourcesUtil.dp(2F)
    private val mTitleMap = mutableMapOf<Int, Point>()
    private var mDefaultItem = 0
    private val mAnimationMaxDuration = 1000
    private val mAnimationSpeed: Long by lazy {
        return@lazy (mAnimationMaxDuration / mItemTitleArray.size).toLong()
    }
    private val mItemTextSize = 15F

    private val mPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        private var mCurrentPoint: Point? = null
        private var mScrollPoint: Point? = null
        private var mOldPercent = 0F
        private var mItemWidthDifferenceValue = -1

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when (state) {
                ViewPager2.SCROLL_STATE_IDLE -> {
                    // LogUtil.e("scroll", "停止！")
                    mCurrentPoint = null
                    mScrollPoint = null
                    mOldPercent = 0F
                    mItemWidthDifferenceValue = -1
                }
                ViewPager2.SCROLL_STATE_DRAGGING -> {
                    LogUtil.e("scroll", "正在拖动 ！")
                }
                ViewPager2.SCROLL_STATE_SETTLING -> {
                    // LogUtil.e("scroll", "快到结束了！")
                }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            // 置空对象，重新开始判定
            if (positionOffset == 0F) {
                mCurrentPoint = null
                mScrollPoint = null
            }

            /**
             * 1: 必须是在产生了具体的偏移值的时候才可以去计算
             * 2：判定对象不为空
             * 3：偏移值归零的时候，置空，然后重新去测量
             */
            if ((positionOffset != 0F) && (mCurrentPoint == null || mScrollPoint == null)) {
                /**
                 * 1:左滑的百分比是从1~0，右滑的参数是1~0
                 * 2:左滑的值，越来越大，右滑的值越来越小
                 * 3: 真正判断方向的时候，需要取反反向
                 */
                val isLeft = mOldPercent < positionOffset
                LogUtil.e("isLeft", isLeft)
                mOldPercent = positionOffset
                LogUtil.e("position: $position positionOffset: $positionOffset positionOffsetPixels: $positionOffsetPixels")

                LogUtil.e("重新获取方向，当前方向是：$isLeft")
                mCurrentPoint = mTitleMap[position]
                // 需要计算是向左还是向右，向左 计算下一个，向右计算上一个
                mScrollPoint = if (isLeft) {
                    mTitleMap[position + 1]
                } else {
                    mTitleMap[position - 1]
                }

                // 计算出文字的差值
                if (mCurrentPoint != null && mScrollPoint != null) {
                    mItemWidthDifferenceValue = (mScrollPoint!!.textWidth) - (mCurrentPoint!!.textWidth)
                }
            }

            if ((mCurrentPoint != null) && (mScrollPoint != null) && (mItemWidthDifferenceValue != -1)) {
                scrollPosition(positionOffset, mCurrentPoint!!, mScrollPoint!!, mItemWidthDifferenceValue)
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            mDefaultItem = position
            clickItem(position)
        }
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

    fun withViewPager2(viewPager2: ViewPager2) {
        viewPager2.registerOnPageChangeCallback(mPageChangeListener)
        val context = viewPager2.context
        if (context != null && context is FragmentActivity) {
            context.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        viewPager2.unregisterOnPageChangeCallback(mPageChangeListener)
                    }
                }
            })
        }
    }

    private fun addItem(index: Int, title: String) {
        val textView = TextView(context)
        textView.textSize = mItemTextSize
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
                            val left = (((item.right - item.left) - item.textWidth) / 2)
                            val top = (item.height - mTabIndicatorHeight).toInt()
                            val right = (left + item.textWidth)
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
            var fromLeft = (defaultPoint.right - defaultPoint.left - defaultPoint.textWidth) / 2
            for (index in 0 until mDefaultItem) {
                mTitleMap[index]?.let {
                    fromLeft += it.itemWidth
                }
            }

            var toLeft = (clickPoint.right - clickPoint.left - clickPoint.textWidth) / 2
            for (index in 0 until clickIndex) {
                mTitleMap[index]?.let {
                    toLeft += it.itemWidth
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
                        val right = left + clickPoint.textWidth
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

    private fun scrollPosition(percent: Float, currentPosition: Point, scrollPosition: Point, itemWidthDifferenceValue: Int) {

        // 滑动的时候，计算当前选中的item 和 点击 item 之间的距离，计算出百分比，然后每次滑动就滑动等比的距离
        // 此处要计算出偏移值，偏移值 = 当前item 和下个 item中间的间距
        /**
         * 1: 下一个item的文字左侧位置 - 当前item文字的左侧位置
         * 2：拿到差值，然后乘以 百分比，得到具体的偏移值
         * 3：偏移值 + 当前的文字左侧的位置
         *
         * 1: 滑动文字开始的位置：scroll.left + (scroll.width - scroll.measureWidth) / 2
         * 2：当前文字开始的位置：current.left + (current.width - current.measureWidth) / 2
         * 3: 滑动的具体便宜值 =  滑动的位置 - 当前的位置  = 中间的差值 * 当前的百分比
         *   = (② -①)* percent
         * 4: 开始的位置 = 当前文字的位置 + 偏移的具体位置
         */
        val scrollTextStart = scrollPosition.left + (scrollPosition.itemWidth - scrollPosition.textWidth) / 2
        val currentTextStart = currentPosition.left + (currentPosition.itemWidth - currentPosition.textWidth) / 2
        val offsetX = (scrollTextStart - currentTextStart) * percent
        LogUtil.e("开始的position:$currentTextStart 滑动的position: $scrollTextStart 偏移值：$offsetX")
        val left = (offsetX + currentTextStart).toInt()

        val top = (currentPosition.height - mTabIndicatorHeight).toInt()

        /**
         * 1: 计算出current 和 scroll 宽度的差值
         * 2：差值 * 百分比 = 具体需要放大的值
         * 3：current的宽度 + 需要放大的值
         */

        val rightOffsetX = itemWidthDifferenceValue * percent
        val right = (left + currentPosition.textWidth + rightOffsetX).toInt()
        val bottom = currentPosition.height
        LogUtil.e("left: $left top :$top right: $right bottom: $bottom")
        mTabIndicator.layout(left, top, right, bottom)
    }

    data class Point(var itemWidth: Int, var textWidth: Int, var height: Int, var left: Int, var right: Int)
}