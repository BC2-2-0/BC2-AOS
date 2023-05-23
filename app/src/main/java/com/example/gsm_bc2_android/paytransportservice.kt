package com.example.gsm_bc2_android

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface paytransportservice {
    @FormUrlEncoded
    @POST("/purchase")
    fun requestLogin(
        @Field("email") email:String,
        @Field("balance") balance:Int,
        @Field("menu") menu:String,
        @Field("price") price:Int,
        @Field("quantity") quantity:Int,
        @Field("number") number:Int
    ) : Call<transport>

}