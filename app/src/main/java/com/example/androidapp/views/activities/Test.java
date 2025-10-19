package com.example.androidapp.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Test extends AppCompatActivity {
    Button btn14pro, btns23;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        btn14pro=findViewById(R.id.btn14pro);
        btns23=findViewById(R.id.btns23);
        btn14pro.setOnClickListener(v -> openDetail("iphone_14_pro")); // documentId bạn đã set trước
        btns23.setOnClickListener(v -> openDetail("samsung_galaxy_s23"));
    }
    private void openDetail(String docId){
        Intent intent = new Intent(this, DetailProductActivity.class);
        intent.putExtra("DOC_ID", docId); // gửi document id
        startActivity(intent);
    }

}
