package com.example.cuoiki.KhachHang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private TextView tvEmpty;
    private ImageView btnBack;
    private SQLiteHelper dbHelper;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        loadOrders();
    }

    private void initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        dbHelper = new SQLiteHelper(this);
        orderList = new ArrayList<>();

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        recyclerOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("logged_user", null);

        if (username != null) {
            orderList.clear();
            orderList.addAll(dbHelper.getOrdersByUsername(username));
            adapter.notifyDataSetChanged();
        }

        // Hiển thị empty state
        if (orderList.isEmpty()) {
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
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orders;
        private final DecimalFormat currencyFormat = new DecimalFormat("#,###");

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_history, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            
            holder.tvOrderId.setText("Đơn hàng #" + String.format("%03d", order.getId()));
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
            
            // Hiển thị trạng thái với màu sắc
            String status = order.getStatus();
            holder.tvStatus.setText(status);
            updateStatusColor(holder.tvStatus, status);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
        
        // Method để cập nhật màu sắc trạng thái
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

                tvStatus.setTextColor(0xFF757575);
                tvStatus.setBackgroundColor(0xFFF5F5F5);
            }
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvProducts, tvTotalPrice, tvStatus;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvProducts = itemView.findViewById(R.id.tvProducts);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}

