package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class UserManagementActivity extends AppCompatActivity {
    private StockDatabaseHelper dbHelper;
    private LinearLayout usersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        dbHelper = new StockDatabaseHelper(this);
        usersContainer = findViewById(R.id.users_container);
        Button addUserButton = findViewById(R.id.add_user_button);
        TextView ownerLabel = findViewById(R.id.user_management_owner_label);

        if (!Utils.canManageUsers(this)) {
            Utils.showToast(this, "Only the Owner can manage users.");
            redirectToLogin();
            return;
        }

        ownerLabel.setText("Signed in as " + Utils.getSessionUserName(this));
        addUserButton.setOnClickListener(v -> startActivity(
                new Intent(this, RegisterActivity.class)
                        .putExtra(RegisterActivity.EXTRA_OWNER_MANAGED, true)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.canManageUsers(this)) {
            redirectToLogin();
            return;
        }
        renderUsers();
    }

    private void renderUsers() {
        usersContainer.removeAllViews();
        List<User> users = dbHelper.getAllUsers();

        for (User user : users) {
            LinearLayout row = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.layout_user_row, usersContainer, false);

            TextView fullName = row.findViewById(R.id.user_row_name);
            TextView username = row.findViewById(R.id.user_row_username);
            TextView email = row.findViewById(R.id.user_row_email);
            TextView role = row.findViewById(R.id.user_row_role);
            TextView status = row.findViewById(R.id.user_row_status);
            Button toggleRoleButton = row.findViewById(R.id.user_row_role_button);
            Button toggleActiveButton = row.findViewById(R.id.user_row_active_button);

            fullName.setText(user.getFullName());
            username.setText("Username: " + user.getUsername());
            email.setText(user.getEmail().isEmpty()
                    ? "Email: Not set"
                    : "Email: " + user.getEmail());
            role.setText("Role: " + user.getRole());
            status.setText(user.isActive() ? "Status: Active" : "Status: Disabled");
            email.setOnClickListener(v -> showEmailDialog(user));

            boolean isOwner = Utils.ROLE_OWNER.equals(user.getRole());
            if (isOwner) {
                toggleRoleButton.setEnabled(false);
                toggleRoleButton.setText("Owner");
                toggleActiveButton.setEnabled(false);
                toggleActiveButton.setText("Always Active");
            } else {
                String nextRole = Utils.ROLE_MANAGER.equals(user.getRole())
                        ? Utils.ROLE_CASHIER : Utils.ROLE_MANAGER;
                toggleRoleButton.setText("Make " + nextRole);
                toggleRoleButton.setOnClickListener(v -> {
                    dbHelper.updateUserRole(user.getId(), nextRole);
                    renderUsers();
                });

                toggleActiveButton.setText(user.isActive() ? "Disable" : "Enable");
                toggleActiveButton.setOnClickListener(v -> {
                    dbHelper.updateUserActiveStatus(user.getId(), !user.isActive());
                    renderUsers();
                });
            }

            usersContainer.addView(row);
        }
    }

    public void goBack(android.view.View view) {
        finish();
    }

    private void showEmailDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update email for " + user.getFullName());

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setHint("Enter email address");
        input.setText(user.getEmail());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String email = input.getText().toString().trim().toLowerCase();
            boolean requiresEmail = Utils.ROLE_OWNER.equals(user.getRole()) || Utils.ROLE_MANAGER.equals(user.getRole());

            if (requiresEmail && email.isEmpty()) {
                Utils.showToast(this, "Owner and Manager accounts need an email.");
                return;
            }

            if (!email.isEmpty() && !Utils.isValidEmail(email)) {
                Utils.showToast(this, "Enter a valid email address.");
                return;
            }

            dbHelper.updateUserEmail(user.getId(), email);
            renderUsers();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void redirectToLogin() {
        Utils.clearSession(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
