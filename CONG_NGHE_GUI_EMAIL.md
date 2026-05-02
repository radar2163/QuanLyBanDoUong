# CÔNG NGHỆ GỬI EMAIL TRONG ỨNG DỤNG
## JavaMail API - Gửi Email Tự Động Qua SMTP

---

## 1. TỔNG QUAN

### 1.1. Công nghệ sử dụng
Ứng dụng sử dụng **JavaMail API** để gửi email tự động qua SMTP server.

**JavaMail API** là:
- Thư viện Java chuẩn để gửi và nhận email
- Hỗ trợ các giao thức: SMTP, POP3, IMAP
- Có thể gửi email HTML hoặc plain text
- Hỗ trợ authentication, TLS/SSL encryption

---

## 2. DEPENDENCIES (Thư viện)

### 2.1. Trong file `build.gradle.kts`

```kotlin
// 📧 JavaMail API để gửi email tự động
implementation("com.sun.mail:android-mail:1.6.7")
implementation("com.sun.mail:android-activation:1.6.7")
```

### 2.2. Giải thích từng thư viện

#### A. `android-mail:1.6.7`
- **Tên đầy đủ**: JavaMail API for Android
- **Chức năng**: 
  - Cung cấp các class để gửi email (Session, Message, Transport)
  - Hỗ trợ SMTP protocol
  - Xử lý authentication
  - Hỗ trợ TLS/SSL encryption

#### B. `android-activation:1.6.7`
- **Tên đầy đủ**: JavaBeans Activation Framework (JAF) for Android
- **Chức năng**:
  - Hỗ trợ xử lý MIME types
  - Cần thiết cho JavaMail API hoạt động
  - Xử lý attachments (nếu có)

---

## 3. CÁC CLASS JAVA MAIL API SỬ DỤNG

### 3.1. Import statements

```java
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
```

### 3.2. Giải thích từng class

#### A. `javax.mail.Session`
- **Chức năng**: Quản lý kết nối với SMTP server
- **Sử dụng**: Tạo session với Properties và Authenticator
- **Ví dụ**:
```java
Session session = Session.getInstance(properties, authenticator);
```

#### B. `javax.mail.internet.MimeMessage`
- **Chức năng**: Tạo email message (hỗ trợ HTML)
- **Sử dụng**: Set người gửi, người nhận, tiêu đề, nội dung
- **Ví dụ**:
```java
Message message = new MimeMessage(session);
message.setFrom(new InternetAddress("from@email.com"));
message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("to@email.com"));
message.setSubject("Subject");
message.setContent(body, "text/html; charset=utf-8");
```

#### C. `javax.mail.Transport`
- **Chức năng**: Gửi email qua SMTP server
- **Sử dụng**: Gọi `Transport.send(message)`
- **Ví dụ**:
```java
Transport.send(message);
```

#### D. `javax.mail.Authenticator`
- **Chức năng**: Xác thực với SMTP server (username/password)
- **Sử dụng**: Tạo class extends Authenticator và override `getPasswordAuthentication()`
- **Ví dụ**:
```java
Session session = Session.getInstance(properties, new Authenticator() {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
});
```

#### E. `java.util.Properties`
- **Chức năng**: Cấu hình SMTP server (host, port, TLS, v.v.)
- **Sử dụng**: Set các properties cho SMTP
- **Ví dụ**:
```java
Properties properties = new Properties();
properties.put("mail.smtp.host", "smtp.gmail.com");
properties.put("mail.smtp.port", "587");
properties.put("mail.smtp.auth", "true");
properties.put("mail.smtp.starttls.enable", "true");
```

---

## 4. CẤU HÌNH SMTP

### 4.1. Gmail SMTP Configuration

```java
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String SMTP_USERNAME = "<redacted>";
private static final String SMTP_PASSWORD = "<redacted>"; // App Password
private static final String FROM_EMAIL = "<redacted>";
```

### 4.2. Properties Configuration

```java
Properties properties = new Properties();
properties.put("mail.smtp.host", SMTP_HOST);              // SMTP server
properties.put("mail.smtp.port", SMTP_PORT);              // Port (587 cho TLS)
properties.put("mail.smtp.auth", "true");                 // Yêu cầu authentication
properties.put("mail.smtp.starttls.enable", "true");      // Bật TLS encryption
properties.put("mail.smtp.starttls.required", "true");    // Bắt buộc TLS
properties.put("mail.smtp.ssl.trust", SMTP_HOST);         // Trust SMTP host
```

### 4.3. Giải thích các Properties

- **`mail.smtp.host`**: Địa chỉ SMTP server (Gmail: smtp.gmail.com)
- **`mail.smtp.port`**: Port kết nối (587 cho TLS, 465 cho SSL)
- **`mail.smtp.auth`**: Yêu cầu xác thực (true = cần username/password)
- **`mail.smtp.starttls.enable`**: Bật TLS encryption (bảo mật)
- **`mail.smtp.starttls.required`**: Bắt buộc TLS
- **`mail.smtp.ssl.trust`**: Trust certificate của SMTP host

---

## 5. QUY TRÌNH GỬI EMAIL

### 5.1. Các bước thực hiện

```
1. Tạo Properties (cấu hình SMTP)
   ↓
2. Tạo Session với Authenticator
   ↓
3. Tạo MimeMessage
   ↓
4. Set From, To, Subject, Content
   ↓
5. Gọi Transport.send(message)
   ↓
6. Email được gửi qua SMTP server
```

### 5.2. Code Implementation

```java
public static boolean sendEmail(String toEmail, String subject, String body) {
    try {
        // Bước 1: Cấu hình Properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Bước 2: Tạo Session với Authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        
        // Bước 3: Tạo Message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        
        // Bước 4: Set Content (HTML hoặc plain text)
        if (body.trim().startsWith("<html") || body.trim().startsWith("<!DOCTYPE")) {
            message.setContent(body, "text/html; charset=utf-8");
        } else {
            message.setText(body);
        }
        
        // Bước 5: Gửi email
        Transport.send(message);
        return true;
        
    } catch (Exception e) {
        Log.e(TAG, "Lỗi khi gửi email: " + e.getMessage(), e);
        return false;
    }
}
```

---

## 6. CÁC SMTP SERVER HỖ TRỢ

### 6.1. Gmail
```java
SMTP_HOST = "smtp.gmail.com"
SMTP_PORT = "587"  // TLS
// Hoặc
SMTP_PORT = "465"  // SSL
```
**Yêu cầu**: App Password (không dùng mật khẩu chính)

### 6.2. SendGrid
```java
SMTP_HOST = "smtp.sendgrid.net"
SMTP_PORT = "587"
SMTP_USERNAME = "apikey"
SMTP_PASSWORD = "your-api-key"
```

### 6.3. Mailgun
```java
SMTP_HOST = "smtp.mailgun.org"
SMTP_PORT = "587"
SMTP_USERNAME = "postmaster@your-domain.mailgun.org"
SMTP_PASSWORD = "your-password"
```

### 6.4. Outlook/Hotmail
```java
SMTP_HOST = "smtp-mail.outlook.com"
SMTP_PORT = "587"
```

---

## 7. ƯU ĐIỂM CỦA JAVAMAIL API

### 7.1. Ưu điểm
- ✅ **Chuẩn Java**: Thư viện chuẩn, được hỗ trợ rộng rãi
- ✅ **Dễ sử dụng**: API đơn giản, dễ hiểu
- ✅ **Hỗ trợ nhiều SMTP**: Gmail, SendGrid, Mailgun, v.v.
- ✅ **Bảo mật**: Hỗ trợ TLS/SSL encryption
- ✅ **HTML Email**: Hỗ trợ gửi email HTML đẹp mắt
- ✅ **Android Compatible**: Có version riêng cho Android

### 7.2. Nhược điểm
- ⚠️ **Cần SMTP server**: Phải có tài khoản SMTP
- ⚠️ **Blocking**: Gửi email có thể block thread (nên dùng background thread)
- ⚠️ **Dependency**: Cần thêm thư viện vào project

---

## 8. SO SÁNH VỚI CÁC CÔNG NGHỆ KHÁC

### 8.1. JavaMail API vs Firebase Cloud Messaging (FCM)
- **JavaMail**: Gửi email trực tiếp qua SMTP
- **FCM**: Gửi push notification, không phải email

### 8.2. JavaMail API vs SendGrid API
- **JavaMail**: Gửi qua SMTP (chuẩn)
- **SendGrid API**: REST API (cần HTTP client)

### 8.3. JavaMail API vs Gmail API
- **JavaMail**: SMTP protocol (đơn giản hơn)
- **Gmail API**: REST API (phức tạp hơn, cần OAuth)

---

## 9. TÀI LIỆU THAM KHẢO

### 9.1. Official Documentation
- **JavaMail API**: https://javaee.github.io/javamail/
- **Android Mail**: https://github.com/javaee/javamail

### 9.2. Maven Repository
- **android-mail**: https://mvnrepository.com/artifact/com.sun.mail/android-mail
- **android-activation**: https://mvnrepository.com/artifact/com.sun.mail/android-activation

### 9.3. Tutorials
- **Gmail SMTP Setup**: https://support.google.com/mail/answer/7126229
- **App Password**: https://support.google.com/accounts/answer/185833

---

## 10. KẾT LUẬN

**JavaMail API** là công nghệ được sử dụng để gửi email trong ứng dụng:
- ✅ Thư viện chuẩn Java, dễ sử dụng
- ✅ Hỗ trợ nhiều SMTP server
- ✅ Bảo mật với TLS/SSL
- ✅ Hỗ trợ HTML email
- ✅ Tích hợp dễ dàng vào Android app

**Dependencies cần thiết:**
```kotlin
implementation("com.sun.mail:android-mail:1.6.7")
implementation("com.sun.mail:android-activation:1.6.7")
```

---

*Tài liệu này giải thích chi tiết về công nghệ gửi email được sử dụng trong ứng dụng.*

