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
import com.example.androidapp.views.adapters.MyOrdersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class MyOrderListFragment extends Fragment {

    private static final String ARG_STATUS = "order_status";
    private String status;
    private String currentUid;

    private RecyclerView recyclerView;
    private MyOrdersAdapter adapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;

    public static MyOrderListFragment newInstance(String status) {
        MyOrderListFragment fragment = new MyOrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        // Lấy UID của User
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tái sử dụng layout từ Giai đoạn 2
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.rv_order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MyOrdersAdapter(orderList, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForUserOrders(); // Bắt đầu lắng nghe
    }

    @Override
    public void onStop() {
        super.onStop();
        if (orderListener != null) {
            orderListener.remove(); // Dừng lắng nghe
        }
    }

    // --- LOGIC TRUY VẤN QUAN TRỌNG NHẤT ---
    private void listenForUserOrders() {
        if (status == null || currentUid == null) return;

        Query query = db.collection("orders")
                .whereEqualTo("userId", currentUid) // 1. Lọc theo User
                .whereEqualTo("status", status)     // 2. Lọc theo Trạng thái
                .orderBy("createdAt", Query.Direction.DESCENDING);

        orderListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                orderList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Order order = doc.toObject(Order.class);
                    order.setOrderId(doc.getId());
                    orderList.add(order);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}