package com.example.androidapp.repositories;

import androidx.annotation.NonNull;
import com.example.androidapp.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class NotificationRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_USERS = "users";
    private static final String SUBCOLLECTION_NOTIFICATIONS = "notifications";

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy danh sách thông báo của user (realtime)
     */
    public void getNotifications(@NonNull String userId, OnNotificationsLoadedListener listener) {
        android.util.Log.d("NotificationRepo", "Start listening notifications for user: " + userId);
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("NotificationRepo", "Error listening notifications", error);
                        listener.onFailure(error);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Notification> notifications = querySnapshot.toObjects(Notification.class);
                        // Set ID cho mỗi notification
                        for (int i = 0; i < notifications.size(); i++) {
                            notifications.get(i).setId(querySnapshot.getDocuments().get(i).getId());
                        }
                        android.util.Log.d("NotificationRepo",
                                "Loaded " + notifications.size() + " notifications for user: " + userId);
                        listener.onNotificationsLoaded(notifications);
                    }
                });
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    public void getUnreadCount(@NonNull String userId, OnUnreadCountListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        listener.onCountLoaded(0);
                        return;
                    }

                    int count = querySnapshot != null ? querySnapshot.size() : 0;
                    listener.onCountLoaded(count);
                });
    }

    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead(@NonNull String userId, @NonNull String notificationId,
            OnOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Đã đọc"))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    public void markAllAsRead(@NonNull String userId, OnOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        listener.onSuccess("Không có thông báo mới");
                        return;
                    }

                    int[] counter = { 0 };
                    int total = querySnapshot.size();

                    querySnapshot.getDocuments().forEach(doc -> {
                        doc.getReference().update("isRead", true)
                                .addOnSuccessListener(aVoid -> {
                                    counter[0]++;
                                    if (counter[0] == total) {
                                        listener.onSuccess("Đã đọc tất cả");
                                    }
                                });
                    });
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Xóa thông báo
     */
    public void deleteNotification(@NonNull String userId, @NonNull String notificationId,
            OnOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess("Đã xóa"))
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Tạo thông báo mới (dùng cho admin hoặc system)
     */
    public void createNotification(@NonNull String userId, @NonNull Notification notification,
            OnOperationListener listener) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Ghi lại ID vào document để dễ truy xuất sau này
                    String docId = documentReference.getId();
                    documentReference.update("id", docId)
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("NotificationRepo",
                                        "Notification created with id: " + docId + " for user: " + userId);
                                listener.onSuccess(docId);
                            })
                            .addOnFailureListener(e -> {
                                // Nếu update id thất bại, vẫn báo thành công nhưng log lỗi
                                android.util.Log.w("NotificationRepo",
                                        "Created notification but failed to set id field", e);
                                listener.onSuccess(docId);
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("NotificationRepo", "Failed to create notification for user: " + userId, e);
                    listener.onFailure(e);
                });
    }

    // Callback Interfaces
    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);

        void onFailure(Exception e);
    }

    public interface OnUnreadCountListener {
        void onCountLoaded(int count);
    }

    public interface OnOperationListener {
        void onSuccess(String message);

        void onFailure(Exception e);
    }
}
