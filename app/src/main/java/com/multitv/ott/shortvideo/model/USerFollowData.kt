package com.multitv.ott.shortvideo.model

import com.google.gson.annotations.SerializedName

data class USerFollowData(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("action")
	val isFollow: String? = null,

	@field:SerializedName("follower_count")
	val followerCount: Int? = null
)
