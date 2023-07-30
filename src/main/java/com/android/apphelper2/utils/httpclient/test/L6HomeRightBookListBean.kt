package com.android.apphelper2.utils.httpclient.test

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author : 流星
 * @CreateDate: 2022/11/7-00:10
 * @Description:
 */
data class L6HomeRightBookListBean(var row1: List<Row1?>? = null, var row2: List<Row2?>? = null) {

    data class Row1(var unit_id: String = "", var unit_name: String? = null, var unit_name_cn: String? = null, var c1: String? = null,
                    var c2: String? = null, var c3: String? = null, var pic: String? = null, var booktype: String = "",
                    var booktype_desc: String? = null, var courseid: String? = null, var term_suiji: String = "",
                    var content: MutableList<Content>? = null, var canread: Canread? = null, var icon: String? = null,
                    var book_open: Boolean = false, var book_finish: Boolean = false, var book_read_pro: String? = null) {

        data class Content(var content_id: String? = null, var unitcontent_id: String? = null, var content_name: String? = null,
                           var gp_jd: String? = null, var content_type: String? = null, var study_date: String? = null,
                           var open_desc: String? = null, var open: Boolean = false, var finish: Boolean = false,
                           var canread: Canread? = null)
    }

    data class Row2(var unit_id: String? = null, var unit_name: String? = null, var unit_name_cn: String? = null, var c1: String? = null,
                    var c2: String? = null, var c3: String? = null, var pic: String? = null, var booktype: String? = null,
                    var booktype_desc: String? = null, var courseid: String? = null, var term_suiji: String? = null,
                    var content: List<Content?>? = null, var canread: Canread? = null, var book_finish: Boolean? = null,
                    var icon: String? = null, var book_open: Boolean = false, var book_read_pro: String? = null) {

        data class Content(
            var content_id: String? = null,
            var unitcontent_id: String? = null,
            var content_name: String? = null,
            var gp_jd: String? = null,
            var content_type: String? = null,
            var study_date: String? = null,
            var open_desc: String? = null,
            var `open`: Boolean? = null,
            var finish: Boolean? = null,
        )
    }

    @Parcelize
    data class Canread(
        var book_info: BookInfo = BookInfo(),
        var name: String = "",
        var unid: String = "",
        var suiji: String = "",
        var contentid: String = "",
    ) : Parcelable {

        @Parcelize
        data class BookInfo(
            var id: String? = null,
            var name: String? = null,
            var alicover: String? = null,
            var hsflag: String? = null,
            var hsflag_desc: String = "",
            var count_page: Int = 0,
            var read_pro: Int = 0,
        ) : Parcelable
    }
}
