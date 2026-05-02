package com.example.cuoiki.QuanLyThongBao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.User;

import java.util.ArrayList;
import java.util.List;

public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.UserViewHolder> {

    private List<User> userList;
    private List<String> selectedUsernames;

    public UserSelectAdapter(List<User> userList) {
        this.userList = userList;
        this.selectedUsernames = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_select, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvFullName.setText(user.getFull_name());

        holder.checkboxUser.setChecked(selectedUsernames.contains(user.getUsername()));

        holder.checkboxUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUsernames.contains(user.getUsername())) {
                    selectedUsernames.add(user.getUsername());
                }
            } else {
                selectedUsernames.remove(user.getUsername());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public List<String> getSelectedUsernames() {
        return selectedUsernames;
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        this.selectedUsernames.clear();
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxUser;
        TextView tvUsername, tvFullName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxUser = itemView.findViewById(R.id.checkboxUser);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvFullName = itemView.findViewById(R.id.tvFullName);
        }
    }
}

