package com.example.finance;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagementActivity extends AppCompatActivity {
    private StockDatabaseHelper dbHelper;
    private User currentUser;

    private ConstraintLayout dashboard;
    private LinearLayout stockTaking;
    private LinearLayout progressTab;
    private Button saveButton;
    private Button createButton;
    private ImageView addItemButton;
    private ImageView addSizeButton;
    private TextView currentUserLabel;
    private TextView lowStockSummary;
    private Spinner categorySpinner;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        dbHelper = new StockDatabaseHelper(this);
        dbHelper.seedDefaultSelections();
        ReportScheduler.scheduleDailyReport(this);

        if (!loadActiveUser()) {
            return;
        }

        initializeViews();
        setupFormEnhancements();
        setupCategoryFilter();
        refreshUi();
    }

    private boolean loadActiveUser() {
        if (!Utils.hasActiveSession(this)) {
            redirectToLogin();
            return false;
        }

        currentUser = dbHelper.getUserById(Utils.getSessionUserId(this));
        if (currentUser == null || !currentUser.isActive()) {
            Utils.clearSession(this);
            redirectToLogin();
            return false;
        }
        return true;
    }

    private void initializeViews() {
        dashboard = findViewById(R.id.dashboard);
        stockTaking = findViewById(R.id.stock_taking);
        progressTab = findViewById(R.id.progress_tab);
        saveButton = findViewById(R.id.submit_button);
        createButton = findViewById(R.id.create_button);
        addItemButton = findViewById(R.id.add_item_button);
        addSizeButton = findViewById(R.id.add_size_button);
        currentUserLabel = findViewById(R.id.current_user_label);
        lowStockSummary = findViewById(R.id.low_stock_summary);
        categorySpinner = findViewById(R.id.item_category_spinner);

        saveButton.setOnClickListener(this::handleSave);
    }

    private void setupFormEnhancements() {
        EditText costPrice = findViewById(R.id.cost_price);
        EditText sellingPrice = findViewById(R.id.selling_price);
        EditText category = findViewById(R.id.category);
        costPrice.addTextChangedListener(new CurrencyTextWatcher(costPrice));
        sellingPrice.addTextChangedListener(new CurrencyTextWatcher(sellingPrice));
        Utils.setCaps(category);
    }

    private void setupCategoryFilter() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                fetchAndDisplayItems(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void refreshUi() {
        currentUser = dbHelper.getUserById(Utils.getSessionUserId(this));
        currentUserLabel.setText(
                currentUser.getFullName() + " • " + currentUser.getRole() + " • " + Utils.getTavernName(this));

        boolean canEditInventory = Utils.canEditInventory(this);
        createButton.setVisibility(canEditInventory ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(canEditInventory ? View.VISIBLE : View.GONE);
        addItemButton.setVisibility(canEditInventory ? View.VISIBLE : View.GONE);
        addSizeButton.setVisibility(canEditInventory ? View.VISIBLE : View.GONE);

        if (!canEditInventory && stockTaking.getVisibility() == View.VISIBLE) {
            stockTaking.setVisibility(View.GONE);
            dashboard.setVisibility(View.VISIBLE);
        }

        adaptors();
        populateTextViews();
        updateLowStockSummary();
        progressTab.setVisibility(dbHelper.getTotalSales(Utils.getCurrentDate()) > 0 ? View.VISIBLE : View.GONE);
        fetchAndDisplayItems(selectedCategory);
    }

    public void adaptors() {
        List<String> itemNames = new ArrayList<>();
        for (Items item : dbHelper.getAllItems()) {
            itemNames.add(item.getItemName());
        }

        List<String> itemSizes = new ArrayList<>();
        for (ItemSizes item : dbHelper.getAllItemSizes()) {
            itemSizes.add(item.getItemSize());
        }

        Spinner itemNameSpinner = findViewById(R.id.item_name);
        Spinner sizeSpinner = findViewById(R.id.size);

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itemNames);
        itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemNameSpinner.setAdapter(itemAdapter);

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itemSizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dbHelper.getCategories());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        int categoryPosition = categoryAdapter.getPosition(selectedCategory);
        if (categoryPosition >= 0) {
            categorySpinner.setSelection(categoryPosition);
        }
    }

    public void exitApp(View view) {
        if (!Utils.canViewReports(this)) {
            finishAffinity();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Close store")
                .setMessage("Do you want to close the store and send today's report before exiting?")
                .setPositiveButton("Close & Send", (dialog, which) -> closeStoreAndExit())
                .setNegativeButton("Exit Only", (dialog, which) -> finishAffinity())
                .setNeutralButton("Cancel", null)
                .show();
    }

    public void menu(View view) {
        List<String> actions = new ArrayList<>();
        if (Utils.canManageUsers(this)) {
            actions.add("Manage users");
            actions.add("Edit tavern details");
            actions.add("Low stock settings");
        }
        if (Utils.canViewReports(this)) {
            actions.add("Send full report");
            actions.add("Close store and send report");
        }
        if (Utils.canResetDatabase(this)) {
            actions.add("Reset sales and stock");
        }
        actions.add("Logout");

        String[] items = actions.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Choose action")
                .setItems(items, (dialog, which) -> handleMenuAction(items[which]))
                .show();
    }

    private void handleMenuAction(String action) {
        switch (action) {
            case "Manage users":
                startActivity(new Intent(this, UserManagementActivity.class));
                break;
            case "Edit tavern details":
                showTavernDetailsDialog();
                break;
            case "Low stock settings":
                showLowStockSettingsDialog();
                break;
            case "Send full report":
                sendFullReport();
                break;
            case "Close store and send report":
                closeStoreAndExit();
                break;
            case "Reset sales and stock":
                confirmDatabaseReset();
                break;
            case "Logout":
                Utils.clearSession(this);
                redirectToLogin();
                break;
            default:
                break;
        }
    }

    public void addItem(View view) {
        if (!Utils.canEditInventory(this)) {
            Utils.showToast(this, "You are not allowed to add stock items.");
            return;
        }

        showTextEntryDialog("Add item name", "Enter item", enteredValue -> {
            dbHelper.addItem(enteredValue);
            adaptors();
        });
    }

    public void addItemSize(View view) {
        if (!Utils.canEditInventory(this)) {
            Utils.showToast(this, "You are not allowed to add item sizes.");
            return;
        }

        showTextEntryDialog("Add item size", "Enter item size (e.g. 660ml)", enteredValue -> {
            if (!enteredValue.matches("^\\d+ml$")) {
                Utils.showToast(this, "Invalid size. Use format like 660ml.");
                return;
            }
            dbHelper.addItemSize(enteredValue);
            adaptors();
        });
    }

    private void showTextEntryDialog(String titleText, String hint, ValueConsumer consumer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.dialog_quantity);
        Button actionButton = dialogView.findViewById(R.id.dialog_sell_button);
        input.setGravity(Gravity.CENTER);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(hint);
        actionButton.setText("Save");

        AlertDialog dialog = builder.create();
        dialog.show();
        actionButton.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                Utils.showToast(this, "Enter a value first.");
                return;
            }
            consumer.accept(value);
            dialog.dismiss();
        });
    }

    public void handleSave(View view) {
        if (!Utils.canEditInventory(this)) {
            Utils.showToast(this, "You are not allowed to edit inventory.");
            return;
        }

        Spinner itemNameSpinner = findViewById(R.id.item_name);
        Spinner itemSizeSpinner = findViewById(R.id.size);
        if (itemNameSpinner.getSelectedItem() == null || itemSizeSpinner.getSelectedItem() == null) {
            Utils.showToast(this, "Add the item name and size first.");
            return;
        }

        String itemName = itemNameSpinner.getSelectedItem().toString().trim();
        String itemSize = itemSizeSpinner.getSelectedItem().toString().trim();
        String costPriceStr = ((EditText) findViewById(R.id.cost_price)).getText().toString().trim();
        String sellingPriceStr = ((EditText) findViewById(R.id.selling_price)).getText().toString().trim();
        String quantityStr = ((EditText) findViewById(R.id.quantity)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.description)).getText().toString().trim();
        String category = ((EditText) findViewById(R.id.category)).getText().toString().trim();

        if (itemName.isEmpty() || itemSize.isEmpty() || category.isEmpty()
                || costPriceStr.isEmpty() || sellingPriceStr.isEmpty() || quantityStr.isEmpty()) {
            Utils.showToast(this, "All fields must be filled out.");
            return;
        }

        if ("Select Item".equals(itemName) || "Select Item size".equals(itemSize)) {
            Utils.showToast(this, "Please select a valid item and item size.");
            return;
        }

        try {
            double costPrice = Double.parseDouble(costPriceStr.replace(",", "."));
            double sellingPrice = Double.parseDouble(sellingPriceStr.replace(",", "."));
            int quantity = Integer.parseInt(quantityStr);

            if (!dbHelper.isStockItemExists(itemName, itemSize)) {
                dbHelper.addStockItem(itemName, costPrice, sellingPrice, quantity, description, category, itemSize);
            } else {
                dbHelper.restockItem(itemName, itemSize, costPrice, sellingPrice, quantity, description, category);
            }

            clearStockForm();
            refreshUi();
            Utils.success(this, "Stock saved successfully.");
        } catch (NumberFormatException e) {
            Utils.showToast(this, "Please enter valid numbers for price and quantity.");
        }
    }

    private void clearStockForm() {
        ((EditText) findViewById(R.id.cost_price)).setText("");
        ((EditText) findViewById(R.id.selling_price)).setText("");
        ((EditText) findViewById(R.id.quantity)).setText("");
        ((EditText) findViewById(R.id.description)).setText("");
        ((EditText) findViewById(R.id.category)).setText("");
    }

    public void ChangeLayouts(View view) {
        if (!Utils.canEditInventory(this)) {
            Utils.showToast(this, "Only Owner and Manager can manage stock.");
            return;
        }
        dashboard.setVisibility(View.GONE);
        stockTaking.setVisibility(View.VISIBLE);
    }

    public void ChangeLayout(View view) {
        stockTaking.setVisibility(View.GONE);
        dashboard.setVisibility(View.VISIBLE);
        refreshUi();
    }

    private void fetchAndDisplayItems(String category) {
        TableLayout tableLayout = findViewById(R.id.items_table);
        tableLayout.removeAllViews();

        List<StockItem> allItems = dbHelper.getStockItemsByCategory(category);
        Integer globalThreshold = dbHelper.getGlobalLowStockThreshold();

        if (allItems == null || allItems.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(Utils.canEditInventory(this)
                    ? "No stock added yet. Use Manage Stock to add your first item."
                    : "No stock is currently available.");
            emptyView.setPadding(8, 8, 8, 8);
            tableLayout.addView(emptyView);
            return;
        }

        for (StockItem item : allItems) {
            TableRow row = new TableRow(this);
            row.addView(createItemCell(item.getItemName(), 1f, true, v -> showSellDialog(item)));
            row.addView(createItemCell(item.getItemSize(), 1f, false, null));
            row.addView(createItemCell("R" + item.getSellingPrice(), 1f, false, null));

            int threshold = Utils.resolveLowStockThreshold(item.getReceivedQuantity(), globalThreshold);
            TextView quantityView = createItemCell(String.valueOf(item.getQuantity()), 1f, false, null);
            Utils.setStockAvailabilityColor(quantityView, item.getQuantity(), threshold);
            row.addView(quantityView);
            tableLayout.addView(row);
        }
    }

    private TextView createItemCell(String text, float weight, boolean clickable, View.OnClickListener listener) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
        textView.setGravity(Gravity.START);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        if (clickable && listener != null) {
            textView.setOnClickListener(listener);
        }
        return textView;
    }

    private void showSellDialog(StockItem item) {
        if (!Utils.canSell(this)) {
            Utils.showToast(this, "You are not allowed to sell stock.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getItemName() + " " + item.getItemSize());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sell_item, null);
        builder.setView(dialogView);

        EditText quantityEditText = dialogView.findViewById(R.id.dialog_quantity);
        Button sellButton = dialogView.findViewById(R.id.dialog_sell_button);

        AlertDialog dialog = builder.create();
        dialog.show();

        sellButton.setOnClickListener(v -> {
            String quantityText = quantityEditText.getText().toString().trim();
            if (quantityText.isEmpty()) {
                Utils.showToast(this, "Please provide a valid quantity.");
                return;
            }

            int quantityToSell;
            try {
                quantityToSell = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                Utils.showToast(this, "Please enter a valid number.");
                return;
            }

            if (quantityToSell <= 0) {
                Utils.showToast(this, "Quantity must be greater than zero.");
                return;
            }

            if (quantityToSell > item.getQuantity()) {
                Utils.showToast(this, "Not enough stock to sell.");
                return;
            }

            dbHelper.updateItemQuantity(
                    item.getItemName(),
                    item.getItemSize(),
                    item.getCostPrice(),
                    item.getSellingPrice(),
                    item.getQuantity() - quantityToSell
            );
            dbHelper.saveSale(
                    item.getItemName(),
                    item.getCostPrice(),
                    item.getSellingPrice() * quantityToSell,
                    quantityToSell,
                    item.getDescription(),
                    item.getItemSize(),
                    item.getCategory()
            );

            Utils.success(this, "Item sold.");
            dialog.dismiss();
            refreshUi();
        });
    }

    private void populateTextViews() {
        TextView mostSoldTextView = findViewById(R.id.most_sold);
        TextView totalAmountTextView = findViewById(R.id.total_amount_for_the_day);
        String currentDate = Utils.getCurrentDate();

        String mostSoldItem = dbHelper.getMostAppearingItemWithSizeForDate(currentDate);
        double totalAmount = dbHelper.getSumOfSellingPrice(currentDate);
        int totalSales = dbHelper.getTotalSales(currentDate);

        mostSoldTextView.setText(mostSoldItem != null
                ? "Most Sold: " + mostSoldItem + "\nItems sold: " + totalSales
                : "Most Sold\nNo Data");
        totalAmountTextView.setText("Money Made\nR" + String.format(Locale.getDefault(), "%.2f", totalAmount));
    }

    private void updateLowStockSummary() {
        List<StockItem> lowStockItems = dbHelper.getLowStockStockItems();
        Integer globalThreshold = dbHelper.getGlobalLowStockThreshold();

        if (lowStockItems.isEmpty()) {
            lowStockSummary.setText(globalThreshold != null && globalThreshold > 0
                    ? "Low stock monitoring active. Global threshold: " + globalThreshold
                    : "Low stock monitoring active. Using 10% fallback of received stock.");
            return;
        }

        lowStockSummary.setText(lowStockItems.size() + " low-stock item(s) detected. " +
                (globalThreshold != null && globalThreshold > 0
                        ? "Global threshold: " + globalThreshold
                        : "Using 10% fallback."));
    }

    private void sendFullReport() {
        if (!Utils.canViewReports(this)) {
            Utils.showToast(this, "You are not allowed to send reports.");
            return;
        }

        ReportManager.sendReportAsync(
                this,
                Utils.getCurrentDate(),
                StockDatabaseHelper.REPORT_KIND_MANUAL_FULL,
                currentUser.getFullName() + " (" + currentUser.getRole() + ")",
                false,
                status -> {
                    if (status == ReportManager.SendStatus.SENT) {
                        Utils.success(this, "Report sent to Owner and Manager recipients.");
                    } else {
                        Utils.showToast(this, "Report was not sent. Check email setup and recipient addresses.");
                    }
                }
        );
    }

    private void closeStoreAndExit() {
        if (!Utils.canViewReports(this)) {
            finishAffinity();
            return;
        }

        ReportManager.sendReportAsync(
                this,
                Utils.getCurrentDate(),
                StockDatabaseHelper.REPORT_KIND_STORE_CLOSE,
                currentUser.getFullName() + " (" + currentUser.getRole() + ")",
                true,
                status -> {
                    if (status == ReportManager.SendStatus.FAILED) {
                        Utils.showToast(this, "Store close report failed. The day was not marked as closed.");
                        return;
                    }

                    if (status == ReportManager.SendStatus.SKIPPED) {
                        Utils.showToast(this, "Today's store-close report was already sent.");
                    } else {
                        Utils.success(this, "Store-close report sent.");
                    }
                    finishAffinity();
                }
        );
    }

    private void confirmDatabaseReset() {
        if (!Utils.canResetDatabase(this)) {
            Utils.showToast(this, "Only the Owner can reset data.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset sales and stock")
                .setMessage("Are you sure you want to delete all stock and sales records?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.clearStockTable();
                    refreshUi();
                    Utils.success(this, "Stock and sales data cleared.");
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showLowStockSettingsDialog() {
        if (!Utils.canManageUsers(this)) {
            Utils.showToast(this, "Only the Owner can change low stock settings.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Global low stock threshold");
        builder.setMessage("Leave the field empty to use the 10% fallback of received stock.");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        Integer currentThreshold = dbHelper.getGlobalLowStockThreshold();
        if (currentThreshold != null && currentThreshold > 0) {
            input.setText(String.valueOf(currentThreshold));
        }
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                dbHelper.setGlobalLowStockThreshold(null);
                refreshUi();
                Utils.success(this, "10% fallback mode enabled.");
                return;
            }

            try {
                int threshold = Integer.parseInt(value);
                if (threshold <= 0) {
                    Utils.showToast(this, "Threshold must be greater than zero.");
                    return;
                }
                dbHelper.setGlobalLowStockThreshold(threshold);
                refreshUi();
                Utils.success(this, "Global low stock threshold updated.");
            } catch (NumberFormatException e) {
                Utils.showToast(this, "Enter a valid number.");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showTavernDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Tavern Information");
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
            refreshUi();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private interface ValueConsumer {
        void accept(String value);
    }
}
