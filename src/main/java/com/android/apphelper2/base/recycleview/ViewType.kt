package com.android.apphelper2.base.recycleview

object ViewType {

    /**
     * 正常数据的布局
     */
    val TYPE_DATA = ViewTypeEnum(1)

    /**
     * 空布局
     */
    var TYPE_EMPTY = ViewTypeEnum(2)

    /**
     * 异常布局
     */
    var TYPE_ERROR = ViewTypeEnum(3)

    /**
     * 头布局
     */
    var TYPE_HEAD = ViewTypeEnum(4)

    /**
     * 脚布局
     */
    var TYPE_FOOT = ViewTypeEnum(5)
}