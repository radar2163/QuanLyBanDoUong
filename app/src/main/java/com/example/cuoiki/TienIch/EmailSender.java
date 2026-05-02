package com.example.cuoiki.TienIch;

import android.util.Log;

import com.example.cuoiki.BuildConfig;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Class để gửi email tự động qua SMTP server
 * Hỗ trợ Gmail, SendGrid và các SMTP server khác
 */
public class EmailSender {
    
    private static final String TAG = "EmailSender";
    

    private static final String SMTP_HOST = BuildConfig.SMTP_HOST;
    private static final String SMTP_PORT = BuildConfig.SMTP_PORT;
    private static final String SMTP_USERNAME = BuildConfig.SMTP_USERNAME;
    private static final String SMTP_PASSWORD = BuildConfig.SMTP_PASSWORD;
    private static final String FROM_EMAIL = BuildConfig.SMTP_FROM_EMAIL;
    

    
    /**
     * Gửi email
     * @param toEmail Email người nhận
     * @param subject Tiêu đề email
     * @param body Nội dung email
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean sendEmail(String toEmail, String subject, String body) {
        if (SMTP_USERNAME.isEmpty() || SMTP_PASSWORD.isEmpty()) {
            Log.e(TAG, "SMTP chưa cấu hình: thêm smtp.username và smtp.password vào local.properties (xem local.properties.example)");
            return false;
        }
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
            message.setFrom(new InternetAddress(FROM_EMAIL)); // Email người gửi
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            
            // Kiểm tra nếu body là HTML (bắt đầu bằng <html> hoặc <!DOCTYPE)
            if (body.trim().startsWith("<html") || body.trim().startsWith("<!DOCTYPE")) {
                // Gửi email HTML
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                // Gửi email plain text
                message.setText(body);
            }
            
            // Gửi email
            Transport.send(message);
            
            Log.d(TAG, "Email đã được gửi thành công đến: " + toEmail);
            return true;
            
        } catch (javax.mail.AuthenticationFailedException e) {
            Log.e(TAG, "Lỗi xác thực (Authentication Failed): " + e.getMessage(), e);
            Log.e(TAG, "Kiểm tra lại email và App Password");
            e.printStackTrace();
            return false;
        } catch (javax.mail.MessagingException e) {
            Log.e(TAG, "Lỗi messaging: " + e.getMessage(), e);
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gửi email: " + e.getMessage(), e);
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gửi email với cấu hình tùy chỉnh
     */
    public static boolean sendEmail(String toEmail, String subject, String body, 
                                   String smtpHost, String smtpPort, 
                                   String smtpUsername, String smtpPassword) {
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
            properties.put("mail.smtp.ssl.trust", smtpHost);
            
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            
            Log.d(TAG, "Email đã được gửi thành công đến: " + toEmail);
            return true;
            
        } catch (javax.mail.AuthenticationFailedException e) {
            Log.e(TAG, "Lỗi xác thực (Authentication Failed): " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        } catch (javax.mail.MessagingException e) {
            Log.e(TAG, "Lỗi messaging: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gửi email: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }
}



