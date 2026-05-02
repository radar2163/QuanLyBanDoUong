package com.example.cuoiki.QuanLyDonHang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Order;
import com.example.cuoiki.TienIch.SQLiteHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderManageActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private TextView tvEmpty;
    private ImageView btnBack;
    private Button btnAll, btnPending, btnProcessing, btnSuccess, btnFailed;
    private SQLiteHelper dbHelper;
    private List<Order> orderList;
    private List<Order> filteredOrderList;
    private OrderManageAdapter adapter;
    private String currentFilter = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);

        initViews();
        loadOrders();
    }

    private void initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);
        
        // Tab buttons
        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnSuccess = findViewById(R.id.btnSuccess);
        btnFailed = findViewById(R.id.btnFailed);

        btnBack.setOnClickListener(v -> finish());

        dbHelper = new SQLiteHelper(this);
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderManageAdapter(filteredOrderList);
        recyclerOrders.setAdapter(adapter);
        
        // Setup tab buttons
        setupTabButtons();
        
        // Chọn tab "Tất cả" mặc định
        updateTabButtons();
    }
    
    private void setupTabButtons() {
        btnAll.setOnClickListener(v -> filterByStatus("Tất cả"));
        btnPending.setOnClickListener(v -> filterByStatus("Đang chờ xử lý"));
        btnProcessing.setOnClickListener(v -> filterByStatus("Đang xử lý"));
        btnSuccess.setOnClickListener(v -> filterByStatus("Thành công"));
        btnFailed.setOnClickListener(v -> filterByStatus("Thất bại"));
    }
    
    private void filterByStatus(String status) {
        currentFilter = status;
        filteredOrderList.clear();
        
        if ("Tất cả".equals(status)) {
            filteredOrderList.addAll(orderList);
        } else if ("Thành công".equals(status)) {
            // Filter "Thành công" cũng hiển thị "Đã thanh toán" (cả hai đều là trạng thái thành công)
            for (Order order : orderList) {
                String orderStatus = order.getStatus();
                if ("Thành công".equals(orderStatus) || "Đã thanh toán".equals(orderStatus)) {
                    filteredOrderList.add(order);
                }
            }
        } else {
            for (Order order : orderList) {
                if (status.equals(order.getStatus())) {
                    filteredOrderList.add(order);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateTabButtons();
        checkEmptyState();
    }
    
    private void updateTabButtons() {
        // Reset tất cả buttons về màu xám
        int grayColor = 0xFF9E9E9E;
        int purpleColor = 0xFF5E17EB;
        
        btnAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayColor));
        btnPending.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayColor));
        btnProcessing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayColor));
        btnSuccess.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayColor));
        btnFailed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayColor));
        
        // Highlight button được chọn
        switch (currentFilter) {
            case "Tất cả":
                btnAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                break;
            case "Đang chờ xử lý":
                btnPending.setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                break;
            case "Đang xử lý":
                btnProcessing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                break;
            case "Thành công":
                btnSuccess.setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                break;
            case "Thất bại":
                btnFailed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(purpleColor));
                break;
        }
    }

    private void loadOrders() {
        orderList.clear();
        orderList.addAll(dbHelper.getAllOrders());
        
        // Áp dụng filter hiện tại
        filterByStatus(currentFilter);
    }
    
    private void checkEmptyState() {
        if (filteredOrderList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerOrders.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    // Adapter cho danh sách đơn hàng
    private class OrderManageAdapter extends RecyclerView.Adapter<OrderManageAdapter.OrderViewHolder> {
        private List<Order> orders;
        private final DecimalFormat currencyFormat = new DecimalFormat("#,###");
        private final String[] statusOptions = {"Đang chờ xử lý", "Đang xử lý", "Thành công", "Đã thanh toán", "Thất bại"};

        public OrderManageAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_manage, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            
            holder.tvOrderId.setText("Đơn hàng #" + String.format("%03d", order.getId()));
            holder.tvCustomerName.setText("Khách hàng: " + order.getCustomerName());
            holder.tvPhoneNumber.setText("SĐT: " + order.getPhoneNumber());
            holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
            holder.tvTotalPrice.setText(currencyFormat.format(order.getTotalPrice()) + " đ");
            
            // Hiển thị danh sách sản phẩm
            StringBuilder productsText = new StringBuilder();
            if (order.getItems() != null) {
                for (int i = 0; i < order.getItems().size(); i++) {
                    Order.OrderItem item = order.getItems().get(i);
                    productsText.append(item.getProductName());
                    productsText.append(" x").append(item.getQuantity());
                    if (i < order.getItems().size() - 1) {
                        productsText.append(", ");
                    }
                }
            }
            holder.tvProducts.setText(productsText.toString());
            
            // Setup Spinner trạng thái với custom adapter để có màu chữ
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                statusOptions
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextColor(0xFF1976D2); // Màu xanh dương
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextColor(0xFF424242); // Màu xám đậm cho dropdown
                    return view;
                }
            };
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerStatus.setAdapter(spinnerAdapter);
            
            // Chọn trạng thái hiện tại
            int statusIndex = -1;
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equals(order.getStatus())) {
                    statusIndex = i;
                    break;
                }
            }
            if (statusIndex >= 0) {
                holder.spinnerStatus.setSelection(statusIndex);
            }
            
            // Cập nhật màu trạng thái
            updateStatusColor(holder.tvStatus, order.getStatus());
            
            // Lắng nghe thay đổi trạng thái
            holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = statusOptions[position];
                    if (!newStatus.equals(order.getStatus())) {
                        // Cập nhật trạng thái trong database
                        boolean success = dbHelper.updateOrderStatus(order.getId(), newStatus);
                        if (success) {
                            order.setStatus(newStatus);
                            updateStatusColor(holder.tvStatus, newStatus);
                            
                            // Reload orders và áp dụng filter lại
                            loadOrders();
                            
                            Toast.makeText(OrderManageActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OrderManageActivity.this, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private void updateStatusColor(TextView tvStatus, String status) {
            tvStatus.setText(status);
            if ("Đang chờ xử lý".equals(status)) {
                tvStatus.setTextColor(0xFFFF9800);
                tvStatus.setBackgroundColor(0xFFFFF3E0);
            } else if ("Đang xử lý".equals(status)) {
                tvStatus.setTextColor(0xFF2196F3);
                tvStatus.setBackgroundColor(0xFFE3F2FD);
            } else if ("Thành công".equals(status) || "Đã thanh toán".equals(status)) {
                tvStatus.setTextColor(0xFF4CAF50);
                tvStatus.setBackgroundColor(0xFFE8F5E9);
            } else if ("Thất bại".equals(status)) {
                tvStatus.setTextColor(0xFFF44336);
                tvStatus.setBackgroundColor(0xFFFFEBEE);
            } else {
                // Màu mặc định cho các trạng thái khác
                tvStatus.setTextColor(0xFF757575);
                tvStatus.setBackgroundColor(0xFFF5F5F5);
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvCustomerName, tvPhoneNumber, tvOrderDate, tvProducts, tvTotalPrice, tvStatus;
            Spinner spinnerStatus;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvProducts = itemView.findViewById(R.id.tvProducts);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            }
        }
    }
}

