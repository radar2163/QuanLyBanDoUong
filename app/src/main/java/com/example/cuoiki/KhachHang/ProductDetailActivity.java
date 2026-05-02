package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.text.DecimalFormat;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productQuantity, productCategory, productUnit, productDescription;
    private Button btnAddToCart, btnBuyNow;
    private SQLiteHelper dbHelper;
    private Product product;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProductDetails();
    }

    private void initViews() {
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productQuantity = findViewById(R.id.productQuantity);
        productCategory = findViewById(R.id.productCategory);
        productUnit = findViewById(R.id.productUnit);
        productDescription = findViewById(R.id.productDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        dbHelper = new SQLiteHelper(this);

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());
    }

    private void loadProductDetails() {
        product = dbHelper.getProductById(productId);
        
        if (product == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productName.setText(product.getName());
        
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        productPrice.setText(currencyFormat.format(product.getPrice()) + " đ");
        
        if (product.getQuantity() > 0) {
            productQuantity.setText("Còn lại: " + product.getQuantity() + " " + product.getInit());
            btnAddToCart.setEnabled(true);
            btnBuyNow.setEnabled(true);
        } else {
            productQuantity.setText("Hết hàng");
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
        }
        
        productCategory.setText("Danh mục: " + product.getCategory());
        productUnit.setText("Đơn vị: " + product.getInit());

        // Clear ảnh cũ trước khi load ảnh mới
        productImage.setImageDrawable(null);
        
        // Load ảnh bằng Glide (xử lý cả URL, URI, drawable) - đồng bộ với các adapter khác
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {

            Uri imageUri = Uri.parse(product.getImageUri());
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .fallback(R.drawable.no_image) // ảnh mặc định nếu URI null
                    .into(productImage);
        } else if (product.getImageResId() != 0) {
            // Load ảnh từ drawable resource
            productImage.setImageResource(product.getImageResId());
        } else {
            // Dùng ảnh mặc định
            productImage.setImageResource(R.drawable.no_image);
        }
    }

    private void addToCart() {
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

    private void buyNow() {
        if (product.getQuantity() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thêm vào giỏ và chuyển đến màn hình giỏ hàng
        addToCart();
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }
}

