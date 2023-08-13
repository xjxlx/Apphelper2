package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.text.TextUtils
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
import com.android.common.utils.ResourcesUtil
import kotlin.math.max

@SuppressLint("RestrictedApi")
class BottomNavigationView2 constructor(private val mContext: Context, attSet: AttributeSet) : LinearLayout(mContext, attSet) {

    private val tag = "BottomNavigationView2"
    private var mMenuItemSize = 0
    private var mMenuItemViewMaxWidth: Int = 0
    private var mItemBackgroundColor = 0
    private var mShowLineFlag = false

    private val mLineHeight = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_1)

    private var mIconColor: ColorStateList? = null
    private var mIconSize = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_10)

    private var mTextColor: ColorStateList? = null
    private var mTextSize = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_5)

    private var mPaddingTop: Float = 0F
    private var mInterval: Float = 0F
    private var mPaddingBottom: Float = 0F
    private var mListener: ClickListener? = null
    private var mOldPosition: Int = -1
    private var currentPosition = 0

    init {
        this.orientation = VERTICAL

        val typedArray: TypedArray = context.obtainStyledAttributes(attSet, R.styleable.BottomNavigationView2)

        // item background color
        mItemBackgroundColor = typedArray.getColor(R.styleable.BottomNavigationView2_navigation_itemBackgroundColor, 0)

        // show line
        typedArray.getColor(R.styleable.BottomNavigationView2_navigation_lineColor, 0)
            .also { color ->
                if (color != 0) {
                    this.addView(FrameLayout(mContext).also { view ->
                        view.setBackgroundColor(color)
                        mShowLineFlag = true
                    }, LayoutParams(LayoutParams.MATCH_PARENT, mLineHeight.toInt()))
                }
            }

        // icon
        mIconColor = typedArray.getColorStateList(R.styleable.BottomNavigationView2_navigation_itemIconTint)
        typedArray.getDimension(R.styleable.BottomNavigationView2_navigation_itemIconSize, 0F)
            .also {
                if (it != 0F) {
                    mIconSize = it
                }
            }

        // text
        mTextColor = typedArray.getColorStateList(R.styleable.BottomNavigationView2_navigation_itemTextColor)
        typedArray.getDimension(R.styleable.BottomNavigationView2_navigation_itemTextSize, 0F)
            .also {
                if (it != 0F) {
                    mTextSize = it
                }
            }

        // padding
        typedArray.getDimension(R.styleable.BottomNavigationView2_navigation_paddingTop, 0F)
            .also {
                mPaddingTop = it
            }
        typedArray.getDimension(R.styleable.BottomNavigationView2_navigation_interval, 0F)
            .also {
                mInterval = it
            }
        typedArray.getDimension(R.styleable.BottomNavigationView2_navigation_paddingBottom, 0F)
            .also {
                mPaddingBottom = it
            }

        // menu
        typedArray.getResourceId(R.styleable.BottomNavigationView2_navigation_menu, 0)
            .also { resources ->
                if (mContext is FragmentActivity) {
                    MenuBuilder(mContext).also {
                        mContext.menuInflater.inflate(resources, it)
                        mMenuItemSize = it.size()
                        for (index in 0 until mMenuItemSize) {
                            addItemView(index, it)
                        }
                        // default selector item
                        checked(currentPosition)
                    }
                }
            }
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var mMaxHeight = 134 // 默认数据

        if (!isInEditMode) {
            var maxImageHeight = 0
            var maxTextHeight = 0

            for (rootIndex in 0 until childCount) {
                val rootChild: View? = getChildAt(rootIndex)
                if (rootChild != null && rootChild is LinearLayout) {
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

            mMaxHeight = 0
            if (mShowLineFlag) {
                mMaxHeight += mLineHeight.toInt()
            }
            mMaxHeight += (maxImageHeight + maxTextHeight + mPaddingTop + mInterval + mPaddingBottom).toInt()
        }
        setMeasuredDimension(widthMeasureSpec, mMaxHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (mMenuItemSize > 0) {
            mMenuItemViewMaxWidth = (right - left) / mMenuItemSize

            var itemLeft = 0
            var itemTop = 0
            var itemBottom = measuredHeight
            if (mShowLineFlag) {
                itemTop += mLineHeight.toInt()
                itemBottom -= mLineHeight.toInt()
            }
            var itemRight = mMenuItemViewMaxWidth

            for (index in 0 until childCount) {
                val rootView = getChildAt(index)
                if (rootView is FrameLayout) {
                    rootView.layout(0, 0, rootView.measuredWidth, rootView.measuredHeight)
                } else if (rootView is LinearLayout) {
                    // root layout
                    rootView.layout(itemLeft, itemTop, itemRight, itemBottom)
                    // LogUtil.e("rootIndex: $index  rootLeft: $itemLeft rootTop: $itemTop rootRight: $itemRight rootBottom：$itemBottom")

                    var imageBottom = 0
                    for (childIndex in 0 until rootView.childCount) {
                        val childView = rootView[childIndex]

                        if (childView is ImageView) {
                            val imageWidth = childView.measuredWidth
                            val imageHeight = childView.measuredHeight

                            val imageLeft = (mMenuItemViewMaxWidth - imageWidth) / 2
                            var imageTop = 0
                            if (mShowLineFlag) {
                                imageTop += mLineHeight.toInt()
                            }
                            if (mPaddingTop != 0F) {
                                imageTop += mPaddingTop.toInt()
                            }

                            val imageRight = imageLeft + imageWidth
                            imageBottom = (imageTop + imageHeight)

                            childView.layout(imageLeft, imageTop, imageRight, imageBottom)
                            // LogUtil.e("image-index: $childIndex  leftImage: $imageLeft topImage: $imageTop rightImage: $imageRight  bottomImage：$imageBottom")
                        } else if (childView is TextView) {
                            val textWidth = childView.measuredWidth
                            val textHeight = childView.measuredHeight

                            val leftText = (mMenuItemViewMaxWidth - textWidth) / 2
                            val topText = (imageBottom + mInterval).toInt()
                            val rightText = (leftText + textWidth)
                            val bottomText = (topText + textHeight + mPaddingBottom).toInt()
                            childView.layout(leftText, topText, rightText, bottomText)
                            // LogUtil.e("leftText: $leftText topText: $topText rightText: $rightText  bottomText：$bottomText")
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

        this.addView(LinearLayout(mContext).also { root ->
            root.id = itemId
            root.orientation = VERTICAL
            if (mItemBackgroundColor != 0) {
                root.setBackgroundColor(mItemBackgroundColor)
            }
            // add icon
            root.addView(ImageView(mContext).also { image ->
                image.setImageDrawable(icon)
                mIconColor?.let {
                    image.imageTintList = it
                }
            }, LayoutParams(mIconSize.toInt(), mIconSize.toInt()))

            // add title
            if (!TextUtils.isEmpty(title)) {
                root.addView(TextView(mContext).also { text ->
                    text.text = title.trim()
                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
                    mTextColor?.let {
                        text.setTextColor(it)
                    }
                }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            }

            // click item
            root.setOnClickListener {
                checked(index)
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    fun checked(position: Int) {
        val tempIndex = if (mShowLineFlag) {
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
                clickItem(childAt, childAt.id, position)
            } else {
                if (childAt is ViewGroup) {
                    childAt.forEach { child ->
                        child.isSelected = false
                    }
                }
            }
        }
    }

    fun currentItem(): Int {
        return currentPosition
    }

    private fun clickItem(view: View, itemId: Int, index: Int) {
        // LogUtil.e(tag, "clickItem index:$index listener: $mListener")
        val position = if (mShowLineFlag) {
            index + 1
        } else {
            index
        }
        mListener?.let {
            if (mOldPosition != position) {
                it.onClick(position, itemId, view)
                mOldPosition = position
                currentPosition = position
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