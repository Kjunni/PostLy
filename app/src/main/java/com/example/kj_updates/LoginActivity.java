package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.buttonDemoLogin.setOnClickListener(v -> enterDemoMode());
        binding.textGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SessionManager.isDemoMode(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        FirebaseUser user = auth == null ? null : auth.getCurrentUser();
        if (user != null) {
            routeAuthenticatedUser(user.getUid());
        }
    }

    private void enterDemoMode() {
        SessionManager.setDemoMode(this, true);
        Snackbar.make(binding.getRoot(), R.string.message_demo_login, Snackbar.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void attemptLogin() {
        String email = String.valueOf(binding.inputEmail.getText()).trim();
        String password = String.valueOf(binding.inputPassword.getText()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage(R.string.message_fill_all_fields);
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> routeAuthenticatedUser(result.getUser().getUid()))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void routeAuthenticatedUser(String uid) {
        SessionManager.setDemoMode(this, false);
        setLoading(true);
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        startActivity(new Intent(this, ProfileSetupActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean loading) {
        binding.progressLogin.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.buttonLogin.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
