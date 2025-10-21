package com.example.androidapp.views.adapters.cartAdt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidapp.R;
import com.example.androidapp.models.PaymentMethod;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
    private final List<PaymentMethod> list;
    private final Context context;
    private int selectedPosition = -1;

    public PaymentMethodAdapter(List<PaymentMethod> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public PaymentMethodAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodAdapter.ViewHolder holder, int position) {
        PaymentMethod method = list.get(position);
        holder.tvName.setText(method.getName());

        Glide.with(context)
                .load(method.getIconUrl())
                .placeholder(R.drawable.ic_cod)
                .into(holder.ivIcon);

        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();

            // Return the selected payment method to the calling Activity
            if (context instanceof Activity) {
                Intent result = new Intent();
                result.putExtra("paymentMethodId", method.getId());
                result.putExtra("paymentMethodName", method.getName());
                ((Activity) context).setResult(Activity.RESULT_OK, result);
                ((Activity) context).finish();
            }
        });

        holder.radioButton.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public PaymentMethod getSelectedMethod() {
        return selectedPosition != -1 ? list.get(selectedPosition) : null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
