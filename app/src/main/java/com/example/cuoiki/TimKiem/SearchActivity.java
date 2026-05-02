package com.example.cuoiki.TimKiem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.ManHinhChinh.MainActivity;
import com.example.cuoiki.ManHinhChinh.ProductAdapter;
import com.example.cuoiki.HoSo.ProfileActivity;
import com.example.cuoiki.ThongKe.StaticActivity;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Product;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private AutoCompleteTextView edtSearch;
    private Button btnSearch, btnCancel;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private SQLiteHelper dbHelper;

    private List<Product> allProducts = new ArrayList<>();
    private List<String> nameSuggestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnCancel = findViewById(R.id.btnCancel);
        recyclerView = findViewById(R.id.recyclerSearch);

        dbHelper = new SQLiteHelper(this);

        //  Load toàn bộ sản phẩm từ DB
        allProducts = dbHelper.getAllProducts();
        for (Product p : allProducts) {
            nameSuggestions.add(p.getName());
        }

        //  Gợi ý tên sản phẩm khi nhập
        ArrayAdapter<String> suggestAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, nameSuggestions);
        edtSearch.setAdapter(suggestAdapter);
        edtSearch.setThreshold(1); // bắt đầu gợi ý từ ký tự đầu tiên

        //  RecyclerView hiển thị kết quả tìm kiếm
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(new ArrayList<>()); // khởi tạo rỗng ban đầu
        recyclerView.setAdapter(adapter);

        //  Khi gõ tự động tìm kiếm theo tên
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //  Khi nhấn nút TÌM
        btnSearch.setOnClickListener(v -> {
            String keyword = edtSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên sản phẩm cần tìm!", Toast.LENGTH_SHORT).show();
                return;
            }
            filterProducts(keyword);
        });

        //  Khi nhấn HỦY → reset
        btnCancel.setOnClickListener(v -> {
            edtSearch.setText("");
            adapter.updateData(new ArrayList<>());
        });

        //  Ẩn nút thống kê cho nhân viên
        setupRoleBasedUI();

        //  Bottom Navigation
        findViewById(R.id.btm_home).setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, com.example.cuoiki.ManHinhChinh.MainActivity.class));
            finish();
        });
        findViewById(R.id.btm_profile).setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, com.example.cuoiki.HoSo.ProfileActivity.class));
            finish();
        });
        findViewById(R.id.btm_search).setOnClickListener(v -> {
            // Đã ở màn hình Search rồi, không cần làm gì
        });
        findViewById(R.id.btm_static).setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, com.example.cuoiki.ThongKe.StaticActivity.class));
            finish();
        });
    }

    //  Hàm lọc danh sách sản phẩm theo tên (không phân biệt hoa thường)
    private void filterProducts(String keyword) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(p);
            }
        }

        if (filtered.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
        }

        adapter.updateData(filtered);
    }

    //  Thiết lập giao diện theo quyền người dùng
    private void setupRoleBasedUI() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        // Ẩn nút thống kê cho nhân viên
        if (!"admin".equalsIgnoreCase(userRole)) {
            findViewById(R.id.btm_static).setVisibility(android.view.View.GONE);
        }
    }

}
