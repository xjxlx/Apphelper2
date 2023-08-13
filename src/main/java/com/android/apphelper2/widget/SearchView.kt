package com.android.apphelper2.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.android.apphelper2.R
import com.android.common.utils.ResourcesUtil

class SearchView(private val context: Context, private val attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private var mBackground: Drawable? = ResourcesUtil.getDrawable(context, R.drawable.shape_round_4_search_background)
    private var mSearchHintShow = true
    private var mSearchTextHint: String = "搜索"
    private var mSearchTextHintColor: Int = ResourcesUtil.getColor(context, R.color.search_hint_color)
    private var mSearchTextSize = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.sp_17)
    private var mSearchTextColor: Int = ResourcesUtil.getColor(context, R.color.search_hint_color)
    private var mSearchTextLeft = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_17)
    private var mSearchTextRight = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_10)
    private var mSearchTextMarginHorizontal = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_12)
    private var mSearchTextGravity: Int = Gravity.LEFT

    private var mSearchButton: Int = R.drawable.icon_search_button
    private var mSearchButtonWidth: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_20)
    private var mSearchButtonHeight: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_20)
    private var mSearchButtonLeft: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_10)
    private var mSearchButtonRight: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_18)
    private var mSearchListener: SearchListener<*>? = null

    init {
        this.orientation = HORIZONTAL
        initView()
    }

    fun setBackground(@DrawableRes background: Int) {
        val drawable = ResourcesUtil.getDrawable(context, background)
        this.mBackground = drawable
    }

    fun setSearchText(showHint: Boolean = mSearchHintShow, hint: String = mSearchTextHint, colorHint: Int = 0, size: Float = 0F,
                      color: Int = 0, left: Float = mSearchTextLeft, marginHorizontal: Float = mSearchTextMarginHorizontal) {
        this.mSearchHintShow = showHint
        if (!TextUtils.isEmpty(hint)) {
            this.mSearchTextHint = hint
        }
        if (colorHint != 0) {
            this.mSearchTextHintColor = colorHint
        }
        if (size != 0F) {
            this.mSearchTextSize = size
        }
        if (color != 0) {
            this.mSearchTextColor = color
        }
        if (left != 0F) {
            this.mSearchTextLeft = left
        }
        if (marginHorizontal != 0F) {
            this.mSearchTextMarginHorizontal = marginHorizontal
        }
    }

    fun setSearchButton(buttonResource: Int = mSearchButton, width: Float = mSearchButtonWidth, height: Float = mSearchButtonHeight,
                        left: Float = mSearchButtonLeft, right: Float = mSearchButtonRight) {
        this.mSearchButton = buttonResource
        if (width != 0F) {
            this.mSearchButtonWidth = width
        }
        if (height != 0F) {
            this.mSearchButtonHeight = height
        }
        if (left != 0F) {
            this.mSearchButtonLeft = left
        }
        if (right != 0F) {
            this.mSearchButtonRight = right
        }
    }

    fun initView() {
        // add background
        background = mBackground

        // add search text
        addView(TextView(context).also {
            if (mSearchHintShow) {
                it.hint = mSearchTextHint
                it.setHintTextColor(mSearchTextHintColor)
            }
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSearchTextSize)
            it.setTextColor(mSearchTextColor)
            it.setPadding(mSearchTextLeft.toInt(), mSearchTextMarginHorizontal.toInt(), mSearchTextRight.toInt(),
                mSearchTextMarginHorizontal.toInt())
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { params ->
            params.gravity = mSearchTextGravity
        })

        // add editText
        val editText = EditText(context, attributeSet).also {
            it.background = null
            it.isSingleLine = true
            it.maxLines = 1
        }
        addView(editText, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).also { params ->
            params.width = 0
            params.weight = 1F
        })

        // add search icon
        val searchIcon = ImageView(context, attributeSet).also {
            it.setImageResource(mSearchButton)
        }
        addView(searchIcon, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { params ->
            params.gravity = Gravity.CENTER_VERTICAL
            params.leftMargin = mSearchButtonLeft.toInt()
            params.rightMargin = mSearchButtonRight.toInt()
            params.width = mSearchButtonWidth.toInt()
            params.height = mSearchButtonHeight.toInt()
        })
    }

    fun <T> setSearchListener(listener: SearchListener<T>) {
        this.mSearchListener = listener
    }

    interface SearchListener<T> {
        fun search(t: T)
    }
}