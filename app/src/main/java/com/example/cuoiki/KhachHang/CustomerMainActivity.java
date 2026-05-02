package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.Banner;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DangNhap.LoginActivity;
import com.example.cuoiki.KhachHang.CustomerProductAdapter;
import com.example.cuoiki.KhachHang.CartActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.widget.LinearLayout;

public class CustomerMainActivity extends AppCompatActivity {

    private RecyclerView productRecyclerView;
    private SQLiteHelper dbHelper;
    private List<Product> productList;
    private CustomerProductAdapter adapter;
    
    // Banner slider
    private ViewPager2 bannerViewPager;
    private LinearLayout bannerIndicators;
    private BannerAdapter bannerAdapter;
    private List<Banner> bannerList;
    
    // Header views
    private TextView tvWelcome, btnLoginHeader, cartBadge;
    private ImageView btnCartHeader, btnLogout, btnNotification, btnCloseBanner;
    private Button btnLogin;
    private android.view.View personalizedBanner;
    
    // Category buttons
    private Button btnAll, btnBeer, btnWine, btnSoftDrink, btnWater, btnOther;
    
    // Bottom navigation
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        // Khởi tạo dbHelper trước
        dbHelper = new SQLiteHelper(this);
        
        initViews();
        setupBannerSlider();
        setupRecyclerView();
        loadProducts();
        setupCategoryFilters();
        checkLoginStatus();
        updateCartBadge(); // Cập nhật badge khi khởi tạo
    }

    private void initViews() {
        // Header
        tvWelcome = findViewById(R.id.tvWelcome);
        btnCartHeader = findViewById(R.id.btnCartHeader);
        cartBadge = findViewById(R.id.cartBadge);
        btnNotification = findViewById(R.id.btnNotification);
        btnLogout = findViewById(R.id.btnLogout);
        btnLoginHeader = findViewById(R.id.btnLoginHeader);
        btnLogin = findViewById(R.id.btnLogin);
        personalizedBanner = findViewById(R.id.personalizedBanner);
        btnCloseBanner = findViewById(R.id.btnCloseBanner);
        
        // Category buttons
        btnAll = findViewById(R.id.btnAll);
        btnBeer = findViewById(R.id.btnBeer);
        btnWine = findViewById(R.id.btnWine);
        btnSoftDrink = findViewById(R.id.btnSoftDrink);
        btnWater = findViewById(R.id.btnWater);
        btnOther = findViewById(R.id.btnOther);
        
        // Bottom navigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Click listeners
        btnCartHeader.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });
        
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerNotificationActivity.class);
            startActivity(intent);
        });
        
        // Click icon search trong header - mở màn hình tìm kiếm
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            // onResume() sẽ tự động cập nhật trạng thái đăng nhập khi quay lại
        });
        
        // Nút đăng nhập trong header
        btnLoginHeader.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            // onResume() sẽ tự động cập nhật trạng thái đăng nhập khi quay lại
        });
        
        // Icon đăng xuất
        btnLogout.setOnClickListener(v -> {
            // Đăng xuất - xóa thông tin đăng nhập
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("logged_user");
            editor.remove("user_role");
            editor.remove("full_name");
            editor.remove("phone_number");
            // Giữ lại saved_user, saved_pass, remember_login để người dùng có thể đăng nhập lại nhanh
            editor.apply();
            
            // Không chuyển đến màn hình đăng nhập, chỉ refresh lại màn hình hiện tại
            // checkLoginStatus() sẽ tự động cập nhật giao diện (hiển thị banner đăng nhập, nút đăng nhập)
            checkLoginStatus();
        });
        
        btnCloseBanner.setOnClickListener(v -> {
            personalizedBanner.setVisibility(View.GONE);
        });
        
        // Bottom navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(this, CustomerSearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_favorite) {
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_cart) {
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String loggedUser = prefs.getString("logged_user", null);
                if (loggedUser != null) {
                    // Đã đăng nhập - kiểm tra role
                    String userRole = prefs.getString("user_role", "");
                    if ("Admin".equalsIgnoreCase(userRole) || "Nhân viên".equalsIgnoreCase(userRole)) {
                        // Admin/Nhân viên → chuyển đến MainActivity
                        Intent intent = new Intent(this, com.example.cuoiki.ManHinhChinh.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Khách hàng → chuyển đến CustomerProfileActivity
                        Intent intent = new Intent(this, CustomerProfileActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });
    }

    private void setupBannerSlider() {
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerIndicators = findViewById(R.id.bannerIndicators);
        
        // Load banner từ database
        loadBannersFromDatabase();
        
        if (bannerList == null || bannerList.isEmpty()) {
            // Nếu không có banner trong database, tạo banner mẫu
            bannerList = new ArrayList<>();
            Banner defaultBanner = new Banner(0, null, R.drawable.no_image, 1, true);
            bannerList.add(defaultBanner);
        }
        
        bannerAdapter = new BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);
        
        // Tạo indicators (chấm tròn)
        setupIndicators();
        
        // Auto-scroll banner (chỉ khi có nhiều hơn 1 banner)
        if (bannerList.size() > 1) {
            startAutoScroll();
        }
        
        // Cập nhật indicators khi scroll
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
    }
    
    private void loadBannersFromDatabase() {
        if (dbHelper != null) {
            bannerList = dbHelper.getAllActiveBanners();
            if (bannerList == null) {
                bannerList = new ArrayList<>();
            }
        } else {
            bannerList = new ArrayList<>();
        }
    }
    
    private void setupIndicators() {
        if (bannerIndicators == null || bannerList == null) return;
        
        bannerIndicators.removeAllViews();
        
        for (int i = 0; i < bannerList.size(); i++) {
            View indicator = new View(this);
            int size = 8;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.indicator_unselected);
            bannerIndicators.addView(indicator);
        }
        
        if (bannerList.size() > 0) {
            updateIndicators(0);
        }
    }
    
    private void updateIndicators(int position) {
        if (bannerIndicators == null) return;
        
        for (int i = 0; i < bannerIndicators.getChildCount(); i++) {
            View indicator = bannerIndicators.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_selected);
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_unselected);
            }
        }
    }
    
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    
    private void startAutoScroll() {
        if (bannerList == null || bannerList.size() <= 1) return;
        
        // Lấy thời gian auto-scroll từ SharedPreferences (mặc định 6 giây = 6000ms)
        SharedPreferences prefs = getSharedPreferences("banner_prefs", MODE_PRIVATE);
        int autoScrollInterval = prefs.getInt("auto_scroll_interval", 6000);
        
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager != null) {
                    int currentItem = bannerViewPager.getCurrentItem();
                    int nextItem = (currentItem + 1) % bannerList.size();
                    bannerViewPager.setCurrentItem(nextItem, true);
                    autoScrollHandler.postDelayed(this, autoScrollInterval);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollInterval);
    }
    
    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void setupRecyclerView() {
        productRecyclerView = findViewById(R.id.productRecyclerView);
        
        // Grid layout cho danh sách sản phẩm chính
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        adapter = new CustomerProductAdapter(this, productList, new CustomerProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Mở màn hình chi tiết sản phẩm
                Intent intent = new Intent(CustomerMainActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product);
            }
        });
        
        productRecyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        productList = dbHelper.getAllProducts();
        adapter.updateData(productList);
    }

    private void setupCategoryFilters() {
        btnAll.setOnClickListener(v -> filterByCategory("Tất cả"));
        btnBeer.setOnClickListener(v -> filterByCategory("Bia"));
        btnWine.setOnClickListener(v -> filterByCategory("Rượu"));
        btnSoftDrink.setOnClickListener(v -> filterByCategory("Nước ngọt"));
        btnWater.setOnClickListener(v -> filterByCategory("Nước suối"));
        btnOther.setOnClickListener(v -> filterByCategory("Khác"));
    }

    private void filterByCategory(String category) {
        if ("Tất cả".equals(category)) {
            productList = dbHelper.getAllProducts();
        } else {
            productList = dbHelper.getProductsByCategory(category);
        }
        adapter.updateData(productList);
    }

    private void addToCart(Product product) {
        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Lưu sản phẩm vào giỏ hàng (dùng SharedPreferences đơn giản)
        String cartKey = "cart_item_" + product.getId();
        int currentQuantity = prefs.getInt(cartKey, 0);
        editor.putInt(cartKey, currentQuantity + 1);
        editor.apply();
        
        // Cập nhật badge số lượng giỏ hàng
        updateCartBadge();
        
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
    
    // Hàm đếm số lượng sản phẩm trong giỏ hàng và cập nhật badge
    private void updateCartBadge() {
        if (dbHelper == null) {
            return; // Chưa khởi tạo dbHelper
        }
        
        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        int totalItems = 0;
        
        // Đếm tổng số lượng sản phẩm trong giỏ hàng
        List<Product> allProducts = dbHelper.getAllProducts();
        for (Product product : allProducts) {
            String cartKey = "cart_item_" + product.getId();
            int quantity = prefs.getInt(cartKey, 0);
            totalItems += quantity;
        }
        
        // Cập nhật badge
        if (cartBadge != null) {
            if (totalItems > 0) {
                cartBadge.setText(String.valueOf(totalItems));
                cartBadge.setVisibility(View.VISIBLE);
            } else {
                cartBadge.setVisibility(View.GONE);
            }
        }
    }

    private void checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String loggedUser = prefs.getString("logged_user", null);
        String userRole = prefs.getString("user_role", "");
        
        if (loggedUser != null) {
            // Đã đăng nhập
            String fullName = prefs.getString("full_name", "Khách hàng");
            tvWelcome.setText("XIN CHÀO");
            
            // Ẩn nút đăng nhập trong banner
            if (btnLogin != null) {
                btnLogin.setVisibility(View.GONE);
            }
            // Ẩn nút đăng nhập trong header
            if (btnLoginHeader != null) {
                btnLoginHeader.setVisibility(View.GONE);
            }
            // Hiển thị icon đăng xuất
            if (btnLogout != null) {
                btnLogout.setVisibility(View.VISIBLE);
            }
            
            // Ẩn banner đăng nhập nếu đã đăng nhập
            if (personalizedBanner != null) {
                personalizedBanner.setVisibility(View.GONE);
            }
        } else {
            // Chưa đăng nhập
            tvWelcome.setText("XIN CHÀO");
            
            // Hiển thị nút đăng nhập trong banner
            if (btnLogin != null) {
                btnLogin.setVisibility(View.VISIBLE);
            }
            // Hiển thị nút đăng nhập trong header
            if (btnLoginHeader != null) {
                btnLoginHeader.setVisibility(View.VISIBLE);
            }
            // Ẩn icon đăng xuất
            if (btnLogout != null) {
                btnLogout.setVisibility(View.GONE);
            }
            
            // Hiển thị banner đăng nhập nếu chưa đăng nhập
            if (personalizedBanner != null) {
                personalizedBanner.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatus();
        // Reload products để cập nhật số lượng và trạng thái yêu thích
        loadProducts();
        // Cập nhật badge giỏ hàng
        updateCartBadge();
        // Cập nhật adapter để refresh trạng thái yêu thích
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        // Reload banner từ database
        loadBannersFromDatabase();
        if (bannerAdapter != null && bannerList != null) {
            bannerAdapter = new BannerAdapter(bannerList);
            bannerViewPager.setAdapter(bannerAdapter);
            setupIndicators();
        }
        // Dừng auto-scroll cũ và khởi động lại với thời gian mới (nếu có thay đổi)
        stopAutoScroll();
        if (bannerList != null && bannerList.size() > 1) {
            startAutoScroll();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Dừng auto-scroll khi màn hình tạm dừng
        stopAutoScroll();
    }
}

