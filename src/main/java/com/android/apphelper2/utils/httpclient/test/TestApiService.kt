package com.android.apphelper2.utils.httpclient.test

import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TestApiService {

    // l6 课程首页 - 左侧列表
    @FormUrlEncoded
    @POST("https://web.jollyeng.com/")
    suspend fun getL6List(@FieldMap map: MutableMap<String, Any>): HttpResponse<MutableList<L6HomeLeftBean>>

    // L6 - 课程首页 - 右侧列表
    @FormUrlEncoded
    @POST("https://web.jollyeng.com/")
    suspend fun getL6BookList(@FieldMap map: MutableMap<String, Any>): HttpResponse<String>

    @FormUrlEncoded
    @POST("https://web.jollyeng.com/")
    suspend fun getL6GalleryList(@FieldMap map: MutableMap<String, Any>): HttpResponse<L6GalleryBean>

    @FormUrlEncoded
    @POST("https://web.jollyeng.com/")
    suspend fun getTurnPage(@FieldMap map: MutableMap<String, Any>): HttpResponse<String>

    @FormUrlEncoded
    @POST("https://web.jollyeng.com/")
    suspend fun saveUserHistory(@FieldMap map: MutableMap<String, Any>): HttpResponse<Array<String>>
}