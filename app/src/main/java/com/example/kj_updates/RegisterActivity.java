package com.example.kj_updates;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kj_updates.databinding.ActivityRegisterBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        showMessage("Google sign in failed: " + e.getMessage());
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.buttonRegister.setOnClickListener(v -> attemptRegister());
        binding.buttonGoogleRegister.setOnClickListener(v -> signUpWithGoogle());
        binding.textGoLogin.setOnClickListener(v -> finish());
    }

    private void signUpWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> routeAuthenticatedUser(authResult.getUser().getUid()))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showMessage(e.getLocalizedMessage());
                });
    }

    private void routeAuthenticatedUser(String uid) {
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
        binding.buttonGoogleRegister.setEnabled(!loading);
    }

    private void showMessage(int resId) {
        Snackbar.make(binding.getRoot(), resId, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
