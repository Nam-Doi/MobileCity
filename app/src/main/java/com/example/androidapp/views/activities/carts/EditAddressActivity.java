package com.example.androidapp.views.activities.carts;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidapp.R;
import com.example.androidapp.models.AddressItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAddressActivity extends AppCompatActivity {
    private Button btnUpdate, btnDelete;
    private EditText edtName, edtPhone, edtAddress;
    private ImageView imgBack;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private AddressItems currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_address);
        initViews();
        initFireBase();
        loadAddressData();
        setupListeners();
    }
    private void initViews(){
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        imgBack = findViewById(R.id.btnBack);

    }
    private void initFireBase(){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    private void loadAddressData(){
        currentAddress = new AddressItems();
        currentAddress.setAddressId(getIntent().getStringExtra("addressId"));
        currentAddress.setReceiverName(getIntent().getStringExtra("receiverName"));
        currentAddress.setReceiverPhone(getIntent().getStringExtra("receiverPhone"));
        currentAddress.setAddress(getIntent().getStringExtra("address"));
        edtName.setText(currentAddress.getReceiverName());
        edtPhone.setText(currentAddress.getReceiverPhone());
        edtAddress.setText(currentAddress.getAddress());


    }

    private void setupListeners() {
        imgBack.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateAddress());
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void updateAddress(){
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)){
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;

        }
        String UserId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(UserId)
                .collection("addresses")
                .document(currentAddress.getAddressId())
                .update(
                        "receiverName", name,
                        "receiverPhone", phone,
                        "address", address
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật địa chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAddress())
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void deleteAddress() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(currentAddress.getAddressId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa địa chỉ!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}