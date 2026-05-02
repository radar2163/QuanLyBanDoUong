package com.example.cuoiki.KhachHang;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView favoriteRecyclerView;
    private SQLiteHelper dbHelper;
    private List<Product> favoriteList;
    private CustomerProductAdapter adapter;
    private TextView tvEmptyFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        loadFavoriteProducts();
    }

    private void initViews() {
        favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
        tvEmptyFavorite = findViewById(R.id.tvEmptyFavorite);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        dbHelper = new SQLiteHelper(this);
        favoriteList = new ArrayList<>();
        
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        adapter = new CustomerProductAdapter(this, favoriteList, new CustomerProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Mở màn hình chi tiết sản phẩm
                android.content.Intent intent = new android.content.Intent(FavoriteActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product);
            }
        });
        
        // Lắng nghe thay đổi yêu thích để tự động refresh danh sách
        adapter.setOnFavoriteChangeListener(() -> {
            loadFavoriteProducts(); // Reload danh sách khi có thay đổi yêu thích
        });
        
        favoriteRecyclerView.setAdapter(adapter);
    }

    private void loadFavoriteProducts() {
        favoriteList.clear();
        SharedPreferences prefs = getSharedPreferences("favorite_prefs", MODE_PRIVATE);
        
        // Lấy tất cả sản phẩm từ database
        List<Product> allProducts = dbHelper.getAllProducts();
        
        for (Product product : allProducts) {
            boolean isFavorite = prefs.getBoolean("favorite_" + product.getId(), false);
            if (isFavorite) {
                favoriteList.add(product);
            }
        }
        
        adapter.updateData(favoriteList);
        
        // Hiển thị thông báo nếu danh sách trống
        if (favoriteList.isEmpty()) {
            tvEmptyFavorite.setVisibility(View.VISIBLE);
            favoriteRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyFavorite.setVisibility(View.GONE);
            favoriteRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void addToCart(Product product) {
        if (product.getQuantity() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String cartKey = "cart_item_" + product.getId();
        int currentQuantity = prefs.getInt(cartKey, 0);
        editor.putInt(cartKey, currentQuantity + 1);
        editor.apply();
        
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload danh sách yêu thích khi quay lại (có thể đã bỏ yêu thích ở màn hình khác)
        loadFavoriteProducts();
    }
}

