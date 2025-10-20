package com.example.androidapp.views.adapters.cartAdt;

import android.content.Context;
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

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder>{
    private List<PaymentMethod> list;
    private Context context;
    private int selectedPossition = -1;
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
                .placeholder(R.drawable.ic_cod) // icon mặc định
                .into(holder.ivIcon);

        holder.radioButton.setChecked(position == selectedPossition);
        holder.itemView.setOnClickListener(v -> {
            selectedPossition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public PaymentMethod getSelectedMethod(){
        return selectedPossition


 != -1 ? list.get(selectedPossition


) : null;
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
