package com.multitv.ott.shortvideo.model

import com.google.gson.annotations.SerializedName

data class UserBehivourData(

	@field:SerializedName("result")
	val result: Data? = null,

	@field:SerializedName("code")
	val code: Int? = null
)

data class Data(

	@field:SerializedName("is_favorite")
	val isFavorite: Int? = null,

	@field:SerializedName("is_follow")
	val isFollow: Int? = null,

	@field:SerializedName("is_like")
	val isLike: Int? = null,

	@field:SerializedName("is_abuse")
	val isAbuse: Int? = null,

	@field:SerializedName("is_subscriber")
	val isSubscriber: Int? = null
)
