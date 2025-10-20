package com.example.androidapp.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidapp.R;
import com.example.androidapp.models.Order;
import com.example.androidapp.views.adapters.OrderManagementAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class OrderListFragment extends Fragment {

    private static final String ARG_STATUS = "order_status";
    private String status;

    private RecyclerView recyclerView;
    private OrderManagementAdapter adapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener; // Để lắng nghe real-time

    // "Nhà máy" tạo ra Fragment này
    public static OrderListFragment newInstance(String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status); // Gói trạng thái (pending, shipped...)
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS); // Nhận trạng thái
        }
        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout "fragment_order_list.xml"
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.rv_order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dùng Adapter của bạn (chúng ta sẽ tạo ở bước 6)
        adapter = new OrderManagementAdapter(orderList, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForOrders(); // Bắt đầu lắng nghe khi Fragment hiển thị
    }

    @Override
    public void onStop() {
        super.onStop();
        if (orderListener != null) {
            orderListener.remove(); // Dừng lắng nghe khi Fragment bị ẩn
        }
    }

    private void listenForOrders() {
        if (status == null) return;

        Query query = db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Đây là logic lọc chính
        query = query.whereEqualTo("status", status);

        // Dùng addSnapshotListener để tự động cập nhật (real-time)
        orderListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                orderList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Order order = doc.toObject(Order.class);
                    // order.setOrderId(doc.getId()); // Model đã có orderId rồi
                    orderList.add(order);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}