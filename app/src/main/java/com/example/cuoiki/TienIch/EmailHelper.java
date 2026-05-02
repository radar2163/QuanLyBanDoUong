package com.example.cuoiki.TienIch;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.cuoiki.DuLieu.Order;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class EmailHelper {
    
    

    public static void sendOrderConfirmationEmail(Context context, Order order) {
        try {
            // Lấy email của khách hàng
            String customerEmail = getCustomerEmail(context, order.getUsername());
            
            if (customerEmail == null || customerEmail.isEmpty()) {
                Toast.makeText(context, "Không tìm thấy email của khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Tạo nội dung email
            String subject = "Xác nhận đơn hàng #" + order.getId();
            String body = createOrderEmailBody(order);
            
            // Gửi email tự động qua SMTP (chạy trong background thread)
            new Thread(() -> {
                try {
                    boolean success = EmailSender.sendEmail(customerEmail, subject, body);
                    
                    // Hiển thị kết quả trên UI thread
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        if (success) {
                            Toast.makeText(context, "Email xác nhận đã được gửi đến " + customerEmail, Toast.LENGTH_LONG).show();
                        } else {
                            // Hiển thị thông báo lỗi chi tiết hơn
                            android.util.Log.e("EmailHelper", "Không thể gửi email đến: " + customerEmail);
                            Toast.makeText(context, "Không thể gửi email. Vui lòng kiểm tra Logcat để xem chi tiết lỗi.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("EmailHelper", "Exception khi gửi email: " + e.getMessage(), e);
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi gửi email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Lấy email của khách hàng từ database hoặc SharedPreferences
     */
    private static String getCustomerEmail(Context context, String username) {
        if (username == null || username.isEmpty()) {
            // Thử lấy từ SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            return prefs.getString("user_email", null);
        }
        
        // Lấy từ database
        SQLiteHelper dbHelper = new SQLiteHelper(context);
        com.example.cuoiki.DuLieu.User user = dbHelper.getUserByUsername(username);
        
        if (user != null && user.getGmail() != null && !user.getGmail().isEmpty()) {
            return user.getGmail();
        }
        
        // Fallback: lấy từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_email", null);
    }
    

    private static String createOrderEmailBody(Order order) {
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        
        // Format ngày đặt hàng với giờ phút
        String orderDateFormatted = formatOrderDateWithTime(order.getOrderDate());
        
        // Tạo HTML email với CSS inline
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("</head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px;'>");
        
        // Container chính
        html.append("<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>");
        
        // Header với màu nền
        html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center;'>");
        html.append("<h1 style='margin: 0; font-size: 28px;'>Xác nhận đơn hàng</h1>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;'>Cảm ơn bạn đã đặt hàng!</p>");
        html.append("</div>");
        
        // Nội dung chính
        html.append("<div style='padding: 30px;'>");
        
        // Lời chào
        html.append("<p style='font-size: 16px; margin-bottom: 20px;'>Xin chào <strong>").append(escapeHtml(order.getCustomerName())).append("</strong>,</p>");
        html.append("<p style='font-size: 16px; margin-bottom: 30px; color: #666;'>Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi!</p>");
        
        // Bảng thông tin đơn hàng
        html.append("<div style='background-color: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 30px; border-left: 4px solid #667eea;'>");
        html.append("<h2 style='margin-top: 0; color: #667eea; font-size: 20px; margin-bottom: 20px;'>THÔNG TIN ĐƠN HÀNG</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        
        addTableRow(html, "Mã đơn hàng", "#" + order.getId(), true);
        addTableRow(html, "Ngày đặt hàng", orderDateFormatted, false);
        addTableRow(html, "Họ tên", escapeHtml(order.getCustomerName()), false);
        addTableRow(html, "Số điện thoại", escapeHtml(order.getPhoneNumber()), false);
        addTableRow(html, "Địa chỉ giao hàng", escapeHtml(order.getAddress()), false);
        addTableRow(html, "Phương thức thanh toán", escapeHtml(order.getPaymentMethod()), false);
        addTableRow(html, "Trạng thái", "<span style='background-color: #28a745; color: white; padding: 5px 12px; border-radius: 5px; font-weight: bold;'>" + escapeHtml(order.getStatus()) + "</span>", false);
        
        html.append("</table>");
        html.append("</div>");
        
        // Bảng chi tiết sản phẩm
        html.append("<h2 style='color: #667eea; font-size: 20px; margin-bottom: 15px;'>CHI TIẾT ĐƠN HÀNG</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px; background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1);'>");
        
        // Header của bảng sản phẩm
        html.append("<thead>");
        html.append("<tr style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;'>");
        html.append("<th style='padding: 15px; text-align: left; border: none;'>STT</th>");
        html.append("<th style='padding: 15px; text-align: left; border: none;'>Sản phẩm</th>");
        html.append("<th style='padding: 15px; text-align: center; border: none;'>SL</th>");
        html.append("<th style='padding: 15px; text-align: right; border: none;'>Đơn giá</th>");
        html.append("<th style='padding: 15px; text-align: right; border: none;'>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        List<Order.OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            int stt = 1;
            boolean isEven = false;
            for (Order.OrderItem item : items) {
                double itemTotal = item.getPrice() * item.getQuantity();
                String rowColor = isEven ? "#f8f9fa" : "#ffffff";
                html.append("<tr style='background-color: ").append(rowColor).append(";'>");
                html.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef;'>").append(stt).append("</td>");
                html.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef; font-weight: 500;'>").append(escapeHtml(item.getProductName())).append("</td>");
                html.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef; text-align: center;'>").append(item.getQuantity()).append("</td>");
                html.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef; text-align: right;'>").append(currencyFormat.format(item.getPrice())).append(" đ</td>");
                html.append("<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef; text-align: right; font-weight: bold; color: #28a745;'>").append(currencyFormat.format(itemTotal)).append(" đ</td>");
                html.append("</tr>");
                stt++;
                isEven = !isEven;
            }
        }
        
        html.append("</tbody>");
        html.append("</table>");
        
        // Tổng tiền
        html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: right; margin-top: 20px;'>");
        html.append("<div style='font-size: 18px; font-weight: bold;'>TỔNG TIỀN: <span style='font-size: 24px;'>").append(currencyFormat.format(order.getTotalPrice())).append(" đ</span></div>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 2px solid #e9ecef; color: #666;'>");
        html.append("<p style='margin-bottom: 10px;'>Chúng tôi sẽ xử lý đơn hàng của bạn trong thời gian sớm nhất.</p>");
        html.append("<p style='margin-bottom: 0;'>Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.</p>");
        html.append("</div>");
        
        html.append("</div>"); // End nội dung chính
        
        // Footer cuối
        html.append("<div style='background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; border-top: 1px solid #e9ecef;'>");
        html.append("<p style='margin: 0; font-size: 14px;'><strong>Cửa hàng của chúng tôi</strong></p>");
        html.append("<p style='margin: 5px 0 0 0; font-size: 12px;'>Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi!</p>");
        html.append("</div>");
        
        html.append("</div>"); // End container
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    

    private static void addTableRow(StringBuilder html, String label, String value, boolean isBold) {
        html.append("<tr>");
        html.append("<td style='padding: 10px 0; font-weight: bold; color: #495057; width: 40%;'>").append(escapeHtml(label)).append(":</td>");
        if (isBold) {
            html.append("<td style='padding: 10px 0; color: #667eea; font-size: 18px; font-weight: bold;'>").append(value).append("</td>");
        } else {
            html.append("<td style='padding: 10px 0; color: #212529;'>").append(value).append("</td>");
        }
        html.append("</tr>");
    }
    

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Format ngày đặt hàng với giờ phút
     * Nếu orderDate đã có giờ phút thì giữ nguyên, nếu không thì thêm giờ phút hiện tại
     */
    private static String formatOrderDateWithTime(String orderDate) {
        if (orderDate == null || orderDate.isEmpty()) {
            // Nếu không có ngày, dùng ngày giờ hiện tại
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date());
        }
        
        // Kiểm tra xem orderDate đã có giờ phút chưa (có chứa HH:mm hoặc HH:mm:ss)
        if (orderDate.matches(".*\\d{2}:\\d{2}.*")) {
            // Đã có giờ phút, trả về nguyên bản
            return orderDate;
        }
        
        // Nếu chỉ có ngày, thêm giờ phút hiện tại
        try {
            // Thử parse các format ngày thường gặp
            SimpleDateFormat inputFormat;
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            
            // Thử format: dd/MM/yyyy
            if (orderDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(orderDate);
                return outputFormat.format(date);
            }
            // Thử format: yyyy-MM-dd
            else if (orderDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(orderDate);
                return outputFormat.format(date);
            }
            // Nếu không match format nào, thêm giờ phút hiện tại vào cuối
            else {
                Date now = new Date();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return orderDate + " " + timeFormat.format(now);
            }
        } catch (Exception e) {
            // Nếu parse lỗi, thêm giờ phút hiện tại vào cuối
            Date now = new Date();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return orderDate + " " + timeFormat.format(now);
        }
    }
}

