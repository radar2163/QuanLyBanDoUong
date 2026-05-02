package com.example.cuoiki.QuanLyBanner;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Banner;
import com.bumptech.glide.Glide;

import java.util.List;

public class BannerManageAdapter extends RecyclerView.Adapter<BannerManageAdapter.BannerViewHolder> {

    private List<Banner> bannerList;
    private Context context;
    private OnBannerActionListener listener;

    public interface OnBannerActionListener {
        void onEdit(Banner banner);
        void onDelete(Banner banner);
        void onToggleActive(Banner banner, boolean isActive);
    }

    public BannerManageAdapter(Context context, List<Banner> bannerList, OnBannerActionListener listener) {
        this.context = context;
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);

        // Hiển thị ảnh banner
        if (banner.getImageUri() != null && !banner.getImageUri().isEmpty()) {
            Uri imageUri = Uri.parse(banner.getImageUri());
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.imgBanner);
        } else if (banner.getImageResId() != 0) {
            holder.imgBanner.setImageResource(banner.getImageResId());
        } else {
            holder.imgBanner.setImageResource(R.drawable.no_image);
        }

        holder.tvOrder.setText("Thứ tự: " + banner.getDisplayOrder());
        holder.switchActive.setChecked(banner.isActive());

        // Click sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(banner);
            }
        });

        // Click xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(banner);
            }
        });

        // Toggle active
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleActive(banner, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerList != null ? bannerList.size() : 0;
    }

    public void updateList(List<Banner> newList) {
        this.bannerList = newList;
        notifyDataSetChanged();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView tvOrder;
        Switch switchActive;
        Button btnEdit, btnDelete;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            switchActive = itemView.findViewById(R.id.switchActive);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

