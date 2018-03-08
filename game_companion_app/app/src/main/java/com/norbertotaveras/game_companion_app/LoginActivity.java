package com.norbertotaveras.game_companion_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private BasicFieldValidator emailValidator;
    private BasicFieldValidator passwordValidator;
    private BasicFieldValidator confirmValidator;

    private TextView emailError;
    private TextView passwordError;
    private TextView confirmError;

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

        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);
        confirmError = findViewById(R.id.confirm_error);

        signIn = findViewById(R.id.sign_in);
        toggleRegister = findViewById(R.id.toggle_register);
        accountRecovery = findViewById(R.id.account_recovery);

        signIn.setOnClickListener(this);
        toggleRegister.setOnClickListener(this);
        accountRecovery.setOnClickListener(this);

        emailValidator = new EmailFieldValidator(email, emailError);
        passwordValidator = new PasswordFieldValidator(password, passwordError);
        confirmValidator = new PasswordsMatchValidator(confirmPassword, password, confirmError);
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

        if (!BasicFieldValidator.allValid(emailValidator, passwordValidator, confirmValidator)) {
            UIHelper.showToast(this, "Errors in form", Toast.LENGTH_SHORT);
            return;
        }

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
                    Exception err = task.getException();
                    String message = messageFromFirebaseException(err);

                    UIHelper.showToast(getApplicationContext(),
                            "Unable to create account: " + message, Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void signInAccount() {
        if (busy)
            return;

        if (!BasicFieldValidator.allValid(emailValidator, passwordValidator)) {
            UIHelper.showToast(this, "Errors in form", Toast.LENGTH_SHORT);
            return;
        }

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
                    Exception err = task.getException();
                    String message = messageFromFirebaseException(err);
                    UIHelper.showToast(getApplicationContext(),
                            "Invalid email and/or password: " + message, Toast.LENGTH_LONG);
                }
            }
        });
    }

    private String messageFromFirebaseException(Exception err) {
        String message;
        if (err instanceof FirebaseAuthEmailException) {
            message = "invalid email address";
        } else if (err instanceof FirebaseAuthWeakPasswordException) {
            message = "password is too weak, use a longer password";
        } else if (err instanceof FirebaseAuthUserCollisionException) {
            message = "an account with that email already exists";
        } else if (err instanceof FirebaseAuthInvalidCredentialsException) {
            message = "invalid email or password";
        } else if (err instanceof FirebaseAuthException) {
            message = "authentication failed";
        } else {
            message = "unexpected authentication error: " + err.toString();
        }
        return message;
    }

    private abstract static class BasicFieldValidator
            implements TextWatcher, View.OnFocusChangeListener {
        protected final TextView textView;
        protected final TextView errorView;

        protected boolean valid;

        static boolean allValid(BasicFieldValidator... validators) {
            boolean result = true;

            for (BasicFieldValidator validator : validators) {
                if (validator != null && !validator.valid) {
                    result = false;
                    break;
                }
            }

            return result;
        }

        public BasicFieldValidator(TextView textView, TextView errorView) {
            this.textView = textView;
            this.errorView = errorView;
            valid = false;
            textView.addTextChangedListener(this);
            textView.setOnFocusChangeListener(this);
        }

        protected void setErrorText(String text) {
            errorView.setText(text != null ? text : "");
            errorView.setVisibility(text != null ? View.VISIBLE : View.GONE);
        }

        protected abstract String getErrorMessage(String text, boolean interactive);

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String message = getErrorMessage(editable.toString(), true);
            valid = (message == null);
            setErrorText(message);
        }

        @Override
        public void onFocusChange(View view, boolean b) {
            String message = getErrorMessage(textView.getText().toString(), view.hasFocus());
            valid = (message == null);
            setErrorText(message);
        }
    }

    private static class EmailFieldValidator extends BasicFieldValidator {
        private Pattern pattern;

        public EmailFieldValidator(TextView textView, TextView errorView) {
            super(textView, errorView);

            pattern = Pattern.compile(
                    "^(?:[^@ ]|\\\\@])+@[^'+,|!\"£$%&/()=?^*ç°§;:_>\\\\\\]\\[@ ]{3,63}$");
        }

        @Override
        protected String getErrorMessage(String text, boolean interactive) {
            // Don't whine while the user is entering a value
            if (interactive)
                return null;

            Matcher matcher = pattern.matcher(text);

            if (!matcher.matches())
                return "Valid email address required";

            // Looks good
            return null;
        }
    }

    private static class PasswordFieldValidator extends BasicFieldValidator {

        public PasswordFieldValidator(TextView textView, TextView errorView) {
            super(textView, errorView);
        }

        @Override
        protected String getErrorMessage(String text, boolean interactive) {
            // Don't whine while editing password
            if (interactive)
                return null;

            int len = text.length();

            if (len == 0)
                return "Password is required";

            if (len < 8)
                return "Password is too short";

            // Looks good
            return null;
        }
    }

    private static class PasswordsMatchValidator extends BasicFieldValidator {
        private final TextView passwordView;

        public PasswordsMatchValidator(
                TextView confirmView, TextView passwordView, TextView errorView) {
            super(confirmView, errorView);
            this.passwordView = confirmView;
        }

        @Override
        protected String getErrorMessage(String text, boolean interactive) {
            if (interactive)
                return null;

            String passwordText = passwordView.getText().toString();

            if (!passwordText.equals(text))
                return null;

            return "Passwords must match";
        }
    }
}
