package com.example.cuoiki.TienIch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.Banner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Helper class để export/import ảnh khi chia sẻ project
 * Giải quyết vấn đề ảnh không hiển thị khi chuyển máy
 */
public class ImageExportHelper {
    
    private static final String TAG = "ImageExportHelper";
    private static final String IMAGE_DIR = "app_images";  // Thư mục lưu ảnh trong app
    
    /**
     * Copy ảnh từ URI vào thư mục internal storage của app
     * Trả về đường dẫn mới (có thể dùng trên mọi máy)
     */
    public static String copyImageToAppStorage(Context context, Uri imageUri) {
        if (imageUri == null) return null;
        
        try {
            // Tạo thư mục lưu ảnh trong internal storage
            File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            
            // Tạo tên file duy nhất
            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(imageDir, fileName);
            
            // Copy file
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
                
                // Trả về đường dẫn có thể dùng lại
                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying image: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Load ảnh từ đường dẫn đã lưu
     */
    public static Bitmap loadImageFromPath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem ảnh có tồn tại không
     */
    public static boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;
        
        // Nếu là URI (content:// hoặc file://), kiểm tra khác
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            try {
                Uri uri = Uri.parse(imagePath);
                File file = new File(uri.getPath());
                return file.exists();
            } catch (Exception e) {
                return false;
            }
        }
        
        // Nếu là đường dẫn file thông thường
        File file = new File(imagePath);
        return file.exists();
    }
    
    /**
     * Export tất cả ảnh từ database vào thư mục export
     * Để chia sẻ cùng với database
     */
    public static void exportAllImages(Context context, SQLiteHelper dbHelper, String exportDirPath) {
        try {
            File exportDir = new File(exportDirPath);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            // Export ảnh sản phẩm
            List<Product> products = dbHelper.getAllProducts();
            for (Product product : products) {
                if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
                    copyImageToExport(context, product.getImageUri(), exportDir, "product_" + product.getId() + ".jpg");
                }
            }
            
            // Export ảnh banner
            List<Banner> banners = dbHelper.getAllBanners();
            for (Banner banner : banners) {
                if (banner.getImageUri() != null && !banner.getImageUri().isEmpty()) {
                    copyImageToExport(context, banner.getImageUri(), exportDir, "banner_" + banner.getId() + ".jpg");
                }
            }
            
            Log.d(TAG, "Export images completed to: " + exportDirPath);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void copyImageToExport(Context context, String imageUriString, File exportDir, String fileName) {
        try {
            Uri imageUri = Uri.parse(imageUriString);
            File destFile = new File(exportDir, fileName);
            
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying image to export: " + e.getMessage());
        }
    }
}

