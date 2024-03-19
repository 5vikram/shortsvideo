package com.multitv.ott.shortvideo.model

import com.google.gson.annotations.SerializedName

data class AuthModel(
    @field:SerializedName("code")
    val code: Int? = null,

    @field:SerializedName("master_urls")
    val masterUrls: MasterUrl? = null,

    )

data class MasterUrl(
    @field:SerializedName("content_list")
    val contentList: String? = null,

    @field:SerializedName("content_detail")
    val content_detail: String? = null,
)
