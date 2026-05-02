package com.example.cuoiki.CapNhatKho;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.TienIch.FirebaseBackupHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity {

    private Spinner spinnerProduct;
    private Button btnUpdateProduct, btnclose;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        spinnerProduct = findViewById(R.id.product_spinner);
        btnUpdateProduct = findViewById(R.id.searchButton);
        btnclose = findViewById(R.id.btnclose);

        // Mở cơ sở dữ liệu
        db = new SQLiteHelper(this).getWritableDatabase();

        // Thiết lập Spinner với dữ liệu
        List<String> productNames = getProductNamesFromDatabase(); // Giả sử bạn lấy danh sách tên sản phẩm từ cơ sở dữ liệu
        
        // Custom adapter để text hiển thị màu trắng
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, productNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(android.graphics.Color.WHITE);
                textView.setTextSize(18f);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(android.graphics.Color.WHITE);
                textView.setBackgroundColor(0xFF281A56);
                textView.setTextSize(18f);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(adapter);

        // Cập nhật khi nhấn nút
        btnUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedProduct = spinnerProduct.getSelectedItem().toString(); // Lấy tên sản phẩm đã chọn từ Spinner
                // Tiến hành xử lý với sản phẩm đã chọn
                // Ví dụ: Cập nhật số lượng
                EditText editTextText2 = findViewById(R.id.quantity_edit_text);
                String quantityText = editTextText2.getText().toString();
                if (quantityText.isEmpty()) {
                    Toast.makeText(UpdateActivity.this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    return;
                }
                int addedQuantity = Integer.parseInt(quantityText);
                updateProductQuantity(selectedProduct,addedQuantity);

                //thêm giao dịch vào bảng transactions
                // Thực hiện thêm giao dịch vào bảng transactions

            }
        });
        // Xử lý nút thoát
        btnclose.setOnClickListener(v -> finish());
    }




    private List<String> getProductNamesFromDatabase() {
        // Giả sử bạn lấy tên sản phẩm từ cơ sở dữ liệu
        List<String> productNames = new ArrayList<>();
        // Thực hiện truy vấn cơ sở dữ liệu để lấy danh sách tên sản phẩm
        Cursor cursor = db.query("product", new String[]{"name"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex("name");
            String productName = null;  // Khai báo productName ngoài vòng if

            if (columnIndex != -1) {
                productName = cursor.getString(columnIndex);
            } else {
                // Xử lý lỗi nếu cột không tồn tại
                Log.e("DB Error", "Cột 'name' không tồn tại trong cơ sở dữ liệu.");
            }

            if (productName != null) {
                productNames.add(productName);  // Chỉ thêm vào danh sách nếu productName không null
            }
        }
        cursor.close();
        return productNames;
    }

    private void updateProductQuantity(String productName, int addedQuantity) {
        Cursor cursor = null;
        try {
            // Truy vấn để lấy số lượng hiện tại của sản phẩm
            cursor = db.query("product", new String[]{"id", "quantity"}, "name = ?", new String[]{productName}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Kiểm tra xem cột "id" và "quantity" có tồn tại không
                int idColumnIndex = cursor.getColumnIndex("id");
                int quantityColumnIndex = cursor.getColumnIndex("quantity");

                if (idColumnIndex != -1 && quantityColumnIndex != -1) {
                    int productId = cursor.getInt(idColumnIndex);
                    int currentQuantity = cursor.getInt(quantityColumnIndex);
                    int newQuantity = currentQuantity + addedQuantity;

                    // Cập nhật lại số lượng cho sản phẩm
                    ContentValues values = new ContentValues();
                    values.put("quantity", newQuantity);

                    // Thực hiện cập nhật
                    int rowsUpdated = db.update("product", values, "name = ?", new String[]{productName});

                    if (rowsUpdated > 0) {
                        // Lấy thông tin người dùng hiện tại từ SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String loggedUser = prefs.getString("logged_user", null);
                        String fullName = prefs.getString("full_name", null);
                        String email = null;
                        if (loggedUser != null) {
                            SQLiteHelper dbHelper = new SQLiteHelper(this);
                            com.example.cuoiki.DuLieu.User user = dbHelper.getUserByUsername(loggedUser);
                            if (user != null) {
                                email = user.getGmail();
                            }
                        }

                        // Thêm giao dịch vào bảng transactions
                        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        ContentValues transactionValues = new ContentValues();
                        transactionValues.put("product_id", productId);
                        transactionValues.put("transaction_type", "Nhập");
                        transactionValues.put("quantity", addedQuantity);
                        transactionValues.put("transaction_date", currentDate);
                        transactionValues.put("user_username", loggedUser);
                        transactionValues.put("user_full_name", fullName);
                        transactionValues.put("user_email", email);

                        // Thực hiện thêm giao dịch vào bảng transactions
                        long transactionId = db.insert("transactions", null, transactionValues);

                        if (transactionId != -1) {
                            // Thêm hóa đơn vào bảng invoice
                            ContentValues invoiceValues = new ContentValues();
                            invoiceValues.put("order_id", transactionId); // Liên kết hóa đơn với giao dịch
                            invoiceValues.put("total_amount", calculateTotalAmount(productId, addedQuantity)); // Tính tổng tiền (có thể thêm giá sản phẩm vào cơ sở dữ liệu nếu chưa có)
                            invoiceValues.put("invoice_date", currentDate);
                            invoiceValues.put("status", "Đã thanh toán");

                            // Thực hiện thêm hóa đơn vào bảng invoice
                            long invoiceId = db.insert("invoice", null, invoiceValues);

                            if (invoiceId != -1) {
                                Toast.makeText(this, "Cập nhật số lượng, ghi giao dịch và hóa đơn thành công", Toast.LENGTH_SHORT).show();
                                
                                // 💾 Tự động backup dữ liệu lên Firebase
                                FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
                                backupHelper.backupAllData(null);
                                
                                setResult(Activity.RESULT_OK);  // Trả kết quả về Activity trước khi finish
                                finish();
                            } else {
                                Toast.makeText(this, "Có lỗi khi ghi lại hóa đơn", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Có lỗi khi ghi lại giao dịch", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Nếu cột "id" hoặc "quantity" không tồn tại
                    Toast.makeText(this, "Không tìm thấy cột 'id' hoặc 'quantity' trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Nếu không tìm thấy sản phẩm
                Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi khi truy vấn cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close(); // Đảm bảo đóng con trỏ khi hoàn tất
            }
        }
    }

    private double calculateTotalAmount(int productId, int addedQuantity) {
        // Giả sử bạn có giá sản phẩm trong cơ sở dữ liệu hoặc một cách nào đó để tính giá
        Cursor cursor = null;
        double price = 0;
        try {
            // Truy vấn cơ sở dữ liệu để lấy giá của sản phẩm
            cursor = db.query("product", new String[]{"price"}, "id = ?", new String[]{String.valueOf(productId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int priceColumnIndex = cursor.getColumnIndex("price");
                // Kiểm tra xem chỉ số cột có hợp lệ không
                if (priceColumnIndex != -1) {
                    price = cursor.getDouble(priceColumnIndex); // Lấy giá sản phẩm
                } else {
                    Log.e("DB Error", "Cột 'price' không tồn tại trong cơ sở dữ liệu.");
                }
            } else {
                Log.e("DB Error", "Không tìm thấy sản phẩm với ID: " + productId);
            }
        } catch (Exception e) {
            Log.e("DB Error", "Lỗi khi truy vấn cơ sở dữ liệu: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Đảm bảo đóng con trỏ khi hoàn tất
            }
        }

        // Tính tổng tiền nếu giá hợp lệ
        if (price > 0) {
            return price * addedQuantity; // Tính tổng tiền (số lượng * giá sản phẩm)
        } else {
            Log.e("Calculation Error", "Giá sản phẩm không hợp lệ.");
            return 0;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

}
