package com.example.cuoiki.ThongKe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Transaction;
import com.example.cuoiki.ThongKe.TransactionsAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaticActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    private RecyclerView recyclerViewTransactions;
    private TransactionsAdapter transactionsAdapter;
    private Button btnSearch, btnFilterToday, btnFilterWeek, btnFilterMonth, btnFilterAll;
    private EditText editTextSearch;
    private TextView tvTotalImport, tvTotalExport, tvTotalTransactions;
    
    private List<Transaction> allTransactions;
    private String currentFilter = "all"; // "today", "week", "month", "all"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static);

        //  Kiểm tra quyền - chỉ admin mới được xem thống kê
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        if (!"admin".equalsIgnoreCase(userRole)) {
            Toast.makeText(this, "Bạn không có quyền truy cập chức năng này!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo SQLiteHelper
        dbHelper = new SQLiteHelper(this);

        // Ánh xạ views
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        btnSearch = findViewById(R.id.btnSearch);
        editTextSearch = findViewById(R.id.editTextSearch);
        tvTotalImport = findViewById(R.id.tvTotalImport);
        tvTotalExport = findViewById(R.id.tvTotalExport);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        
        btnFilterToday = findViewById(R.id.btnFilterToday);
        btnFilterWeek = findViewById(R.id.btnFilterWeek);
        btnFilterMonth = findViewById(R.id.btnFilterMonth);
        btnFilterAll = findViewById(R.id.btnFilterAll);

        // Thiết lập RecyclerView
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));

        // Load tất cả giao dịch
        allTransactions = dbHelper.getAllTransactions();
        updateStatistics(allTransactions);
        updateTransactionList(allTransactions);

        // Xử lý tìm kiếm
        btnSearch.setOnClickListener(v -> {
            String searchQuery = editTextSearch.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    searchTransactions(searchQuery);
                } else {
                applyFilter(currentFilter);
            }
        });

        // Xử lý filter buttons
        btnFilterToday.setOnClickListener(v -> {
            currentFilter = "today";
            applyFilter("today");
            updateButtonStates(btnFilterToday);
        });

        btnFilterWeek.setOnClickListener(v -> {
            currentFilter = "week";
            applyFilter("week");
            updateButtonStates(btnFilterWeek);
        });

        btnFilterMonth.setOnClickListener(v -> {
            currentFilter = "month";
            applyFilter("month");
            updateButtonStates(btnFilterMonth);
        });

        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilter("all");
            updateButtonStates(btnFilterAll);
        });

        // Đăng ký menu ngữ cảnh
        registerForContextMenu(recyclerViewTransactions);

        //  Setup Bottom Navigation
        setupBottomNavigation();
    }

    private void updateButtonStates(Button selectedButton) {
        // Reset all buttons
        btnFilterToday.setAlpha(0.6f);
        btnFilterWeek.setAlpha(0.6f);
        btnFilterMonth.setAlpha(0.6f);
        btnFilterAll.setAlpha(0.6f);

        // Highlight selected button
        selectedButton.setAlpha(1.0f);
    }

    private void applyFilter(String filterType) {
        List<Transaction> filtered = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Transaction transaction : allTransactions) {
            try {
                Date transactionDate = sdf.parse(transaction.getTransactionDate());
                if (transactionDate == null) continue;

                Calendar transCal = Calendar.getInstance();
                transCal.setTime(transactionDate);

                boolean include = false;
                switch (filterType) {
                    case "today":
                        include = isSameDay(transCal, calendar);
                        break;
                    case "week":
                        include = isSameWeek(transCal, calendar);
                        break;
                    case "month":
                        include = isSameMonth(transCal, calendar);
                        break;
                    case "all":
                        include = true;
                        break;
                }

                if (include) {
                    filtered.add(transaction);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        updateStatistics(filtered);
        updateTransactionList(filtered);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    private void updateStatistics(List<Transaction> transactions) {
        int totalImport = 0;
        int totalExport = 0;

        for (Transaction transaction : transactions) {
            if ("Nhập".equals(transaction.getTransactionType())) {
                totalImport += transaction.getQuantity();
            } else if ("Xuất".equals(transaction.getTransactionType())) {
                totalExport += transaction.getQuantity();
            }
        }

        tvTotalImport.setText(String.valueOf(totalImport));
        tvTotalExport.setText(String.valueOf(totalExport));
        tvTotalTransactions.setText(String.valueOf(transactions.size()));
    }

    private void updateTransactionList(List<Transaction> transactions) {
        transactionsAdapter = new TransactionsAdapter(transactions);
        // Thêm click listener để hiển thị chi tiết
        transactionsAdapter.setOnTransactionClickListener(transaction -> {
            showTransactionDetails(transaction);
        });
        recyclerViewTransactions.setAdapter(transactionsAdapter);
    }

    private void showTransactionDetails(Transaction transaction) {
        StringBuilder details = new StringBuilder();
        details.append("Sản phẩm: ").append(transaction.getProductName()).append("\n");
        details.append("Loại: ").append(transaction.getTransactionType()).append("\n");
        details.append("Số lượng: ").append(transaction.getQuantity()).append("\n");
        details.append("Ngày giờ: ").append(transaction.getTransactionDate()).append("\n");
        
        if (transaction.getUserFullName() != null && !transaction.getUserFullName().isEmpty()) {
            details.append("\n--- Thông tin nhân viên ---\n");
            details.append("Họ tên: ").append(transaction.getUserFullName()).append("\n");
            if (transaction.getUserEmail() != null && !transaction.getUserEmail().isEmpty()) {
                details.append("Email: ").append(transaction.getUserEmail()).append("\n");
            }
            if (transaction.getUserUsername() != null && !transaction.getUserUsername().isEmpty()) {
                details.append("Username: ").append(transaction.getUserUsername()).append("\n");
            }
        } else {
            details.append("\n--- Thông tin nhân viên ---\n");
            details.append("Không có thông tin");
        }

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết giao dịch")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void searchTransactions(String query) {
        List<Transaction> searchResults = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT t.product_id, p.name, t.transaction_type, t.quantity, t.transaction_date, " +
                "t.user_username, t.user_full_name, t.user_email " +
                "FROM transactions t JOIN product p ON t.product_id = p.id " +
                "WHERE p.name LIKE ? ORDER BY t.transaction_date DESC", new String[]{"%" + query + "%"});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int productId = cursor.getInt(0);
                    String productName = cursor.getString(1);
                    String transactionType = cursor.getString(2);
                    int quantity = cursor.getInt(3);
                    String transactionDate = cursor.getString(4);
                    String userUsername = cursor.isNull(5) ? null : cursor.getString(5);
                    String userFullName = cursor.isNull(6) ? null : cursor.getString(6);
                    String userEmail = cursor.isNull(7) ? null : cursor.getString(7);
                    searchResults.add(new Transaction(productId, productName, transactionType, quantity, transactionDate, userUsername, userFullName, userEmail));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        updateStatistics(searchResults);
        transactionsAdapter.updateTransactionList(searchResults);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = transactionsAdapter.getSelectedPosition();
        Transaction selectedTransaction = transactionsAdapter.getTransaction(position);

        if (item.getItemId() == R.id.menu_show_invoice) {
            if (selectedTransaction != null) {
                showInvoice(selectedTransaction);
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void showInvoice(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT i.id, i.total_amount, i.invoice_date, i.status " +
                        "FROM invoice i " +
                        "WHERE i.order_id = ?",
                new String[]{String.valueOf(transaction.getProductId())}
        );

        if (cursor != null && cursor.moveToFirst()) {
            int invoiceIdIndex = cursor.getColumnIndex("id");
            int totalAmountIndex = cursor.getColumnIndex("total_amount");
            int invoiceDateIndex = cursor.getColumnIndex("invoice_date");
            int statusIndex = cursor.getColumnIndex("status");

            if (invoiceIdIndex != -1 && totalAmountIndex != -1 && invoiceDateIndex != -1 && statusIndex != -1) {
                int invoiceId = cursor.getInt(invoiceIdIndex);
                double totalAmount = cursor.getDouble(totalAmountIndex);
                String invoiceDate = cursor.getString(invoiceDateIndex);
                String status = cursor.getString(statusIndex);

                cursor.close();

                new AlertDialog.Builder(this)
                        .setTitle("Chi tiết hóa đơn")
                        .setMessage(
                                "ID Hóa đơn: " + invoiceId +
                                        "\nTổng tiền: " + String.format("%,.0f", totalAmount) + " VND" +
                                        "\nNgày: " + invoiceDate +
                                        "\nTrạng thái: " + status
                        )
                        .setPositiveButton("Đóng", null)
                        .show();
            } else {
                Toast.makeText(this, "Cột dữ liệu không tồn tại.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Không tìm thấy hóa đơn.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this activity
        allTransactions = dbHelper.getAllTransactions();
        applyFilter(currentFilter);
    }

    private void setupBottomNavigation() {
        // 🔹 Bottom Navigation
        findViewById(R.id.btm_home).setOnClickListener(v -> {
            startActivity(new Intent(StaticActivity.this, com.example.cuoiki.ManHinhChinh.MainActivity.class));
            finish();
        });
        findViewById(R.id.btm_profile).setOnClickListener(v -> {
            startActivity(new Intent(StaticActivity.this, com.example.cuoiki.HoSo.ProfileActivity.class));
            finish();
        });
        findViewById(R.id.btm_search).setOnClickListener(v -> {
            startActivity(new Intent(StaticActivity.this, com.example.cuoiki.TimKiem.SearchActivity.class));
            finish();
        });
        findViewById(R.id.btm_static).setOnClickListener(v -> {
            // Đã ở màn hình Thống kê rồi, không cần làm gì
        });
    }
}
