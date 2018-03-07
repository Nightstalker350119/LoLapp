package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private EditText email;
    private EditText password;
    private Button signIn;
    private TextView toggleRegister;
    private TextView accountRecovery;
    private TextView confirmPasswordTitle;
    private EditText confirmPassword;

    private static boolean autoShowCompleted = false;

    boolean busy = false;
    boolean registerMode = false;

    public static void startSignIn(Activity source) {
        Intent intent = new Intent(source, LoginActivity.class);
        source.startActivity(intent);
        enterTransition(source);
    }

    private static void enterTransition(Activity source) {
        source.overridePendingTransition(R.anim.slide_from_right, R.anim.stay_put);
    }

    private static void leaveTransition(Activity source) {
        source.overridePendingTransition(0, R.anim.slide_to_right);
    }

    @Override
    public void onBackPressed() {
        finish();
        leaveTransition(this);
    }

    public static void autoShowSignIn(Activity source) {
        if (autoShowCompleted)
            return;
        autoShowCompleted = true;

        startSignIn(source);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPasswordTitle = findViewById(R.id.confirm_password_title);
        confirmPassword = findViewById(R.id.confirm_password);
        signIn = findViewById(R.id.sign_in);
        toggleRegister = findViewById(R.id.toggle_register);
        accountRecovery = findViewById(R.id.account_recovery);

        signIn.setOnClickListener(this);
        toggleRegister.setOnClickListener(this);
        accountRecovery.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in:
                if (!registerMode)
                    signInAccount();
                else
                    registerAccount();
                break;
            case R.id.toggle_register:
                toggleSignInRegister();
                break;
            case R.id.account_recovery:
                recoverAccount();
                break;
        }
    }

    private void recoverAccount() {
        if (busy)
            return;

        String userEmail = email.getText().toString();
        Task<Void> task = auth.sendPasswordResetEmail(userEmail);
        busy = true;
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                busy = false;
                if (task.isSuccessful()) {
                    UIHelper.showToast(getApplicationContext(),
                            "The password reset email was sent", Toast.LENGTH_SHORT);
                } else {
                    UIHelper.showToast(getApplicationContext(), "Failed", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void toggleSignInRegister() {
        registerMode = !registerMode;

        if (registerMode) {
            signIn.setText("Register");
            toggleRegister.setText("Sign in with existing account");
            confirmPasswordTitle.setVisibility(View.VISIBLE);
            confirmPassword.setVisibility(View.VISIBLE);
        } else {
            signIn.setText("Sign In");
            toggleRegister.setText("Register");
            confirmPasswordTitle.setVisibility(View.GONE);
            confirmPassword.setVisibility(View.GONE);
        }
    }

    private void registerAccount() {

        String userEmail;
        String userPassword;
        String userConfirmPassword;

        userEmail = email.getText().toString();
        userPassword = password.getText().toString();
        userConfirmPassword = confirmPassword.getText().toString();

        if (!userPassword.equals(userConfirmPassword)) {
            UIHelper.showToast(getApplicationContext(),
                    "Passwords must match", Toast.LENGTH_SHORT);
            return;
        }

        Task<AuthResult> task = auth.createUserWithEmailAndPassword(userEmail, userPassword);
        busy = true;
        task.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                busy = false;
                if (task.isSuccessful()) {
                    finish();
                } else {
                    UIHelper.showToast(getApplicationContext(),
                            "Unable to create account", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void signInAccount() {
        if (busy)
            return;

        String userEmail;
        String userPassword;

        userEmail = email.getText().toString();
        userPassword = password.getText().toString();
        Task<AuthResult> task = auth.signInWithEmailAndPassword(userEmail, userPassword);
        busy = true;
        task.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                busy = false;
                if (task.isSuccessful()) {
                    finish();
                } else {
                    UIHelper.showToast(getApplicationContext(),
                            "Invalid email and/or password.", Toast.LENGTH_SHORT);
                }
            }
        });
    }
}
