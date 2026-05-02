# HƯỚNG DẪN TÍCH HỢP VNPAY

## ✅ Đã tích hợp:
1. **VNPayHelper.java** - Class xử lý tạo payment URL và hash
2. **VNPayPaymentActivity.java** - Activity với WebView để thanh toán
3. **CheckoutActivity.java** - Đã cập nhật để chuyển sang VNPay khi chọn QR
4. **AndroidManifest.xml** - Đã thêm activity và intent filter

## 📋 Các bước cấu hình:

### Bước 1: Đăng ký tài khoản test VNPay

#### Cách 1: Đăng ký trực tuyến (Khuyến nghị)
1. **Truy cập trang đăng ký sandbox:**
   - Vào: https://sandbox.vnpayment.vn/devreg/
   - Điền đầy đủ thông tin:
     - Tên công ty/cá nhân
     - Email
     - Số điện thoại
     - Mật khẩu
   
2. **Xác thực email:**
   - Kiểm tra email và click link xác thực
   - Đăng nhập vào sandbox dashboard

3. **Lấy thông tin cấu hình:**
   - Sau khi đăng nhập, vào **"Thông tin tích hợp"** hoặc **"API Integration"**
   - Copy các thông tin sau:
     - **Terminal ID (TMN Code)**: Dạng `2QXUI4J4` (8 ký tự)
     - **Secret Key**: Dạng `RAOCTRKMRBODHZAXDROCSWEYBVKQLLQW` (32 ký tự)
     - **Payment URL**: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`

#### Cách 2: Liên hệ trực tiếp
- **Email hỗ trợ:** kdctt@vnpay.vn
- **Hotline:** 1900 5454 09
- Yêu cầu cung cấp tài khoản test sandbox

#### Tải tài liệu và code mẫu:
- **Tài liệu kỹ thuật:** https://sandbox.vnpayment.vn/apis/downloads/
- **Code mẫu:** Có sẵn cho Java, PHP, C#, Python, NodeJS

### Bước 2: Cập nhật VNPayHelper.java

**Vị trí file:** `CuoiKi/app/src/main/java/com/example/cuoiki/TienIch/VNPayHelper.java`

**Cập nhật dòng 20-22:**

```java
// Thay YOUR_TMN_CODE bằng Terminal ID bạn nhận được (ví dụ: "2QXUI4J4")
private static final String VNPAY_TMN_CODE = "2QXUI4J4"; 

// Thay YOUR_SECRET_KEY bằng Secret Key bạn nhận được (ví dụ: "RAOCTRKMRBODHZAXDROCSWEYBVKQLLQW")
private static final String VNPAY_SECRET_KEY = "RAOCTRKMRBODHZAXDROCSWEYBVKQLLQW";
```

**Ví dụ sau khi cập nhật:**
```java
private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
private static final String VNPAY_TMN_CODE = "2QXUI4J4"; // ⚠️ Terminal ID của bạn
private static final String VNPAY_SECRET_KEY = "RAOCTRKMRBODHZAXDROCSWEYBVKQLLQW"; // ⚠️ Secret Key của bạn
```

**⚠️ Lưu ý quan trọng:**
- Terminal ID thường có 8 ký tự (chữ và số)
- Secret Key thường có 32 ký tự (chữ hoa và số)
- Giữ bí mật Secret Key, không chia sẻ công khai

### Bước 3: Test thanh toán
1. Build và chạy app
2. Thêm sản phẩm vào giỏ hàng
3. Vào checkout, chọn **"Thanh toán qua QR"**
4. Bấm **"Xác nhận đặt hàng"**
5. App sẽ mở WebView với trang thanh toán VNPay
6. Thực hiện thanh toán test
7. Sau khi thanh toán thành công, app sẽ tự động:
   - Cập nhật trạng thái đơn hàng
   - Gửi email xác nhận
   - Chuyển đến màn hình thành công

## 🔧 Cấu hình môi trường:

### Sandbox (Test):
```java
private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
```

### Production:
```java
private static final String VNPAY_URL = "https://www.vnpayment.vn/paymentv2/vpcpay.html";
```

## ⚠️ Lưu ý:
- **Secret Key**: Giữ bí mật, không commit lên Git
- **IP Address**: Hiện tại dùng IP mặc định `127.0.0.1` cho test
- **Callback URL**: `vnpay://return` - đã cấu hình trong AndroidManifest
- **Hash**: Tự động tạo SHA512 hash để bảo mật

## 🐛 Debug:
- Kiểm tra Logcat với tag `VNPayHelper` và `VNPayPaymentActivity`
- Kiểm tra payment URL có được tạo đúng không
- Kiểm tra callback URL có được xử lý đúng không

## 📞 Hỗ trợ và liên hệ:

### VNPay Support:
- **Email:** kdctt@vnpay.vn
- **Hotline:** 1900 5454 09
- **Website:** https://sandbox.vnpayment.vn/
- **Tài liệu:** https://sandbox.vnpayment.vn/apis/downloads/

### Thông tin thường gặp:
- **Terminal ID không hoạt động:** Kiểm tra lại đã copy đúng chưa (không có khoảng trắng)
- **Secret Key sai:** Đảm bảo copy đầy đủ 32 ký tự
- **Payment URL lỗi:** Kiểm tra URL sandbox có đúng không
- **Callback không hoạt động:** Kiểm tra AndroidManifest đã cấu hình intent filter chưa

### Tài khoản test mẫu (nếu VNPay cung cấp):
Một số trường hợp VNPay cung cấp tài khoản test mẫu để demo. Kiểm tra trong email hoặc dashboard sau khi đăng ký.

