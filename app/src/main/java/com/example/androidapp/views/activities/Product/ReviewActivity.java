package com.example.androidapp.views.activities.Product;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Review;
import com.example.androidapp.views.adapters.ReviewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity implements ReviewAdapter.OnReviewInteractionListener{
    // Views
    Spinner spn_rating;
    RatingBar rb_rating, overallRatingBar;
    EditText edt_review;
    Button btn_review;
    TextView tv_overall_rating, tvTotalReviews, tv_count1, tv_count2, tv_count3, tv_count4, tv_count5;
    ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    boolean editordelete = false;
    String currentEditingReviewId = null, currentProductId;
    RecyclerView rv_reviews;
    ReviewAdapter reviewAdapter;
    List<Review> reviewList = new ArrayList<>(); // Danh sách gốc chứa tất cả review

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Hàm ánh xạ view
        initViews();

        // Lấy ID sản phẩm
        currentProductId = getIntent().getStringExtra("PRODUCT_ID");
        if (currentProductId == null) {
            Log.e("ReviewError", "Không nhận được Product ID");
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cài đặt các thành phần
        setupRatingBarInput();
        setupSpinner();
        setupRecyclerView();

        // Cài đặt sự kiện click cho nút "Đánh giá"
        btn_review.setOnClickListener(v -> handleSubmitReview());

        // Tải danh sách đánh giá
        loadReviews();
    }

    private void initViews() {
        // Phần nhập review
        spn_rating = findViewById(R.id.spn_rating);
        rb_rating = findViewById(R.id.rb_rating);
        edt_review = findViewById(R.id.edt_review);
        btn_review = findViewById(R.id.btn_review);
        // Phần thống kê
        tv_overall_rating = findViewById(R.id.tv_overall_rating);
        overallRatingBar = findViewById(R.id.rb_overall_rating);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        progressBar5 = findViewById(R.id.progressBar5);
        tv_count5 = findViewById(R.id.tv_count5);
        progressBar4 = findViewById(R.id.progressBar4);
        tv_count4 = findViewById(R.id.tv_count4);
        progressBar3 = findViewById(R.id.progressBar3);
        tv_count3 = findViewById(R.id.tv_count3);
        progressBar2 = findViewById(R.id.progressBar2);
        tv_count2 = findViewById(R.id.tv_count2);
        progressBar1 = findViewById(R.id.progressBar1);
        tv_count1 = findViewById(R.id.tv_count1);
        //Phần danh sách bl
        rv_reviews = findViewById(R.id.rv_reviews);
    }

    //Hàm rating
    private void setupRatingBarInput() {
        rb_rating.setRating(1.0f); // Set mặc định là 1
        rb_rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser && rating < 1.0f) {
                    ratingBar.setRating(1.0f);
                }
            }
        });
    }

    //Spiner lọc bình luận
    private void setupSpinner() {
        String[] items = new String[]{"Tất cả", "5 ★", "4 ★", "3 ★", "2 ★", "1 ★"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_rating.setAdapter(adapter);
        spn_rating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterReviewsByRating(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    //set up recyclerview
    private void setupRecyclerView() {
        String userId = currentUser.getUid();
        // Khởi tạo Adapter với danh sách rỗng
        reviewAdapter = new ReviewAdapter(this, new ArrayList<>(), userId, this);
        rv_reviews.setLayoutManager(new LinearLayoutManager(this));
        rv_reviews.setAdapter(reviewAdapter);
    }

    //Lấy review từ database
    private void loadReviews() {
        db.collection("reviews")
                .whereEqualTo("productId", currentProductId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear(); // Xóa list cũ
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Review review = document.toObject(Review.class);
                            review.setId(document.getId());
                            reviewList.add(review);
                        }
                    }
                    // Cập nhật adapter (hiển thị "Tất cả" theo mặc định)
                    filterReviewsByRating(spn_rating.getSelectedItemPosition());

                    // Cập nhật giao diện thống kê
                    calculateAndDisplayReviewStats(reviewList);
                })
                .addOnFailureListener(e -> {
                    Log.e("LoadReviews", "Lỗi khi đọc reviews", e);
                });
    }

    //Lọc đánh giá theo số sao
    private void filterReviewsByRating(int position) {
        // Nếu danh sách gốc rỗng, không làm gì cả
        if (reviewList == null) return;

        // position 0 = "Tất cả"
        if (position == 0) {
            reviewAdapter.setReviews(reviewList); // Hiển thị tất cả
            return;
        }

        // Các vị trí khác: 1 = 5 sao, 2 = 4 sao, ..., 5 = 1 sao
        int targetRating = 5 - (position - 1);
        List<Review> filteredList = new ArrayList<>();
        for (Review review : reviewList) {
            if (Math.round(review.getRating()) == targetRating) {
                filteredList.add(review);
            }
        }
        reviewAdapter.setReviews(filteredList);
    }

    //Thống kê review(số đánh giá, progressbar)
    private void calculateAndDisplayReviewStats(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            // Đặt về trạng thái mặc định nếu không có review
            tv_overall_rating.setText("--/5");
            tvTotalReviews.setText("0 lượt đánh giá");
            overallRatingBar.setRating(0);
            progressBar5.setProgress(0); tv_count5.setText("0");
            progressBar4.setProgress(0); tv_count4.setText("0");
            progressBar3.setProgress(0); tv_count3.setText("0");
            progressBar2.setProgress(0); tv_count2.setText("0");
            progressBar1.setProgress(0); tv_count1.setText("0");
            return;
        }

        int totalReviews = reviews.size();
        double totalRatingSum = 0;
        int[] starCounts = new int[6]; // Index 1-5

        for (Review review : reviews) {
            totalRatingSum += review.getRating();
            int star = (int) Math.round(review.getRating());
            if (star >= 1 && star <= 5) {
                starCounts[star]++;
            }
        }

        double averageRating = totalRatingSum / totalReviews;

        // Cập nhật UI
        tv_overall_rating.setText(String.format(Locale.US, "%.1f/5", averageRating));
        overallRatingBar.setRating((float) averageRating);
        tvTotalReviews.setText(totalReviews + " lượt đánh giá");

        // Cập nhật progress bar
        progressBar5.setProgress((int) ((double) starCounts[5] / totalReviews * 100));
        tv_count5.setText(String.valueOf(starCounts[5]));
        progressBar4.setProgress((int) ((double) starCounts[4] / totalReviews * 100));
        tv_count4.setText(String.valueOf(starCounts[4]));
        progressBar3.setProgress((int) ((double) starCounts[3] / totalReviews * 100));
        tv_count3.setText(String.valueOf(starCounts[3]));
        progressBar2.setProgress((int) ((double) starCounts[2] / totalReviews * 100));
        tv_count2.setText(String.valueOf(starCounts[2]));
        progressBar1.setProgress((int) ((double) starCounts[1] / totalReviews * 100));
        tv_count1.setText(String.valueOf(starCounts[1]));
    }

    //Hàm xem là đang sửa hay xóa bl
    private void handleSubmitReview() {
        if (editordelete) {
            // Nếu đang sửa -> gọi logic Cập nhật
            updateExistingReview();
        } else {
            // Nếu không -> gọi logic Tạo mới
            createNewReview();
        }
    }


    //Vết bl mới

    private void createNewReview() {
        double rating = rb_rating.getRating();
        String comment = edt_review.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng viết bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        // Gọi hàm để lấy tên và gửi review
        fetchUserNameAndSubmitReview(rating, comment);
    }

    //Lấy tên người dùng từ Firestore, sau đó tạo và gửi Review
    private void fetchUserNameAndSubmitReview(double rating, String comment) {
        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fullnameFromDb = documentSnapshot.getString("fullName");
                    String userName = fullnameFromDb;
                    // Đã có tên -> Tạo Review và gửi
                    Review newReview = new Review(currentProductId, userId, userName, rating, comment);
                    submitReviewToFirestore(newReview); // Gọi hàm gửi riêng
                });
    }

    //Gửi đối tượng Review đã tạo lên collection "reviews".
    private void submitReviewToFirestore(Review review) {
        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ReviewActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                    loadReviews();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReviewActivity.this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //Sửa bl
    private void updateExistingReview() {
        double rating = rb_rating.getRating();
        String comment = edt_review.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng viết bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thực hiện cập nhật trên Firestore
        db.collection("reviews").document(currentEditingReviewId)
                .update(
                        "rating", rating,
                        "comment", comment
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    resetForm(); // Reset lại form
                    loadReviews(); // Tải lại danh sách
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //Đặt lại trạng thái form về như cũ trước khi sửa bl
    private void resetForm() {
        edt_review.setText("");
        rb_rating.setRating(1.0f); // Reset về 1 sao
        btn_review.setText("Đánh giá"); // Đổi chữ về "Đánh giá"

        // Reset trạng thái
        editordelete = false;
        currentEditingReviewId = null;
    }

    //SK khi ấn sửa bl
    @Override
    public void onEditReview(Review review) {
        rb_rating.setRating((float) review.getRating());
        edt_review.setText(review.getComment());
        edt_review.requestFocus(); // Focus vào ô
        btn_review.setText("Cập nhật");
        editordelete = true;
        currentEditingReviewId = review.getId();
    }

    //SK khi chọn xóa
    @Override
    public void onDeleteReview(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Người dùng đồng ý, tiến hành xóa
                    db.collection("reviews").document(review.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                                loadReviews(); // Tải lại danh sách review sau khi xóa
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null) // Không làm gì khi bấm Hủy
                .show();
    }
}
