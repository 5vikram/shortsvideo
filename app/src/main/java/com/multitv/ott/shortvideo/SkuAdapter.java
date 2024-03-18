package com.multitv.ott.shortvideo;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SkuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SkuItem> categoryContentArrayList;
    private Context context;


    public SkuAdapter(Context context, List<SkuItem> categoryContentArrayList) {
        this.categoryContentArrayList = categoryContentArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sku_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        SkuItem contentHome = categoryContentArrayList.get(position);

        /*int widthAndHeightOfIcon = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());
        int viewHeight = 0;
        int viewWidht = 0;

        viewWidht = ScreenUtils.INSTANCE.getScreenWidth(context) / 2 - widthAndHeightOfIcon;
        viewHeight = viewWidht / 9 * 16 - 40;

        RecyclerView.LayoutParams buttonLayoutParams = new RecyclerView.LayoutParams(viewWidht, viewHeight);
        buttonLayoutParams.setMargins(0, 0, 10, 0);
        itemViewHolder.cardView.setLayoutParams(buttonLayoutParams);*/

        int presentAmount = 0;

        if (contentHome.getPrice() != null && !TextUtils.isEmpty(contentHome.getPrice()) && contentHome.getDiscount() != null && !TextUtils.isEmpty(contentHome.getDiscount())) {
            if (Integer.parseInt(contentHome.getPrice()) > 0 && Integer.parseInt(contentHome.getDiscount()) > 0) {
                presentAmount = Integer.parseInt(contentHome.getPrice()) - Integer.parseInt(contentHome.getDiscount());
            }
        }

        if (contentHome.getImage() != null && !TextUtils.isEmpty(contentHome.getImage()))
            loadImageUrl(itemViewHolder, contentHome.getImage());
        else
            itemViewHolder.thumbnailIc.setImageResource(R.color.black);

        if (contentHome.getTitle() != null && !TextUtils.isEmpty(contentHome.getTitle())) {
            itemViewHolder.titleTv.setText(contentHome.getTitle());
        } else
            itemViewHolder.titleTv.setText(context.getString(R.string.app_name));

        if (contentHome.getPrice() != null && !TextUtils.isEmpty(contentHome.getPrice())) {
            itemViewHolder.priceTv.setText("Rs " + contentHome.getPrice());
            itemViewHolder.priceTv.setPaintFlags(itemViewHolder.titleTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }


        if (presentAmount > 0) {
            itemViewHolder.discountPriceTv.setText("Rs " + presentAmount);
        } else {
            if (contentHome.getDiscount() != null && !TextUtils.isEmpty(contentHome.getDiscount()))
                itemViewHolder.discountPriceTv.setText(contentHome.getDiscount() + " off");
        }

        itemViewHolder.adTobagTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                if (contentHome.getRedirectUrl() != null && !TextUtils.isEmpty(contentHome.getRedirectUrl())) {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("title", contentHome.getTitle());
                    intent.putExtra("url", contentHome.getRedirectUrl());
                    context.startActivity(intent);
                }
*/
            }
        });


    }

    private void loadImageUrl(ItemViewHolder itemViewHolder, String url) {

        Glide.with(context)
                .load(url)
                .error(R.color.black)
                .into(itemViewHolder.thumbnailIc);
    }

    @Override
    public int getItemCount() {
        return categoryContentArrayList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumbnailIc;
        private final CardView cardView;

        private final TextView titleTv;
        private final TextView descriptionTv;
        private final TextView priceTv;
        private final TextView discountPriceTv;
        private TextView adTobagTv;

        public ItemViewHolder(View view) {
            super(view);
            thumbnailIc = view.findViewById(R.id.thumbnailIc);
            cardView = view.findViewById(R.id.cardView);

            titleTv = view.findViewById(R.id.titleTv);
            adTobagTv = view.findViewById(R.id.adTobagTv);
            descriptionTv = view.findViewById(R.id.descriptionTv);
            priceTv = view.findViewById(R.id.priceTv);
            discountPriceTv = view.findViewById(R.id.discountPriceTv);

        }
    }

}
