package com.example.finance;

import static com.example.finance.MainActivity.drinks;
import static com.example.finance.MainActivity.sizes;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
//
import java.util.Date;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.text.InputType;
import android.graphics.Typeface; // Add this import

import androidx.core.content.ContextCompat;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.widget.LinearLayout;import android.content.Intent;

public class ManagementActivity extends AppCompatActivity {
    private StockDatabaseHelper dbHelper;

    private ConstraintLayout StockTaking, Dashboard;
    private LinearLayout ProgressTab;
    private Button Save;
    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new StockDatabaseHelper(this);
        setContentView(R.layout.activity_management);
        initialiseViews();
        handleOnclickListeners();
        adaptors();



        EditText CostPrice = findViewById(R.id.cost_price);
        EditText SellingPrice = findViewById(R.id.selling_price);
        EditText Category = findViewById(R.id.category);

        CostPrice.addTextChangedListener(new CurrencyTextWatcher(CostPrice));
        SellingPrice.addTextChangedListener(new CurrencyTextWatcher(SellingPrice));
        ProgressTab.setVisibility(dbHelper.getTotalSales(currentDate) > 0 ? View.VISIBLE : View.GONE);
        Utils.setCaps(Category);
        populateTextViews();

    }
//    @Override
//    public void onBackPressed() {
//        // Optionally, display a confirmation dialog before going back
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
//    }


    public void adaptors() {
        // Get list of sizes from the database
        List<ItemSizes> sizesList = dbHelper.getAllItemSizes();
        List<String> itemSizes = new ArrayList<>();

        for (ItemSizes item : sizesList) {
            itemSizes.add(item.getItemSize()); // Ensure this retrieves sizes only
        }

        // Set up adapter for the sizes spinner
        Spinner spinner1 = findViewById(R.id.size);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(ManagementActivity.this, android.R.layout.simple_spinner_item, itemSizes);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        // Get list of item names from the database
        List<Items> itemsList = dbHelper.getAllItems();
        List<String> itemNames = new ArrayList<>();

        for (Items item : itemsList) {
            itemNames.add(item.getItemName());
        }

        // Set up adapter for the item names spinner
        Spinner spinner = findViewById(R.id.item_name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ManagementActivity.this, android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Spinner categorySpinner = findViewById(R.id.item_category_spinner);
        List<String> categories = dbHelper.getCategories();
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter2);

// Set a listener for item selection
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected category
                String selectedCategory = parentView.getItemAtPosition(position).toString();

                // Call fetchAndDisplayItems with the selected category
                fetchAndDisplayItems(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally, handle the case where no item is selected
            }
        });

// Default call to fetch all items
        fetchAndDisplayItems("All");


    }

    private void handleOnclickListeners() {
        Save.setOnClickListener(v -> handleSave());
    }

    public void exitApp(View view) {
        MainActivity.sendReportFromOtherActivity(this, true);


    }

    public void menu(View view) {

        setupMenuButton(view);
    }

    public void addItem(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("Add item name");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        // Initialize views correctly using dialogView
        EditText item = dialogView.findViewById(R.id.dialog_quantity);
        Utils.setCaps(item);
        item.setGravity(Gravity.CENTER);
        item.setInputType(InputType.TYPE_CLASS_TEXT);
        item.setHint("Enter item");

        Button addButton = dialogView.findViewById(R.id.dialog_sell_button);
        addButton.setText("Add");

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the Sell button's click listener
        addButton.setOnClickListener(v -> {
            // Get the item text from input
            String itemText = item.getText().toString();
            if (!itemText.isEmpty()) {
                // Add the item to your database or list
                dbHelper.addItem(itemText);
                adaptors(); // Refresh the adapter if needed
                Spinner spinner = findViewById(R.id.item_name);
                spinner.performClick(); // This will open the dropdown



                // Close the dialog
                dialog.dismiss();  // Close the dialog when "Add" is clicked
            } else {
                Utils.showToast(this, "Enter Item");
            }
        });
    }

    public void addItemSize(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        TextView title = new TextView(this);
        title.setText("Add item size");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        // Initialize views correctly using dialogView
        EditText item = dialogView.findViewById(R.id.dialog_quantity);
        item.setGravity(Gravity.CENTER);
        item.setInputType(InputType.TYPE_CLASS_TEXT);
        item.setHint("Enter item size (e.g., 660ml)");

        Button addButton = dialogView.findViewById(R.id.dialog_sell_button);
        addButton.setText("Add");

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the Add button's click listener
        addButton.setOnClickListener(v -> {
            String itemText = item.getText().toString().trim();

            // Regular expression to allow only sizes like "660ml", "750ml", etc.
            if (itemText.matches("^\\d+ml$")) {
                dbHelper.addItemSize(itemText); // Add valid size to the database
                adaptors(); // Refresh the adapter if needed
                Spinner spinner = findViewById(R.id.size);
                spinner.performClick(); // This will open the dropdown


                dialog.dismiss(); // Close the dialog
            } else {
                Utils.showToast(this, "Invalid size! Use format like '660ml'.");
            }
        });
    }

    public void setupMenuButton(View menuButton) {
        // Set up long press listener on the Menu Button
        menuButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(ManagementActivity.this)
                        .setTitle("Delete Database")
                        .setMessage("Are you sure you want to delete the entire stock database?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Call the method to clear the stock table
                                showConfirmationDialog(null, true);
                            }
                        })
                        .setNegativeButton("No", null)
                        .setCancelable(true)
                        .show();
                return true; // Return true to indicate the event was handled
            }
        });
    }

    private void handleSave() {

        // Get the values from the EditText fields
        String itemName = ((Spinner) findViewById(R.id.item_name)).getSelectedItem().toString().trim();
        String costPriceStr = ((EditText) findViewById(R.id.cost_price)).getText().toString().trim();
        String sellingPriceStr = ((EditText) findViewById(R.id.selling_price)).getText().toString().trim();
        String quantityStr = ((EditText) findViewById(R.id.quantity)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.description)).getText().toString().trim();
        String category = ((EditText) findViewById(R.id.category)).getText().toString().trim();
        String itemSize = ((Spinner) findViewById(R.id.size)).getSelectedItem().toString().trim();

        // Validate inputs
        if (itemName.isEmpty() || itemSize.isEmpty() || category.isEmpty() || costPriceStr.isEmpty() || sellingPriceStr.isEmpty() || quantityStr.isEmpty()) {
            // Show a validation error message (e.g., using Toast)
            Toast.makeText(this, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if validation fails
        }

        // Convert strings to appropriate data types
        double costPrice = 0.0, sellingPrice = 0.0;
        int quantity = 0;

        try {
            // Replace commas with periods for locale-based decimal separators
            costPriceStr = costPriceStr.replace(",", ".");
            sellingPriceStr = sellingPriceStr.replace(",", ".");

            // Parse the values
            costPrice = Double.parseDouble(costPriceStr);
            sellingPrice = Double.parseDouble(sellingPriceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            // Show a number format error message
            Toast.makeText(this, "Please enter valid numbers for price and quantity.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if the conversion fails
        }

        if (!dbHelper.isStockItemExists(itemName, itemSize)) {
            System.out.println("Adding Stock Item:");
            System.out.println("Item Name: " + itemName);
            System.out.println("Cost Price: " + costPrice);
            System.out.println("Selling Price: " + sellingPrice);
            System.out.println("Quantity: " + quantity);
            System.out.println("Description: " + description);
            System.out.println("Category: " + category);
            System.out.println("Item Size: " + itemSize);
            dbHelper.addStockItem(itemName, costPrice, sellingPrice, quantity, description, category, itemSize);
            Toast.makeText(this, "Item saved successfully!", Toast.LENGTH_SHORT).show();

            // Clear the fields if needed (optional)
            ((EditText) findViewById(R.id.cost_price)).setText("");
            ((EditText) findViewById(R.id.selling_price)).setText("");
            ((EditText) findViewById(R.id.quantity)).setText("");
            ((EditText) findViewById(R.id.description)).setText("");
            ((EditText) findViewById(R.id.category)).setText("");
        } else {
            int cQuantity = dbHelper.getCurrentQuantity(itemName, itemSize);
            dbHelper.updateItemQuantity(itemName, itemSize, costPrice, sellingPrice, Integer.parseInt(quantityStr) + cQuantity);
        }

    }

    private void initialiseViews() {
        StockTaking = findViewById(R.id.stock_taking);
        Dashboard = findViewById(R.id.dashboard);
        Save = findViewById(R.id.submit_button);
        ProgressTab = findViewById(R.id.progress_tab);
        fetchAndDisplayItems("All");
    }

    public void ChangeLayouts(View view) {
        showConfirmationDialog(view, false);
    }

    public void ChangeLayout(View view) {
        adaptors();
        fetchAndDisplayItems("All");
        StockTaking.setVisibility(StockTaking.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        Dashboard.setVisibility(Dashboard.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

    }

    private void fetchAndDisplayItems(String category) {
        // Fetch all the items from the database
        List<StockItem> allItems = dbHelper.getStockItemsByCategory(category);

        // Get reference to the TableLayout where the items will be displayed
        TableLayout tableLayout = findViewById(R.id.items_table);

        // Clear any previous rows
        tableLayout.removeAllViews();

        // If the list of items is not empty
        if (allItems != null && !allItems.isEmpty()) {
            // Make the TableLayout visible
            tableLayout.setVisibility(View.VISIBLE);

            // Iterate through the list of items and create a row for each item
            for (StockItem item : allItems) {
                // Create a new TableRow for each item
                TableRow tableRow = new TableRow(this);

                // Create TextViews for each column (Item Name, Cost Price, Selling Price, Quantity)
                TextView itemName = new TextView(this);
                itemName.setText(item.getItemName());
                itemName.setPadding(8, 8, 8, 8);
                itemName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Set weight
                itemName.setGravity(Gravity.LEFT);
                itemName.setOnClickListener(v -> showSellDialog(item));  // Pass the item here
                itemName.setTextColor(ContextCompat.getColor(this, R.color.text_color));


                TextView costPrice = new TextView(this);
                costPrice.setText(String.valueOf(item.getItemSize()));
                costPrice.setPadding(8, 8, 8, 8);
                costPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Set weight
                costPrice.setGravity(Gravity.LEFT);
                costPrice.setTextColor(ContextCompat.getColor(this, R.color.text_color));


                TextView sellingPrice = new TextView(this);
                sellingPrice.setText("R" + String.valueOf(item.getSellingPrice()));
                sellingPrice.setPadding(8, 8, 8, 8);
                sellingPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Set weight
                sellingPrice.setGravity(Gravity.LEFT);
                sellingPrice.setTextColor(ContextCompat.getColor(this, R.color.text_color));

                TextView quantity = new TextView(this);
                quantity.setText(String.valueOf(item.getQuantity()));
                quantity.setPadding(8, 8, 8, 8);
                quantity.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1)); // Set weight
                Utils.setStockAvailabilityColor(quantity, item.getQuantity());
                quantity.setGravity(Gravity.LEFT);
                quantity.setTextColor(ContextCompat.getColor(this, R.color.text_color));

                // Add the TextViews to the TableRow
                tableRow.addView(itemName);
                tableRow.addView(costPrice);
                tableRow.addView(sellingPrice);
                tableRow.addView(quantity);

                // Add the TableRow to the TableLayout
                tableLayout.addView(tableRow);
            }
        } else {
            Dashboard.setVisibility(View.GONE);
            StockTaking.setVisibility(View.VISIBLE);
        }
    }

    private void showSellDialog(StockItem item) {
        // Create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getItemName() + " " + item.getItemSize());

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        // Initialize views correctly using dialogView
        EditText quantityEditText = dialogView.findViewById(R.id.dialog_quantity);
        Button sellButton = dialogView.findViewById(R.id.dialog_sell_button);


        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the Sell button's click listener
        sellButton.setOnClickListener(v -> {
            // Get the quantity from input
            String quantityText = quantityEditText.getText().toString();

            if (!quantityText.isEmpty()) {
                int quantityToSell = Integer.parseInt(quantityText);
                if (quantityToSell <= item.getQuantity()) {
                    String itemName = item.getItemName();
                    String itemSize = item.getItemSize();
                    int currentQuantity = item.getQuantity();
                    double costPrice = item.getCostPrice();
                    double sellingPrice = item.getSellingPrice();
                    String description = item.getDescription();
                    String category = item.getCategory();

                    dbHelper.updateItemQuantity(itemName, itemSize, costPrice, sellingPrice, currentQuantity - quantityToSell);

                    dbHelper.saveSale(itemName, costPrice, sellingPrice * quantityToSell, quantityToSell, description, itemSize, category);

                    Toast.makeText(this, "Item Sold!", Toast.LENGTH_SHORT).show();
                    ProgressTab.setVisibility(dbHelper.getTotalSales(currentDate) > 0 ? View.VISIBLE : View.GONE);


                    populateTextViews();

                    // Close the dialog
                    dialog.dismiss();

                    // Refresh the table to reflect the updated item
                    fetchAndDisplayItems("All");
                } else {
                    Toast.makeText(this, "Not enough stock to sell.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please provide a valid quantity.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTextViews() {
        // Initialize TextViews
        TextView mostSoldTextView = findViewById(R.id.most_sold);
        TextView totalAmountTextView = findViewById(R.id.total_amount_for_the_day);


        // Fetch values from database functions
        String mostSoldItem = dbHelper.getMostAppearingItemWithSizeForDate(currentDate);
        double totalAmount = dbHelper.getSumOfSellingPrice(currentDate);  // Assuming it returns a double
        int totalSale = dbHelper.getTotalSales(currentDate);  // Assuming it returns an int

        // Convert double to String with 2 decimal places
        String totalAmountString = String.format(Locale.getDefault(), "%.2f", totalAmount);

        // Populate TextViews with the retrieved values
        mostSoldTextView.setText(mostSoldItem != null
                ? "Most Sold :" + mostSoldItem + "\nItems sold :" + totalSale
                : "Most Sold\nNo Data");

        totalAmountTextView.setText("Money Made\nR" + totalAmountString); // Display total amount formatted
    }

    private void showConfirmationDialog(View view, boolean deleteDb) {
        // Create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and center it
        TextView title = new TextView(this);
        title.setText("Login");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD); // Ensure Typeface is imported
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);

        // Inflate the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        // Initialize views using dialogView
        EditText passwordEditText = dialogView.findViewById(R.id.dialog_quantity); // Using it as password input
        Button confirmButton = dialogView.findViewById(R.id.dialog_sell_button); // Change button name

        // Update hint text for the password field
        passwordEditText.setHint("Enter password");
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Hide input
        passwordEditText.setGravity(Gravity.CENTER);

        // Update button text
        confirmButton.setText("Confirm");

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set the Confirm button's click listener
        confirmButton.setOnClickListener(v -> {
            if (deleteDb) {
                StockDatabaseHelper dbHelper = new StockDatabaseHelper(ManagementActivity.this);
                dbHelper.clearStockTable();
                ChangeLayouts(null);
            }
            // Get entered password
            String enteredPassword = passwordEditText.getText().toString();
            String correctPassword = getReversedDate(); // Get the reversed date as the correct password

            // Dismiss the dialog
            dialog.dismiss();

            if (enteredPassword.equals(correctPassword)) {
                // Toggle visibility of StockTaking and Dashboard
                StockTaking.setVisibility(StockTaking.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                Dashboard.setVisibility(Dashboard.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

                // Fetch and display items if Dashboard is visible
                if (Dashboard.getVisibility() == View.VISIBLE) {
                    fetchAndDisplayItems("All");
                }
            } else {
                // If password doesn't match, show error message
                Toast.makeText(this, "Incorrect password! Action canceled.", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private String getReversedDate() {
        return new StringBuilder(currentDate).reverse().toString();
    }

}
