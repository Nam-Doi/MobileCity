package com.example.androidapp.views.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    public static final String TYPE_COLOR = "color";
    public static final String TYPE_MEMORY = "memory";

    private List<String> options;
    private String type;
    private int selectedPosition = 0;

    public OptionAdapter(List<String> options, String type) {
        this.options = options;
        this.type = type;
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

        // Dựa vào "type" để quyết định cách hiển thị
        if (TYPE_COLOR.equals(type)) {
            // Nếu là màu sắc, tô màu nền và không hiển thị chữ
            holder.optionView.setText("");
            holder.optionView.getBackground().setTint(Color.parseColor(option));
        } else {
            // Nếu là bộ nhớ (hoặc loại khác), hiển thị chữ
            holder.optionView.setText(option);
            // Bỏ tint để nền hiển thị đúng theo selector
            holder.optionView.getBackground().setTintList(null);
        }
        // Xử lý logic chọn
        holder.itemView.setSelected(selectedPosition == position);
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
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
