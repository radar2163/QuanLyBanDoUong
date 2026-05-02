package com.example.cuoiki.KhachHang;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> banners; // Danh sách banner từ database

    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        
        // Load ảnh bằng Glide (hỗ trợ cả URI và resource ID)
        if (banner.getImageUri() != null && !banner.getImageUri().isEmpty()) {
            Uri imageUri = Uri.parse(banner.getImageUri());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.bannerImage);
        } else if (banner.getImageResId() != 0) {
            holder.bannerImage.setImageResource(banner.getImageResId());
        } else {
            holder.bannerImage.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
        }
    }
}

