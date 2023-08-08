package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil

@SuppressLint("RestrictedApi")
class BottomNavigationView2 constructor(private val mContext: Context, attSet: AttributeSet) : FrameLayout(mContext, attSet) {

    private var mMenuBuilder: MenuBuilder? = null
    private var mMenuItemSize = 0
    private var mMenuItemViewMaxWidth: Int = 0
    private var mMenuItemViewMaxHeight: Int = 0
    private var mTextSize = ResourcesUtil.getDimension(mContext, com.apphelper.demens.R.dimen.dp_8)

    init {
        val array: TypedArray = context.obtainStyledAttributes(attSet, R.styleable.BottomNavigationView2)
        val menuResource = array.getResourceId(R.styleable.BottomNavigationView2_bnv_menu, 0)
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        LogUtil.e("onLayout ---> ")

        mMenuBuilder?.let {
            mMenuItemViewMaxWidth = (right - left) / mMenuItemSize
            mMenuItemViewMaxHeight = (bottom - top)
            var itemLeft = 0
            val itemTop = 0
            var itemRight = mMenuItemViewMaxWidth
            val itemBottom = mMenuItemViewMaxHeight

            for (index in 0 until mMenuItemSize) {
                val childAt = getChildAt(index)
                if (childAt is LinearLayout) {
                    childAt.layoutParams = LinearLayout.LayoutParams(mMenuItemViewMaxWidth, mMenuItemViewMaxHeight)
                    childAt.layout(itemLeft, itemTop, itemRight, itemBottom)
                    LogUtil.e("itemLeft: $itemLeft itemTop: $itemTop itemRight: $itemRight  itemBottom：$itemBottom")
                    itemLeft += mMenuItemViewMaxWidth
                    itemRight += mMenuItemViewMaxWidth
                }
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mMenuBuilder?.also {
            for (index in 0 until mMenuItemSize) {
                addItemView(index, it)
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
            root.orientation = LinearLayout.VERTICAL

            // add icon
            root.addView(ImageView(mContext).also { image ->
                val intrinsicWidth = icon.intrinsicWidth
                val intrinsicHeight = icon.intrinsicHeight

                image.layoutParams = LayoutParams(intrinsicWidth, intrinsicHeight)
                image.setImageDrawable(icon)
            })

            // add title
            root.addView(TextView(mContext).also { text ->
                text.text = title
                text.setBackgroundColor(Color.GRAY)
                text.setTextColor(Color.YELLOW)
                text.textSize = mTextSize
            })
        })
    }
}