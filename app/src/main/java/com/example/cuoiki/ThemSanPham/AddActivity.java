package com.example.cuoiki.ThemSanPham;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.io.IOException;

public class AddActivity extends AppCompatActivity {

    private EditText edtName, edtQuantity, edtPrice;
    private Spinner spinnerCategory, spinnerUnit;
    private ImageView imgPreview;
    private Button btnAdd, btnChooseImage;

    private SQLiteHelper db;
    private Uri selectedImageUri = null;
    private String selectedCategory = "Khác";
    private String selectedUnit = "Chai";

    //  Chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        imgPreview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Không đọc được ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activitty); // ⚠️ Kiểm tra tên file XML đúng là add_activitty.xml

        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        imgPreview = findViewById(R.id.imgPreview);
        btnAdd = findViewById(R.id.btnAdd);
        btnChooseImage = findViewById(R.id.btnChooseImage);

        db = new SQLiteHelper(this);

        // 🏷 Spinner Loại hàng
        String[] categories = {"Bia", "Nước ngọt", "Nước suối", "Rượu", "Khác"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE); // 🔹 màu chữ trắng
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE); // 🔹 màu chữ trắng khi mở dropdown
                view.setBackgroundColor(Color.parseColor("#2E0066")); // 🔹 nền tím nhẹ cho menu
                return view;
            }
        };
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedCategory = categories[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ️ Spinner Đơn vị
        String[] units = {"Chai", "Lon", "Thùng"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, units) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#2E0066"));
                return view;
            }
        };
        spinnerUnit.setAdapter(unitAdapter);
        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedUnit = units[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🖼 Nút chọn ảnh
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh sản phẩm từ tệp"));
        });

        // 🏷 Nút Thêm sản phẩm
        btnAdd.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();

            if (name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                double price = Double.parseDouble(priceStr);
                
                // Lưu sản phẩm vào SQLite
                String imageUriString = null;
                if (selectedImageUri != null) {
                    imageUriString = selectedImageUri.toString();
                }
                
                db.addProduct(name, quantity, selectedUnit, price, 0, imageUriString, selectedCategory);
                Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();

                setResult(Activity.RESULT_OK);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Sai định dạng dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
