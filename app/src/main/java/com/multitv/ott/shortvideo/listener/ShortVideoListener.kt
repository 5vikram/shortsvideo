package com.multitv.ott.shortvideo.listener

import android.widget.ImageView
import android.widget.TextView

interface ShortVideoListener {

    fun onFollowIconClicked(
        u_id: String,
        userName: String,
        regularTextView: TextView,
        imageView: ImageView
    )

    fun onUserProfileClicked(u_id: String)
    fun onVideoLiked(
        c_id: String,
        contentName: String,
        imageView: ImageView,
        likesCountTv: TextView,
        position: Int
    )

    fun onCommentIconClicked(c_id: String)
    fun onCommentDelete(c_id: String, comment_id: String)
    fun onShareVideoIconClicked(shareUrl: String)


}