# HƯỚNG DẪN DÙNG GMAIL ĐỂ GỬI EMAIL TỰ ĐỘNG

## ✅ Ưu điểm:
- Gửi được đến **BẤT KỲ** email nào (không cần verify)
- Miễn phí, không giới hạn số lượng email
- Đơn giản, dễ cấu hình
- Ổn định, ít lỗi

## 📋 Các bước cấu hình:

### Bước 1: Tạo Gmail (nếu chưa có)
- Vào https://gmail.com và tạo tài khoản mới

### Bước 2: Bật 2-Step Verification
1. Vào https://myaccount.google.com/
2. Click **Security** (Bảo mật)
3. Tìm **2-Step Verification** (Xác minh 2 bước)
4. Click **Get Started** và làm theo hướng dẫn
5. Bật 2-Step Verification

### Bước 3: Tạo App Password
1. Vào https://myaccount.google.com/apppasswords
   - Hoặc: Security → 2-Step Verification → App passwords
2. Chọn:
   - **App:** Mail
   - **Device:** Other (Custom name)
   - Nhập tên: `Android App`
3. Click **Generate**
4. **Copy mật khẩu 16 ký tự** (dạng: `abcd efgh ijkl mnop`)
   - ⚠️ **Lưu ý:** Chỉ hiện 1 lần, copy ngay!

### Bước 4: Cập nhật EmailSender.java
1. Mở file `EmailSender.java`
2. Comment phần Mailgun (dòng 40-44)
3. Uncomment phần Gmail (dòng 27-33)
4. Điền thông tin:
   ```java
   private static final String SMTP_USERNAME = "your-gmail@gmail.com";
   private static final String SMTP_PASSWORD = "abcd efgh ijkl mnop"; // App Password 16 ký tự
   private static final String FROM_EMAIL = "your-gmail@gmail.com";
   ```

### Bước 5: Test
- Đặt hàng trong app và kiểm tra email khách hàng

## ⚠️ Lưu ý:
- App Password khác với mật khẩu Gmail thường
- Nếu quên App Password, tạo lại ở bước 3
- Không chia sẻ App Password với ai
- Gmail có giới hạn: ~500 emails/ngày (đủ cho app nhỏ)

## 🔄 Nếu muốn dùng email trường học:
- Có thể dùng email trường nếu trường có SMTP server
- Cập nhật theo TÙY CHỌN 4 trong EmailSender.java

