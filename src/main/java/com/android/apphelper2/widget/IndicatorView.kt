package com.android.apphelper2.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.animation.addListener
import androidx.core.view.size
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.android.apphelper2.utils.CustomViewUtil
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil
import com.google.android.material.tabs.TabLayout
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

/**
 * 1：小于等于4个，就占据全部的区域
 * 2：大于4，就加上间隔，然后自动延伸
 * 使用说明
 *      1：这个指示器文件，适用于ViewPager、ViewPager2、TabLayout等View
 *      2：使用这个view的好处是，可以的随意修改下划线的颜色、宽度、高度等，避免了原生view版本不同，修改起来的困扰
 *      3：使用的时候，需要去绑定，用withPager2()就可以一键绑定
 *      4：如果item的个数小于等于4，则会评分整行的宽度，如果大于4，则需要去手动设置每个item之间的间距，默认的间距是30
 */
class IndicatorView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    //<editor-fold desc=" variable  ">
    private var mItemTitleArray: Array<String> = arrayOf()

    private val mRootView = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        tag = "root"
    }
    private val mTitleArrayView = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        tag = "title-array"
    }
    private val mTabIndicatorTag = "indicator"
    private val mTabIndicator by lazy {
        return@lazy View(context).also {
            it.setBackgroundColor(mTabIndicatorColor)
            it.tag = mTabIndicatorTag
        }
    }

    private var mItemMaxCount = 4
    private var mItemSpaceInterval = ResourcesUtil.dp(30F)
    private var mItemAnimationMaxDuration = 1000
    private val mItemAnimationSpeed: Long by lazy {
        return@lazy (mItemAnimationMaxDuration / mItemTitleArray.size).toLong()
    }
    private var mItemTextSize = 14F
    @ColorInt
    private var mItemColor: Int = Color.BLACK
    @ColorInt
    private var mItemBackgroundColor: Int = Color.WHITE

    private var mTabIndicatorInterval = ResourcesUtil.dp(10F)
    @ColorInt
    private var mTabIndicatorColor = mItemColor
    private var mTabIndicatorHeight = ResourcesUtil.dp(2.5F)
    private var mTabIndicatorWidthOffset = ResourcesUtil.dp(2F)
    private var mTabIndicatorWidthRatioOffset = 0F

    private val mTitleMap = mutableMapOf<Int, Point>()
    private var mDefaultItem = 0

    private val mPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        private var mCurrentPoint: Point? = null
        private var mScrollPoint: Point? = null
        private var mOldPercent = 0F
        private var mItemWidthDifferenceValue = -1F
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when (state) {
                ViewPager2.SCROLL_STATE_IDLE -> {
                    // LogUtil.e("scroll", "停止！")
                    mCurrentPoint = null
                    mScrollPoint = null
                    mOldPercent = 0F
                    mItemWidthDifferenceValue = -1F
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

            if ((mCurrentPoint != null) && (mScrollPoint != null) && (mItemWidthDifferenceValue != -1F)) {
                scrollPosition(positionOffset, mCurrentPoint!!, mScrollPoint!!, mItemWidthDifferenceValue)
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            mDefaultItem = position
            clickItem(position)
        }
    }
    //</editor-fold>

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
                            mTotalWidth += (mItemSpaceInterval * (titleArray.childCount - 1))
                        }
                    }
            }
        }

        if (mItemTitleArray.size <= mItemMaxCount) {
            mTotalWidth = measuredWidth.toFloat()
        }

        // 总高度 = 文字高度 + 指示器间距 + 指示器高度
        mTotalHeight += (mTabIndicatorInterval + mTabIndicatorHeight).toInt()

        setMeasuredDimension(ceil(mTotalWidth).toInt(), mTotalHeight)
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
                                    for (index in 0..titleArray.size) {
                                        titleArray.getChildAt(index)
                                            ?.let { item ->
                                                if (item is TextView) {
                                                    val itemWidth = CustomViewUtil.getTextWidth(item.paint!!, item.text.toString())
                                                    mTitleMap[index] = Point(item.width, itemWidth, item.height, item.left, item.right)
                                                }
                                            }
                                    }
                                }
                            }

                        // tabIndicator layout
                        mTitleMap[mDefaultItem]?.let { item ->
                            var left = ceil(((item.right - item.left - item.textWidth) / 2) - mTabIndicatorWidthOffset)
                            val top = (item.ItemHeight + mTabIndicatorInterval).toInt()
                            var right = ceil(left + item.textWidth + mTabIndicatorWidthOffset * 2)
                            val bottom = (top + mTabIndicatorInterval + mTabIndicatorHeight).toInt()

                            if (mTabIndicatorWidthRatioOffset != 0F) {
                                val ratioWidth = mTabIndicatorWidthRatioOffset * (right - left)
                                left -= ratioWidth
                                right += ratioWidth
                            }
                            indicator.layout(left.toInt(), top, right.toInt(), bottom)
                        }
                    }
                }
            }
        }
    }

    //<editor-fold desc=" control method">

    /**
     * 设置item固定的个数，如果小于等于这个count，则会评分整个屏幕的宽度，否则就会一个个的从左到右的排列
     * 默认是一行4个
     */
    fun setItemMaxCount(count: Int): IndicatorView {
        this.mItemMaxCount = count
        return this
    }

    /**
     * 设置item之间的间距，适用于item个数大于指定个数的时候
     * 默认是30dp
     */
    fun setItemInterval(interval: Float): IndicatorView {
        this.mItemSpaceInterval = interval
        return this
    }

    /**
     * 设置点击item时候，动画的最大持续时长
     * 默认是最大持续1s
     */
    fun setItemAnimationMaxDuration(duration: Int): IndicatorView {
        this.mItemAnimationMaxDuration = duration
        return this
    }

    /**
     * 单个Item的TextSize,设置的时候，因为已经指定了单位是sp，所以只能使用具体的数字，不能使用通过资源获取的dimenRes资源，否则会高度异常
     * 默认是14sp
     */
    fun setItemSize(size: Float): IndicatorView {
        this.mItemTextSize = size
        return this
    }

    /**
     * 单个Item的文字color
     * 默认是黑色
     */
    fun setItemColor(@ColorInt color: Int): IndicatorView {
        this.mItemColor = color
        return this
    }

    /**
     * 单个Item的背景color
     * 默认是白色
     */
    fun setItemBackgroundColor(@ColorInt color: Int): IndicatorView {
        this.mItemBackgroundColor = color
        return this
    }

    /**
     * 设置item和指示器之间的间距
     * 默认是10dp
     */
    fun setItemIndicatorInterval(interval: Float): IndicatorView {
        this.mTabIndicatorInterval = interval
        return this
    }

    /**
     * 指示器的color
     * 默认是和文档颜色相同的颜色
     */
    fun setTabIndicatorColor(@ColorInt color: Int): IndicatorView {
        this.mTabIndicatorColor = color
        return this
    }

    /**
     * 指示器的高度
     * 默认是2.5dp
     */
    fun setTabIndicatorHeight(size: Float): IndicatorView {
        this.mTabIndicatorHeight = size
        return this
    }

    /**
     * 1：指示器的宽度偏移值,默认的情况下，指示器的宽度是和文字的宽度相同的，但是有时候，指示器需要指定宽度，这个时候可以通过设置偏移值去设置宽度
     * 2：默认的指示器宽度和文字宽度相同
     * 3：如果偏移值为正数，则会向两边扩大指定的偏移值，如果偏移值为负数，则会向中心缩小指定的偏移值，则指示器会变小
     * 默认是2dp
     */
    fun setTabIndicatorOffsetWidth(offset: Float): IndicatorView {
        this.mTabIndicatorWidthOffset = offset
        return this
    }

    /**
     * 1:设置文字宽度的偏移比例，这个比例是文字本身宽度的比例
     * 2：增加的时候，是左右两侧都增加文字宽度的倍数，例如：value =0.5,则左右都会增加0.5倍，整体就是增加了一倍
     * 4：减小的时候，是左右两边都开始减小的，例如：减小0.5倍，就是左右两边都会减小0.5倍，则整个item平分的左右两侧都被减掉了，
     *    这个时候就是不可见的状态，所以，最小的值就是-0.5
     *
     * 默认比例是0，也就是不做任何放大，如果为负数，则会缩小，如果是正数，则会放大
     * 最小是-0.5，则完全不可见
     * 最大限制为10，正常情况下不可能那么大
     */

    fun setTabIndicatorRatioWidth(@FloatRange(from = -0.5, to = 10.0) ratio: Float): IndicatorView {
        this.mTabIndicatorWidthRatioOffset = ratio
        return this
    }

    /**
     * @param viewPager2 对象
     * @param titleArray title的数组
     */
    fun withViewPager2(viewPager2: ViewPager2, titleArray: Array<String>) {
        // 1：绑定滑动监听
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

        this.mItemTitleArray = titleArray
        // 2：设置布局
        initView()
    }

    /**
     * @param viewPager2 viewPager2的对象
     * @param tabLayout TabLayout的对象，这个对象必须是已经设置过Tab的，否则在取值tab的text的时候，会那不到具体的值
     */
    fun withViewPager2(viewPager2: ViewPager2, tabLayout: TabLayout) {
        val tabCount = tabLayout.tabCount
        val titleArray: Array<String> = Array(tabCount) { "" }
        for (index in 0 until tabCount) {
            tabLayout.getTabAt(index)?.text?.let {
                titleArray[index] = it.toString()
            }
        }
        withViewPager2(viewPager2, titleArray)
    }
    //</editor-fold>

    //<editor-fold desc=" private method ">
    private fun initView() {
        removeAllViews()

        // add root
        addView(mRootView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // add title array
        mRootView.addView(mTitleArrayView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        // add item
        mItemTitleArray.forEachIndexed { index, s ->
            addTabItem(index, s)
        }

        // add tabIndicator
        addView(mTabIndicator, LayoutParams(LayoutParams.WRAP_CONTENT, mTabIndicatorHeight.toInt()))
        requestLayout()
    }

    private fun addTabItem(index: Int, title: String) {
        val textView = TextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mItemTextSize)
        textView.setTextColor(mItemColor)
        textView.text = title

        //  val r = Random.nextInt(0, 255)
        //  val g = Random.nextInt(0, 255)
        //  val b = Random.nextInt(0, 255)
        //  textView.setBackgroundColor(Color.rgb(r, g, b))

        textView.setBackgroundColor(mItemBackgroundColor)
        textView.tag = "title - index: $index title:$title"
        textView.gravity = Gravity.CENTER

        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .also {
                    if (mItemTitleArray.size <= mItemMaxCount) {
                        it.width = 0
                        it.weight = 1F
                    } else {
                        it.marginEnd = mItemSpaceInterval.toInt()
                    }
                }
        mTitleArrayView.addView(textView, layoutParams)
        textView.setOnClickListener {
            it as TextView
            LogUtil.e("item:" + it.text)
            clickItem(index)
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
            ValueAnimator.ofFloat(fromLeft, toLeft)
                .apply {
                    duration = abs((mItemAnimationSpeed * (clickIndex - mDefaultItem)))
                    addUpdateListener {
                        var left = ceil(it.animatedValue as Float - mTabIndicatorWidthOffset)
                        val top = (clickPoint.ItemHeight + mTabIndicatorInterval).toInt()
                        var right = ceil(left + clickPoint.textWidth + mTabIndicatorWidthOffset * 2)
                        val bottom = (top + mTabIndicatorInterval + mTabIndicatorHeight).toInt()

                        if (mTabIndicatorWidthRatioOffset != 0F) {
                            val ratioWidth = mTabIndicatorWidthRatioOffset * (right - left)
                            left -= ratioWidth
                            right += ratioWidth
                        }
                        mTabIndicator.layout(left.toInt(), top, right.toInt(), bottom)
                    }
                    addListener(onEnd = {
                        mDefaultItem = clickIndex
                    })
                    start()
                }
        }
    }

    private fun scrollPosition(percent: Float, currentPosition: Point, scrollPosition: Point, itemWidthDifferenceValue: Float) {
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
        var left = ceil(offsetX + currentTextStart - mTabIndicatorWidthOffset)
        val top = (currentPosition.ItemHeight + mTabIndicatorInterval).toInt()

        /**
         * 1: 计算出current 和 scroll 宽度的差值
         * 2：差值 * 百分比 = 具体需要放大的值
         * 3：current的宽度 + 需要放大的值
         */
        val rightOffsetX = itemWidthDifferenceValue * percent
        var right = ceil(left + currentPosition.textWidth + rightOffsetX + mTabIndicatorWidthOffset * 2)
        val bottom = (top + mTabIndicatorInterval + mTabIndicatorHeight).toInt()
        LogUtil.e("left: $left top :$top right: $right bottom: $bottom")

        if (mTabIndicatorWidthRatioOffset != 0F) {
            val ratioWidth = mTabIndicatorWidthRatioOffset * (right - left)
            left -= ratioWidth
            right += ratioWidth
        }
        mTabIndicator.layout(left.toInt(), top, right.toInt(), bottom)
    }
    //</editor-fold>
    data class Point(var itemWidth: Int, var textWidth: Float, var ItemHeight: Int, var left: Int, var right: Int)
}