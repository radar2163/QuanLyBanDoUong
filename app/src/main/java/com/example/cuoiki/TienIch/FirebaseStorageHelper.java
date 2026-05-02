package com.example.cuoiki.TienIch;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Helper class để upload/download ảnh từ Firebase Storage
 * Giải quyết vấn đề ảnh không hiển thị khi chia sẻ code
 */
public class FirebaseStorageHelper {
    
    private static final String TAG = "FirebaseStorage";
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    
    /**
     * Khởi tạo Firebase Storage
     */
    private static void init() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
        }
    }
    
    /**
     * Upload ảnh sản phẩm lên Firebase Storage
     * @param context Context
     * @param imageUri URI của ảnh cần upload
     * @param productId ID của sản phẩm
     * @param callback Callback khi upload xong (trả về URL)
     */
    public static void uploadProductImage(Context context, Uri imageUri, int productId, 
                                         ImageUploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) {
                callback.onFailure("Không có ảnh để upload");
            }
            return;
        }
        
        init();
        
        // Tạo đường dẫn: images/products/product_1.jpg
        String fileName = "products/product_" + productId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);
        
        // Upload ảnh
        UploadTask uploadTask = imageRef.putFile(imageUri);
        
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload thành công, lấy URL công khai
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        String imageUrl = downloadUri.toString();
                        android.util.Log.d(TAG, "Upload thành công: " + imageUrl);
                        
                        if (callback != null) {
                            callback.onSuccess(imageUrl);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        android.util.Log.e(TAG, "Lỗi lấy URL: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure("Lỗi lấy URL: " + e.getMessage());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                android.util.Log.e(TAG, "Upload thất bại: " + e.getMessage());
                if (callback != null) {
                    callback.onFailure("Upload thất bại: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Upload ảnh banner lên Firebase Storage
     */
    public static void uploadBannerImage(Context context, Uri imageUri, int bannerId,
                                        ImageUploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) {
                callback.onFailure("Không có ảnh để upload");
            }
            return;
        }
        
        init();
        
        // Tạo đường dẫn: images/banners/banner_1.jpg
        String fileName = "banners/banner_" + bannerId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);
        
        // Upload ảnh
        UploadTask uploadTask = imageRef.putFile(imageUri);
        
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        String imageUrl = downloadUri.toString();
                        android.util.Log.d(TAG, "Upload banner thành công: " + imageUrl);
                        
                        if (callback != null) {
                            callback.onSuccess(imageUrl);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) {
                            callback.onFailure("Lỗi lấy URL: " + e.getMessage());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null) {
                    callback.onFailure("Upload thất bại: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Xóa ảnh từ Firebase Storage
     */
    public static void deleteImage(String imageUrl, OnDeleteCallback callback) {
        if (imageUrl == null || imageUrl.isEmpty() || !isFirebaseStorageUrl(imageUrl)) {
            // Không phải URL Firebase, không cần xóa
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        
        init();
        
        try {
            // Extract path từ URL
            // URL format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{path}?alt=media&token=...
            // Cần extract path từ URL
            String path = extractPathFromUrl(imageUrl);
            if (path == null) {
                if (callback != null) {
                    callback.onFailure("Không thể extract path từ URL");
                }
                return;
            }
            
            // Lấy reference từ path
            StorageReference imageRef = storageRef.child(path);
            
            // Xóa file
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    android.util.Log.d(TAG, "Xóa ảnh thành công: " + imageUrl);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    android.util.Log.e(TAG, "Xóa ảnh thất bại: " + e.getMessage());
                    // Không báo lỗi nếu file không tồn tại (có thể đã bị xóa trước đó)
                    if (callback != null) {
                        callback.onSuccess(); // Vẫn coi là thành công
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Lỗi xóa ảnh: " + e.getMessage());
            if (callback != null) {
                callback.onSuccess(); // Vẫn coi là thành công để không block flow
            }
        }
    }
    
    /**
     * Extract path từ Firebase Storage URL
     * URL format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{encodedPath}?alt=media&token=...
     */
    private static String extractPathFromUrl(String url) {
        try {
            // Tìm phần /o/ trong URL
            int startIndex = url.indexOf("/o/");
            if (startIndex == -1) return null;
            
            startIndex += 3; // Bỏ qua "/o/"
            
            // Tìm dấu ? (bắt đầu query string)
            int endIndex = url.indexOf("?", startIndex);
            if (endIndex == -1) {
                endIndex = url.length();
            }
            
            String encodedPath = url.substring(startIndex, endIndex);
            // Decode URL encoding
            return java.net.URLDecoder.decode(encodedPath, "UTF-8");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Lỗi extract path: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Kiểm tra xem URL có phải từ Firebase Storage không
     */
    public static boolean isFirebaseStorageUrl(String url) {
        return url != null && url.startsWith("https://firebasestorage.googleapis.com/");
    }
    
    /**
     * Callback khi upload ảnh
     */
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);  // URL công khai từ Firebase Storage
        void onFailure(String error);
    }
    
    /**
     * Callback khi xóa ảnh
     */
    public interface OnDeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }
}

