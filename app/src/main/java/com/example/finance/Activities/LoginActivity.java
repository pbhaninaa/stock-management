package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private StockDatabaseHelper dbHelper;
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new StockDatabaseHelper(this);

        if (!dbHelper.hasUsers()) {
            startActivity(new Intent(this, RegisterActivity.class)
                    .putExtra(RegisterActivity.EXTRA_FIRST_OWNER, true));
            finish();
            return;
        }

        usernameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView registerHint = findViewById(R.id.login_register_hint);

        loginButton.setOnClickListener(v -> login());
        registerHint.setOnClickListener(v ->
                Utils.showToast(this, "Only the Owner can create more users from inside the app."));
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Utils.showToast(this, "Enter both username and password.");
            return;
        }

        User user = dbHelper.authenticateUser(username, password);
        if (user == null) {
            Utils.showToast(this, "Invalid username or password.");
            return;
        }

        Utils.saveSession(this, user);
        Utils.success(this, "Welcome " + user.getFullName());

        Intent intent = new Intent(this, ManagementActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
