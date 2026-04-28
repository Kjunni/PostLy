package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityRegisterBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.buttonRegister.setOnClickListener(v -> attemptRegister());
        binding.textGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String email = String.valueOf(binding.inputEmail.getText()).trim();
        String password = String.valueOf(binding.inputPassword.getText()).trim();
        String confirmPassword = String.valueOf(binding.inputConfirmPassword.getText()).trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage(R.string.message_fill_all_fields);
            return;
        }

        if (password.length() < 6) {
            showMessage(R.string.message_password_short);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage(R.string.message_password_mismatch);
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    Snackbar.make(binding.getRoot(), R.string.message_register_success, Snackbar.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void setLoading(boolean loading) {
        binding.progressRegister.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.buttonRegister.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
