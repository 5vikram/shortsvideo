package com.multitv.ott.shortvideo.vikrammodel


import com.google.gson.annotations.SerializedName

data class Vikram(
    @SerializedName("code")
    var code: Int?,
    @SerializedName("result")
    var result: Result?
) {
    data class Result(
        @SerializedName("content")
        var content: List<Content?>?,
        @SerializedName("offset")
        var offset: Int?,
        @SerializedName("totalCount")
        var totalCount: Int?,
        @SerializedName("version")
        var version: String?
    ) {
        data class Content(
            @SerializedName("category")
            var category: String?,
            @SerializedName("category_ids")
            var categoryIds: List<String?>?,
            @SerializedName("content_partner")
            var contentPartner: String?,
            @SerializedName("created")
            var created: String?,
            @SerializedName("des")
            var des: String?,
            @SerializedName("duration")
            var duration: String?,
            @SerializedName("favorite_count")
            var favoriteCount: String?,
            @SerializedName("first_name")
            var firstName: String?,
            @SerializedName("genre")
            var genre: String?,
            @SerializedName("id")
            var id: String?,
            @SerializedName("is_favorite")
            var isFavorite: Int?,
            @SerializedName("is_like")
            var isLike: Int?,
            @SerializedName("layout_thumbs")
            var layoutThumbs: List<LayoutThumb?>?,
            @SerializedName("like_count")
            var likeCount: String?,
            @SerializedName("media_type")
            var mediaType: String?,
            @SerializedName("profile_pic")
            var profilePic: String?,
            @SerializedName("resolu_type")
            var resoluType: String?,
            @SerializedName("share_url")
            var shareUrl: String?,
            @SerializedName("sku")
            var sku: List<Sku?>?,
            @SerializedName("source")
            var source: String?,
            @SerializedName("status")
            var status: String?,
            @SerializedName("thumbs")
            var thumbs: Any?,
            @SerializedName("title")
            var title: String?,
            @SerializedName("type")
            var type: String?,
            @SerializedName("uid")
            var uid: String?,
            @SerializedName("url")
            var url: String?,
            @SerializedName("user_id")
            var userId: String?,
            @SerializedName("username")
            var username: String?,
            @SerializedName("watch")
            var watch: String?
        ) {
            data class LayoutThumb(
                @SerializedName("id")
                var id: String?,
                @SerializedName("image_size")
                var imageSize: List<ImageSize?>?,
                @SerializedName("layout")
                var layout: String?
            ) {
                data class ImageSize(
                    @SerializedName("height")
                    var height: String?,
                    @SerializedName("identifier")
                    var identifier: String?,
                    @SerializedName("url")
                    var url: String?,
                    @SerializedName("width")
                    var width: String?
                )
            }

            data class Sku(
                @SerializedName("discount")
                var discount: String?,
                @SerializedName("image")
                var image: String?,
                @SerializedName("price")
                var price: String?,
                @SerializedName("redirect_url")
                var redirectUrl: String?,
                @SerializedName("title")
                var title: String?
            )
        }
    }
}