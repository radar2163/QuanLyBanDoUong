package com.example.cuoiki.QuanLyThongBao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Notification;

import java.util.List;

public class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.HistoryViewHolder> {

    private List<Notification> notificationList;
    private OnRecallClickListener listener;

    public interface OnRecallClickListener {
        void onRecallClick(Notification notification);
    }

    public NotificationHistoryAdapter(List<Notification> notificationList, OnRecallClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        
        // Hiển thị người nhận
        String receiver = notification.getReceiverUsername();
        if (receiver != null && receiver.equals("ALL")) {
            holder.tvReceiver.setText("Đến: Tất cả");
        } else {
            holder.tvReceiver.setText("Đến: " + receiver);
        }
        
        // Format date
        String dateStr = notification.getCreatedDate();
        if (dateStr != null && dateStr.length() > 10) {
            dateStr = dateStr.substring(0, 16);
        }
        holder.tvDate.setText(dateStr);

        holder.btnRecall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecallClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvReceiver, tvDate;
        Button btnRecall;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnRecall = itemView.findViewById(R.id.btnRecall);
        }
    }
}

