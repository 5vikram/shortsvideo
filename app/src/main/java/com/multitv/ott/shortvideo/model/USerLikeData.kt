package com.multitv.ott.shortvideo.model

import com.google.gson.annotations.SerializedName

data class USerLikeData(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("action")
	val action: String? = null,

	@field:SerializedName("is_favorite")
	val isFavorite: Int? = null,

	@field:SerializedName("like_count")
	val likeCount: Int? = null,

	@field:SerializedName("is_like")
	val isLike: Int? = null,

	@field:SerializedName("is_abuse")
	val isAbuse: Int? = null,

	@field:SerializedName("favorite_count")
	val favoriteCount: Int? = null
)
