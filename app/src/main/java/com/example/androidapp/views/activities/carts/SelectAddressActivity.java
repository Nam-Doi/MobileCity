package com.example.androidapp.views.activities.carts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.AddressItems;
import com.example.androidapp.views.adapters.cartAdt.AddressAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity implements AddressAdapter.OnCartChangeListener {
    private ImageView imgBack;
    private RecyclerView rvCheckoutAddress;
    private List<AddressItems> addressList;
    private AddressAdapter addressAdapter;
    private LinearLayout layoutAddAddress;


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference addressRef;
    private ListenerRegistration addressListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_address);
        initViews();
        initFireBase();
        listenToAddressUpdates();
        setupRecyclerView();
        setupListener();
    }
    private void initViews(){
        imgBack = findViewById(R.id.btnBack);
        rvCheckoutAddress = findViewById(R.id.rvCheckoutAddress);
        layoutAddAddress = findViewById(R.id.addAddress);
    }
    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        addressRef = db.collection(
                "users").document(userId).
                collection("addresses");

    }
    private void setupRecyclerView() {
        addressList = new ArrayList<>();

        addressAdapter = new AddressAdapter(addressList, this, address -> {
            // Trả về dữ liệu địa chỉ đã chọn về CheckoutActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("receiverName", address.getReceiverName());
            resultIntent.putExtra("receiverPhone", address.getReceiverPhone());
            resultIntent.putExtra("address", address.getAddress());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        rvCheckoutAddress.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutAddress.setAdapter(addressAdapter);
    }

    // Dùng snapshot listener để realtime update
    private void listenToAddressUpdates() {
        addressListener = addressRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (querySnapshot != null) {
                addressList.clear();
                for (var document : querySnapshot.getDocuments()) {
                    AddressItems address = document.toObject(AddressItems.class);
                    if (address != null) {
                        address.setAddressId(document.getId());
                        addressList.add(address);
                    }
                }
                addressAdapter.notifyDataSetChanged();
            }
        });
    }
    private void setupListener(){
        layoutAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivity(intent);
        });
        imgBack.setOnClickListener(v -> finish());


    }
    @Override
    public void onCartUpdated() {
        // Không cần làm gì vì đã có realtime listener
    }
    @Override
    public void onEditAddress(AddressItems item) {
        // Mở màn hình Edit Address
        Intent intent = new Intent(this, EditAddressActivity.class);
        intent.putExtra("addressId", item.getAddressId());
        intent.putExtra("receiverName", item.getReceiverName());
        intent.putExtra("receiverPhone", item.getReceiverPhone());
        intent.putExtra("address", item.getAddress());
        startActivity(intent);
    }
    protected void onDestroy(){
        super.onDestroy();
        if (addressListener != null) {
            addressListener.remove();
        }
    }

}