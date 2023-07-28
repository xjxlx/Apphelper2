package com.android.apphelper2.interfaces

import android.view.View

/**
 * @作者 徐腾飞
 * @创建时间 2019/11/2  14:04
 * @更新者 HongJing
 * @更新时间 2019/11/2  14:04
 * @描述 列表条目的点击事件回调
 */
interface ItemClickListener<T> {
    fun onItemClick(view: View?, position: Int, t: T)
}