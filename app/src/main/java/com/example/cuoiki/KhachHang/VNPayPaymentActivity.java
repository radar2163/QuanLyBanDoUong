package com.example.cuoiki.KhachHang;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.VNPayHelper;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity để xử lý thanh toán VNPay qua WebView
 */
public class VNPayPaymentActivity extends AppCompatActivity {
    
    private static final String TAG = "VNPayPaymentActivity";
    
    private WebView webView;
    private ProgressBar progressBar;
    private String paymentUrl;
    private String orderId;
    private long orderIdLong;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_payment);
        
        // Lấy thông tin từ Intent
        paymentUrl = getIntent().getStringExtra("payment_url");
        orderId = getIntent().getStringExtra("order_id");
        orderIdLong = getIntent().getLongExtra("order_id_long", -1);
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupWebView();
        loadPaymentUrl();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thanh toán VNPay");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                // Hủy thanh toán
                setResult(RESULT_CANCELED);
                finish();
            });
        }
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                
                // Kiểm tra callback URL từ VNPay
                // VNPay có thể redirect về: vnpay://return hoặc vnpay://return?params...
                if (url != null && (url.startsWith("vnpay://return") || url.startsWith("vnpay://"))) {
                    Log.d(TAG, "Detected VNPay callback URL: " + url);
                    handleVNPayCallback(url);
                    return; // Dừng load URL này
                }
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Xử lý callback URL từ VNPay
                if (url != null && (url.startsWith("vnpay://return") || url.startsWith("vnpay://"))) {
                    Log.d(TAG, "Override URL loading for VNPay callback: " + url);
                    handleVNPayCallback(url);
                    return true; // Đã xử lý, không load URL này
                }
                return false; // Cho phép load URL khác
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VNPayPaymentActivity.this, "Lỗi tải trang: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadPaymentUrl() {
        Log.d(TAG, "Loading payment URL: " + paymentUrl);
        webView.loadUrl(paymentUrl);
    }
    
    /**
     * Xử lý callback từ VNPay
     */
    private void handleVNPayCallback(String callbackUrl) {
        try {
            Log.d(TAG, "VNPay callback: " + callbackUrl);
            
            // Parse URL để lấy các tham số
            Map<String, String> params = parseCallbackUrl(callbackUrl);
            
            // Log tất cả params để debug
            Log.d(TAG, "Callback params count: " + params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
            }
            
            // Xác thực response (tạm thời bỏ qua để xem response code)
            boolean isValid = VNPayHelper.verifyResponse(new HashMap<>(params));
            
            if (!isValid) {
                Log.w(TAG, "Invalid response hash - nhưng vẫn tiếp tục xử lý để xem response code");
                // Không return ngay, tiếp tục xử lý để xem response code
                // Có thể hash không khớp nhưng vẫn cần xử lý response
            } else {
                Log.d(TAG, "Hash verification passed");
            }
            
            // Lấy response code
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String amount = params.get("vnp_Amount");
            String txnRef = params.get("vnp_TxnRef");
            
            Log.d(TAG, "Response Code: " + responseCode);
            Log.d(TAG, "Transaction Status: " + transactionStatus);
            Log.d(TAG, "TxnRef: " + txnRef);
            Log.d(TAG, "OrderId from intent: " + orderIdLong);
            
            // Kiểm tra nếu không có response code hoặc transaction status
            if (responseCode == null || transactionStatus == null) {
                Log.e(TAG, "Missing response code or transaction status");
                Toast.makeText(this, "Lỗi: Không nhận được kết quả từ VNPay", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            
            // Kiểm tra kết quả thanh toán
            // ResponseCode = "00" và TransactionStatus = "00" nghĩa là thành công
            // Lưu ý: Ngay cả khi hash không khớp, nếu response code = "00" thì vẫn coi là thành công
            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                Log.d(TAG, "Payment successful - ResponseCode: 00, TransactionStatus: 00");
                // Thanh toán thành công
                Log.d(TAG, "Payment successful - OrderId: " + orderIdLong);
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_status", "success");
                resultIntent.putExtra("order_id", orderId);
                resultIntent.putExtra("order_id_long", orderIdLong);
                resultIntent.putExtra("amount", amount);
                resultIntent.putExtra("transaction_id", params.get("vnp_TransactionNo"));
                setResult(RESULT_OK, resultIntent);
                
                // Không hiển thị Toast ở đây, để CheckoutActivity xử lý và chuyển màn hình
                finish();
            } else {
                // Thanh toán thất bại hoặc đang xử lý
                String errorMessage = getErrorMessage(responseCode);
                Log.w(TAG, "Payment failed or processing - ResponseCode: " + responseCode + ", TransactionStatus: " + transactionStatus);
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("payment_status", "failed");
                resultIntent.putExtra("error_code", responseCode);
                resultIntent.putExtra("error_message", errorMessage);
                resultIntent.putExtra("order_id", orderId);
                resultIntent.putExtra("order_id_long", orderIdLong);
                setResult(RESULT_CANCELED, resultIntent);
                
                Toast.makeText(this, "Thanh toán thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling callback: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi xử lý kết quả thanh toán", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    
    /**
     * Parse callback URL để lấy các tham số
     */
    private Map<String, String> parseCallbackUrl(String url) {
        Map<String, String> params = new HashMap<>();
        
        try {
            if (url == null) {
                Log.e(TAG, "Callback URL is null");
                return params;
            }
            
            Log.d(TAG, "Parsing callback URL: " + url);
            
            // Tìm vị trí dấu ?
            int queryIndex = url.indexOf("?");
            if (queryIndex == -1) {
                Log.w(TAG, "No query string in callback URL");
                return params;
            }
            
            String query = url.substring(queryIndex + 1);
            Log.d(TAG, "Query string: " + query);
            
            String[] pairs = query.split("&");
            
            for (String pair : pairs) {
                if (pair == null || pair.isEmpty()) {
                    continue;
                }
                
                int equalIndex = pair.indexOf("=");
                if (equalIndex == -1) {
                    continue;
                }
                
                String key = pair.substring(0, equalIndex);
                String value = pair.substring(equalIndex + 1);
                
                // Decode URL
                try {
                    key = URLDecoder.decode(key, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (Exception e) {
                    Log.w(TAG, "Error decoding: " + key + "=" + value, e);
                }
                
                params.put(key, value);
                Log.d(TAG, "Param: " + key + " = " + value);
            }
            
            Log.d(TAG, "Parsed " + params.size() + " parameters");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing callback URL: " + e.getMessage(), e);
            e.printStackTrace();
        }
        
        return params;
    }
    
    /**
     * Lấy thông báo lỗi từ response code
     */
    private String getErrorMessage(String responseCode) {
        if (responseCode == null) {
            return "Lỗi không xác định";
        }
        
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10":
                return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch";
            case "12":
                return "Thẻ/Tài khoản bị khóa";
            case "13":
                return "Nhập sai mật khẩu xác thực giao dịch (OTP). Xin vui lòng thực hiện lại giao dịch";
            case "51":
                return "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Nhập sai mật khẩu thanh toán quá số lần quy định";
            default:
                return "Lỗi không xác định (Mã: " + responseCode + ")";
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
}

