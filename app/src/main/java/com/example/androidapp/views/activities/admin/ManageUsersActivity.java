package com.example.androidapp.views.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.R;
import com.example.androidapp.views.adapters.UserAdapter;
import com.example.androidapp.models.users;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private static final String TAG = "ManageUsersActivity";

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<users> userList;
    private FirebaseFirestore db;
    private SearchView searchViewEmail;
    private Spinner spinnerRole;
    private FloatingActionButton fabCreateUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        // Ánh xạ các view
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        searchViewEmail = findViewById(R.id.searchViewEmail);
        spinnerRole = findViewById(R.id.spinnerRole);
        fabCreateUser = findViewById(R.id.fabCreateUser);

        // Màn hình tạo tài khoản
        fabCreateUser.setOnClickListener(view -> {
            Intent intent = new Intent(ManageUsersActivity.this, CreateUserActivity.class);
            startActivity(intent);
        });

        // Tạo Adapter
        setupRecyclerView();

        // Các hàm sau chỉ lọc dữ liệu
        setupRoleSpinner();
        setupSearchView();

        // Tải toàn bộ danh sách user ban đầu
        loadUsersFromFirestore(db.collection("users"));
    }

    private void setupRecyclerView() {
        // Adapter được tạo với userList và listener
        userAdapter = new UserAdapter(this, userList, user -> {
            Intent intent = new Intent(ManageUsersActivity.this, UserDetailActivity.class);
            intent.putExtra("USER_ID", user.getUid());
            startActivity(intent);
        });
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        // Gán adapter cho RecyclerView
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = parent.getItemAtPosition(position).toString();
                Query query;
                if (selectedRole.equals("Tất cả")) {
                    query = db.collection("users");
                } else {
                    query = db.collection("users").whereEqualTo("role", selectedRole);
                }
                loadUsersFromFirestore(query);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchView() {
        searchViewEmail.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    loadUsersFromFirestore(db.collection("users").whereEqualTo("email", query.trim()));
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.isEmpty()) {
                    // Khi xóa hết chữ, tải lại danh sách theo bộ lọc role hiện tại
                    String selectedRole = spinnerRole.getSelectedItem().toString();
                    Query query;
                    if (selectedRole.equals("Tất cả")) {
                        query = db.collection("users");
                    } else {
                        query = db.collection("users").whereEqualTo("role", selectedRole);
                    }
                    loadUsersFromFirestore(query);
                }
                return false;
            }
        });
    }

    private void loadUsersFromFirestore(Query query) {
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        users user = document.toObject(users.class);
                        userList.add(user);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi convert document: " + document.getId(), e);
                    }
                }
                userAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Lỗi khi truy vấn: ", task.getException());
            }
        });
    }
}