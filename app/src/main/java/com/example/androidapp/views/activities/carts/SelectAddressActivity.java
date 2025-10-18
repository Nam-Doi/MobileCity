package com.example.androidapp.views.activities.carts;

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
import com.example.androidapp.views.adapters.AddressAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity {
    private ImageView imgBack;
    private RecyclerView rvCheckoutAddress;
    private List<AddressItems> addressList;
    private AddressAdapter addressAdapter;
    private LinearLayout layoutAddAddress;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_address);
        initViews();
        loadAddressData();
        setupRecyclerView();
        setupListener();
    }
    private void initViews(){
        imgBack = findViewById(R.id.btnBack);
        rvCheckoutAddress = findViewById(R.id.rvCheckoutAddress);
        layoutAddAddress = findViewById(R.id.addAddress);
    }
    private void loadAddressData(){
        addressList = new ArrayList<>();
        addressList.add(new AddressItems(
                "Arthur Chen",
                "0362346089",
                "Nhà Văn Hoá Thôn 1 Quảng Hải, Xã Quảng Hải, Huyện Quảng Xương, Thanh Hóa",
                false
        ));
    }
    private void setupRecyclerView(){
        addressAdapter = new AddressAdapter(addressList, null);
        rvCheckoutAddress.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutAddress.setAdapter(addressAdapter);

    }
    private void setupListener(){
        layoutAddAddress.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thêm địa chỉ đang phát triển", Toast.LENGTH_SHORT).show();
        });
        imgBack.setOnClickListener(v -> finish());


    }


}