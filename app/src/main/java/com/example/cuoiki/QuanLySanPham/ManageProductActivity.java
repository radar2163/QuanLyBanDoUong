package com.example.cuoiki.QuanLySanPham;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.QuanLySanPham.ManageProductAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageProductActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private AutoCompleteTextView searchProduct;
    private ManageProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private ArrayAdapter<String> suggestAdapter;
    private SQLiteHelper dbHelper;

    // Chọn ảnh khi sửa sản phẩm ---
    private Uri selectedImageUri = null;
    private ImageView currentPreviewImage;

    //  Sử dụng ActivityResultLauncher để chọn ảnh từ File Manager
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (currentPreviewImage != null && selectedImageUri != null) {
                        currentPreviewImage.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        searchProduct = findViewById(R.id.searchProduct);
        dbHelper = new SQLiteHelper(this);

        //  Load dữ liệu thật từ DB
        loadProducts();

        adapter = new ManageProductAdapter(this, productList, new ManageProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                openEditDialog(product);
            }

            @Override
            public void onDelete(Product product) {
                dbHelper.deleteProduct(product.getId());
                productList = dbHelper.getAllProducts();
                adapter.updateList(productList);
            }
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProducts.setAdapter(adapter);

        setupSearchSuggestions();
    }

    //  Load danh sách sản phẩm từ SQLite
    private void loadProducts() {
        productList = dbHelper.getAllProducts();
    }

    //  Tìm kiếm gợi ý
    private void setupSearchSuggestions() {
        List<String> names = new ArrayList<>();
        for (Product p : productList) names.add(p.getName());
        suggestAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        searchProduct.setAdapter(suggestAdapter);

        searchProduct.setOnItemClickListener((parent, view, position, id) -> {
            String selected = suggestAdapter.getItem(position);
            List<Product> filtered = new ArrayList<>();
            for (Product p : productList)
                if (p.getName().equalsIgnoreCase(selected)) filtered.add(p);
            adapter.updateList(filtered);
        });

        // Nếu xóa nội dung tìm kiếm -> hiển thị lại toàn bộ
        searchProduct.setOnDismissListener(() -> {
            if (searchProduct.getText().toString().isEmpty()) {
                adapter.updateList(productList);
            }
        });
    }

    //  Dialog sửa sản phẩm
    private void openEditDialog(Product product) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_product);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        currentPreviewImage = imgPreview;
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        EditText edtName = dialog.findViewById(R.id.edtProductName);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        Spinner spinnerUnit = dialog.findViewById(R.id.spinnerUnit);
        EditText edtPrice = dialog.findViewById(R.id.edtPrice);
        EditText edtQuantity = dialog.findViewById(R.id.edtQuantity);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // --- Gán dữ liệu cũ ---
        edtName.setText(product.getName());
        edtPrice.setText(String.valueOf(product.getPrice()));
        edtQuantity.setText(String.valueOf(product.getQuantity()));

        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            imgPreview.setImageURI(Uri.parse(product.getImageUri()));
        } else if (product.getImageResId() != 0) {
            imgPreview.setImageResource(product.getImageResId());
        } else {
            imgPreview.setImageResource(R.drawable.no_image);
        }

        // --- Spinner loại hàng ---
        String[] categories = {"Bia", "Nước ngọt", "Nước suối", "Rượu", "Khác"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        int catPos = Arrays.asList(categories).indexOf(product.getCategory());
        if (catPos >= 0) spinnerCategory.setSelection(catPos);

        // --- Spinner đơn vị ---
        String[] units = {"Chai", "Lon", "Thùng"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, units);
        spinnerUnit.setAdapter(unitAdapter);

        int unitPos = Arrays.asList(units).indexOf(product.getInit());
        if (unitPos >= 0) spinnerUnit.setSelection(unitPos);

        //  Chọn ảnh từ File Manager
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh sản phẩm từ tệp"));
        });

        // --- Lưu thay đổi ---
        btnSave.setOnClickListener(v -> {
            product.setName(edtName.getText().toString());
            product.setCategory(spinnerCategory.getSelectedItem().toString());
            product.setInit(spinnerUnit.getSelectedItem().toString());
            product.setPrice(Double.parseDouble(edtPrice.getText().toString()));
            product.setQuantity(Integer.parseInt(edtQuantity.getText().toString()));

            // Nếu có ảnh mới, cập nhật URI
            if (selectedImageUri != null) {
                product.setImageUri(selectedImageUri.toString());
            }
            
            dbHelper.updateProduct(product);
            productList = dbHelper.getAllProducts();
            adapter.updateList(productList);
            
            Toast.makeText(ManageProductActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
