package com.example.androidapp.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
//        signIn("nammount19@gmail.com","Nammount19@");
//        signIn("nammount28@gmail.com","Nammount19@");
        createAccountAndSendVerification("nammount19@gmail.com","Nammount19@");


    }
	private void signIn(String email, String password){
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInWithEmail:success");
							FirebaseUser user = mAuth.getCurrentUser();
							if (user != null) {
								Log.d(TAG, "Signed in as: " + user.getEmail());
								if (!user.isEmailVerified()) {
									sendVerificationEmail(user);
								} else {
									Toast.makeText(MainActivity.this, "Email đã được xác minh.", Toast.LENGTH_SHORT).show();
								}
							}
						} else {
							Exception e = task.getException();
							Log.w(TAG, "signInWithEmail:failure", e);
							Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
						}
					}
				});
	}
	private void createAccountAndSendVerification(String email, String password) {
		mAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {
						Log.d(TAG, "createUserWithEmail:success");
						FirebaseUser user = mAuth.getCurrentUser();
						if (user != null) {
							sendVerificationEmail(user);
						} else {
							Log.e(TAG, "createUserWithEmail: User is null after successful registration.");
						}
					} else {
						Exception e = task.getException();
						Log.w(TAG, "createUserWithEmail:failure", e);
						if (e instanceof FirebaseAuthUserCollisionException) {
							// Tài khoản đã tồn tại: đăng nhập rồi gửi lại email xác minh
							signIn(email, password);
						} else {
							Toast.makeText(MainActivity.this, "Đăng ký thất bại: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
						}
					}
				});
	}
    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email verification sent to: " + user.getEmail());
                            Toast.makeText(MainActivity.this,
                                    "Email verification sent.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to send verification email.", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        //kiem tra nguoi dung dang nhap chua
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "onStart: " + currentUser.getEmail());
        }else{
            Log.d(TAG, "onStart: null");
        }
    }

}