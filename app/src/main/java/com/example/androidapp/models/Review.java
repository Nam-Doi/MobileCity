package com.example.androidapp.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Review {

    // @DocumentId sẽ tự động gán ID của document vào trường này
    // Giờ đây bạn không cần gọi review.setId() nữa, nhưng chúng ta vẫn thêm
    // getter/setter cho đầy đủ
    @DocumentId
    private String id;

    // Các trường dữ liệu của bạn
    String productId;
    String userId;
    String userName;
    double rating;
    String comment;

    // @ServerTimestamp sẽ tự động gán ngày giờ của server khi tạo
    @ServerTimestamp
    Date timestamp;

    // Constructor rỗng bắt buộc cho Firestore
    public Review() {}

    // Constructor để tạo review mới
    public Review(String productId, String userId, String userName, double rating, String comment) {
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        // timestamp sẽ được gán tự động bởi server
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
