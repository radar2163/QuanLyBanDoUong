package com.example.cuoiki.ThongKe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Transaction;

import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public TransactionsAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.productName.setText(transaction.getProductName());
        
        // Hiển thị loại giao dịch (Nhập/Xuất) với màu sắc
        String transactionType = transaction.getTransactionType();
        holder.transactionType.setText(transactionType);
        
        // Format ngày giờ đẹp hơn
        String dateStr = transaction.getTransactionDate();
        if (dateStr != null && dateStr.length() > 10) {
            // Chỉ hiển thị ngày và giờ
            dateStr = dateStr.substring(0, 16); // "2025-11-22 19:49"
        }
        holder.transactionDate.setText(dateStr);
        
        // Hiển thị số lượng
        holder.quantity.setText(String.valueOf(transaction.getQuantity()));

        // Gán sự kiện click để hiển thị chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(transaction);
            }
        });

        // Gán sự kiện nhấn giữ để lưu vị trí
        holder.itemView.setOnLongClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            v.showContextMenu(); // Hiển thị menu ngữ cảnh
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Transaction getTransaction(int position) {
        return transactionList.get(position);
    }

    // Phương thức cập nhật danh sách giao dịch khi tìm kiếm
    public void updateTransactionList(List<Transaction> newTransactionList) {
        this.transactionList = newTransactionList;
        notifyDataSetChanged(); // Cập nhật giao diện sau khi thay đổi danh sách
    }

    // Lớp ViewHolder
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView productName, transactionType, quantity, transactionDate;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            transactionType = itemView.findViewById(R.id.transactionType);
            quantity = itemView.findViewById(R.id.quantity);
            transactionDate = itemView.findViewById(R.id.transactionDate);
        }
    }
}
