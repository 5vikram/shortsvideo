package com.multitv.ott.shortvideo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multitv.ott.shortvideo.ContentItem
import com.multitv.ott.shortvideo.R
import com.multitv.ott.shortvideo.listener.OnLoadMoreListener
import com.multitv.ott.shortvideo.listener.ShareVideoListener
import com.multitv.ott.shortvideo.utils.Tracer
import de.hdodenhof.circleimageview.CircleImageView

class ShortsVideoAdapter(
    private val context: Context,
    private val videoCacheUrlList: List<ContentItem>,
    recyclerView: RecyclerView,
    private val onLoadMoreListener: OnLoadMoreListener,
    private val shareVideoListener: ShareVideoListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


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
            val shareImageView = playerLayout.findViewById<ImageView>(R.id.shareImageView)
            shareImageView.setOnClickListener {
                if (!videoCacheUrlList[adapterPosition].shareUrl.isNullOrEmpty()) shareVideoListener.shareVideo(
                    videoCacheUrlList[adapterPosition].shareUrl!!
                )
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
                    context, contentHome.sku
                )
                contentAdapterRecyclerview.setAdapter(homeContentAdapter)
                //contentAdapterRecyclerview.visibility = View.VISIBLE
            } else {
                contentAdapterRecyclerview.visibility = View.GONE
            }

            val videoTitleTv_new = playerLayout.findViewById<TextView>(R.id.videoTitleTv)
            val viewCountTv = playerLayout.findViewById<TextView>(R.id.viewCountTv)
            val videoImageView = playerLayout.findViewById<ImageView>(R.id.videoImageView)

            if (contentHome.title != null && !TextUtils.isEmpty(contentHome.title)) {
                videoTitleTv_new.text = contentHome.title
                videoTitleTv_new.visibility = View.VISIBLE
            } else {
                videoTitleTv_new.visibility = View.GONE
            }

            if (contentHome.watch != null && !TextUtils.isEmpty(contentHome.watch)) viewCountTv.text =
                contentHome.watch

            if (contentHome.layoutThumbs != null && contentHome.layoutThumbs.size > 0 && contentHome.layoutThumbs.get(
                    0
                ).imageSize.size > 0 && !contentHome.layoutThumbs.get(0).imageSize.get(0).url.isNullOrEmpty()
            ) {
                Glide.with(context)
                    .load(contentHome.layoutThumbs.get(0).imageSize.get(0).url)
                    .error(R.color.black)
                    .into(videoImageView)
            }


        }
    }

}