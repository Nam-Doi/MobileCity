package com.example.androidapp.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.Notification;
import com.example.androidapp.repositories.NotificationRepository;
import com.example.androidapp.views.adapters.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notifications;
    private NotificationRepository repository;
    private TextView tvMarkAllRead;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notify, container, false);

        // Init views
        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.tvEmpty);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);

        // Setup RecyclerView
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                handleNotificationClick(notification);
            }

            @Override
            public void onDeleteClick(Notification notification, int position) {
                deleteNotification(notification, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Init repository
        repository = new NotificationRepository();

        // Mark all as read button
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Load notifications
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        android.util.Log.d("NotificationFragment", "Loading notifications for user: " + user.getUid());

        repository.getNotifications(user.getUid(), new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onNotificationsLoaded(List<Notification> loadedNotifications) {
                android.util.Log.d("NotificationFragment",
                        "onNotificationsLoaded: " + (loadedNotifications != null ? loadedNotifications.size() : 0));
                notifications.clear();
                notifications.addAll(loadedNotifications);
                adapter.notifyDataSetChanged();

                // Show/hide empty state
                if (notifications.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvMarkAllRead.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvMarkAllRead.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                android.util.Log.e("NotificationFragment", "Failed to load notifications", e);
                Toast.makeText(getContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        // Đánh dấu đã đọc
        if (!notification.isRead()) {
            repository.markAsRead(user.getUid(), notification.getId(),
                    new NotificationRepository.OnOperationListener() {
                        @Override
                        public void onSuccess(String message) {
                            // UI đã tự động cập nhật qua snapshot listener
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Silent fail
                        }
                    });
        }

        // TODO: Navigate đến màn hình chi tiết dựa vào notification.getActionUrl()
        // Ví dụ: nếu type = "order" thì mở OrderDetailActivity
    }

    private void deleteNotification(Notification notification, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        repository.deleteNotification(user.getUid(), notification.getId(),
                new NotificationRepository.OnOperationListener() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Không thể xóa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markAllAsRead() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        repository.markAllAsRead(user.getUid(), new NotificationRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}