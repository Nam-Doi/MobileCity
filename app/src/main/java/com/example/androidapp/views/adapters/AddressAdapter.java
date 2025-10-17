package com.example.androidapp.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.AddressItems;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<AddressItems> addressItemsList;
    private OnCartChangeListener listener;

    public AddressAdapter(List<AddressItems> addressItemsList, OnCartChangeListener listener) {
        this.addressItemsList = addressItemsList;
        this.listener = listener;
    }
    public interface OnCartChangeListener {
        void onCartUpdated();
        void onEditAddress(AddressItems item);

    }
    @NonNull
    @Override
    public AddressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressAdapter.AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.AddressViewHolder holder, int position) {
        AddressItems item = addressItemsList.get(position);
        holder.tvReceiverName.setText(item.getReceiverName());
        holder.tvReceiverPhone.setText(item.getReceiverPhone());
        holder.tvAddress.setText(item.getAddress());
        holder.rbSelectAddress.setChecked(item.isSelected());

        // Khi click vào radio hoặc layout thì chọn địa chỉ
        View.OnClickListener selectListener = v -> {
            for (AddressItems address : addressItemsList) {
                address.setSelected(false);
            }
            item.setSelected(true);
            notifyDataSetChanged(); // cập nhật lại danh sách

            if (listener != null) {
                listener.onCartUpdated(); // báo cho Activity biết có thay đổi
            }
        };

        holder.layoutAddress.setOnClickListener(selectListener);
        holder.rbSelectAddress.setOnClickListener(selectListener);
        //edit dia chi
        holder.tvEditAddress.setOnClickListener(v -> {
            if (listener != null) listener.onEditAddress(item);
        });


    }

    @Override
    public int getItemCount() {
        return addressItemsList.size();
    }
    public void removeItem(int position) {
        addressItemsList.remove(position);
        notifyItemRemoved(position);
        if (listener != null) {
            listener.onCartUpdated();
        }
    }
    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbSelectAddress;
        LinearLayout layoutAddress;
        TextView tvReceiverName, tvReceiverPhone, tvAddress, tvEditAddress;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            rbSelectAddress = itemView.findViewById(R.id.rbSelectAddress);
            layoutAddress = itemView.findViewById(R.id.layoutAddress);
            tvReceiverName = itemView.findViewById(R.id.tvReceiverName);
            tvReceiverPhone = itemView.findViewById(R.id.tvReceiverPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvEditAddress = itemView.findViewById(R.id.tvEditAddress);

        }

    }

}
