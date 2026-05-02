package com.example.cuoiki.QuanLySanPham;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.bumptech.glide.Glide;

import android.net.Uri;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);

        void onDelete(Product product);
    }

    public ManageProductAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText("Giá: " + formatter.format(product.getPrice()) + " VNĐ");

        // Clear ảnh cũ trước khi load ảnh mới (tránh hiển thị ảnh sai khi RecyclerView reuse view)
        holder.imgProduct.setImageDrawable(null);
        
        //  Load ảnh bằng Glide (xử lý cả URL, URI, drawable) - đồng bộ với CustomerProductAdapter
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            // Parse URI để xử lý content:// hoặc file://
            Uri imageUri = Uri.parse(product.getImageUri());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.no_image) // ảnh mặc định khi đang load hoặc lỗi
                    .error(R.drawable.no_image)
                    .fallback(R.drawable.no_image) // ảnh mặc định nếu URI null
                    .into(holder.imgProduct);
        } else if (product.getImageResId() != 0) {
            // Load ảnh từ drawable resource
            holder.imgProduct.setImageResource(product.getImageResId());
        } else {
            // Dùng ảnh mặc định
            holder.imgProduct.setImageResource(R.drawable.no_image);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }


    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvCategory, tvPrice;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);   // ✅ thêm dòng này
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
