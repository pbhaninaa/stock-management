package com.example.finance;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressLint("Range")
public class StockDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "stock.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_STOCK = "stock_table";
    private static final String TABLE_SALES = "sales_table";
    private static final String ITEMS_TABLE_STOCK = "items_in_store";
    private static final String ITEMS_SIZE_TABLE_STOCK = "items_sizes_in_store";

    // Column Names
    private static final String COL_ID = "id";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_COST_PRICE = "cost_price";
    private static final String COL_SELLING_PRICE = "selling_price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_CATEGORY = "category";
    private static final String COL_SIZE = "size";
    private static final String DATE = "date";

    // SQL statement to create the table
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT, " +
            COL_COST_PRICE + " REAL, " +
            COL_SELLING_PRICE + " REAL, " +
            COL_QUANTITY + " INTEGER, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_SIZE + " TEXT, " +
            COL_CATEGORY + " TEXT);";

    private static final String CREATE_SALES_TABLE = "CREATE TABLE " + TABLE_SALES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT, " +
            COL_COST_PRICE + " REAL, " +
            COL_SELLING_PRICE + " REAL, " +
            COL_QUANTITY + " INTEGER, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_SIZE + " TEXT, " +
            DATE + " TEXT, " +
            COL_CATEGORY + " TEXT);";

    private static final String CREATE_ITEMS_TABLE = "CREATE TABLE " + ITEMS_TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT);";

    private static final String CREATE_SIZE_ITEMS_TABLE = "CREATE TABLE " + ITEMS_SIZE_TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SIZE + " TEXT);";

    public StockDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
        db.execSQL(CREATE_SIZE_ITEMS_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK);
        onCreate(db);
    }

    public void clearStockTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCK, null, null); // This deletes all rows from the table
        db.delete(TABLE_SALES, null, null); // This deletes all rows from the table
        db.close();
    }

    public long addStockItem(String itemName, double costPrice, double sellingPrice, int quantity, String description, String category, String itemSize) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        values.put(COL_SIZE, itemSize);

        return db.insert(TABLE_STOCK, null, values);
    }

    public long addItem(String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the item already exists
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + ITEMS_TABLE_STOCK +
                " WHERE " + COL_ITEM_NAME + " = ?", new String[]{itemName});

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close(); // Close the cursor to avoid memory leaks
            return -1; // Return -1 to indicate the item already exists
        }

        // Close the cursor after checking
        if (cursor != null) {
            cursor.close();
        }

        // Insert new item if it doesn't exist
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        return db.insert(ITEMS_TABLE_STOCK, null, values);
    }

    public StockItem getStockItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STOCK, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            // Get column index safely
            int columnIndexId = cursor.getColumnIndex(COL_ID);
            int columnIndexItemName = cursor.getColumnIndex(COL_ITEM_NAME);
            int columnIndexCostPrice = cursor.getColumnIndex(COL_COST_PRICE);
            int columnIndexSellingPrice = cursor.getColumnIndex(COL_SELLING_PRICE);
            int columnIndexQuantity = cursor.getColumnIndex(COL_QUANTITY);
            int columnIndexDescription = cursor.getColumnIndex(COL_DESCRIPTION);
            int columnIndexCategory = cursor.getColumnIndex(COL_CATEGORY);
            int columnIndexSize = cursor.getColumnIndex(COL_SIZE);

            // Ensure column index is valid
            if (columnIndexId != -1 && columnIndexItemName != -1 && columnIndexCostPrice != -1 &&
                    columnIndexSellingPrice != -1 && columnIndexQuantity != -1 && columnIndexDescription != -1 &&
                    columnIndexCategory != -1) {
                // Access data
                int stockId = cursor.getInt(columnIndexId);
                String itemName = cursor.getString(columnIndexItemName);
                double costPrice = cursor.getDouble(columnIndexCostPrice);
                double sellingPrice = cursor.getDouble(columnIndexSellingPrice);
                int quantity = cursor.getInt(columnIndexQuantity);
                String description = cursor.getString(columnIndexDescription);
                String category = cursor.getString(columnIndexCategory);
                String itemSize = cursor.getString(columnIndexSize);

                cursor.close();
                return new StockItem(stockId, itemName, costPrice, sellingPrice, quantity, description, category, itemSize);
            } else {
                Log.e("StockDatabaseHelper", "One or more columns are missing in the database.");
            }
        }
        return null;
    }

    public boolean isStockItemExists(String itemName, String size) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK, null, COL_ITEM_NAME + "=? AND " + COL_SIZE + "=?",
                new String[]{itemName, size}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public long addItemSize(String itemSize) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the size already exists in the table
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + ITEMS_SIZE_TABLE_STOCK +
                " WHERE " + COL_SIZE + " = ?", new String[]{itemSize});

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close(); // Close the cursor to prevent memory leaks
            return -1; // Return -1 to indicate the size already exists
        }

        // Close the cursor after checking
        if (cursor != null) {
            cursor.close();
        }

        // Insert new size if it doesn't exist
        ContentValues values = new ContentValues();
        values.put(COL_SIZE, itemSize);
        return db.insert(ITEMS_SIZE_TABLE_STOCK, null, values);
    }

    public int updateStockItem(int id, String itemName, String size, double costPrice, double sellingPrice, int quantity, String description, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        values.put(COL_SIZE, size);

        return db.update(TABLE_STOCK, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateItemQuantity(String name, String size, double costPrice, double sellingPrice, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();


        // Step 3: Update the new total quantity in the database
        ContentValues values = new ContentValues();
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, newQuantity); // Store the updated quantity

        db.update(TABLE_STOCK, values, COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?", new String[]{name, size});

        db.close(); // Close database connection
    }

    public void saveSale(String itemName, double costPrice, double sellingPrice, int quantity, String description, String size, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Get the current date and time (excluding seconds)
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_SIZE, size);
        values.put(DATE, currentDateTime);
        values.put(COL_CATEGORY, category);

        db.insert(TABLE_SALES, null, values);
        db.close();
    }


    public List<Items> getAllItems() {
        List<Items> stockList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ITEMS_TABLE_STOCK, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Items item = new Items();
                item.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME)));
                System.out.println("Item Name1 " + item.getItemName());
                stockList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return stockList;
    }

    public List<ItemSizes> getAllItemSizes() {
        List<ItemSizes> itemSize = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ITEMS_SIZE_TABLE_STOCK, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                ItemSizes item = new ItemSizes();
                item.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                item.setItemSize(cursor.getString(cursor.getColumnIndex(COL_SIZE)));
                itemSize.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return itemSize;
    }
    public List<StockItem> getAllStockItems() {
        List<StockItem> stockList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query the database
        Cursor cursor = db.query(TABLE_STOCK, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String itemName = cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
                double costPrice = cursor.getDouble(cursor.getColumnIndex(COL_COST_PRICE));
                double sellingPrice = cursor.getDouble(cursor.getColumnIndex(COL_SELLING_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COL_QUANTITY));
                String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndex(COL_CATEGORY));
                String itemSize = cursor.getString(cursor.getColumnIndex(COL_SIZE));
                // Add each stock item to the list
                StockItem stockItem = new StockItem(id, itemName, costPrice, sellingPrice, quantity, description, category, itemSize);
                stockList.add(stockItem);
            } while (cursor.moveToNext()); // Iterate through the cursor
            cursor.close();
        }
        return stockList;
    }
    public List<String> getCategories() {
        List<String> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get distinct categories from the stock table
        Cursor cursor = db.query(
                true, // This ensures we only get distinct categories
                TABLE_STOCK,
                new String[]{COL_CATEGORY}, // Only select the category column
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndex(COL_CATEGORY));
                categoryList.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Add "All" as the first item in the list for the spinner default
        categoryList.add(0, "All");

        return categoryList;
    }

    public List<StockItem> getStockItemsByCategory(String category) {
        List<StockItem> stockList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        if (category.equalsIgnoreCase("All")) {
            // Fetch all items if "All" is passed
            cursor = db.query(
                    TABLE_STOCK,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } else {
            // Fetch items where category matches the parameter
            cursor = db.query(
                    TABLE_STOCK,
                    null,
                    COL_CATEGORY + " = ?",
                    new String[]{category},
                    null,
                    null,
                    null
            );
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String itemName = cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
                double costPrice = cursor.getDouble(cursor.getColumnIndex(COL_COST_PRICE));
                double sellingPrice = cursor.getDouble(cursor.getColumnIndex(COL_SELLING_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COL_QUANTITY));
                String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
                String itemSize = cursor.getString(cursor.getColumnIndex(COL_SIZE));
                String itemCategory = cursor.getString(cursor.getColumnIndex(COL_CATEGORY));

                // Create a StockItem object and add it to the list
                StockItem stockItem = new StockItem(id, itemName, costPrice, sellingPrice, quantity, description, itemCategory, itemSize);
                stockList.add(stockItem);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return stockList;
    }



    public String getLowStockItems() {
        StringBuilder lowStockReport = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();

        // First, check if the table has any records at all
        Cursor checkCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_STOCK, null);
        if (checkCursor != null && checkCursor.moveToFirst()) {
            int totalRecords = checkCursor.getInt(0);
            checkCursor.close();

            // If there are no items in the database
            if (totalRecords == 0) {
                return "⚠️ The stock database is currently empty.\n";
            }
        }

        // Query to get items where quantity is less than 50
        Cursor cursor = db.query(TABLE_STOCK, null, COL_QUANTITY + " < ?", new String[]{"50"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            lowStockReport.append("🚨 **Low Stock Alert** 🚨\n");
            do {
                String itemName = cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
                String itemSize = cursor.getString(cursor.getColumnIndex(COL_SIZE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COL_QUANTITY));

                // Append item details to the report
                lowStockReport.append("🔹 ").append(itemName).append(" (").append(itemSize).append(") - ")
                        .append(quantity).append(" left\n");
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            lowStockReport.append("✅ All items are sufficiently stocked.\n");
        }

        return lowStockReport.toString();
    }

    public int getCurrentQuantity(String name, String size) {
        SQLiteDatabase db = this.getReadableDatabase();
        int currentQuantity = 0;

        // Query to retrieve the current quantity
        Cursor cursor = db.rawQuery("SELECT " + COL_QUANTITY + " FROM " + TABLE_STOCK +
                        " WHERE " + COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{name, size});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentQuantity = cursor.getInt(0); // Get the current quantity
            }
            cursor.close(); // Close cursor after use
        }

        return currentQuantity; // Return the retrieved quantity
    }

    public String getSalesReportForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder reportBuilder = new StringBuilder();

        // Query the sales table for items on the specified date
        Cursor cursor = db.query(TABLE_SALES, null, DATE + " = ?", new String[]{date}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            reportBuilder.append("📊 Full Report ").append(date).append(" 📊\n\n");

            // Loop through the cursor to retrieve all the sales items
            do {
                String itemName = cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
                double sellingPrice = cursor.getDouble(cursor.getColumnIndex(COL_SELLING_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COL_QUANTITY));
                String size = cursor.getString(cursor.getColumnIndex(COL_SIZE));
                // Print the values using System.out.println
                System.out.println("Phils Item: " + itemName + " | Price: " + sellingPrice + " | Quantity: " + quantity + " | Size: " + size);

                // Calculate the total revenue for the item and round to 2 decimal places


                // Append item details to the report
                reportBuilder.append("🔹 ").append(itemName).append(" (").append(size).append(") - ")
                        .append(quantity).append(" sold, Sold for R").append(sellingPrice).append("\n\n");

            } while (cursor.moveToNext());

            cursor.close();
        } else {
            reportBuilder.append("No sales recorded for ").append(date).append(".\n\n");
        }

        return reportBuilder.toString();
    }

    public String exportSalesToCSV(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        String csvFileName = "sales_data.csv";
        File csvFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName);

        try (FileWriter writer = new FileWriter(csvFile)) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SALES, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Write column headers
                writer.append("Item Name,Cost Price,Selling Price,Quantity,Description,Size,Date,Category\n");

                do {
                    writer.append(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME))).append(",")
                            .append(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_COST_PRICE)))).append(",")
                            .append(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SELLING_PRICE)))).append(",")
                            .append(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(DATE))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY))).append("\n");
                } while (cursor.moveToNext());

                cursor.close();
            }
            db.close();

            return csvFile.getAbsolutePath(); // Return the file path
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to generate CSV file.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public double getSumOfSellingPrice(String date) {
        double totalSellingPrice = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(" + COL_SELLING_PRICE + ") FROM " + TABLE_SALES + " WHERE " + DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            totalSellingPrice = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        System.out.println("Total Made : R" + totalSellingPrice);

        return totalSellingPrice;
    }

    public int getTotalSales(String date) {
        int totalItemsSold = 0;

        SQLiteDatabase db = this.getReadableDatabase();


        String query = "SELECT SUM(" + COL_QUANTITY + ") FROM " + TABLE_SALES + " WHERE " + DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            totalItemsSold = cursor.getInt(0);
        }

        cursor.close();
        db.close();


        return totalItemsSold; // You can modify this to return a different value if needed
    }

    public String getMostAppearingItemWithSizeForDate(String date) {
        String result = null;
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to find the most sold item (by total quantity) with size for the given date
        String query = "SELECT " + COL_ITEM_NAME + ", " + COL_SIZE + ", SUM(" + COL_QUANTITY + ") AS total_quantity " +
                "FROM " + TABLE_SALES +
                " WHERE " + DATE + " = ? " +
                " GROUP BY " + COL_ITEM_NAME + ", " + COL_SIZE +
                " ORDER BY total_quantity DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            String itemName = cursor.getString(cursor.getColumnIndex(COL_ITEM_NAME));
            String size = cursor.getString(cursor.getColumnIndex(COL_SIZE));
//            int totalQuantity = cursor.getInt(cursor.getColumnIndex("total_quantity")); // Get total quantity

            result = itemName + "," + size;
        }

        cursor.close();
        db.close();
        System.out.println("Most Sold: " + result);

        return result;
    }

    public void deleteStockItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCK, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

}
