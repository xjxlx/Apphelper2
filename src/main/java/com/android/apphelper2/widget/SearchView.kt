package com.android.apphelper2.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.view.marginBottom
import com.android.apphelper2.R
import com.android.common.utils.LogUtil
import com.android.common.utils.ResourcesUtil

class SearchView(private val context: Context, private val attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private var mBackground: Drawable? = ResourcesUtil.getDrawable(context, R.drawable.shape_round_4_search_background)

    private var mLeftText: TextView? = null
    private var mLeftShow = true
    private var mLeftContent: String = "搜索"
    private var mLeftSize: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.sp_17)
    private var mLeftColor: Int = ResourcesUtil.getColor(context, R.color.search_hint_color)
    private var mLeftStart = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_17)
    private var mLeftEnd = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_10)

    private var mSearch: EditText? = null
    private var mSearchContent: String = ""
    private var mSearchSize: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.sp_17)
    private var mSearchLeft: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_10)
    private var mSearchMarginVertical = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_12)

    private var mRightImage: ImageView? = null
    private var mSearchButton: Int = R.drawable.icon_search_button
    private var mSearchButtonWidth: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_20)
    private var mSearchButtonHeight: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_20)
    private var mSearchButtonLeft: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_10)
    private var mSearchButtonRight: Float = ResourcesUtil.getDimension(context, com.apphelper.demens.R.dimen.dp_18)
    private var mSearchListener: SearchListener? = null

    init {
        this.orientation = HORIZONTAL
        initView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var maxHeight = 0

        for (index in 0 until childCount) {
            val childAt = getChildAt(index)
            val paddingTop = childAt.paddingTop
            val marginBottom = childAt.marginBottom
            val measuredHeight = childAt.measuredHeight
            LogUtil.e("view:" + getChildAt(index))
//            maxHeight = max(maxHeight, (paddingTop + marginBottom + measuredHeight))
        }
//        setMeasuredDimension(measuredWidth, maxHeight)
    }

    fun setBackground(@DrawableRes resource: Int) {
        val drawable = ResourcesUtil.getDrawable(context, resource)
        this.mBackground = drawable
        background = this.mBackground
    }

    fun setLeftShow(showHint: Boolean = false): SearchView {
        this.mLeftShow = showHint
        if (this.mLeftShow) {
            this.mLeftText?.visibility = View.VISIBLE
        } else {
            this.mLeftText?.visibility = View.GONE
        }
        return this
    }

    fun setLeftContent(content: String = ""): SearchView {
        if (!TextUtils.isEmpty(content)) {
            this.mLeftContent = content
            this.mLeftText?.text = this.mLeftContent
        }
        return this
    }

    fun setLeftSize(@DimenRes size: Int = 0): SearchView {
        if (size != 0) {
            this.mLeftSize = ResourcesUtil.getDimension(context, size)
            this.mLeftText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLeftSize)
        }
        return this
    }

    fun setLeftColor(@ColorRes color: Int = 0): SearchView {
        if (color != 0) {
            this.mLeftColor = ResourcesUtil.getColor(context, color)
            this.mLeftText?.setTextColor(this.mLeftColor)
        }
        return this
    }

    fun setLeftStart(@DimenRes left: Int = 0): SearchView {
        if (left != 0) {
            this.mLeftStart = ResourcesUtil.getDimension(context, left)
            this.mLeftText?.setPadding(mLeftStart.toInt(), mSearchMarginVertical.toInt(), mLeftEnd.toInt(), mSearchMarginVertical.toInt())
        }
        return this
    }

    fun setLeftEnd(@DimenRes right: Int = 0): SearchView {
        if (right != 0) {
            this.mLeftEnd = ResourcesUtil.getDimension(context, right)
            this.mLeftText?.setPadding(mLeftStart.toInt(), mSearchMarginVertical.toInt(), mLeftEnd.toInt(), mSearchMarginVertical.toInt())
        }
        return this
    }

    fun setLeftMarginVertical(@DimenRes marginVertical: Int = 0): SearchView {
        if (marginVertical != 0) {
            this.mSearchMarginVertical = ResourcesUtil.getDimension(context, marginVertical)
        }
        return this
    }

    fun setSearchText(content: String = ""): SearchView {
        if (!TextUtils.isEmpty(content)) {
            mSearch?.setText(content)
        }
        return this
    }

    fun setSearchTextSize(@DimenRes size: Int = 0): SearchView {
        if (size != 0) {
            this.mSearchSize = ResourcesUtil.getDimension(context, size)
            this.mSearch?.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSearchSize)
        }
        return this
    }

    fun setSearchLeft(@DimenRes left: Int = 0): SearchView {
        if (left != 0) {
            this.mSearchLeft = ResourcesUtil.getDimension(context, left)
            this.mSearch?.setPadding(mSearchLeft.toInt(), mSearchMarginVertical.toInt(), 0, mSearchMarginVertical.toInt())
        }
        return this
    }

    fun setRightEnd(@DimenRes right: Int = 0): SearchView {
        if (right != 0) {
            this.mSearchButtonRight = ResourcesUtil.getDimension(context, right)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.rightMargin = this.mSearchButtonRight.toInt()
            }
        }
        return this
    }

    fun setRightStart(@DimenRes left: Int = 0): SearchView {
        if (left != 0) {
            this.mSearchButtonLeft = ResourcesUtil.getDimension(context, left)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.leftMargin = this.mSearchButtonLeft.toInt()
            }
        }
        return this
    }

    fun setRightHeight(@DimenRes height: Int = 0): SearchView {
        if (height != 0) {
            this.mSearchButtonHeight = ResourcesUtil.getDimension(context, height)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.height = this.mSearchButtonHeight.toInt()
            }
        }

        return this
    }

    fun setRightWidth(@DimenRes width: Int = 0): SearchView {
        if (width != 0) {
            this.mSearchButtonWidth = ResourcesUtil.getDimension(context, width)
            this.mRightImage?.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.width = this.mSearchButtonWidth.toInt()
            }
        }
        return this
    }

    fun setRightResource(@DrawableRes resource: Int = 0): SearchView {
        if (resource != 0) {
            this.mSearchButton = resource
            this.mRightImage?.setImageResource(mSearchButton)
        }
        return this
    }

    @SuppressLint("ResourceAsColor")
    fun initView() {
        removeAllViews()
        // add background
        background = mBackground

        // add search hint text
        addView(TextView(context).also {
            this.mLeftText = it
            if (mLeftShow) {
                it.text = mLeftContent
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLeftSize)
                it.setTextColor(mLeftColor)
                it.setPadding(mLeftStart.toInt(), mSearchMarginVertical.toInt(), mLeftEnd.toInt(), mSearchMarginVertical.toInt())
                it.gravity = Gravity.CENTER_VERTICAL
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        // add editText
        addView(EditText(context, attributeSet).also {
            this.mSearch = it
            it.background = null
            it.isSingleLine = true
            it.maxLines = 1
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSearchSize)
            it.setPadding(mSearchLeft.toInt(), mSearchMarginVertical.toInt(), 0, mSearchMarginVertical.toInt())
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (!TextUtils.isEmpty(s)) {
                            mSearchContent = s.toString()
                        }
                    }
                }
            })
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).also { params ->
            params.width = 0
            params.weight = 1F
        })

        // add search icon
        addView(ImageView(context, attributeSet).also {
            this.mRightImage = it
            it.setImageResource(mSearchButton)
            it.setOnClickListener {
                mSearchListener?.search(mSearchContent)
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also { params ->
            params.gravity = Gravity.CENTER_VERTICAL
            params.leftMargin = mSearchButtonLeft.toInt()
            params.rightMargin = mSearchButtonRight.toInt()
            params.width = mSearchButtonWidth.toInt()
            params.height = mSearchButtonHeight.toInt()
        })

        requestLayout()
    }

    fun setSearchListener(listener: SearchListener) {
        this.mSearchListener = listener
    }

    interface SearchListener {
        fun search(search: String)
    }
}