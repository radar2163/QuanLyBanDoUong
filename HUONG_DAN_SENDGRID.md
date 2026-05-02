# HƯỚNG DẪN CẤU HÌNH SENDGRID

## 📧 SendGrid là gì?

SendGrid là dịch vụ email transaction miễn phí, cho phép gửi 100 emails/ngày. Dễ cấu hình hơn Gmail và không cần App Password.

## 🔧 Các bước cấu hình:

### Bước 1: Đăng ký SendGrid

1. Vào https://sendgrid.com/
2. Click "Start for free" hoặc "Sign Up"
3. Điền thông tin đăng ký
4. Xác thực email

### Bước 2: Tạo API Key

1. Đăng nhập vào SendGrid Dashboard
2. Vào **Settings** → **API Keys**
3. Click **Create API Key**
4. Đặt tên: "Android App" (hoặc tên bất kỳ)
5. Chọn quyền: **Full Access** (hoặc **Restricted Access** với quyền Mail Send)
6. Click **Create & View**
7. **QUAN TRỌNG:** Copy API Key ngay (chỉ hiển thị 1 lần!)
   - API Key có dạng: `SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Bước 3: Verify Sender Email (Quan trọng!)

1. Vào **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Điền thông tin:
   - Email: `2224802010259@student.tdmu.edu.vn`
   - Tên: Tên của bạn
   - Địa chỉ, thành phố, v.v.
4. Click **Create**
5. Kiểm tra email và click link xác thực

### Bước 4: Cập nhật code

Mở file `EmailSender.java` và thay thế:

```java
private static final String SMTP_PASSWORD = "YOUR_SENDGRID_API_KEY";
```

Bằng API Key bạn vừa copy:

```java
private static final String SMTP_PASSWORD = "SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
```

## ✅ Ưu điểm của SendGrid:

- ✅ Miễn phí 100 emails/ngày
- ✅ Không cần App Password
- ✅ Dễ cấu hình
- ✅ Hoạt động với mọi email (không chỉ Gmail)
- ✅ Dashboard để theo dõi emails đã gửi

## 🔍 Kiểm tra:

Sau khi cấu hình, test bằng cách đặt hàng. Nếu thành công, bạn sẽ thấy:
- Toast: "Email xác nhận đã được gửi đến [email]"
- Email sẽ xuất hiện trong inbox của khách hàng

## 🐛 Nếu gặp lỗi:

1. Kiểm tra API Key đã đúng chưa
2. Kiểm tra email sender đã verify chưa
3. Kiểm tra Logcat với tag "EmailSender"

