package com.example.cuoiki.KhachHang;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;

import android.net.Uri;
import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartActivity.CartItem> cartItems;
    private OnCartItemChangeListener listener;
    private Context context;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(Context context, List<CartActivity.CartItem> cartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartActivity.CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();
        int quantity = cartItem.getQuantity();

        holder.productName.setText(product.getName());
        
        // Hiển thị đơn vị (lon, thùng, chai)
        if (product.getInit() != null && !product.getInit().isEmpty()) {
            holder.productUnit.setText(product.getInit());
            holder.productUnit.setVisibility(View.VISIBLE);
        } else {
            holder.productUnit.setVisibility(View.GONE);
        }
        
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        double itemTotal = product.getPrice() * quantity;
        holder.productPrice.setText(currencyFormat.format(itemTotal) + " đ");
        holder.productQuantity.setText("x" + quantity);
        holder.quantityText.setText(String.valueOf(quantity));

        // Clear ảnh cũ trước khi load ảnh mới
        holder.productImage.setImageDrawable(null);
        
        //  Load ảnh bằng Glide (xử lý cả URL, URI, drawable) - đồng bộ với các adapter khác
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            // Parse URI để xử lý content:// hoặc file://
            Uri imageUri = Uri.parse(product.getImageUri());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.no_image) // ảnh mặc định khi đang load hoặc lỗi
                    .error(R.drawable.no_image)
                    .fallback(R.drawable.no_image) // ảnh mặc định nếu URI null
                    .into(holder.productImage);
        } else if (product.getImageResId() != 0) {
            // Load ảnh từ drawable resource
            holder.productImage.setImageResource(product.getImageResId());
        } else {
            // Dùng ảnh mặc định
            holder.productImage.setImageResource(R.drawable.no_image);
        }

        // Tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity(); // Lấy số lượng hiện tại từ cartItem
            int maxQuantity = product.getQuantity(); // Số lượng tồn kho
            
            if (currentQuantity < maxQuantity) {
                int newQuantity = currentQuantity + 1;
                cartItem.setQuantity(newQuantity);
                updateCartItem(product.getId(), newQuantity);
                holder.quantityText.setText(String.valueOf(newQuantity));
                holder.productQuantity.setText("x" + newQuantity);
                double newTotal = product.getPrice() * newQuantity;
                holder.productPrice.setText(currencyFormat.format(newTotal) + " đ");
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            } else {
                android.widget.Toast.makeText(context, "Số lượng vượt quá tồn kho (Tối đa: " + maxQuantity + ")", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity(); // Lấy số lượng hiện tại từ cartItem
            if (currentQuantity > 1) {
                int newQuantity = currentQuantity - 1;
                cartItem.setQuantity(newQuantity);
                updateCartItem(product.getId(), newQuantity);
                holder.quantityText.setText(String.valueOf(newQuantity));
                holder.productQuantity.setText("x" + newQuantity);
                double newTotal = product.getPrice() * newQuantity;
                holder.productPrice.setText(currencyFormat.format(newTotal) + " đ");
                if (listener != null) {
                    listener.onQuantityChanged();
                }
            }
        });

        // Xóa sản phẩm
        holder.btnRemove.setOnClickListener(v -> {
            removeCartItem(product.getId());
            if (listener != null) {
                listener.onItemRemoved(position);
            }
        });
    }

    private void updateCartItem(int productId, int quantity) {
        SharedPreferences prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("cart_item_" + productId, quantity);
        editor.apply();
    }

    private void removeCartItem(int productId) {
        SharedPreferences prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("cart_item_" + productId);
        editor.apply();
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity, quantityText, productUnit;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productUnit = itemView.findViewById(R.id.productUnit);
            quantityText = itemView.findViewById(R.id.quantityText);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

