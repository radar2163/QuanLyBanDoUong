package com.example.cuoiki.KhachHang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class CustomerSearchActivity extends AppCompatActivity {

    private AutoCompleteTextView edtSearch;
    private ImageView btnBack, btnClear;
    private RecyclerView recyclerView;
    private TextView tvSectionTitle;
    private CustomerProductAdapter adapter;
    private SQLiteHelper dbHelper;

    private List<Product> allProducts = new ArrayList<>();
    private List<String> nameSuggestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_search);

        initViews();
        loadAllProducts();
        setupSearch();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerSearch);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);

        dbHelper = new SQLiteHelper(this);

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            recyclerView.setVisibility(View.GONE);
            tvSectionTitle.setVisibility(View.GONE);
        });

        // RecyclerView hiển thị kết quả tìm kiếm
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CustomerProductAdapter(this, new ArrayList<>(), new CustomerProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(CustomerSearchActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadAllProducts() {
        allProducts = dbHelper.getAllProducts();
        for (Product p : allProducts) {
            nameSuggestions.add(p.getName());
        }
    }

    private void setupSearch() {
        // Gợi ý tên sản phẩm khi nhập
        ArrayAdapter<String> suggestAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, nameSuggestions);
        edtSearch.setAdapter(suggestAdapter);
        edtSearch.setThreshold(1); // Bắt đầu gợi ý từ ký tự đầu tiên

        // Khi chọn từ gợi ý
        edtSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            edtSearch.setText(selectedText);
            filterProducts(selectedText);
        });

        // Khi gõ tự động tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvSectionTitle.setVisibility(View.GONE);
                    btnClear.setVisibility(View.GONE);
                } else {
                    btnClear.setVisibility(View.VISIBLE);
                    filterProducts(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String keyword) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(p);
            }
        }

        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvSectionTitle.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvSectionTitle.setVisibility(View.VISIBLE);
            adapter.updateData(filtered);
        }
    }

    private void addToCart(Product product) {
        if (product.getQuantity() <= 0) {
            android.widget.Toast.makeText(this, "Sản phẩm đã hết hàng", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String cartKey = "cart_item_" + product.getId();
        int currentQuantity = prefs.getInt(cartKey, 0);
        editor.putInt(cartKey, currentQuantity + 1);
        editor.apply();
        
        android.widget.Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
    }
}

