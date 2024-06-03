package com.multitv.ott.shortvideo.model


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("about_me")
    var aboutMe: String?,
    @SerializedName("address")
    var address: String?,
    @SerializedName("age_group")
    var ageGroup: String?,
    @SerializedName("badge")
    var badge: String?,
    @SerializedName("contact_no")
    var contactNo: String?,
    @SerializedName("email")
    var email: String?,
    @SerializedName("first_name")
    var firstName: String?,
    @SerializedName("gender")
    var gender: String?,
    @SerializedName("id")
    var id: Int?,
    @SerializedName("image")
    var image: String?,
    @SerializedName("is_follow")
    var isFollow: Int?,
    @SerializedName("last_name")
    var lastName: String?,
    @SerializedName("lat")
    var lat: String?,
    @SerializedName("long")
    var long: String?,
    @SerializedName("otp")
    var otp: String?,
    @SerializedName("star")
    var star: String?,
    @SerializedName("status")
    var status: String?,
    @SerializedName("total_followers")
    var totalFollowers: Int?,
    @SerializedName("total_following")
    var totalFollowing: Int?,
    @SerializedName("total_likes")
    var totalLikes: Int?,
    @SerializedName("total_video")
    var totalVideo: Int?,
    @SerializedName("username")
    var username: String?
)