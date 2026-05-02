# TRÌNH BÀY TÍNH NĂNG THANH TOÁN VNPAY VÀ GỬI EMAIL THÔNG BÁO
## Ứng dụng Cuối Kỳ - Tích Hợp Payment Gateway & Email Notification

---

## 1. TỔNG QUAN

### 1.1. Mục đích
Ứng dụng tích hợp 2 tính năng chính:
- **Thanh toán VNPay**: Cho phép khách hàng thanh toán đơn hàng qua cổng thanh toán VNPay (QR code)
- **Gửi email thông báo**: Tự động gửi email xác nhận đơn hàng đến khách hàng sau khi đặt hàng thành công

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **Payment Gateway**: VNPay Sandbox API
- **Email**: JavaMail API (SMTP)
- **Security**: HMAC SHA512 hashing
- **UI**: WebView (hiển thị trang thanh toán VNPay)

---

## 2. TÍNH NĂNG THANH TOÁN VNPAY

### 2.1. Tổng quan VNPay Integration

#### A. Quy trình thanh toán
```
1. Khách hàng chọn "Thanh toán qua QR" trong CheckoutActivity
2. CheckoutActivity tạo payment URL bằng VNPayHelper
3. Mở VNPayPaymentActivity với WebView
4. Khách hàng thanh toán trên trang VNPay
5. VNPay redirect về callback URL (vnpay://return)
6. VNPayPaymentActivity xử lý callback và verify hash
7. Trả kết quả về CheckoutActivity
8. CheckoutActivity cập nhật đơn hàng và gửi email
```

#### B. Các thành phần chính
- **VNPayHelper**: Tạo payment URL, verify response, hash generation
- **VNPayPaymentActivity**: WebView để hiển thị trang thanh toán, xử lý callback
- **CheckoutActivity**: Khởi tạo thanh toán, xử lý kết quả

---

### 2.2. VNPayHelper.java

#### A. Cấu hình VNPay
```java
// Cấu hình VNPay Sandbox (Test)
private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
private static final String VNPAY_TMN_CODE = "<redacted>";  // Terminal Code
private static final String VNPAY_SECRET_KEY = "<redacted>";  // Secret Key
private static final String VNPAY_RETURN_URL = "vnpay://return";  // Callback URL
```

#### B. Tạo Payment URL
```java
public static String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
    // Tạo TxnRef duy nhất bằng cách thêm timestamp
    SimpleDateFormat txnRefFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    String uniqueTxnRef = orderId + "_" + txnRefFormatter.format(new Date());
    
    Map<String, String> vnpParams = new HashMap<>();
    
    // Thông tin bắt buộc
    vnpParams.put("vnp_Version", "2.1.0");
    vnpParams.put("vnp_Command", "pay");
    vnpParams.put("vnp_TmnCode", VNPAY_TMN_CODE);
    vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
    vnpParams.put("vnp_CurrCode", "VND");
    vnpParams.put("vnp_TxnRef", uniqueTxnRef); // Dùng unique TxnRef
    vnpParams.put("vnp_OrderInfo", orderInfo);
    vnpParams.put("vnp_OrderType", "other");
    vnpParams.put("vnp_Locale", "vn");
    vnpParams.put("vnp_ReturnUrl", VNPAY_RETURN_URL);
    vnpParams.put("vnp_IpAddr", ipAddress);
    vnpParams.put("vnp_CreateDate", formatter.format(new Date()));
    
    // Sắp xếp params theo alphabet
    List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
    Collections.sort(fieldNames);
    
    // Tạo hash data và query string
    StringBuilder queryString = new StringBuilder();
    StringBuilder hashData = new StringBuilder();
    
    for (String fieldName : fieldNames) {
        String fieldValue = vnpParams.get(fieldName);
        if (fieldValue != null && fieldValue.length() > 0) {
            // Hash data: fieldName=encodedValue
            hashData.append(fieldName).append('=');
            hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
            
            // Query string: encodedFieldName=encodedValue
            queryString.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
            queryString.append('=');
            queryString.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
        }
    }
    
    // Tạo secure hash (HMAC SHA512)
    String vnp_SecureHash = hmacSHA512(VNPAY_SECRET_KEY, hashData.toString());
    
    // Thêm hash vào query string
    queryString.append("&vnp_SecureHash=").append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8.toString()));
    
    return VNPAY_URL + "?" + queryString.toString();
}
```

#### C. HMAC SHA512 Hashing
```java
private static String hmacSHA512(String key, String data) {
    Mac hmac = Mac.getInstance("HmacSHA512");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    hmac.init(secretKey);
    byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    
    // Convert to hex string (uppercase)
    StringBuilder hexString = new StringBuilder();
    for (byte hashByte : hashBytes) {
        String hex = Integer.toHexString(0xff & hashByte);
        if (hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
    }
    return hexString.toString().toUpperCase(); // VNPay yêu cầu chữ hoa
}
```

#### D. Verify Response
```java
public static boolean verifyResponse(Map<String, String> vnpParams) {
    String vnp_SecureHash = vnpParams.remove("vnp_SecureHash");
    if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
        return false;
    }
    
    // Sắp xếp params và tạo hash data (giống như khi tạo URL)
    List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
    Collections.sort(fieldNames);
    
    StringBuilder hashData = new StringBuilder();
    for (String fieldName : fieldNames) {
        String fieldValue = vnpParams.get(fieldName);
        if (fieldValue != null && fieldValue.length() > 0) {
            hashData.append(fieldName).append('=');
            hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
        }
    }
    
    // Tính hash và so sánh (không phân biệt hoa thường)
    String calculatedHash = hmacSHA512(VNPAY_SECRET_KEY, hashData.toString());
    return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
}
```

---

### 2.3. VNPayPaymentActivity.java

#### A. Setup WebView
```java
@SuppressLint("SetJavaScriptEnabled")
private void setupWebView() {
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setLoadWithOverviewMode(true);
    webView.getSettings().setUseWideViewPort(true);
    
    webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Kiểm tra callback URL từ VNPay
            if (url != null && url.startsWith("vnpay://return")) {
                handleVNPayCallback(url);
                return;
            }
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url != null && url.startsWith("vnpay://return")) {
                handleVNPayCallback(url);
                return true; // Đã xử lý
            }
            return false;
        }
    });
}
```

#### B. Xử lý Callback
```java
private void handleVNPayCallback(String callbackUrl) {
    // Parse URL để lấy các tham số
    Map<String, String> params = parseCallbackUrl(callbackUrl);
    
    // Xác thực response
    boolean isValid = VNPayHelper.verifyResponse(new HashMap<>(params));
    
    // Lấy response code
    String responseCode = params.get("vnp_ResponseCode");
    String transactionStatus = params.get("vnp_TransactionStatus");
    
    // Kiểm tra kết quả thanh toán
    // ResponseCode = "00" và TransactionStatus = "00" nghĩa là thành công
    if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
        // Thanh toán thành công
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_status", "success");
        resultIntent.putExtra("order_id_long", orderIdLong);
        setResult(RESULT_OK, resultIntent);
        finish();
    } else {
        // Thanh toán thất bại
        String errorMessage = getErrorMessage(responseCode);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("payment_status", "failed");
        resultIntent.putExtra("error_message", errorMessage);
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}
```

#### C. Parse Callback URL
```java
private Map<String, String> parseCallbackUrl(String url) {
    Map<String, String> params = new HashMap<>();
    
    int queryIndex = url.indexOf("?");
    if (queryIndex == -1) {
        return params;
    }
    
    String query = url.substring(queryIndex + 1);
    String[] pairs = query.split("&");
    
    for (String pair : pairs) {
        int equalIndex = pair.indexOf("=");
        if (equalIndex == -1) continue;
        
        String key = URLDecoder.decode(pair.substring(0, equalIndex), "UTF-8");
        String value = URLDecoder.decode(pair.substring(equalIndex + 1), "UTF-8");
        params.put(key, value);
    }
    
    return params;
}
```

---

### 2.4. Tích hợp trong CheckoutActivity

#### A. Xử lý thanh toán VNPay
```java
private void processVNPayPayment(long orderId) {
    try {
        String paymentUrl = VNPayHelper.createPaymentUrl(
            (int) orderId,
            totalPrice,
            "Thanh toan don hang #" + orderId,
            VNPayHelper.getIpAddress()
        );
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không thể tạo URL thanh toán.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Chuyển đến màn hình thanh toán VNPay
        Intent intent = new Intent(this, VNPayPaymentActivity.class);
        intent.putExtra("payment_url", paymentUrl);
        intent.putExtra("order_id_long", orderId);
        startActivityForResult(intent, 1001);
    } catch (Exception e) {
        Toast.makeText(this, "Lỗi không thể xử lý thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```

#### B. Xử lý kết quả thanh toán
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == 1001) {
        if (resultCode == RESULT_OK && data != null) {
            String paymentStatus = data.getStringExtra("payment_status");
            long orderId = data.getLongExtra("order_id_long", -1);
            
            if ("success".equals(paymentStatus) && orderId > 0) {
                // Cập nhật đơn hàng
                Order order = dbHelper.getOrderById((int) orderId);
                if (order != null) {
                    order.setPaymentMethod("Thanh toán qua VNPay");
                    order.setStatus("Đã thanh toán");
                    dbHelper.updateOrder(order);
                    
                    // Gửi email xác nhận
                    EmailHelper.sendOrderConfirmationEmail(this, order);
                }
                
                // Xóa giỏ hàng và chuyển màn hình
                clearCart();
                Intent intent = new Intent(this, OrderSuccessActivity.class);
                intent.putExtra("order_id", orderId);
                startActivity(intent);
                finish();
            }
        }
    }
}
```

---

## 3. TÍNH NĂNG GỬI EMAIL THÔNG BÁO

### 3.1. Tổng quan Email Integration

#### A. Quy trình gửi email
```
1. Đơn hàng được tạo thành công (COD hoặc VNPay)
2. EmailHelper.sendOrderConfirmationEmail() được gọi
3. Lấy email khách hàng từ database hoặc SharedPreferences
4. Tạo nội dung email HTML với thông tin đơn hàng
5. EmailSender.sendEmail() gửi email qua SMTP
6. Hiển thị Toast thông báo kết quả
```

#### B. Các thành phần chính
- **EmailHelper**: Tạo nội dung email, lấy email khách hàng
- **EmailSender**: Gửi email qua SMTP server (Gmail, SendGrid, v.v.)

---

### 3.2. EmailSender.java

#### A. Cấu hình SMTP
```java
// Cấu hình Gmail SMTP
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String SMTP_USERNAME = "<redacted>";
private static final String SMTP_PASSWORD = "<redacted>"; // App Password
private static final String FROM_EMAIL = "<redacted>";
```

#### B. Gửi Email
```java
public static boolean sendEmail(String toEmail, String subject, String body) {
    try {
        // Cấu hình properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Tạo session với authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        
        // Tạo message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        
        // Kiểm tra nếu body là HTML
        if (body.trim().startsWith("<html") || body.trim().startsWith("<!DOCTYPE")) {
            message.setContent(body, "text/html; charset=utf-8");
        } else {
            message.setText(body);
        }
        
        // Gửi email
        Transport.send(message);
        return true;
        
    } catch (Exception e) {
        Log.e(TAG, "Lỗi khi gửi email: " + e.getMessage(), e);
        return false;
    }
}
```

---

### 3.3. EmailHelper.java

#### A. Gửi Email Xác Nhận Đơn Hàng
```java
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
                        Toast.makeText(context, "Không thể gửi email. Vui lòng kiểm tra Logcat.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e("EmailHelper", "Exception khi gửi email: " + e.getMessage(), e);
            }
        }).start();
        
    } catch (Exception e) {
        Toast.makeText(context, "Lỗi khi gửi email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
```

#### B. Tạo Nội Dung Email HTML
```java
private static String createOrderEmailBody(Order order) {
    DecimalFormat currencyFormat = new DecimalFormat("#,###");
    String orderDateFormatted = formatOrderDateWithTime(order.getOrderDate());
    
    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>");
    html.append("<html>");
    html.append("<head>");
    html.append("<meta charset='UTF-8'>");
    html.append("</head>");
    html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
    
    // Header với gradient
    html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center;'>");
    html.append("<h1 style='margin: 0; font-size: 28px;'>Xác nhận đơn hàng</h1>");
    html.append("</div>");
    
    // Thông tin đơn hàng
    html.append("<div style='padding: 30px;'>");
    html.append("<h2>THÔNG TIN ĐƠN HÀNG</h2>");
    html.append("<table style='width: 100%;'>");
    html.append("<tr><td>Mã đơn hàng:</td><td><strong>#").append(order.getId()).append("</strong></td></tr>");
    html.append("<tr><td>Ngày đặt hàng:</td><td>").append(orderDateFormatted).append("</td></tr>");
    html.append("<tr><td>Họ tên:</td><td>").append(escapeHtml(order.getCustomerName())).append("</td></tr>");
    html.append("<tr><td>Số điện thoại:</td><td>").append(escapeHtml(order.getPhoneNumber())).append("</td></tr>");
    html.append("<tr><td>Địa chỉ:</td><td>").append(escapeHtml(order.getAddress())).append("</td></tr>");
    html.append("<tr><td>Phương thức thanh toán:</td><td>").append(escapeHtml(order.getPaymentMethod())).append("</td></tr>");
    html.append("</table>");
    
    // Chi tiết sản phẩm
    html.append("<h2>CHI TIẾT ĐƠN HÀNG</h2>");
    html.append("<table style='width: 100%; border-collapse: collapse;'>");
    html.append("<thead>");
    html.append("<tr style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;'>");
    html.append("<th style='padding: 15px; text-align: left;'>STT</th>");
    html.append("<th style='padding: 15px; text-align: left;'>Sản phẩm</th>");
    html.append("<th style='padding: 15px; text-align: center;'>SL</th>");
    html.append("<th style='padding: 15px; text-align: right;'>Đơn giá</th>");
    html.append("<th style='padding: 15px; text-align: right;'>Thành tiền</th>");
    html.append("</tr>");
    html.append("</thead>");
    html.append("<tbody>");
    
    List<Order.OrderItem> items = order.getItems();
    if (items != null && !items.isEmpty()) {
        int stt = 1;
        for (Order.OrderItem item : items) {
            double itemTotal = item.getPrice() * item.getQuantity();
            html.append("<tr>");
            html.append("<td style='padding: 12px 15px;'>").append(stt).append("</td>");
            html.append("<td style='padding: 12px 15px;'>").append(escapeHtml(item.getProductName())).append("</td>");
            html.append("<td style='padding: 12px 15px; text-align: center;'>").append(item.getQuantity()).append("</td>");
            html.append("<td style='padding: 12px 15px; text-align: right;'>").append(currencyFormat.format(item.getPrice())).append(" đ</td>");
            html.append("<td style='padding: 12px 15px; text-align: right; font-weight: bold; color: #28a745;'>").append(currencyFormat.format(itemTotal)).append(" đ</td>");
            html.append("</tr>");
            stt++;
        }
    }
    
    html.append("</tbody>");
    html.append("</table>");
    
    // Tổng tiền
    html.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: right; margin-top: 20px;'>");
    html.append("<div style='font-size: 18px; font-weight: bold;'>TỔNG TIỀN: <span style='font-size: 24px;'>").append(currencyFormat.format(order.getTotalPrice())).append(" đ</span></div>");
    html.append("</div>");
    
    html.append("</div>");
    html.append("</body>");
    html.append("</html>");
    
    return html.toString();
}
```

---

## 4. CÁC TÍNH NĂNG CHÍNH

### 4.1. Thanh toán VNPay
- ✅ Tạo payment URL với HMAC SHA512 hash
- ✅ WebView hiển thị trang thanh toán VNPay
- ✅ Xử lý callback từ VNPay
- ✅ Verify response hash để đảm bảo an toàn
- ✅ Cập nhật trạng thái đơn hàng sau thanh toán
- ✅ Xử lý lỗi và hiển thị thông báo phù hợp

### 4.2. Gửi Email Thông Báo
- ✅ Gửi email HTML đẹp mắt với thông tin đơn hàng
- ✅ Tự động lấy email khách hàng từ database
- ✅ Background thread để không block UI
- ✅ Error handling và logging chi tiết
- ✅ Toast notification cho user

---

## 5. BẢO MẬT

### 5.1. VNPay Security
- **HMAC SHA512**: Hash tất cả parameters để đảm bảo tính toàn vẹn
- **Unique TxnRef**: Thêm timestamp để tránh duplicate transaction
- **Hash Verification**: Verify response từ VNPay để đảm bảo không bị giả mạo

### 5.2. Email Security
- **SMTP Authentication**: Sử dụng username/password để xác thực
- **TLS/SSL**: Kết nối được mã hóa qua STARTTLS
- **App Password**: Sử dụng App Password thay vì mật khẩu chính (Gmail)

---

## 6. LIFECYCLE VÀ PERFORMANCE

### 6.1. Background Processing
- Email được gửi trong background thread để không block UI
- WebView load payment URL không block main thread

### 6.2. Error Handling
- Try-catch cho tất cả operations
- Logging chi tiết để debug
- User-friendly error messages

---

## 7. KẾT LUẬN

Hệ thống thanh toán VNPay và gửi email thông báo được xây dựng với:
- **Bảo mật cao**: HMAC SHA512, hash verification
- **User experience tốt**: WebView mượt mà, email đẹp mắt
- **Error handling đầy đủ**: Xử lý mọi trường hợp lỗi
- **Code clean**: Tách biệt logic, dễ maintain

**Công nghệ nổi bật:**
1. VNPay Payment Gateway integration
2. HMAC SHA512 hashing cho security
3. JavaMail API cho email
4. HTML email với CSS inline
5. WebView cho payment flow

---

## 8. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

### 8.1. Quy trình thanh toán VNPay
```
┌─────────────────┐
│ CheckoutActivity│
│  Chọn QR        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  VNPayHelper    │
│  Tạo URL + Hash │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│VNPayPaymentAct. │
│  WebView        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  VNPay Server   │
│  Thanh toán     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Callback       │
│  vnpay://return │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Verify Hash    │
│  Update Order   │
│  Send Email     │
└─────────────────┘
```

### 8.2. Quy trình gửi email
```
┌─────────────────┐
│  Order Created  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  EmailHelper    │
│  Get Email      │
│  Create Body    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  EmailSender    │
│  SMTP Send      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Customer Email │
│  (HTML)         │
└─────────────────┘
```

---

*Tài liệu này có thể copy vào Word và chỉnh sửa theo nhu cầu trình bày.*

