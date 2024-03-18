package com.multitv.ott.shortvideo

import com.google.gson.annotations.SerializedName

data class ShortVideo(

    @field:SerializedName("result")
    val result: Result? = null,

    @field:SerializedName("code")
    val code: Int? = null
)

data class SkuItem(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("price")
    val price: String? = null,

    @field:SerializedName("discount")
    val discount: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("redirect_url")
    val redirectUrl: String? = null
)

data class Thumb(

    @field:SerializedName("small")
    val small: String? = null,

    @field:SerializedName("large")
    val large: String? = null,

    @field:SerializedName("medium")
    val medium: String? = null
)

data class ContentItem(

    @field:SerializedName("is_favorite")
    val isFavorite: Int? = null,

    @field:SerializedName("source")
    val source: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("duration")
    val duration: String? = null,

    @field:SerializedName("uid")
    val uid: String? = null,

    @field:SerializedName("des")
    val des: String? = null,

    @field:SerializedName("media_type")
    val mediaType: String? = null,

    @field:SerializedName("is_like")
    val isLike: Int? = null,

    @field:SerializedName("genre")
    val genre: String? = null,

    @field:SerializedName("favorite_count")
    val favoriteCount: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("category_ids")
    val categoryIds: List<String?>? = null,

    @field:SerializedName("sku")
    val sku: List<SkuItem>? = null,

    @field:SerializedName("first_name")
    val firstName: String? = null,

    @field:SerializedName("like_count")
    val likeCount: String? = null,

    @field:SerializedName("created")
    val created: String? = null,

    @field:SerializedName("profile_pic")
    val profilePic: String? = null,

    @field:SerializedName("content_partner")
    val contentPartner: String? = null,

    @field:SerializedName("resolu_type")
    val resoluType: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("watch")
    val watch: String? = null,

    @field:SerializedName("share_url")
    val shareUrl: String? = null,

    @field:SerializedName("category")
    val category: String? = null,

    @field:SerializedName("username")
    val username: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("thumbs")
    val thumbs: List<ThumbsItem?>? = null
)

data class ThumbsItem(

    @field:SerializedName("thumb")
    val thumb: Thumb? = null,

    @field:SerializedName("name")
    val name: String? = null
)

data class Result(

    @field:SerializedName("offset")
    val offset: Int? = null,

    @field:SerializedName("totalCount")
    val totalCount: Int? = null,

    @field:SerializedName("version")
    val version: String? = null,

    @field:SerializedName("content")
    val content: List<ContentItem>? = null
)
