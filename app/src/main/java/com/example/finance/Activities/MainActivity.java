package com.example.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private StockDatabaseHelper dbHelper;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new StockDatabaseHelper(this);
        dbHelper.seedDefaultSelections();

        statusTextView = findViewById(R.id.test);
        statusTextView.setText("Preparing secure login...");

        if (!Utils.hasTavernDetails(this)) {
            showTavernDetailsDialog(this::routeUser);
        } else {
            routeUser();
        }
    }

    private void routeUser() {
        if (!dbHelper.hasUsers()) {
            startActivity(new Intent(this, RegisterActivity.class)
                    .putExtra(RegisterActivity.EXTRA_FIRST_OWNER, true));
            finish();
            return;
        }

        if (Utils.hasActiveSession(this)) {
            User currentUser = dbHelper.getUserById(Utils.getSessionUserId(this));
            if (currentUser != null && currentUser.isActive()) {
                startActivity(new Intent(this, ManagementActivity.class));
                finish();
                return;
            }
            Utils.clearSession(this);
        }

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showTavernDetailsDialog(Runnable onSaved) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Tavern Information");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tavern_details, null);
        builder.setView(dialogView);

        EditText tavernNameEditText = dialogView.findViewById(R.id.dialog_tavern_name);
        EditText barmanNameEditText = dialogView.findViewById(R.id.dialog_barman_name);
        Button saveButton = dialogView.findViewById(R.id.dialog_save_button);

        Utils.setCaps(tavernNameEditText);
        Utils.setCaps(barmanNameEditText);
        tavernNameEditText.setText(Utils.getTavernName(this));
        barmanNameEditText.setText(Utils.getBarmanName(this));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String tavernName = tavernNameEditText.getText().toString().trim();
            String barmanName = barmanNameEditText.getText().toString().trim();

            if (tavernName.isEmpty() || barmanName.isEmpty()) {
                Utils.showToast(this, "Both Tavern Name and Barman's Name are required.");
                return;
            }

            Utils.saveTavernDetails(this, tavernName, barmanName);
            dialog.dismiss();
            onSaved.run();
        });
    }

    public void exitApp(View view) {
        finishAffinity();
    }

    public void menu(View view) {
        routeUser();
    }
}
