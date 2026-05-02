# HƯỚNG DẪN DEBUG VNPAY

## 🔍 Kiểm tra lỗi "Sai chữ ký":

### Bước 1: Xem log chi tiết
1. Mở **Logcat** trong Android Studio
2. Filter theo tag: `VNPayHelper`
3. Khi thanh toán, bạn sẽ thấy:
   - Hash Data (before HMAC)
   - Secret Key
   - Secure Hash
   - Payment URL

### Bước 2: Kiểm tra thông tin
1. **Terminal Code (TMN Code):**
   - Phải có 8 ký tự
   - Ví dụ: `<redacted>`
   - Kiểm tra trong file `VNPayHelper.java` dòng 30

2. **Secret Key:**
   - Phải có 32 ký tự
   - Ví dụ: `<redacted>`
   - Kiểm tra trong file `VNPayHelper.java` dòng 31

### Bước 3: So sánh với VNPay Dashboard
1. Đăng nhập vào VNPay Sandbox Dashboard
2. Vào **"Thông tin tích hợp"** hoặc **"API Integration"**
3. So sánh:
   - Terminal Code có khớp không?
   - Secret Key có khớp không?

### Bước 4: Test với code mẫu VNPay
1. Tải code mẫu từ: https://sandbox.vnpayment.vn/apis/downloads/
2. So sánh cách tạo hash với code của bạn
3. Đảm bảo:
   - Hash data không encode
   - Query string có encode
   - Hash dùng HMAC SHA512
   - Hash trả về chữ hoa

## ⚠️ Các lỗi thường gặp:

### 1. Secret Key sai
- **Triệu chứng:** "Sai chữ ký"
- **Giải pháp:** Kiểm tra lại Secret Key trong dashboard và code

### 2. Terminal Code sai
- **Triệu chứng:** "Sai chữ ký" hoặc "Terminal không hợp lệ"
- **Giải pháp:** Kiểm tra lại Terminal Code

### 3. Hash không đúng format
- **Triệu chứng:** "Sai chữ ký"
- **Giải pháp:** 
  - Đảm bảo hash data không encode
  - Dùng HMAC SHA512, không phải SHA512 đơn giản
  - Hash trả về chữ hoa

### 4. Thiếu tham số
- **Triệu chứng:** "Thiếu tham số"
- **Giải pháp:** Kiểm tra đầy đủ các tham số bắt buộc

## 📋 Checklist:

- [ ] Terminal Code đúng (8 ký tự)
- [ ] Secret Key đúng (32 ký tự)
- [ ] Hash data không encode giá trị
- [ ] Query string có encode
- [ ] Dùng HMAC SHA512
- [ ] Hash trả về chữ hoa
- [ ] IP address hợp lệ
- [ ] Payment URL đúng (sandbox hoặc production)

## 🔧 Test lại:

1. Build lại project
2. Chạy app và thử thanh toán
3. Xem log trong Logcat với filter `VNPayHelper`
4. Copy log và so sánh với code mẫu VNPay

## 📞 Nếu vẫn lỗi:

1. Copy toàn bộ log từ Logcat (filter `VNPayHelper`)
2. Gửi kèm:
   - Terminal Code (ẩn một phần nếu cần)
   - Hash Data từ log
   - Secure Hash từ log
3. Liên hệ VNPay support: kdctt@vnpay.vn

