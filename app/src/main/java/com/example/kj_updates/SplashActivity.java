package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (SessionManager.isDemoMode(this)) {
            open(MainActivity.class);
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            open(LoginActivity.class);
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        open(MainActivity.class);
                    } else {
                        open(ProfileSetupActivity.class);
                    }
                })
                .addOnFailureListener(e -> open(LoginActivity.class));
    }

    private void open(Class<?> target) {
        startActivity(new Intent(this, target));
        finish();
    }
}
