package com.example.androidapp.views.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    public interface OnOptionClickListener {
        void onOptionClick(String option);
    }

    private List<String> options;
    private int selectedPosition = 0;
    private OnOptionClickListener listener;

    public OptionAdapter(List<String> options, OnOptionClickListener listener) {
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        String option = options.get(position);

        // LUÔN LUÔN HIỂN THỊ CHỮ
        holder.optionView.setText(option);
        // Bỏ tint để nền hiển thị đúng theo selector
        holder.optionView.setBackgroundTintList(null);

        // Xử lý logic chọn (giữ nguyên)
        holder.itemView.setSelected(selectedPosition == position);
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    listener.onOptionClick(options.get(selectedPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    static class OptionViewHolder extends RecyclerView.ViewHolder {
        TextView optionView;
        OptionViewHolder(View itemView) {
            super(itemView);
            optionView = (TextView) itemView;
        }
    }
}
