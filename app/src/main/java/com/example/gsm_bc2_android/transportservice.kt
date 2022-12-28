package com.example.gsm_bc2_android

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface transportservice {
    @FormUrlEncoded
    @POST("/send")
    fun requestLogin(
        @Field("email") email:String,
        @Field("balance") balance:Int,
        @Field("charged_money") charged_money:Int,
    ) : Call<transport>

}