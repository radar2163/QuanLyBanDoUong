package com.example.cuoiki.TienIch;

import android.util.Log;

import com.example.cuoiki.BuildConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class VNPayHelper {
    
    private static final String TAG = "VNPayHelper";
    
    private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNPAY_TMN_CODE = BuildConfig.VNPAY_TMN_CODE;
    private static final String VNPAY_SECRET_KEY = BuildConfig.VNPAY_SECRET_KEY;

    static {
        if (VNPAY_TMN_CODE.isEmpty() || VNPAY_SECRET_KEY.isEmpty()) {
            Log.w(TAG, "VNPay chưa được cấu hình: thêm vnpay.tmn.code và vnpay.secret.key vào local.properties");
        } else {
            Log.d(TAG, "VNPay configured - TMN_CODE: " + VNPAY_TMN_CODE + ", Secret Key length: " + VNPAY_SECRET_KEY.length());
        }
    }

    private static final String VNPAY_RETURN_URL = "vnpay://return";
    /**
     * Tạo payment URL cho VNPay
     * @param orderId Mã đơn hàng (sẽ được thêm timestamp để đảm bảo unique)
     * @param amount Số tiền (VND)
     * @param orderInfo Thông tin đơn hàng
     * @param ipAddress IP address của client
     * @return Payment URL để mở trong WebView
     */
    public static String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress) {
        if (VNPAY_TMN_CODE.isEmpty() || VNPAY_SECRET_KEY.isEmpty()) {
            Log.e(TAG, "Không tạo được URL thanh toán: thiếu vnpay.tmn.code hoặc vnpay.secret.key trong local.properties");
            return null;
        }
        // Tạo TxnRef duy nhất bằng cách thêm timestamp để tránh lỗi "Giao dịch đã tồn tại"
        SimpleDateFormat txnRefFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String uniqueTxnRef = orderId + "_" + txnRefFormatter.format(new Date());
        try {
            Map<String, String> vnpParams = new HashMap<>();
            
            // Thông tin bắt buộc
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNPAY_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", uniqueTxnRef); // Dùng unique TxnRef để tránh lỗi "Giao dịch đã tồn tại"
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", VNPAY_RETURN_URL);
            vnpParams.put("vnp_IpAddr", ipAddress);
            
            // Ngày giờ tạo
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            vnpParams.put("vnp_CreateDate", formatter.format(new Date()));
            
            // Sắp xếp params theo thứ tự alphabet
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            // Tạo query string và hash data
            // Theo tài liệu VNPay: hash data cần encode giá trị, query string cũng encode
            StringBuilder queryString = new StringBuilder();
            StringBuilder hashData = new StringBuilder();
            
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data: fieldName=encodedValue (CÓ encode giá trị)
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    
                    // Build query string: encodedFieldName=encodedValue (CÓ encode cả tên và giá trị)
                    if (queryString.length() > 0) {
                        queryString.append('&');
                    }
                    queryString.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    queryString.append('=');
                    queryString.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                }
            }
            
            // Tạo secure hash từ hashData
            String hashDataString = hashData.toString();
            Log.d(TAG, "Hash Data (before HMAC): " + hashDataString);
            Log.d(TAG, "Secret Key length: " + (VNPAY_SECRET_KEY != null ? VNPAY_SECRET_KEY.length() : 0));
            
            String vnp_SecureHash = hmacSHA512(VNPAY_SECRET_KEY, hashDataString);
            Log.d(TAG, "Secure Hash: " + vnp_SecureHash);
            Log.d(TAG, "Secure Hash length: " + (vnp_SecureHash != null ? vnp_SecureHash.length() : 0));
            
            queryString.append('&');
            queryString.append(URLEncoder.encode("vnp_SecureHash", StandardCharsets.UTF_8.toString()));
            queryString.append('=');
            queryString.append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8.toString()));
            
            String paymentUrl = VNPAY_URL + "?" + queryString.toString();
            
            Log.d(TAG, "Payment URL created successfully, length: " + paymentUrl.length());
            return paymentUrl;
            
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding error when creating payment URL: " + e.getMessage(), e);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error creating payment URL: " + e.getMessage(), e);
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo HMAC SHA512 hash cho VNPay (đúng chuẩn)
     */
    private static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase(); // VNPay yêu cầu chữ hoa
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating HMAC SHA512 hash: " + e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Xác thực response từ VNPay
     * @param vnpParams Các tham số từ callback URL
     * @return true nếu hash hợp lệ
     */
    public static boolean verifyResponse(Map<String, String> vnpParams) {
        if (VNPAY_SECRET_KEY.isEmpty()) {
            Log.e(TAG, "Không verify được: thiếu vnpay.secret.key trong local.properties");
            return false;
        }
        try {
            String vnp_SecureHash = vnpParams.remove("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
                return false;
            }
            
            // Sắp xếp params
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Hash data khi verify: dùng giá trị đã decode từ URL, rồi encode lại
                    // Vì giá trị từ URL đã được decode khi parse, nên cần encode lại để tạo hash
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    // Encode giá trị (đã được decode từ URL khi parse)
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                }
            }
            
            String hashDataString = hashData.toString();
            Log.d(TAG, "Verify - Hash Data: " + hashDataString);
            Log.d(TAG, "Verify - Received Hash: " + vnp_SecureHash);
            
            String calculatedHash = hmacSHA512(VNPAY_SECRET_KEY, hashDataString);
            Log.d(TAG, "Verify - Calculated Hash: " + calculatedHash);
            
            // So sánh hash không phân biệt hoa thường (VNPay có thể trả về chữ thường)
            boolean isValid = calculatedHash.equalsIgnoreCase(vnp_SecureHash);
            Log.d(TAG, "Verify - Hash Match: " + isValid);
            
            if (!isValid) {
                Log.w(TAG, "Hash mismatch - Received (lowercase): " + vnp_SecureHash.toLowerCase());
                Log.w(TAG, "Hash mismatch - Calculated (uppercase): " + calculatedHash);
            }
            
            return isValid;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying response: " + e.getMessage(), e);
            return false;
        }
    }
    
    
    /**
     * Lấy IP address của thiết bị
     */
    public static String getIpAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                        String ip = address.getHostAddress();
                        if (ip != null && !ip.isEmpty()) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address: " + e.getMessage(), e);
        }
        // Fallback: dùng IP mặc định cho test
        return "127.0.0.1";
    }
}

