package com.multitv.ott.shortvideo

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multitv.ott.shortvideo.listener.OnLoadMoreListener
import com.multitv.ott.shortvideo.listener.ShortVideoListener
import com.multitv.ott.shortvideo.utils.Tracer
import de.hdodenhof.circleimageview.CircleImageView

class ShortsVideoAdapter(
    private val context: Context,
    private val videoCacheUrlList: List<ContentItem>,
    var shortVideoListener: ShortVideoListener,
    recyclerView: RecyclerView,
    private val onLoadMoreListener: OnLoadMoreListener,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0

    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                totalItemCount = recyclerView.adapter!!.itemCount

                try {
                    lastVisibleItem =
                        recyclerView.getChildAdapterPosition(recyclerView.getChildAt(recyclerView.childCount - 1))
                } catch (e: Exception) {
                    Tracer.error("Home Adapter load more ===", "error:::: " + e.message)
                    lastVisibleItem = 0
                }

                if (totalItemCount == lastVisibleItem + 1) {
                    onLoadMoreListener.onLoadMore()
                }
            }
        })

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TicTocViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.adapter_tictoc_video_player, viewGroup, false)
        return TicTocViewHolder(v)
    }

    override fun getItemCount(): Int {
        return videoCacheUrlList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as TicTocViewHolder).onBind(context, videoCacheUrlList[position], position)
    }

    inner class TicTocViewHolder(private val playerLayout: View) :
        RecyclerView.ViewHolder(playerLayout) {

        init {
            val commentImageButton =
                playerLayout.findViewById<ImageView>(R.id.addCommentImageButton_new)

            commentImageButton.setOnClickListener {
                shortVideoListener.onCommentIconClicked(videoCacheUrlList[adapterPosition].id!!)
            }

            val likeButton = playerLayout.findViewById<ImageView>(R.id.like_img_new)
            val followLayout = playerLayout.findViewById<RelativeLayout>(R.id.followLayout)
            val share_img = playerLayout.findViewById<ImageView>(R.id.share_img_new)
            val likesCountTv = playerLayout.findViewById<TextView>(R.id.likeTv_new)
            val profilePic = playerLayout.findViewById<CircleImageView>(R.id.user_img_new)
            val addFollowImage = playerLayout.findViewById<ImageView>(R.id.add_follow_new)
            val followButtonTxt =
                playerLayout.findViewById<TextView>(R.id.followButtonTxt_new)

            likeButton.setOnClickListener {
                shortVideoListener.onVideoLiked(
                    videoCacheUrlList[adapterPosition].id!!,
                    videoCacheUrlList[adapterPosition].category!!,
                    likeButton,
                    likesCountTv,
                    adapterPosition
                )
            }

//            followLayout.setOnClickListener {
//                onShortVideoClickedListener.onFollowIconClicked()
//            }

            share_img.setOnClickListener {
                if (!videoCacheUrlList[adapterPosition].shareUrl.isNullOrEmpty())
                    shortVideoListener.onShareVideoIconClicked(videoCacheUrlList[adapterPosition].shareUrl!!)
            }

            followButtonTxt.setOnClickListener {
                shortVideoListener.onFollowIconClicked(
                    videoCacheUrlList[adapterPosition].userId!!,
                    videoCacheUrlList[adapterPosition].username!!,
                    followButtonTxt,
                    addFollowImage
                )
            }

            addFollowImage.setOnClickListener {
                shortVideoListener.onFollowIconClicked(
                    videoCacheUrlList[adapterPosition].userId!!,
                    videoCacheUrlList[adapterPosition].username!!,
                    followButtonTxt,
                    addFollowImage
                )
            }

            profilePic.setOnClickListener {
                shortVideoListener.onUserProfileClicked(videoCacheUrlList[adapterPosition].userId!!)
            }


        }

        @SuppressLint("SetTextI18n")
        fun onBind(context: Context, contentHome: ContentItem, position: Int) {

            val contentAdapterRecyclerview =
                playerLayout.findViewById<RecyclerView>(R.id.contentAdapterRecyclerview)


            if (contentHome.sku != null && contentHome.sku.size > 0) {
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                contentAdapterRecyclerview.setLayoutManager(linearLayoutManager)
                contentAdapterRecyclerview.setNestedScrollingEnabled(false)
                val homeContentAdapter = SkuAdapter(
                    context,
                    contentHome.sku
                )
                contentAdapterRecyclerview.setAdapter(homeContentAdapter)
                contentAdapterRecyclerview.visibility = View.VISIBLE
            } else {
                contentAdapterRecyclerview.visibility = View.GONE
            }

            val videoTitleTv_new = playerLayout.findViewById<TextView>(R.id.videoTitleTv_new)
            val descriptionTv_new = playerLayout.findViewById<TextView>(R.id.descriptionTv_new)
            val likeTv_new = playerLayout.findViewById<TextView>(R.id.likeTv_new)
            val usernameTextView_new =
                playerLayout.findViewById<TextView>(R.id.usernameTextView_new)
            val viewCountTv_new = playerLayout.findViewById<TextView>(R.id.viewCountTv_new)

            if (contentHome.title != null && !TextUtils.isEmpty(contentHome.title))
                videoTitleTv_new.text = contentHome.title

            if (contentHome.des != null && !TextUtils.isEmpty(contentHome.des))
                descriptionTv_new.text = contentHome.des

            if (contentHome.isLike != null && contentHome.isLike != -1)
                likeTv_new.text = "" + contentHome.isLike
            else
                likeTv_new.text = "0"

            if (contentHome.watch != null && !TextUtils.isEmpty(contentHome.watch))
                viewCountTv_new.text = contentHome.watch

            if (!TextUtils.isEmpty(contentHome.username))
                usernameTextView_new.text = contentHome.username
            else
                usernameTextView_new.text = "@${contentHome.firstName}"


        }
    }

}