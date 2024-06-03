package com.multitv.ott.shortvideo.model


import com.google.gson.annotations.SerializedName

data class MobileLoginData(
    @SerializedName("code")
    var code: Int?,
    @SerializedName("result")
    var result: Result?
)