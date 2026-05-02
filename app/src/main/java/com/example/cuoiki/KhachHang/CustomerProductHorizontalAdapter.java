package com.example.cuoiki.KhachHang;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;

import java.text.DecimalFormat;
import java.util.List;

public class CustomerProductHorizontalAdapter extends RecyclerView.Adapter<CustomerProductHorizontalAdapter.ProductViewHolder> {

    private List<Product> productList;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private OnProductClickListener listener;
    private Context context;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCart(Product product);
    }

    public CustomerProductHorizontalAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_product_horizontal, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.productName.setText(product.getName());
        
        // Hiển thị đơn vị (lon, thùng, chai)
        if (product.getInit() != null && !product.getInit().isEmpty()) {
            holder.productUnit.setText(product.getInit());
            holder.productUnit.setVisibility(View.VISIBLE);
        } else {
            holder.productUnit.setVisibility(View.GONE);
        }
        
        // Format giá với ký hiệu ₫
        String formattedPrice = "₫" + currencyFormat.format(product.getPrice());
        holder.productPrice.setText(formattedPrice);
        
        // Hiển thị số lượng còn lại
        if (product.getQuantity() > 0) {
            holder.productQuantity.setText("Còn lại: " + product.getQuantity());
            holder.productQuantity.setVisibility(View.GONE);
            holder.btnAddToCart.setAlpha(1.0f);
            holder.btnAddToCart.setClickable(true);
        } else {
            holder.productQuantity.setText("Hết hàng");
            holder.productQuantity.setVisibility(View.VISIBLE);
            holder.btnAddToCart.setAlpha(0.5f);
            holder.btnAddToCart.setClickable(false);
        }

        // Clear ảnh cũ trước khi load ảnh mới
        holder.productImage.setImageDrawable(null);
        
        // Load ảnh bằng Glide
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            Uri imageUri = Uri.parse(product.getImageUri());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .fallback(R.drawable.no_image)
                    .into(holder.productImage);
        } else if (product.getImageResId() != 0) {
            holder.productImage.setImageResource(product.getImageResId());
        } else {
            holder.productImage.setImageResource(R.drawable.no_image);
        }

        // Click vào sản phẩm để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Click icon thêm vào giỏ
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null && product.getQuantity() > 0) {
                listener.onAddToCart(product);
            }
        });

        // Kiểm tra và hiển thị trạng thái yêu thích
        SharedPreferences prefs = context.getSharedPreferences("favorite_prefs", Context.MODE_PRIVATE);
        boolean isFavorite = prefs.getBoolean("favorite_" + product.getId(), false);
        updateFavoriteIcon(holder.btnFavorite, isFavorite);
        
        // Click icon yêu thích
        holder.btnFavorite.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            boolean currentFavorite = prefs.getBoolean("favorite_" + product.getId(), false);
            
            if (currentFavorite) {
                editor.putBoolean("favorite_" + product.getId(), false);
                updateFavoriteIcon(holder.btnFavorite, false);
            } else {
                editor.putBoolean("favorite_" + product.getId(), true);
                updateFavoriteIcon(holder.btnFavorite, true);
            }
            editor.apply();
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, btnAddToCart, btnFavorite;
        TextView productName, productPrice, productQuantity, productOldPrice, productUnit;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productOldPrice = itemView.findViewById(R.id.productOldPrice);
            productUnit = itemView.findViewById(R.id.productUnit);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
    
    // Hàm cập nhật màu icon yêu thích
    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        if (isFavorite) {
            icon.setImageResource(R.drawable.ic_favorite);
            icon.setColorFilter(android.graphics.Color.parseColor("#FF6B6B"));
        } else {
            icon.setImageResource(R.drawable.ic_favorite_outline);
            icon.setColorFilter(android.graphics.Color.parseColor("#CCCCCC"));
        }
    }
}

