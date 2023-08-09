package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil
import kotlin.math.max

@SuppressLint("RestrictedApi", "UseCompatLoadingForColorStateLists")
class BottomNavigationView2 constructor(private val mContext: Context, attSet: AttributeSet) : LinearLayout(mContext, attSet) {

    private var mMenuBuilder: MenuBuilder? = null
    private var mMenuItemSize = 0
    private var mMenuItemViewMaxWidth: Int = 0
    private var mItemBackgroundColor = 0
    private var mLineColor = 0
    private val mLineHeight = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_1)

    private var mIconColor: ColorStateList? = null
    private var mIconSize = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_10)

    private var mTextColor: ColorStateList? = null
    private var mTextSize = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_5)

    private var mPaddingTop: Float = 0F
    private var mInterval: Float = 0F
    private var mPaddingBottom: Float = 0F
    private var mListener: ClickListener? = null

    init {
        val array: TypedArray = context.obtainStyledAttributes(attSet, R.styleable.BottomNavigationView2)
        val menuResource = array.getResourceId(R.styleable.BottomNavigationView2_bnv_menu, 0)
        mItemBackgroundColor = array.getColor(R.styleable.BottomNavigationView2_bnv_itemBackgroundColor, 0)
        mLineColor = array.getColor(R.styleable.BottomNavigationView2_bnv_lineColor, 0)

        // icon
        mIconColor = array.getColorStateList(R.styleable.BottomNavigationView2_bnv_itemIconTint)
        val iconSize = array.getDimension(R.styleable.BottomNavigationView2_bnv_itemIconSize, 0F)
        if (iconSize != 0F) {
            mIconSize = iconSize
        }

        // text
        mTextColor = array.getColorStateList(R.styleable.BottomNavigationView2_bnv_itemTextColor)
        val textSize = array.getDimension(R.styleable.BottomNavigationView2_bnv_itemTextSize, 0F)
        if (textSize != 0F) {
            mTextSize = textSize
        }

        // padding
        mPaddingTop = array.getDimension(R.styleable.BottomNavigationView2_bnv_paddingTop, 0F)
        mInterval = array.getDimension(R.styleable.BottomNavigationView2_bnv_interval, 0F)
        mPaddingBottom = array.getDimension(R.styleable.BottomNavigationView2_bnv_paddingBottom, 0F)

        if (menuResource != 0) {
            if (mContext is FragmentActivity) {
                MenuBuilder(context).apply {
                    mContext.menuInflater.inflate(menuResource, this)
                    mMenuBuilder = this
                    mMenuItemSize = size()
                }
            }
        }
        array.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        LogUtil.e("onFinishInflate ---> ")
        this.orientation = VERTICAL

        // add line
        if (mLineColor != 0) {
            val view = FrameLayout(mContext).also {
                it.setBackgroundColor(mLineColor)
            }
            this.addView(view, LayoutParams(LayoutParams.MATCH_PARENT, mLineHeight.toInt()))
        }

        mMenuBuilder?.also {
            for (index in 0 until mMenuItemSize) {
                addItemView(index, it)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        LogUtil.e("onMeasure ---> ")
        var maxImageHeight = 0
        var maxTextHeight = 0

        for (rootIndex in 0 until childCount) {
            val rootChild: View? = getChildAt(rootIndex)
            if (rootChild != null && rootChild is LinearLayout) {
                // 测量子view的宽高
                for (childIndex in 0 until rootChild.childCount) {
                    val child = rootChild[childIndex]
                    if (child is ImageView) {
                        maxImageHeight = max(maxImageHeight, child.measuredHeight)
                    } else if (child is TextView) {
                        maxTextHeight = max(maxTextHeight, child.measuredHeight)
                    }
                }
            }
        }
        var maxHeight = 0
        if (mLineColor != 0) {
            maxHeight += mLineHeight.toInt()
        }
        maxHeight += (maxImageHeight + maxTextHeight + mPaddingTop + mInterval + mPaddingBottom).toInt()
        setMeasuredDimension(widthMeasureSpec, maxHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        LogUtil.e("onLayout --->")

        if (mMenuItemSize > 0) {
            mMenuItemViewMaxWidth = (right - left) / mMenuItemSize
        }

        mMenuBuilder?.let {
            var itemLeft = 0
            val itemTop = 0
            var itemRight = mMenuItemViewMaxWidth
            val itemBottom = measuredHeight

            val count = if (mLineColor != 0) {
                mMenuItemSize + 1
            } else {
                mMenuItemSize
            }

            for (index in 0 until count) {
                val rootView = getChildAt(index)
                if (rootView is FrameLayout) {
                    rootView.layout(0, 0, rootView.measuredWidth, rootView.measuredHeight)
                } else if (rootView is LinearLayout) {
                    rootView.layout(itemLeft, itemTop, itemRight, itemBottom)
                    LogUtil.e("rootIndex: $index  rootLeft: $itemLeft rootTop: $itemTop rootRight: $itemRight rootBottom：$itemBottom")

                    var bottomImage = 0
                    for (childIndex in 0 until rootView.childCount) {
                        val childView = rootView[childIndex]

                        if (childView is ImageView) {
                            val imageWidth = childView.measuredWidth
                            val imageHeight = childView.measuredHeight

                            val leftImage = (mMenuItemViewMaxWidth - imageWidth) / 2
                            var topImage = 0
                            if (mPaddingTop != 0F) {
                                topImage = mPaddingTop.toInt()
                            }

                            val rightImage = leftImage + imageWidth
                            bottomImage = (topImage + imageHeight)

                            childView.layout(leftImage, topImage, rightImage, bottomImage)
                            LogUtil.e(
                                "image-index: $childIndex  leftImage: $leftImage topImage: $topImage rightImage: $rightImage  bottomImage：$bottomImage")
                        } else if (childView is TextView) {
                            val textWidth = childView.measuredWidth
                            val textHeight = childView.measuredHeight

                            val leftText = (mMenuItemViewMaxWidth - textWidth) / 2
                            val topText = (bottomImage + mInterval).toInt()
                            val rightText = (leftText + textWidth)
                            val bottomText = (topText + textHeight + mPaddingBottom).toInt()
                            childView.layout(leftText, topText, rightText, bottomText)
                            LogUtil.e("leftText: $leftText topText: $topText rightText: $rightText  bottomText：$bottomText")
                        }
                    }
                    itemLeft += mMenuItemViewMaxWidth
                    itemRight += mMenuItemViewMaxWidth
                }
            }
        }
    }

    private fun addItemView(index: Int, menu: MenuBuilder) {
        val itemId = menu[index].itemId
        val title = menu[index].title
        val icon = menu[index].icon

        LogUtil.e("title: $title itemId:$itemId icon:$icon")

        this.addView(LinearLayout(mContext).also { root ->
            root.id = itemId
            root.orientation = VERTICAL
            if (mItemBackgroundColor != 0) {
                root.setBackgroundColor(mItemBackgroundColor)
            }
            root.setOnClickListener {
                if (mLineColor != 0) {
                    mListener?.onClick(index + 1, itemId, root)
                } else {
                    mListener?.onClick(index, itemId, root)
                }
            }

            // add icon
            root.addView(ImageView(mContext).also { image ->
                image.setImageDrawable(icon)
                mIconColor?.let {
                    image.imageTintList = it
                }
            }, LayoutParams(mIconSize.toInt(), mIconSize.toInt()))

            // add title
            root.addView(TextView(mContext).also { text ->
                text.text = title.trim()
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)

                mTextColor?.let {
                    text.setTextColor(it)
                }
            }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    fun checked(position: Int) {
        val tempIndex = if (mLineColor != 0) {
            position + 1
        } else {
            position
        }
        for (index in 0 until childCount) {
            val childAt = getChildAt(index)
            if (index == tempIndex) {
                if (childAt is ViewGroup) {
                    childAt.forEach { child ->
                        child.isSelected = true
                    }
                }
            } else {
                if (childAt is ViewGroup) {
                    childAt.forEach { child ->
                        child.isSelected = false
                    }
                }
            }
        }
    }

    fun setNavigationItemClickListener(listener: ClickListener) {
        this.mListener = listener
    }

    interface ClickListener {
        fun onClick(position: Int, itemId: Int, view: View)
    }
}