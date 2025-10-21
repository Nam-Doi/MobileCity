package com.example.androidapp.views.adapters; // Hoặc package của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R; // Đảm bảo bạn có R

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls = new ArrayList<>(); // Khởi tạo list rỗng

    public ImageSliderAdapter(Context context) {
        this.context = context;
    }

    // Hàm để cập nhật danh sách ảnh mới
    public void setImages(List<String> newImageUrls) {
        this.imageUrls.clear();
        if (newImageUrls != null) {
            this.imageUrls.addAll(newImageUrls);
        }
        notifyDataSetChanged(); // Báo cho ViewPager biết dữ liệu đã thay đổi
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bạn cần tạo 1 layout đơn giản chỉ chứa ImageView
        // Ví dụ: R.layout.item_slider_image
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                .error(R.drawable.ic_launcher_background) // Ảnh lỗi (nếu có)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // ID của ImageView trong layout item_slider_image.xml
            imageView = itemView.findViewById(R.id.sliderImageView);
        }
    }
}