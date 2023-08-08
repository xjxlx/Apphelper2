package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.R
import com.android.common.utils.LogUtil

class BottomNavigationView : FrameLayout {

    private var mMenuBuilder: MenuBuilder? = null

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attributeSet: AttributeSet, @AttrRes defStyleRes: Int = 0) : super(context, attributeSet, defStyleRes) {
        val array: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BottomNavigationView)
        val menu = array.getResourceId(R.styleable.BottomNavigationView_bnv_menu, 0)
        if (menu != 0) {
            if (context is FragmentActivity) {
                mMenuBuilder = MenuBuilder(context)
                mMenuBuilder?.let {
                    context.menuInflater.inflate(menu, it)
                }
            }
        }
        array.recycle()
    }

    @SuppressLint("RestrictedApi")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mMenuBuilder?.let {
            val size = it.size()
            val maxWidth = measuredWidth / size
            var itemLeft = 0
            var itemTop = 0
            var itemRight = maxWidth
            var itemBottom = 100

            for (item in 0 until size) {
                val menuItem = it.getItem(item)
                val title = menuItem.title
                val itemId = menuItem.itemId
                val icon = menuItem.icon
                LogUtil.e("id: $itemId title: $title icon: $icon")

                val linearLayout = LinearLayout(context)
                linearLayout.layoutParams = LinearLayout.LayoutParams(maxWidth, itemBottom)
                linearLayout.orientation = LinearLayout.VERTICAL
                val textView = TextView(context)
//                textView

//                linearLayout.addView()
                linearLayout.layout(itemLeft, itemTop, itemRight, itemBottom)
                itemRight = itemLeft
                itemLeft += maxWidth
                linearLayout.setBackgroundColor(Color.parseColor("#00ff989"))
            }
        }
    }
}