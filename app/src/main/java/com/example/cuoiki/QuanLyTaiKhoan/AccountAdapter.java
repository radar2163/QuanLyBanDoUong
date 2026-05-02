package com.example.cuoiki.QuanLyTaiKhoan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.User;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private SQLiteHelper dbHelper;

    public void updateData(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    public AccountAdapter(Context context, List<User> userList, SQLiteHelper dbHelper) {
        this.context = context;
        this.userList = userList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUsername.setText("TK: " + user.getUsername());
        holder.tvFullName.setText(" " + user.getFull_name());
        holder.tvEmail.setText(" " + user.getGmail());
        holder.tvRole.setText(" Vai trò: " + user.getRole());

        // Xóa
        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteAccount(user.getUsername());
            userList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Đã xóa " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });

        // Sửa
        holder.btnEdit.setOnClickListener(v -> showEditDialog(user, position));
    }

    private void showEditDialog(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chỉnh sửa tài khoản");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);
        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        Spinner spinnerRole = view.findViewById(R.id.spinnerRole);

        // Gán dữ liệu cũ
        edtFullName.setText(user.getFull_name());
        edtEmail.setText(user.getGmail());
        edtPhone.setText(user.getPhone_number());
        edtPassword.setText(user.getPassword());

        //  Tạo danh sách vai trò
        String[] roles = {"Admin", "Nhân viên", "Khách hàng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        //  Chọn vai trò hiện tại của user
        String currentRole = user.getRole();
        if (currentRole != null) {
            if (currentRole.equalsIgnoreCase("admin")) {
                spinnerRole.setSelection(0);
            } else if (currentRole.equalsIgnoreCase("nhân viên")) {
                spinnerRole.setSelection(1);
            } else if (currentRole.equalsIgnoreCase("khách hàng") || currentRole.equalsIgnoreCase("user")) {
                spinnerRole.setSelection(2);
            } else {
                // Mặc định là "Khách hàng" nếu không khớp
                spinnerRole.setSelection(2);
            }
        } else {
            // Mặc định là "Khách hàng" nếu role null
            spinnerRole.setSelection(2);
        }

        builder.setView(view);
        builder.setPositiveButton("Lưu", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newFullName = edtFullName.getText().toString().trim();
                String newEmail = edtEmail.getText().toString().trim();
                String newPhone = edtPhone.getText().toString().trim();
                String newPassword = edtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(newPassword) || newPassword.length() < 3) {
                    edtPassword.setError("Ít nhất 3 ký tự");
                    return;
                }
                if (TextUtils.isEmpty(newFullName)) {
                    edtFullName.setError("Không được bỏ trống");
                    return;
                }
                if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    edtEmail.setError("Email không hợp lệ");
                    return;
                }
                if (TextUtils.isEmpty(newPhone) || !newPhone.matches("\\d{9,11}")) {
                    edtPhone.setError("Số điện thoại 9-11 số");
                    return;
                }

                user.setFull_name(newFullName);
                user.setGmail(newEmail);
                user.setPhone_number(newPhone);
                user.setPassword(newPassword);
                user.setRole(spinnerRole.getSelectedItem().toString());

                dbHelper.updateUser(user);
                notifyItemChanged(position);
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvFullName, tvEmail, tvRole;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
