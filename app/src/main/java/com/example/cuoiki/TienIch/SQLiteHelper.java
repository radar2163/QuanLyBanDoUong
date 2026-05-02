package com.example.cuoiki.TienIch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.Transaction;
import com.example.cuoiki.DuLieu.User;
import com.example.cuoiki.DuLieu.Notification;
import com.example.cuoiki.DuLieu.Order;
import com.example.cuoiki.DuLieu.Banner;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Tên cơ sở dữ liệu và phiên bản
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 8; // 🔹 tăng version để thêm bảng banners

    // Câu lệnh SQL để tạo các bảng
    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE product (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "quantity INTEGER NOT NULL, " +
            "init TEXT NOT NULL, " +
            "price REAL NOT NULL, " +
            "imageResId INTEGER, " +
            "imageUri TEXT, " +
            "category TEXT" +
            ");";

    private static final String CREATE_TABLE_ORDER = "CREATE TABLE orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_name TEXT NOT NULL, " +
            "phone_number TEXT, " +
            "order_date TEXT NOT NULL, " +
            "address TEXT NOT NULL, " +
            "payment_method TEXT NOT NULL, " +
            "total_price REAL NOT NULL, " +
            "status TEXT NOT NULL, " +
            "username TEXT);";
    
    private static final String CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE order_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "order_id INTEGER NOT NULL, " +
            "product_id INTEGER NOT NULL, " +
            "product_name TEXT NOT NULL, " +
            "quantity INTEGER NOT NULL, " +
            "price REAL NOT NULL, " +
            "FOREIGN KEY(order_id) REFERENCES orders(id), " +
            "FOREIGN KEY(product_id) REFERENCES product(id));";

    private static final String CREATE_TABLE_ACCOUNT = "CREATE TABLE account (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT NOT NULL, " +
            "password TEXT NOT NULL, " +
            "gmail TEXT NOT NULL, " +
            "phone_number TEXT NOT NULL, " +
            "full_name TEXT NOT NULL, " +
            "role TEXT NOT NULL);";

    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE transactions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "product_id INTEGER NOT NULL, " +
            "transaction_type TEXT NOT NULL, " +
            "quantity INTEGER NOT NULL, " +
            "transaction_date TEXT NOT NULL, " +
            "user_username TEXT, " +
            "user_full_name TEXT, " +
            "user_email TEXT, " +
            "FOREIGN KEY(product_id) REFERENCES product(id));";

    private static final String CREATE_TABLE_INVOICE = "CREATE TABLE invoice (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "order_id INTEGER NOT NULL, " +
            "total_amount REAL NOT NULL, " +
            "invoice_date TEXT NOT NULL, " +
            "status TEXT NOT NULL, " +
            "FOREIGN KEY(order_id) REFERENCES orders(id));";

    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE notifications (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT NOT NULL, " +
            "message TEXT NOT NULL, " +
            "sender_username TEXT NOT NULL, " +
            "receiver_username TEXT, " +
            "is_read INTEGER DEFAULT 0, " +
            "created_date TEXT NOT NULL);";

    private static final String CREATE_TABLE_BANNERS = "CREATE TABLE banners (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "imageUri TEXT, " +
            "imageResId INTEGER DEFAULT 0, " +
            "displayOrder INTEGER DEFAULT 0, " +
            "isActive INTEGER DEFAULT 1);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(CREATE_TABLE_PRODUCT);
        db.execSQL(CREATE_TABLE_ORDER);
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_INVOICE);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        db.execSQL(CREATE_TABLE_BANNERS);

        ContentValues admin = new ContentValues();
        admin.put("username", "admin");
        admin.put("password", "123");
        admin.put("gmail", "admin@gmail.com");
        admin.put("phone_number", "0123456789");
        admin.put("full_name", "Lê Văn A");
        admin.put("role", "admin");
        db.insert("account", null, admin);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Cập nhật database schema khi nâng cấp
        if (oldVersion < 8) {
            try {
                // Tạo bảng banners
                db.execSQL(CREATE_TABLE_BANNERS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 7) {
            try {
                // Xóa bảng orders cũ và tạo lại với schema mới
                db.execSQL("DROP TABLE IF EXISTS order_items");
                db.execSQL("DROP TABLE IF EXISTS orders");
                db.execSQL(CREATE_TABLE_ORDER);
                db.execSQL(CREATE_TABLE_ORDER_ITEMS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (oldVersion < 6) {
            // Thêm các cột thông tin người dùng vào bảng transactions
            try {
                db.execSQL("ALTER TABLE transactions ADD COLUMN user_username TEXT");
                db.execSQL("ALTER TABLE transactions ADD COLUMN user_full_name TEXT");
                db.execSQL("ALTER TABLE transactions ADD COLUMN user_email TEXT");
            } catch (Exception e) {
                // Nếu cột đã tồn tại, bỏ qua lỗi
                e.printStackTrace();
            }
        }
        
        // Nếu version cũ hơn nhiều, xóa và tạo lại
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS product");
            db.execSQL("DROP TABLE IF EXISTS orders");
            db.execSQL("DROP TABLE IF EXISTS order_items");
            db.execSQL("DROP TABLE IF EXISTS account");
            db.execSQL("DROP TABLE IF EXISTS transactions");
            db.execSQL("DROP TABLE IF EXISTS invoice");
            db.execSQL("DROP TABLE IF EXISTS notifications");
            onCreate(db);
        }
    }

    //  Kiểm tra tài khoản đăng nhập
    public boolean checkAccount(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM account WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    //  Lấy thông tin người dùng
    public Cursor getUserInfo(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM account WHERE username = ?";
        return db.rawQuery(query, new String[]{username});
    }

    // 📦 Lấy tất cả sản phẩm (sắp xếp theo ID giảm dần - sản phẩm mới nhất lên đầu)
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("product", null, null, null, null, null, "id DESC");

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("init")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")) // ✅ thêm dòng này
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productList;
    }


    //  Thêm sản phẩm
    public long addProduct(String name, int quantity, String init, double price, int imageResId, String imageUri, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("quantity", quantity);
        values.put("init", init);
        values.put("price", price);
        values.put("imageResId", imageResId);
        values.put("imageUri", imageUri);
        values.put("category", category);
        long id = db.insert("product", null, values);
        db.close();
        return id;
    }

    //  Kiểm tra sản phẩm đã tồn tại chưa (theo tên)
    public boolean productExists(String productName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("product", new String[]{"id"}, "name = ?", new String[]{productName}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    public List<Product> getProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        if (category.equalsIgnoreCase("Tất cả")) {
            cursor = db.query("product", null, null, null, null, null, "id DESC");
        } else {
            cursor = db.query("product", null, "category = ?", new String[]{category}, null, null, "id DESC");
        }

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("init")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category"))
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productList;
    }

    // Tìm kiếm sản phẩm theo tên (sắp xếp theo ID giảm dần - sản phẩm mới nhất lên đầu)
    public List<Product> searchProducts(String query) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "name LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        Cursor cursor = db.query("product", null, selection, selectionArgs, null, null, "id DESC");

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("init")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")) // ✅ thêm dòng này
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    //  Lấy sản phẩm theo ID
    public Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;

        Cursor cursor = db.query("product", null, "id = ?", new String[]{String.valueOf(productId)}, null, null, null);

        if (cursor.moveToFirst()) {
            product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("init")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                    cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category"))
            );
        }

        cursor.close();
        db.close();
        return product;
    }

    //  Tạo tài khoản
    public boolean insertAccount(String username, String password, String gmail, String phone, String fullName, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("gmail", gmail);
        contentValues.put("phone_number", phone);
        contentValues.put("full_name", fullName);
        contentValues.put("role", role);
        long result = db.insert("account", null, contentValues);
        return result != -1;
    }

    // Lấy tất cả giao dịch (bao gồm thông tin người dùng)
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT t.product_id, p.name, t.transaction_type, t.quantity, t.transaction_date, " +
                        "t.user_username, t.user_full_name, t.user_email " +
                        "FROM transactions t JOIN product p ON t.product_id = p.id " +
                        "ORDER BY t.transaction_date DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int productId = cursor.getInt(0);
                String productName = cursor.getString(1);
                String transactionType = cursor.getString(2);
                int quantity = cursor.getInt(3);
                String transactionDate = cursor.getString(4);
                String userUsername = cursor.isNull(5) ? null : cursor.getString(5);
                String userFullName = cursor.isNull(6) ? null : cursor.getString(6);
                String userEmail = cursor.isNull(7) ? null : cursor.getString(7);
                transactionList.add(new Transaction(productId, productName, transactionType, quantity, transactionDate, userUsername, userFullName, userEmail));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return transactionList;
    }
    //  Lấy toàn bộ tài khoản
    public Cursor getAllAccounts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM account", null);
    }

    //  Lấy danh sách User
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account", null);
        
        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gmail")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role"))
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    // 🗑 Xóa tài khoản theo username
    public void deleteAccount(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("account", "username=?", new String[]{username});
        db.close();
    }


    //Thêm giao dịch (có thông tin người dùng)
    public void addTransaction(int productId, String transactionType, int quantity, String transactionDate, String userUsername, String userFullName, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_id", productId);
        values.put("transaction_type", transactionType);
        values.put("quantity", quantity);
        values.put("transaction_date", transactionDate);
        values.put("user_username", userUsername);
        values.put("user_full_name", userFullName);
        values.put("user_email", userEmail);
        db.insert("transactions", null, values);
        db.close();
    }
    
    //Thêm giao dịch (không có thông tin người dùng - để tương thích ngược)
    public void addTransaction(int productId, String transactionType, int quantity, String transactionDate) {
        addTransaction(productId, transactionType, quantity, transactionDate, null, null, null);
    }
    //  Lấy thông tin người dùng theo username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account WHERE username = ?", new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gmail")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role"))
            );
            cursor.close();
            db.close();
            return user;
        }
        return null;
    }

    //  Cập nhật thông tin user (dùng cho ProfileActivity)
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", user.getPassword());
        values.put("gmail", user.getGmail());
        values.put("phone_number", user.getPhone_number());
        values.put("full_name", user.getFull_name());
        values.put("role", user.getRole());
        db.update("account", values, "username = ?", new String[]{user.getUsername()});

        db.close();
    }

    //  Kiểm tra quyền người dùng
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM account WHERE username = ?", new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            db.close();
            return role;
        }
        return null;
    }
    // Cập nhật thông tin sản phẩm
    public void updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("quantity", product.getQuantity());
        values.put("init", product.getInit());
        values.put("price", product.getPrice());
        values.put("imageResId", product.getImageResId());
        values.put("imageUri", product.getImageUri());
        values.put("category", product.getCategory());
        db.update("product", values, "id = ?", new String[]{String.valueOf(product.getId())});
        db.close();
    }

    //  Xóa sản phẩm theo ID
    public void deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("product", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    //  Lấy danh sách loại hàng (distinct)
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM product WHERE category IS NOT NULL AND category != ''", null);
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    //  Thêm thông báo mới
    public boolean addNotification(String title, String message, String senderUsername, String receiverUsername, String createdDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            // Kiểm tra xem bảng có tồn tại không
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'", null);
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            
            if (!tableExists) {
                // Tạo bảng nếu chưa tồn tại
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            }
            
            ContentValues values = new ContentValues();
            values.put("title", title);
            values.put("message", message);
            values.put("sender_username", senderUsername);
            values.put("receiver_username", receiverUsername != null ? receiverUsername : "ALL");
            values.put("is_read", 0);
            values.put("created_date", createdDate);
            long result = db.insert("notifications", null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    // 🔔 Lấy thông báo của user (hoặc tất cả nếu receiver_username = "ALL")
    public List<Notification> getNotifications(String username) {
        List<Notification> notificationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Kiểm tra xem bảng có tồn tại không
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'", null);
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            
            if (!tableExists) {
                // Tạo bảng nếu chưa tồn tại
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            }
            
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM notifications WHERE receiver_username = ? OR receiver_username = 'ALL' ORDER BY created_date DESC",
                    new String[]{username});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Notification notification = new Notification(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("message")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sender_username")),
                            cursor.getString(cursor.getColumnIndexOrThrow("receiver_username")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("created_date"))
                    );
                    notificationList.add(notification);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return notificationList;
    }

    //  Lấy thông báo theo trạng thái đã đọc/chưa đọc
    public List<Notification> getNotificationsByReadStatus(String username, boolean isRead) {
        List<Notification> notificationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Kiểm tra xem bảng có tồn tại không
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'", null);
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            
            if (!tableExists) {
                // Tạo bảng nếu chưa tồn tại
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            }
            
            int readStatus = isRead ? 1 : 0;
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM notifications WHERE (receiver_username = ? OR receiver_username = 'ALL') AND is_read = ? ORDER BY created_date DESC",
                    new String[]{username, String.valueOf(readStatus)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Notification notification = new Notification(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("message")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sender_username")),
                            cursor.getString(cursor.getColumnIndexOrThrow("receiver_username")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("created_date"))
                    );
                    notificationList.add(notification);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return notificationList;
    }

    //  Đánh dấu thông báo đã đọc
    public void markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        db.update("notifications", values, "id = ?", new String[]{String.valueOf(notificationId)});
        db.close();
    }

    //  Đếm số thông báo chưa đọc
    public int getUnreadNotificationCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        
        try {
            // Kiểm tra xem bảng có tồn tại không
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'", null);
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            
            if (!tableExists) {
                db.close();
                return 0;
            }
            
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM notifications WHERE (receiver_username = ? OR receiver_username = 'ALL') AND is_read = 0",
                    new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return count;
    }

    //  Kiểm tra thông báo đã tồn tại chưa (dựa trên title, message, sender, receiver, created_date)
    public boolean notificationExists(String title, String message, String senderUsername, String receiverUsername, String createdDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;
        
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM notifications WHERE title = ? AND message = ? AND sender_username = ? AND receiver_username = ? AND created_date = ?",
                    new String[]{title, message, senderUsername, receiverUsername != null ? receiverUsername : "ALL", createdDate});
            
            if (cursor != null && cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return exists;
    }

    //  Lấy danh sách thông báo đã gửi bởi user (theo sender_username)
    public List<Notification> getSentNotifications(String senderUsername) {
        List<Notification> notificationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Kiểm tra xem bảng có tồn tại không
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notifications'", null);
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            
            if (!tableExists) {
                // Tạo bảng nếu chưa tồn tại
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            }
            
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM notifications WHERE sender_username = ? ORDER BY created_date DESC",
                    new String[]{senderUsername});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Notification notification = new Notification(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getString(cursor.getColumnIndexOrThrow("message")),
                            cursor.getString(cursor.getColumnIndexOrThrow("sender_username")),
                            cursor.getString(cursor.getColumnIndexOrThrow("receiver_username")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("created_date"))
                    );
                    notificationList.add(notification);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return notificationList;
    }

    //  Xóa thông báo
    public void deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notifications", "id = ?", new String[]{String.valueOf(notificationId)});
        db.close();
    }

    //  Lưu đơn hàng
    public long insertOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_name", order.getCustomerName());
        values.put("phone_number", order.getPhoneNumber());
        values.put("order_date", order.getOrderDate());
        values.put("address", order.getAddress());
        values.put("payment_method", order.getPaymentMethod());
        values.put("total_price", order.getTotalPrice());
        values.put("status", order.getStatus());
        values.put("username", order.getUsername());
        
        long orderId = db.insert("orders", null, values);
        
        // Lưu các sản phẩm trong đơn hàng
        if (orderId != -1 && order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("order_id", orderId);
                itemValues.put("product_id", item.getProductId());
                itemValues.put("product_name", item.getProductName());
                itemValues.put("quantity", item.getQuantity());
                itemValues.put("price", item.getPrice());
                db.insert("order_items", null, itemValues);
            }
        }
        
        db.close();
        return orderId;
    }

    //  Lấy tất cả đơn hàng
    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders ORDER BY order_date DESC", null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = new Order(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username"))
                );
                
                // Lấy danh sách sản phẩm trong đơn hàng
                List<Order.OrderItem> items = getOrderItems(order.getId());
                order.setItems(items);
                
                orderList.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return orderList;
    }

    //  Lấy đơn hàng theo username
    public List<Order> getOrdersByUsername(String username) {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE username = ? ORDER BY order_date DESC", 
                new String[]{username});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = new Order(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username"))
                );
                
                // Lấy danh sách sản phẩm trong đơn hàng
                List<Order.OrderItem> items = getOrderItems(order.getId());
                order.setItems(items);
                
                orderList.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return orderList;
    }

    //  Lấy chi tiết đơn hàng theo ID
    public Order getOrderById(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE id = ?", 
                new String[]{String.valueOf(orderId)});
        
        Order order = null;
        if (cursor != null && cursor.moveToFirst()) {
            order = new Order(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")),
                cursor.getString(cursor.getColumnIndexOrThrow("status")),
                cursor.getString(cursor.getColumnIndexOrThrow("username"))
            );
            
            // Lấy danh sách sản phẩm trong đơn hàng
            List<Order.OrderItem> items = getOrderItems(orderId);
            order.setItems(items);
            
            cursor.close();
        }
        db.close();
        return order;
    }

    //  Cập nhật đơn hàng
    public boolean updateOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_name", order.getCustomerName());
        values.put("phone_number", order.getPhoneNumber());
        values.put("order_date", order.getOrderDate());
        values.put("address", order.getAddress());
        values.put("payment_method", order.getPaymentMethod());
        values.put("total_price", order.getTotalPrice());
        values.put("status", order.getStatus());
        values.put("username", order.getUsername());
        
        int rowsAffected = db.update("orders", values, "id = ?", 
                new String[]{String.valueOf(order.getId())});
        db.close();
        
        return rowsAffected > 0;
    }
    
    //  Lấy danh sách sản phẩm trong đơn hàng
    private List<Order.OrderItem> getOrderItems(int orderId) {
        List<Order.OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM order_items WHERE order_id = ?", 
                new String[]{String.valueOf(orderId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order.OrderItem item = new Order.OrderItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                );
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return items;
    }

    //  Cập nhật trạng thái đơn hàng
    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int result = db.update("orders", values, "id = ?", new String[]{String.valueOf(orderId)});
        db.close();
        return result > 0;
    }


    // Lấy tất cả banner đang hoạt động (sắp xếp theo displayOrder)
    public List<Banner> getAllActiveBanners() {
        List<Banner> bannerList = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query("banners", null, "isActive = ?", new String[]{"1"}, null, null, "displayOrder ASC, id ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Banner banner = new Banner(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("displayOrder")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
                    );
                    bannerList.add(banner);
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về danh sách rỗng nếu có lỗi
        }
        return bannerList;
    }

    // Lấy tất cả banner (kể cả không hoạt động)
    public List<Banner> getAllBanners() {
        List<Banner> bannerList = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query("banners", null, null, null, null, null, "displayOrder ASC, id ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Banner banner = new Banner(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("displayOrder")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
                    );
                    bannerList.add(banner);
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về danh sách rỗng nếu có lỗi
        }
        return bannerList;
    }

    // Thêm banner mới
    public long addBanner(String imageUri, int imageResId, int displayOrder, boolean isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("imageUri", imageUri);
        values.put("imageResId", imageResId);
        values.put("displayOrder", displayOrder);
        values.put("isActive", isActive ? 1 : 0);
        long id = db.insert("banners", null, values);
        db.close();
        return id;
    }

    // Cập nhật banner
    public boolean updateBanner(Banner banner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("imageUri", banner.getImageUri());
        values.put("imageResId", banner.getImageResId());
        values.put("displayOrder", banner.getDisplayOrder());
        values.put("isActive", banner.isActive() ? 1 : 0);
        int result = db.update("banners", values, "id = ?", new String[]{String.valueOf(banner.getId())});
        db.close();
        return result > 0;
    }

    // Xóa banner
    public boolean deleteBanner(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("banners", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    // Lấy banner theo ID
    public Banner getBannerById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("banners", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        Banner banner = null;
        if (cursor.moveToFirst()) {
            banner = new Banner(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("displayOrder")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1
            );
        }

        cursor.close();
        db.close();
        return banner;
    }
}
