package com.android.apphelper2.utils.httpclient.test

data class L6GalleryBean(
    var content_id: String = "",
    var unitcontent_id: String = "",
    var content_name: String? = "",
    var gp_jd: String? = "",
    var content_type: String? = "",
    var unit_id: String = "",
    var courseid: String = "",
    var term_suiji: String = "",
    var bg_pic: String? = "",
    var content: List<Content>? = listOf(),
) {

    data class Content(
        var title: String? = "",
        var did: String? = "",
        var content_sub_id: String? = "",
        var type: String? = "",
        var pic: String? = "",
        var `fun`: Fun? = Fun(),
        var finish: Boolean? = false,
    ) {
        data class Fun(
            var weburl: String? = "",
            var bookinfo: Bookinfo? = Bookinfo(),
            var book: MutableList<Book>? = mutableListOf(),
            var bookshare: Bookshare? = Bookshare(),
        ) {
            data class Bookinfo(
                var id: String? = "",
                var name: String? = "",
                var alicover: String? = "",
                var hsflag: String? = "",
                var hsflag_desc: String? = "",
                var count_page: Int? = 0,
                var read_pro: Int = 0,
            )

            data class Book(
                var id: String = "",
                var book_id: String = "",
                var pic: String? = "",
                var title: String? = "",
                var fontscale: Int? = 0,
                var ord: String = "",
                var type: String? = "",
                var audio_url: String? = "",
                var luyin: String? = "",
                var book_content_id: String? = "",
                var read_flag: Boolean? = false,
            )

            data class Bookshare(
                var bookid: String? = "",
                var bookname: String? = "",
                var cover: String? = "",
                var bg_pic: String? = "",
                var share: Share = Share(),
            ) {
                data class Share(
                    var url: String = "",
                    var title: String = "",
                    var text: String = "",
                    var quan: String = "",
                    var logo: String = "",
                )
            }
        }
    }
}