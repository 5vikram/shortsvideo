package com.multitv.ott.shortvideo.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.multitv.ott.shortvideo.R
import com.multitv.ott.shortvideo.SkuItem
import com.multitv.ott.shortvideo.WebViewActivity

class SkuAdapter(
    private val context: Context,
    private val categoryContentArrayList: List<SkuItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.sku_adapter, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as ItemViewHolder
        val contentHome = categoryContentArrayList[position]

        /*int widthAndHeightOfIcon = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());
        int viewHeight = 0;
        int viewWidht = 0;

        viewWidht = ScreenUtils.INSTANCE.getScreenWidth(context) / 2 - widthAndHeightOfIcon;
        viewHeight = viewWidht / 9 * 16 - 40;

        RecyclerView.LayoutParams buttonLayoutParams = new RecyclerView.LayoutParams(viewWidht, viewHeight);
        buttonLayoutParams.setMargins(0, 0, 10, 0);
        itemViewHolder.cardView.setLayoutParams(buttonLayoutParams);*/
        var presentAmount = 0

        if (contentHome.price != null && !TextUtils.isEmpty(contentHome.price) && contentHome.discount != null && !TextUtils.isEmpty(
                contentHome.discount
            )
        ) {
            if (contentHome.price.toInt() > 0 && contentHome.discount.toInt() > 0) {
                presentAmount = contentHome.price.toInt() - contentHome.discount.toInt()
            }
        }

        if (contentHome.image != null && !TextUtils.isEmpty(contentHome.image)) loadImageUrl(
            itemViewHolder,
            contentHome.image
        )
        else itemViewHolder.thumbnailIc.setImageResource(R.color.black)

        if (contentHome.title != null && !TextUtils.isEmpty(contentHome.title)) {
            itemViewHolder.titleTv.text = contentHome.title
            itemViewHolder.titleTv.visibility = View.VISIBLE
        } else {
            itemViewHolder.titleTv.text = context.getString(R.string.app_name)
        }

        if (contentHome.price != null && !TextUtils.isEmpty(contentHome.price)) {
            itemViewHolder.priceTv.text = "AED " + contentHome.price
            itemViewHolder.priceTv.visibility = View.VISIBLE
            itemViewHolder.priceTv.paintFlags =
                itemViewHolder.titleTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            itemViewHolder.priceTv.visibility = View.GONE
        }


        if (presentAmount > 0) {
            itemViewHolder.discountPriceTv.text = "AED $presentAmount"
        } else {
            if (contentHome.discount != null && !TextUtils.isEmpty(contentHome.discount)) itemViewHolder.discountPriceTv.text =
                contentHome.discount + " off"
        }

        itemViewHolder.adTobagTv.setOnClickListener {
            if (contentHome.redirectUrl != null && !TextUtils.isEmpty(contentHome.redirectUrl)) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("title", contentHome.title)
                intent.putExtra("url", contentHome.redirectUrl)
                context.startActivity(intent)
            }
        }
    }

    private fun loadImageUrl(itemViewHolder: ItemViewHolder, url: String?) {
        Glide.with(context)
            .load(url)
            .error(R.color.black)
            .into(itemViewHolder.thumbnailIc)
    }

    override fun getItemCount(): Int {
        return categoryContentArrayList.size
    }

    internal inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnailIc: ImageView = view.findViewById(R.id.thumbnailIc)
        private val cardView: CardView = view.findViewById(R.id.cardView)

        val titleTv: TextView = view.findViewById(R.id.titleTv)
        private val descriptionTv: TextView = view.findViewById(R.id.descriptionTv)
        val priceTv: TextView = view.findViewById(R.id.priceTv)
        val discountPriceTv: TextView = view.findViewById(R.id.discountPriceTv)
        val adTobagTv: TextView = view.findViewById(R.id.adTobagTv)
    }
}
