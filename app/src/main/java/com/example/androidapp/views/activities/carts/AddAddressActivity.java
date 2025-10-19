package com.example.androidapp.views.activities.carts;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.example.androidapp.models.AddressItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddAddressActivity extends AppCompatActivity {
    private Button btnCheckout;
    private ImageView imgBack;
    private EditText edtName, edtPhone, edtAddress;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference addressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);
        initViews();
        initFireBase();
        etupListeners();
    }
    private void initViews(){
        btnCheckout = findViewById(R.id.btnCheckout);
        imgBack = findViewById(R.id.btnBack);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
    }
    private void initFireBase(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Lấy userId hiện tại
        String userId = auth.getCurrentUser().getUid();

        // Trỏ đến subcollection addresses
        addressRef = db.collection("users")
                .document(userId)
                .collection("addresses");
    }
    private void etupListeners(){
        imgBack.setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> { saveAddress(); });
    }
    private void saveAddress(){
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressItems newAddress = new AddressItems(name, phone, address, false);

        addressRef.add(newAddress)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã lưu địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình trước
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}