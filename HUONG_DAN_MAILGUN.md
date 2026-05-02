# HƯỚNG DẪN CẤU HÌNH MAILGUN

## Bước 1: Đăng ký Mailgun
1. Vào https://www.mailgun.com/
2. Click "Sign Up" và đăng ký tài khoản miễn phí
3. Xác thực email của bạn

## Bước 2: Lấy thông tin SMTP

### Nếu dùng Sandbox Domain (Miễn phí - chỉ gửi được đến email đã verify):
1. Vào Dashboard → Sending → Domain Settings
2. Bạn sẽ thấy sandbox domain dạng: `sandbox-xxxxx.mailgun.org`
3. **SMTP Username:** `postmaster@sandbox-xxxxx.mailgun.org`
4. **FROM_EMAIL:** Có thể dùng `noreply@sandbox-xxxxx.mailgun.org` hoặc email bất kỳ từ domain này

### Nếu verify domain riêng (Gửi được đến mọi email):
1. Vào Dashboard → Sending → Domains → Add New Domain
2. Thêm domain của bạn (ví dụ: `yourdomain.com`)
3. Thêm DNS records theo hướng dẫn
4. Sau khi verify xong:
   - **SMTP Username:** `postmaster@yourdomain.com`
   - **FROM_EMAIL:** `noreply@yourdomain.com` hoặc email bất kỳ từ domain này

## Bước 3: Lấy API Key
1. Vào Dashboard → Settings → API Keys
2. Copy **Private API key** (dạng: `key-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)
3. ⚠️ **Lưu ý:** Dùng Private API key, không phải Public API key

## Bước 4: Verify email nhận (Nếu dùng Sandbox Domain)
1. Vào Dashboard → Sending → Authorized Recipients
2. Thêm email mà bạn muốn nhận email test
3. Mailgun sẽ gửi email xác thực
4. Click link trong email để verify

## Bước 5: Cập nhật EmailSender.java

Mở file `EmailSender.java` và cập nhật:

```java
private static final String SMTP_HOST = "smtp.mailgun.org";
private static final String SMTP_PORT = "587";
private static final String SMTP_USERNAME = "postmaster@sandbox-xxxxx.mailgun.org"; // Thay xxxxx bằng domain của bạn
private static final String SMTP_PASSWORD = "key-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // Thay bằng API Key của bạn
private static final String FROM_EMAIL = "noreply@sandbox-xxxxx.mailgun.org"; // Thay xxxxx bằng domain của bạn
```

## Lưu ý quan trọng:
- **Sandbox Domain:** Chỉ gửi được đến email đã verify trong Authorized Recipients
- **Verified Domain:** Gửi được đến mọi email
- **API Key:** Giữ bí mật, không commit lên Git
- **Rate Limit:** Miễn phí 5000 emails/tháng đầu tiên, sau đó 1000 emails/tháng

## Test email:
Sau khi cấu hình xong, test bằng cách đặt hàng trong app và kiểm tra email của khách hàng.

