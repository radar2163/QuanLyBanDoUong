package com.example.cuoiki.QuanLyTaiKhoan;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.QuanLyTaiKhoan.AccountAdapter;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AccountManageActivity extends AppCompatActivity {
    private RecyclerView recyclerAccounts;
    private SQLiteHelper dbHelper;
    private AccountAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<String> suggestionList = new ArrayList<>();
    private AutoCompleteTextView searchAccount;
    private FloatingActionButton fabAddAccount;
    private ArrayAdapter<String> suggestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);

        recyclerAccounts = findViewById(R.id.recyclerAccounts);
        recyclerAccounts.setLayoutManager(new LinearLayoutManager(this));

        searchAccount = findViewById(R.id.searchAccount);

        dbHelper = new SQLiteHelper(this);
        loadAccounts();

        // Tạo adapter gợi ý
        suggestAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, suggestionList);
        searchAccount.setAdapter(suggestAdapter);
        searchAccount.setThreshold(1); // Gợi ý sau 1 ký tự

        // Lọc khi người dùng nhập
        searchAccount.setOnItemClickListener((parent, view, position, id) -> filterAccounts(searchAccount.getText().toString()));
        searchAccount.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAccounts(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        fabAddAccount = findViewById(R.id.fabAddAccount);
        fabAddAccount.setOnClickListener(v -> showAddAccountDialog());
    }

    private void loadAccounts() {
        Cursor cursor = dbHelper.getAllAccounts();
        userList.clear();
        suggestionList.clear();

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String gmail = cursor.getString(cursor.getColumnIndexOrThrow("gmail"));
                String full_name = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                userList.add(new User(username, password, gmail, phone, full_name, role));
                suggestionList.add(username);
                suggestionList.add(gmail);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new AccountAdapter(this, userList, dbHelper);
        recyclerAccounts.setAdapter(adapter);

        if (suggestAdapter != null) {
            suggestAdapter.notifyDataSetChanged();
        }
    }

    private void filterAccounts(String query) {
        List<User> filtered = new ArrayList<>();
        for (User u : userList) {
            if (u.getUsername().toLowerCase().contains(query.toLowerCase())
                    || u.getGmail().toLowerCase().contains(query.toLowerCase())
                    || u.getFull_name().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.updateData(filtered);
    }

    private void showAddAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        Spinner spinnerRole = view.findViewById(R.id.spinnerRole);

        String[] roles = {"Admin", "Nhân viên", "Khách hàng"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm tài khoản mới")
                .setView(view)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();

                if (TextUtils.isEmpty(username)) {
                    edtUsername.setError("Không được bỏ trống");
                    return;
                }
                if (TextUtils.isEmpty(password) || password.length() < 3) {
                    edtPassword.setError("Mật khẩu tối thiểu 3 ký tự");
                    return;
                }
                if (TextUtils.isEmpty(fullName)) {
                    edtFullName.setError("Nhập họ tên");
                    return;
                }
                if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEmail.setError("Email không hợp lệ");
                    return;
                }
                if (TextUtils.isEmpty(phone) || !phone.matches("\\d{9,11}")) {
                    edtPhone.setError("Số điện thoại không hợp lệ");
                    return;
                }

                if (dbHelper.getUserByUsername(username) != null) {
                    edtUsername.setError("Tên đã tồn tại");
                    return;
                }

                boolean inserted = dbHelper.insertAccount(username, password, email, phone, fullName, role);
                if (inserted) {
                    Toast.makeText(this, "Đã thêm tài khoản mới!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    searchAccount.setText("");
                    loadAccounts();
                } else {
                    Toast.makeText(this, "Không thể thêm tài khoản!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
