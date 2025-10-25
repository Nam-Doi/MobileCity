package com.example.androidapp.views.adapters; // Gói (package) có thể khác tùy dự án của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.users; // Import model users
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
// import com.bumptech.glide.Glide; // Sẽ cần cho việc tải ảnh từ URL

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<users> userList;
    private OnUserClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnUserClickListener {
        void onUserClick(users user);
    }

    public UserAdapter(Context context, List<users> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho mỗi item từ file XML layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_account, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Lấy dữ liệu của user tại vị trí `position`
        users currentUser = userList.get(position);

        // Gán dữ liệu lên các view trong ViewHolder
        holder.textFullName.setText(currentUser.getFullName());
        holder.textEmail.setText(currentUser.getEmail());
        holder.textRole.setText(currentUser.getRole());

        // Xử lý hiển thị trạng thái (isActive)
        if (currentUser.isActive()) {
            holder.textStatus.setText("Active");
            holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green)); // Bạn cần định nghĩa màu green trong colors.xml
        } else {
            holder.textStatus.setText("Disabled");
            holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red)); // Và màu red
        }

        // Tải ảnh đại diện (avatar)
        // Lưu ý: Cần dùng thư viện như Glide để tải ảnh từ URL
        // Glide.with(context)
        //     .load(currentUser.getAvatarUrl()) // Giả sử model User có getAvatarUrl()
        //     .placeholder(R.drawable.ic_user) // Ảnh hiển thị trong lúc tải
        //     .into(holder.userAvatar);

        // Bắt sự kiện click vào toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            listener.onUserClick(currentUser);
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng item trong danh sách
        return userList.size();
    }

    // Lớp ViewHolder để giữ các tham chiếu đến view của mỗi item
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView userAvatar;
        TextView textFullName, textEmail, textRole, textStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            textFullName = itemView.findViewById(R.id.textFullName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRole = itemView.findViewById(R.id.textRole);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}