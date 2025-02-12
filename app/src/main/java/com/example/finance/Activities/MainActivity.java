package com.example.finance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.Spanned;
import android.graphics.Typeface;

import androidx.appcompat.app.AlertDialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private StockDatabaseHelper dbHelper;
    public static List<String> drinks = new ArrayList<>();
    public static List<String> sizes = new ArrayList<>();
    private TextView timeTextView;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeTextView = findViewById(R.id.test);
        if (!getTavernDetails()) {
            showTavernDetailsDialog();
        } else {
            Continue();
        }
    }
    public void Continue() {
        // Initialize the database helper
        dbHelper = new StockDatabaseHelper(this);

        // Get the TextView reference for time display
        timeTextView = findViewById(R.id.test);
        timeTextView.setOnClickListener(v -> sendFullReport(false));


        // Save the instance of MainActivity for later use
        instance = this;

        // Start the time update function
        startUpdatingTime();

        // List of beers and ciders
        drinks.add("Heineken");
        drinks.add("Castle Lager");

        // List of all possible drink sizes
        sizes.add("750ml");
        sizes.add("660ml");

        // Add the drinks to the database
        addItemsToDatabase();

        // Create an intent to navigate to ManagementActivity
        Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
        startActivity(intent);
    }
    public static void sendReportFromOtherActivity(Context context, boolean closeApp) {
        if (instance != null) {
            instance.sendDailyReport(closeApp); // Call sendReport from MainActivity instance
        } else {
            // Handle case where MainActivity instance is not available
            Toast.makeText(context, "MainActivity not initialized yet", Toast.LENGTH_SHORT).show();
        }
    }
    private void showTavernDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Tavern Information");

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tavern_details, null);
        builder.setView(dialogView);

        // Initialize views correctly using dialogView
        EditText tavernNameEditText = dialogView.findViewById(R.id.dialog_tavern_name);
        Utils.setCaps(tavernNameEditText);
        EditText barmanNameEditText = dialogView.findViewById(R.id.dialog_barman_name);
        Utils.setCaps(barmanNameEditText);
        Button saveButton = dialogView.findViewById(R.id.dialog_save_button);

        // Load previously saved tavern details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("TavernPrefs", MODE_PRIVATE);
        String savedTavernName = sharedPreferences.getString("tavernName", "");
        String savedBarmanName = sharedPreferences.getString("barmanName", "");

        // Set previously saved values (if any)
        tavernNameEditText.setText(savedTavernName);
        barmanNameEditText.setText(savedBarmanName);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // **Disable dialog from being dismissed by tapping outside or pressing back**
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Show the dialog
        dialog.show();

        // Set the Save button's click listener
        saveButton.setOnClickListener(v -> {
            // Get tavern details from input
            String tavernName = tavernNameEditText.getText().toString().trim();
            String barmanName = barmanNameEditText.getText().toString().trim();

            if (!tavernName.isEmpty() && !barmanName.isEmpty()) {
                // Save details to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("tavernName", tavernName);
                editor.putString("barmanName", barmanName);
                editor.apply();

                Toast.makeText(this, "Tavern details saved!", Toast.LENGTH_SHORT).show();
                Continue();
                // Close the dialog only if values are entered
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Both Tavern Name and Barman's Name are required!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean getTavernDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("TavernPrefs", MODE_PRIVATE);
        String savedTavernName = sharedPreferences.getString("tavernName", "");
        String savedBarmanName = sharedPreferences.getString("barmanName", "");

        if (savedTavernName.isEmpty() || savedBarmanName.isEmpty()) {
            return false;
        }
        return true;
    }
    public void sendDailyReport(boolean closeApp) {

        // Fetch sales data
        double totalAmount = dbHelper.getSumOfSellingPrice(currentDate);
        String mostSoldItem = dbHelper.getMostAppearingItemWithSizeForDate(currentDate);
        String salesReport = dbHelper.getSalesReportForDate(currentDate);

        // Get low stock items
        String lowStockItems = dbHelper.getLowStockItems();

        SharedPreferences sharedPreferences = getSharedPreferences("TavernPrefs", MODE_PRIVATE);
        String TavernName = sharedPreferences.getString("tavernName", "");
        String BarmanName = sharedPreferences.getString("barmanName", "");


        // Construct the email subject and body
        String subject = TavernName + " Report for " + currentDate;

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear Admin,\n\n")
                .append("📊 Daily Sales Report - ").append(currentDate).append(" 📊\n\n")
                .append("📌 Total Sales Amount: R").append(totalAmount).append("\n")
                .append("🔥 Best Selling Item: ").append(mostSoldItem != null ? mostSoldItem : "No sales today").append("\n\n")
                .append(lowStockItems).append("\n")
                .append("Report generated at ").append(Utils.getCurrentDateTime()).append(".\n\n");
        if (closeApp) {
            bodyBuilder.append("🚪 Shop Closing Report 🚪\n")
                    .append("This is the final update as the shop is closing for the day.\n");
        } else {
            bodyBuilder.append("✅ Daily Update ✅\n")
                    .append("This is a regular daily update.\n\n");
        }

        bodyBuilder.append("\n\nBest regards,\nJays Sales System\n\n");


        String body = bodyBuilder.toString();

        // Send the email
        Communication.sendEmail(this, subject, body, success -> {
            if (success) {
                if (closeApp) {
                    exitApp(null); // Close the app if it's the shop closing report
                }
            } else {
                Utils.showToast(this, "Failed to send report email.");
            }
        });
    }
    public void sendFullReport(boolean closeApp) {
        double totalAmount = dbHelper.getSumOfSellingPrice(currentDate);
        String mostSoldItem = dbHelper.getMostAppearingItemWithSizeForDate(currentDate);
        String salesReport = dbHelper.getSalesReportForDate(currentDate);
        String lowStockItems = dbHelper.getLowStockItems();
        SharedPreferences sharedPreferences = getSharedPreferences("TavernPrefs", MODE_PRIVATE);
        String TavernName = sharedPreferences.getString("tavernName", "");
        String subject = TavernName + " Full Report for " + currentDate;
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear Admin,\n\n")
                .append("📊 Full Sales Report - ").append(currentDate).append(" 📊\n\n")
                .append("📌 Total Sales Amount: R").append(totalAmount).append("\n")
                .append("🔥 Best Selling Item: ").append(mostSoldItem != null ? mostSoldItem : "No sales today").append("\n\n")
                .append(lowStockItems).append("\n")
                .append("Report generated at ").append(Utils.getCurrentDateTime()).append(".\n\n");
        if (closeApp) {
            bodyBuilder.append("🚪 Shop Closing Report 🚪\n")
                    .append("This is the final update as the shop is closing for the day.\n");
        } else {
            bodyBuilder.append("✅ Daily Update ✅\n")
                    .append("This is a regular daily update.\n\n");
        }
        bodyBuilder.append(salesReport);
        bodyBuilder.append("\n\nBest regards,\nJays Sales System\n\n");
        String body = bodyBuilder.toString();
        Communication.sendEmail(this, subject, body, success -> {
            if (success) {
               menu(null);
            } else {
                Utils.showToast(this, "Failed to send report email.");
            }
        });
    }
    private void startUpdatingTime() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timeUpdater);
    }
    private void updateTime() {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String timeFormatted = currentTime.substring(0, 5);
        String text = "Date : " + currentDate + "\nTime : " + timeFormatted;
        SpannableString spannableText = new SpannableString(text);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Time :"), text.indexOf("Time :") + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        timeTextView.setText(spannableText);
        if (currentTime.equals("00:00:00")) {
            sendDailyReport(false);
        }
    }
    @Override
    public void onBackPressed() {
        Utils.showToast(this, "Unganya mfana!!!");
    }
    private void stopUpdatingTime() {
        handler.removeCallbacks(timeUpdater);
    }
    private void addItemsToDatabase() {
        for (String drink : drinks) {
            dbHelper.addItem(drink);
        }
        for (String size : sizes) {
            dbHelper.addItemSize(size);
        }
    }
    public void exitApp(View view) {
        stopUpdatingTime(); // Stop time updates before exiting
        finishAffinity();
        System.exit(0);
    }
    public void menu(View view) {
        Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
        startActivity(intent);
    }
   @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdatingTime();
    }
}
