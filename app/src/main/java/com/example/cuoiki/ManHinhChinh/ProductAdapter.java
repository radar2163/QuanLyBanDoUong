package com.example.cuoiki.ManHinhChinh;

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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,###");

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //  Inflate layout item_product.xml cho mỗi item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        String formattedPrice = currencyFormat.format(product.getPrice()) + " đ";
        holder.productPrice.setText(formattedPrice);
        holder.productQuantity.setText("SL: " + product.getQuantity() + " " + product.getInit());

        // Clear ảnh cũ trước khi load ảnh mới (tránh hiển thị ảnh sai khi RecyclerView reuse view)
        holder.productImage.setImageDrawable(null);
        
        //  Load ảnh bằng Glide (xử lý cả URL, URI, drawable) - đồng bộ với CustomerProductAdapter
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
    }


    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    //  ViewHolder lưu các view trong layout item_product.xml
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
        }
    }

    //  Cập nhật dữ liệu khi cần
    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}
