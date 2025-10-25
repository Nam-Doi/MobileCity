package com.example.androidapp.views.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onDeleteClick(Notification notification, int position);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(formatTime(notification.getCreatedAt()));

        // Icon theo loại thông báo
        int iconRes = getIconForType(notification.getType());
        holder.imgIcon.setImageResource(iconRes);

        // Hiển thị ảnh nếu có
        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            holder.imgPreview.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(notification.getImageUrl())
                    .into(holder.imgPreview);
        } else {
            holder.imgPreview.setVisibility(View.GONE);
        }

        // Nếu chưa đọc -> in đậm và background khác
        if (!notification.isRead()) {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundResource(R.color.unread_background);
            holder.viewUnreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setBackgroundResource(android.R.color.white);
            holder.viewUnreadIndicator.setVisibility(View.GONE);
        }

        // Click vào notification
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        // Click vào nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void removeItem(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // < 1 phút
            return "Vừa xong";
        } else if (diff < 3600000) { // < 1 giờ
            return (diff / 60000) + " phút trước";
        } else if (diff < 86400000) { // < 1 ngày
            return (diff / 3600000) + " giờ trước";
        } else if (diff < 604800000) { // < 1 tuần
            return (diff / 86400000) + " ngày trước";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    private int getIconForType(String type) {
        switch (type) {
            case "order":
                return R.drawable.manage_order;
            case "promotion":
                return R.drawable.view_product;
            case "system":
                return R.drawable.setting_ad;
            default:
                return R.drawable.ic_notifications;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, imgPreview;
        TextView tvTitle, tvMessage, tvTime;
        View viewUnreadIndicator, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            imgPreview = itemView.findViewById(R.id.imgPreview);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}