package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
            TextView role = row.findViewById(R.id.user_row_role);
            TextView status = row.findViewById(R.id.user_row_status);
            Button toggleRoleButton = row.findViewById(R.id.user_row_role_button);
            Button toggleActiveButton = row.findViewById(R.id.user_row_active_button);

            fullName.setText(user.getFullName());
            username.setText("Username: " + user.getUsername());
            role.setText("Role: " + user.getRole());
            status.setText(user.isActive() ? "Status: Active" : "Status: Disabled");

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

    private void redirectToLogin() {
        Utils.clearSession(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
