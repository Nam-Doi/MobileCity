package com.example.androidapp.views.activities.carts;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.models.PaymentMethod;
import com.example.androidapp.views.adapters.cartAdt.PaymentMethodAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodActivity extends AppCompatActivity {
    FirebaseFirestore db;
    RecyclerView recyclerView;
    List<PaymentMethod> list;
    PaymentMethodAdapter adapter;
    ImageView imgback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_method);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rvpaymentMethod);
        list= new ArrayList<>();
        adapter = new PaymentMethodAdapter(list, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imgback = findViewById(R.id.btnBack);
        imgback.setOnClickListener(v-> finish());

        db.collection("payment_methods")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    list.clear();
                    Log.d("PAYMENT", "Tổng số document: " + querySnapshot.size());
                    for (DocumentSnapshot doc : querySnapshot) {
                        Log.d("PAYMENT", "Doc ID: " + doc.getId() + " => " + doc.getData());
                        PaymentMethod method = doc.toObject(PaymentMethod.class);
                        if (method != null) {
                            method.setId(doc.getId());
                            list.add(method);
                        } else {
                            Log.w("PAYMENT", "Không convert được doc: " + doc.getId());
                        }
                    }
                    Log.d("PAYMENT", "Tổng cộng list: " + list.size());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("PAYMENT", "Lỗi Firestore", e));

    }
}