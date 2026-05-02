# GIẢI PHÁP EMAIL THAY THẾ

## ❌ Vấn đề với SendGrid:
- Tài khoản bị chặn hoặc chưa được kích hoạt
- Cần verify email hoặc liên hệ support

## ✅ Các giải pháp thay thế:

### Giải pháp 1: Dùng Gmail cá nhân (Đơn giản nhất)

1. **Tạo Gmail cá nhân** (nếu chưa có)
2. **Tạo App Password:**
   - Vào https://myaccount.google.com/
   - Security → 2-Step Verification (bật nếu chưa)
   - App passwords → Tạo app password mới
   - Copy password (16 ký tự)

3. **Cập nhật EmailSender.java:**
   ```java
   private static final String SMTP_HOST = "smtp.gmail.com";
   private static final String SMTP_PORT = "587";
   private static final String SMTP_USERNAME = "your-gmail@gmail.com";
   private static final String SMTP_PASSWORD = "your-16-char-app-password";
   private static final String FROM_EMAIL = "your-gmail@gmail.com";
   ```

### Giải pháp 2: Dùng Mailgun (Miễn phí 5000 emails/tháng)

1. Đăng ký tại https://www.mailgun.com/
2. Verify domain hoặc dùng sandbox domain
3. Lấy API Key
4. Cập nhật:
   ```java
   private static final String SMTP_HOST = "smtp.mailgun.org";
   private static final String SMTP_PORT = "587";
   private static final String SMTP_USERNAME = "postmaster@your-domain.mailgun.org";
   private static final String SMTP_PASSWORD = "your-mailgun-api-key";
   ```

### Giải pháp 3: Dùng SMTP của trường (Nếu có)

Liên hệ IT của trường để lấy thông tin SMTP:
- SMTP Host: có thể là `smtp.tdmu.edu.vn` hoặc `mail.tdmu.edu.vn`
- Port: 587 hoặc 465
- Username: email của bạn
- Password: mật khẩu thường

### Giải pháp 4: Tạm thời tắt gửi email

Nếu không cần gửi email ngay, có thể comment lại trong `CheckoutActivity.java`:
```java
// EmailHelper.sendOrderConfirmationEmail(this, order);
```

## 🎯 Khuyến nghị:

**Dùng Gmail cá nhân** là cách đơn giản và ổn định nhất để test. Sau đó có thể chuyển sang dịch vụ chuyên nghiệp hơn khi cần.

