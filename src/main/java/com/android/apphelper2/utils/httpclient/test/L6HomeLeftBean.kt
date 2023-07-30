package com.android.apphelper2.utils.httpclient.test

/**
 * @author : 流星
 * @CreateDate: 2022/11/6-22:29
 * @Description:
 */
data class L6HomeLeftBean(var term_suiji: String? = null, var unid: String? = null, var start_date: String? = null,
                          var end_date: String? = null, var courseid: String? = null, var course_name: String? = null,
                          var pic: String? = null, var status: Int? = null, var status_desc: String? = null, var url: String? = null,
                          var open: Boolean = false, var curr: Boolean = false) {

    override fun toString(): String {
        return "L6HomeBean(term_suiji=$term_suiji, unid=$unid, start_date=$start_date, end_date=$end_date, courseid=$courseid, course_name=$course_name, pic=$pic, status=$status, status_desc=$status_desc, url=$url, curr=$curr)"
    }
}