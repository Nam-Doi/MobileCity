package com.example.androidapp.views.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private SimpleDateFormat dateFormat;
    private String currentUserId;
    private OnReviewInteractionListener listener;

    public interface OnReviewInteractionListener {
        void onEditReview(Review review);
        void onDeleteReview(Review review);
    }

    public ReviewAdapter(Context context, List<Review> reviewList, String currentUserId, OnReviewInteractionListener listener) {
        this.context = context;
        this.reviewList = reviewList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        if (review.getUserName() != null && !review.getUserName().isEmpty()) {
            holder.tvUserName.setText(review.getUserName());
        } else {
            holder.tvUserName.setText("Người dùng ẩn danh");
        }

        holder.rbReviewRating.setRating((float) review.getRating());
        holder.tvComment.setText(review.getComment());

        if (review.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(review.getTimestamp()));
        } else {
            holder.tvTimestamp.setText("");
        }
        //Longclick để sửa xóa bình luận
        holder.itemView.setOnLongClickListener(v -> {
            // Kiểm tra quyền sở hữu
            if (currentUserId.equals(review.getUserId())) {
                showEditDeleteDialog(review);
            } else {
                Toast.makeText(context, "Bạn chỉ có thể sửa/xóa bình luận của mình", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        //Kết thúc longclick
    }

    //Hiển thị lựa chọn sửa,xóa
    private void showEditDeleteDialog(Review review) {
        CharSequence[] options = {"Sửa bình luận", "Xóa bình luận"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn hành động");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                //Chọn sửa
                listener.onEditReview(review);
            } else if (which == 1) {
                //Chọn xóa
                listener.onDeleteReview(review);
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // Hàm để cập nhật dữ liệu mới
    public void setReviews(List<Review> newReviews) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviews);
        notifyDataSetChanged();
    }

    // Lớp ViewHolder
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvTimestamp; // Đã xóa tvAvatar
        RatingBar rbReviewRating;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            // tvAvatar = itemView.findViewById(R.id.tv_avatar); // Đã xóa dòng này
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            rbReviewRating = itemView.findViewById(R.id.rb_review_rating);
        }
    }
}
