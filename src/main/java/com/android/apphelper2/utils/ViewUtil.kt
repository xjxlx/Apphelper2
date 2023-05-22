package com.android.apphelper2.utils

import android.view.View
import android.view.ViewGroup.MarginLayoutParams

object ViewUtil {

    fun setMargin(view: View?, array: IntArray?) {
        if (view == null) {
            return
        }
        if (array == null || array.size != 4) {
            return
        }
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            val left = array[0]
            val top = array[1]
            val right = array[2]
            val bottom = array[3]
            layoutParams.setMargins(left, top, right, bottom)
            view.layoutParams = layoutParams
        }
    }

    fun setMarginBottom(view: View, bottom: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.bottomMargin = bottom
            view.layoutParams = layoutParams
        }
    }

    fun setMarginTop(view: View, topMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.topMargin = topMargin
            view.layoutParams = layoutParams
        }
    }

    fun setMarginStart(view: View, marginStart: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.marginStart = marginStart
            view.layoutParams = layoutParams
        }
    }

    fun setMarginEnd(view: View, marginEnd: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.marginEnd = marginEnd
            view.layoutParams = layoutParams
        }
    }

    fun setLeftMargin(view: View, topMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.leftMargin = topMargin
            view.layoutParams = layoutParams
        }
    }

    fun setRightMargin(view: View, rightMargin: Int) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.rightMargin = rightMargin
            view.layoutParams = layoutParams
        }
    }

    /**
     * 设置view的状态
     *
     * @param view       view
     * @param visibility 状态
     */
    fun setVisibility(view: View?, visibility: Int) {
        if (view != null) {
            val viewVisibility = view.visibility
            if (visibility != viewVisibility) {
                view.visibility = visibility
            }
        }
    }

    /**
     * @param view    指定view
     * @param visible true:可见，false:不可见
     */
    fun setViewVisible(view: View?, visible: Boolean) {
        if (view != null) {
            val visibility = view.visibility
            if (visible) {
                if (visibility != View.VISIBLE) {
                    view.visibility = View.VISIBLE
                }
            } else {
                if (visibility != View.GONE) {
                    view.visibility = View.GONE
                }
            }
        }
    }

    fun getMarginStart(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.marginStart
            }
        }
        return 0
    }

    fun getMarginEnd(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.marginEnd
            }
        }
        return 0
    }

    fun getMarginLeft(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.leftMargin
            }
        }
        return 0
    }

    fun getMarginRight(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.rightMargin
            }
        }
        return 0
    }

    fun getMarginTop(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.topMargin
            }
        }
        return 0
    }

    fun getMarginBottom(view: View?): Int {
        if (view != null) {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                return layoutParams.bottomMargin
            }
        }
        return 0
    }

    fun getLocationOnScreen(view: View?, locationCallBackListener: LocationCallBackListener?) {
        view?.post {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            locationCallBackListener?.onLocation(location)
        }
    }

    interface LocationCallBackListener {
        fun onLocation(location: IntArray?)
    }
}