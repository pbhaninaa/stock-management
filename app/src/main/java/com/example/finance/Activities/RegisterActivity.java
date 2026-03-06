package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    public static final String EXTRA_FIRST_OWNER = "extra_first_owner";
    public static final String EXTRA_OWNER_MANAGED = "extra_owner_managed";

    private StockDatabaseHelper dbHelper;
    private EditText fullNameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner roleSpinner;
    private boolean isFirstOwnerRegistration;
    private boolean isOwnerManagedRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new StockDatabaseHelper(this);
        fullNameEditText = findViewById(R.id.register_full_name);
        usernameEditText = findViewById(R.id.register_username);
        passwordEditText = findViewById(R.id.register_password);
        confirmPasswordEditText = findViewById(R.id.register_confirm_password);
        roleSpinner = findViewById(R.id.register_role_spinner);
        TextView titleTextView = findViewById(R.id.register_title);
        TextView subtitleTextView = findViewById(R.id.register_subtitle);
        Button registerButton = findViewById(R.id.register_button);

        isFirstOwnerRegistration = getIntent().getBooleanExtra(EXTRA_FIRST_OWNER, false);
        isOwnerManagedRegistration = getIntent().getBooleanExtra(EXTRA_OWNER_MANAGED, false);

        setupRoleOptions();
        Utils.setCaps(fullNameEditText);

        if (isFirstOwnerRegistration) {
            if (dbHelper.hasUsers()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            titleTextView.setText("Create the Owner account");
            subtitleTextView.setText("This first account becomes the Owner for the device.");
            roleSpinner.setVisibility(View.GONE);
        } else if (isOwnerManagedRegistration) {
            if (!Utils.canManageUsers(this)) {
                Utils.showToast(this, "Only the Owner can create staff accounts.");
                finish();
                return;
            }
            titleTextView.setText("Create a staff account");
            subtitleTextView.setText("Owner-created accounts can be Manager or Cashier.");
            roleSpinner.setVisibility(View.VISIBLE);
        } else {
            if (dbHelper.hasUsers()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            titleTextView.setText("Create the Owner account");
            subtitleTextView.setText("This first account becomes the Owner for the device.");
            roleSpinner.setVisibility(View.GONE);
        }

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void setupRoleOptions() {
        List<String> roles = new ArrayList<>();
        roles.add(Utils.ROLE_MANAGER);
        roles.add(Utils.ROLE_CASHIER);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Utils.showToast(this, "All fields are required.");
            return;
        }

        if (password.length() < 4) {
            Utils.showToast(this, "Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Utils.showToast(this, "Passwords do not match.");
            return;
        }

        String role = isFirstOwnerRegistration ? Utils.ROLE_OWNER :
                roleSpinner.getSelectedItem().toString();

        User user = dbHelper.createUser(fullName, username, password, role);
        if (user == null) {
            Utils.showToast(this, "That username is already in use.");
            return;
        }

        if (isFirstOwnerRegistration) {
            Utils.saveSession(this, user);
            Utils.success(this, "Owner account created.");
            Intent intent = new Intent(this, ManagementActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Utils.success(this, role + " account created.");
        if (isOwnerManagedRegistration) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
