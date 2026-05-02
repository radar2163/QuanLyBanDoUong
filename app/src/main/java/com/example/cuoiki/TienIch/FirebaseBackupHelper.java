package com.example.cuoiki.TienIch;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.Transaction;
import com.example.cuoiki.DuLieu.Notification;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseBackupHelper {
    private Context context;
    private SQLiteHelper dbHelper;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    public FirebaseBackupHelper(Context context) {
        this.context = context;
        this.dbHelper = new SQLiteHelper(context);
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Lấy user ID để lưu dữ liệu theo từng user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            this.databaseReference = firebaseDatabase.getReference("backups").child(currentUser.getUid());
        } else {
            // Nếu chưa đăng nhập Firebase, dùng username từ SharedPreferences hoặc "default"
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String username = prefs.getString("logged_user", "default");
            this.databaseReference = firebaseDatabase.getReference("backups").child(username);
        }
    }

    //  Backup tất cả dữ liệu từ SQLite lên Firebase
    public void backupAllData(BackupCallback callback) {
        try {
            //  Log thông tin path để debug
            android.util.Log.d("FirebaseBackup", "Backup path: " + databaseReference.toString());
            
            Map<String, Object> backupData = new HashMap<>();

            // 1. Backup Products
            List<Product> products = dbHelper.getAllProducts();
            android.util.Log.d("FirebaseBackup", "Số lượng sản phẩm: " + products.size());
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("name", product.getName());
                productMap.put("quantity", product.getQuantity());
                productMap.put("init", product.getInit());
                productMap.put("price", product.getPrice());
                productMap.put("imageResId", product.getImageResId());
                productMap.put("imageUri", product.getImageUri() != null ? product.getImageUri() : "");
                productMap.put("category", product.getCategory() != null ? product.getCategory() : "");
                productList.add(productMap);
            }
            backupData.put("products", productList);

            // 2. Backup Accounts
            List<Map<String, Object>> accountList = new ArrayList<>();
            Cursor accountCursor = dbHelper.getAllAccounts();
            if (accountCursor != null && accountCursor.moveToFirst()) {
                do {
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("id", accountCursor.getInt(accountCursor.getColumnIndexOrThrow("id")));
                    accountMap.put("username", accountCursor.getString(accountCursor.getColumnIndexOrThrow("username")));
                    accountMap.put("password", accountCursor.getString(accountCursor.getColumnIndexOrThrow("password")));
                    accountMap.put("gmail", accountCursor.getString(accountCursor.getColumnIndexOrThrow("gmail")));
                    accountMap.put("phone_number", accountCursor.getString(accountCursor.getColumnIndexOrThrow("phone_number")));
                    accountMap.put("full_name", accountCursor.getString(accountCursor.getColumnIndexOrThrow("full_name")));
                    accountMap.put("role", accountCursor.getString(accountCursor.getColumnIndexOrThrow("role")));
                    accountList.add(accountMap);
                } while (accountCursor.moveToNext());
                accountCursor.close();
            }
            backupData.put("accounts", accountList);

            // 3. Backup Transactions
            List<Transaction> transactions = dbHelper.getAllTransactions();
            List<Map<String, Object>> transactionList = new ArrayList<>();
            for (Transaction transaction : transactions) {
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("productId", transaction.getProductId());
                transactionMap.put("productName", transaction.getProductName());
                transactionMap.put("transactionType", transaction.getTransactionType());
                transactionMap.put("quantity", transaction.getQuantity());
                transactionMap.put("transactionDate", transaction.getTransactionDate());
                transactionList.add(transactionMap);
            }
            backupData.put("transactions", transactionList);

            // 4. Backup Notifications
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String currentUsername = prefs.getString("logged_user", "admin");
            List<Notification> notifications = dbHelper.getNotifications(currentUsername);
            List<Map<String, Object>> notificationList = new ArrayList<>();
            for (Notification notification : notifications) {
                Map<String, Object> notificationMap = new HashMap<>();
                notificationMap.put("id", notification.getId());
                notificationMap.put("title", notification.getTitle());
                notificationMap.put("message", notification.getMessage());
                notificationMap.put("sender_username", notification.getSenderUsername());
                notificationMap.put("receiver_username", notification.getReceiverUsername());
                notificationMap.put("is_read", notification.isRead() ? 1 : 0);
                notificationMap.put("created_date", notification.getCreatedDate());
                notificationList.add(notificationMap);
            }
            backupData.put("notifications", notificationList);

            // 5. Backup User Preferences (avatar_uri, full_name, etc.)
            Map<String, Object> userPrefsMap = new HashMap<>();
            String avatarUri = prefs.getString("avatar_uri", null);
            String fullName = prefs.getString("full_name", null);
            String phoneNumber = prefs.getString("phone_number", null);
            if (avatarUri != null) {
                userPrefsMap.put("avatar_uri", avatarUri);
            }
            if (fullName != null) {
                userPrefsMap.put("full_name", fullName);
            }
            if (phoneNumber != null) {
                userPrefsMap.put("phone_number", phoneNumber);
            }
            if (!userPrefsMap.isEmpty()) {
                backupData.put("user_preferences", userPrefsMap);
            }

            // 6. Backup Orders (nếu có)
            backupData.put("backupTimestamp", System.currentTimeMillis());

            // Lưu lên Firebase
            databaseReference.setValue(backupData)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) {
                            callback.onSuccess("Backup thành công!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onFailure("Lỗi backup: " + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure("Lỗi: " + e.getMessage());
            }
        }
    }

    //  Restore dữ liệu từ Firebase về SQLite
    public void restoreAllData(RestoreCallback callback) {
        //  Lấy username hiện tại TRƯỚC KHI restore để đảm bảo restore đúng user
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentLoggedUser = prefs.getString("logged_user", null);
        
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (callback != null) {
                        callback.onFailure("Không tìm thấy dữ liệu backup!");
                    }
                    return;
                }
                
                //  Đảm bảo logged_user không bị thay đổi trong quá trình restore
                if (currentLoggedUser != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("logged_user", currentLoggedUser);
                    editor.apply();
                }

                try {
                    // 1. Restore Products (chỉ thêm nếu chưa tồn tại)
                    if (snapshot.hasChild("products")) {
                        DataSnapshot productsSnapshot = snapshot.child("products");
                        for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                            Map<String, Object> productMap = (Map<String, Object>) productSnapshot.getValue();
                            if (productMap != null) {
                                String name = (String) productMap.get("name");
                                
                                //  Kiểm tra sản phẩm đã tồn tại chưa để tránh trùng lặp
                                if (!dbHelper.productExists(name)) {
                                    int quantity = ((Long) productMap.get("quantity")).intValue();
                                    String init = (String) productMap.get("init");
                                    double price = ((Number) productMap.get("price")).doubleValue();
                                    int imageResId = productMap.get("imageResId") != null ? ((Long) productMap.get("imageResId")).intValue() : 0;
                                    String imageUri = productMap.get("imageUri") != null ? (String) productMap.get("imageUri") : "";
                                    String category = productMap.get("category") != null ? (String) productMap.get("category") : "";
                                    
                                    dbHelper.addProduct(name, quantity, init, price, imageResId, imageUri, category);
                                }
                            }
                        }
                    }

                    // 2. Restore Accounts
                    if (snapshot.hasChild("accounts")) {
                        DataSnapshot accountsSnapshot = snapshot.child("accounts");
                        for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                            Map<String, Object> accountMap = (Map<String, Object>) accountSnapshot.getValue();
                            if (accountMap != null) {
                                String username = (String) accountMap.get("username");
                                String password = (String) accountMap.get("password");
                                String gmail = (String) accountMap.get("gmail");
                                String phone = (String) accountMap.get("phone_number");
                                String fullName = (String) accountMap.get("full_name");
                                String role = (String) accountMap.get("role");
                                
                                // Kiểm tra xem account đã tồn tại chưa
                                if (dbHelper.getUserByUsername(username) == null) {
                                    dbHelper.insertAccount(username, password, gmail, phone, fullName, role);
                                }
                            }
                        }
                    }

                    // 3. Restore Transactions
                    if (snapshot.hasChild("transactions")) {
                        DataSnapshot transactionsSnapshot = snapshot.child("transactions");
                        for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
                            Map<String, Object> transactionMap = (Map<String, Object>) transactionSnapshot.getValue();
                            if (transactionMap != null) {
                                int productId = ((Long) transactionMap.get("productId")).intValue();
                                String transactionType = (String) transactionMap.get("transactionType");
                                int quantity = ((Long) transactionMap.get("quantity")).intValue();
                                String transactionDate = (String) transactionMap.get("transactionDate");
                                
                                dbHelper.addTransaction(productId, transactionType, quantity, transactionDate);
                            }
                        }
                    }

                    // 4. Restore Notifications (chỉ thêm nếu chưa tồn tại)
                    if (snapshot.hasChild("notifications")) {
                        DataSnapshot notificationsSnapshot = snapshot.child("notifications");
                        for (DataSnapshot notificationSnapshot : notificationsSnapshot.getChildren()) {
                            Map<String, Object> notificationMap = (Map<String, Object>) notificationSnapshot.getValue();
                            if (notificationMap != null) {
                                String title = (String) notificationMap.get("title");
                                String message = (String) notificationMap.get("message");
                                String senderUsername = (String) notificationMap.get("sender_username");
                                String receiverUsername = (String) notificationMap.get("receiver_username");
                                String createdDate = (String) notificationMap.get("created_date");
                                
                                //  Kiểm tra thông báo đã tồn tại chưa để tránh trùng lặp
                                if (!dbHelper.notificationExists(title, message, senderUsername, receiverUsername, createdDate)) {
                                    dbHelper.addNotification(title, message, senderUsername, receiverUsername, createdDate);
                                }
                            }
                        }
                    }

                    // 5. Restore User Preferences (avatar_uri, full_name, etc.)
                    // ⚠️ QUAN TRỌNG: KHÔNG restore logged_user, chỉ restore avatar và thông tin khác
                    if (snapshot.hasChild("user_preferences")) {
                        DataSnapshot userPrefsSnapshot = snapshot.child("user_preferences");
                        Map<String, Object> userPrefsMap = (Map<String, Object>) userPrefsSnapshot.getValue();
                        if (userPrefsMap != null) {
                            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            
                            //  Lấy logged_user hiện tại TRƯỚC KHI restore (để không bị ghi đè)
                            String currentLoggedUser = prefs.getString("logged_user", null);
                            
                            SharedPreferences.Editor editor = prefs.edit();
                            
                            if (userPrefsMap.containsKey("avatar_uri")) {
                                String avatarUri = (String) userPrefsMap.get("avatar_uri");
                                if (avatarUri != null) {
                                    editor.putString("avatar_uri", avatarUri);
                                }
                            }
                            if (userPrefsMap.containsKey("full_name")) {
                                String fullName = (String) userPrefsMap.get("full_name");
                                if (fullName != null) {
                                    editor.putString("full_name", fullName);
                                }
                            }
                            if (userPrefsMap.containsKey("phone_number")) {
                                String phoneNumber = (String) userPrefsMap.get("phone_number");
                                if (phoneNumber != null) {
                                    editor.putString("phone_number", phoneNumber);
                                }
                            }
                            
                            //  Đảm bảo logged_user không bị thay đổi
                            if (currentLoggedUser != null) {
                                editor.putString("logged_user", currentLoggedUser);
                            }
                            
                            editor.apply();
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess("Restore thành công!");
                    }

                } catch (Exception e) {
                    if (callback != null) {
                        callback.onFailure("Lỗi restore: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onFailure("Lỗi: " + error.getMessage());
                }
            }
        });
    }

    //  Sync dữ liệu (backup tự động)
    public void syncData() {
        backupAllData(new BackupCallback() {
            @Override
            public void onSuccess(String message) {
                // Sync thành công (có thể log hoặc thông báo)
            }

            @Override
            public void onFailure(String error) {
                // Sync thất bại (có thể log)
            }
        });
    }

    // Interface cho callback
    public interface BackupCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface RestoreCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
