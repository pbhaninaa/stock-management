package com.example.finance;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("Range")
public class StockDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "stock.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_STOCK = "stock_table";
    private static final String TABLE_SALES = "sales_table";
    private static final String ITEMS_TABLE_STOCK = "items_in_store";
    private static final String ITEMS_SIZE_TABLE_STOCK = "items_sizes_in_store";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_APP_SETTINGS = "app_settings";
    private static final String TABLE_REPORT_HISTORY = "report_history";

    private static final String COL_ID = "id";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_COST_PRICE = "cost_price";
    private static final String COL_SELLING_PRICE = "selling_price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_RECEIVED_QUANTITY = "received_quantity";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_CATEGORY = "category";
    private static final String COL_SIZE = "size";
    private static final String COL_DATE = "date";

    private static final String COL_FULL_NAME = "full_name";
    private static final String COL_USERNAME = "username";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD_HASH = "password_hash";
    private static final String COL_ROLE = "role";
    private static final String COL_IS_ACTIVE = "is_active";
    private static final String COL_CREATED_AT = "created_at";

    private static final String COL_SETTING_KEY = "setting_key";
    private static final String COL_SETTING_VALUE = "setting_value";

    private static final String COL_REPORT_KIND = "report_kind";
    private static final String COL_SENT_AT = "sent_at";

    public static final String SETTING_GLOBAL_LOW_STOCK_THRESHOLD = "global_low_stock_threshold";
    public static final String REPORT_KIND_STORE_CLOSE = "store_close";
    public static final String REPORT_KIND_MIDNIGHT_AUTO = "midnight_auto";
    public static final String REPORT_KIND_MANUAL_FULL = "manual_full";

    private static final String CREATE_TABLE_STOCK = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT, " +
            COL_COST_PRICE + " REAL, " +
            COL_SELLING_PRICE + " REAL, " +
            COL_QUANTITY + " INTEGER, " +
            COL_RECEIVED_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_SIZE + " TEXT, " +
            COL_CATEGORY + " TEXT)";

    private static final String CREATE_TABLE_SALES = "CREATE TABLE IF NOT EXISTS " + TABLE_SALES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT, " +
            COL_COST_PRICE + " REAL, " +
            COL_SELLING_PRICE + " REAL, " +
            COL_QUANTITY + " INTEGER, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_SIZE + " TEXT, " +
            COL_DATE + " TEXT, " +
            COL_CATEGORY + " TEXT)";

    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM_NAME + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_ITEM_SIZES = "CREATE TABLE IF NOT EXISTS " + ITEMS_SIZE_TABLE_STOCK + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SIZE + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_FULL_NAME + " TEXT NOT NULL, " +
            COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
            COL_EMAIL + " TEXT DEFAULT '', " +
            COL_PASSWORD_HASH + " TEXT NOT NULL, " +
            COL_ROLE + " TEXT NOT NULL, " +
            COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
            COL_CREATED_AT + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_APP_SETTINGS = "CREATE TABLE IF NOT EXISTS " + TABLE_APP_SETTINGS + " (" +
            COL_SETTING_KEY + " TEXT PRIMARY KEY, " +
            COL_SETTING_VALUE + " TEXT)";

    private static final String CREATE_TABLE_REPORT_HISTORY = "CREATE TABLE IF NOT EXISTS " + TABLE_REPORT_HISTORY + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_REPORT_KIND + " TEXT NOT NULL, " +
            COL_SENT_AT + " TEXT NOT NULL)";

    public StockDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_USERS);
        }

        if (oldVersion < 3) {
            createTables(db);

            if (!columnExists(db, TABLE_USERS, COL_EMAIL)) {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_EMAIL + " TEXT DEFAULT ''");
            }

            if (!columnExists(db, TABLE_STOCK, COL_RECEIVED_QUANTITY)) {
                db.execSQL("ALTER TABLE " + TABLE_STOCK + " ADD COLUMN " + COL_RECEIVED_QUANTITY + " INTEGER NOT NULL DEFAULT 0");
                db.execSQL("UPDATE " + TABLE_STOCK + " SET " + COL_RECEIVED_QUANTITY + " = " + COL_QUANTITY +
                        " WHERE " + COL_RECEIVED_QUANTITY + " = 0");
            }
        }
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STOCK);
        db.execSQL(CREATE_TABLE_ITEMS);
        db.execSQL(CREATE_TABLE_ITEM_SIZES);
        db.execSQL(CREATE_TABLE_SALES);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_APP_SETTINGS);
        db.execSQL(CREATE_TABLE_REPORT_HISTORY);
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean exists = false;
        while (cursor.moveToNext()) {
            if (columnName.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                exists = true;
                break;
            }
        }
        cursor.close();
        return exists;
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public void seedDefaultSelections() {
        addItem("Select Item");
        addItemSize("Select Item size");
    }

    public boolean hasUsers() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        boolean hasUsers = false;
        if (cursor.moveToFirst()) {
            hasUsers = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasUsers;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_USERNAME + " = ?",
                new String[]{Utils.normalizeUsername(username)}, null, null, null);
        boolean isTaken = cursor.moveToFirst();
        cursor.close();
        return isTaken;
    }

    public User createUser(String fullName, String username, String email, String password, String role) {
        if (isUsernameTaken(username)) {
            return null;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName.trim());
        values.put(COL_USERNAME, Utils.normalizeUsername(username));
        values.put(COL_EMAIL, email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
        values.put(COL_PASSWORD_HASH, Utils.hashPassword(password));
        values.put(COL_ROLE, role);
        values.put(COL_IS_ACTIVE, 1);
        values.put(COL_CREATED_AT, now());
        long userId = db.insert(TABLE_USERS, null, values);

        if (userId == -1L) {
            return null;
        }
        return getUserById(userId);
    }

    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_USERNAME + " = ?",
                new String[]{Utils.normalizeUsername(username)}, null, null, null);

        User authenticatedUser = null;
        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH));
            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ACTIVE)) == 1;
            if (isActive && Utils.verifyPassword(password, storedHash)) {
                authenticatedUser = cursorToUser(cursor);
            }
        }
        cursor.close();
        return authenticatedUser;
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null,
                COL_ROLE + " ASC, " + COL_FULL_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    public boolean updateUserActiveStatus(long userId, boolean isActive) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_ACTIVE, isActive ? 1 : 0);
        return db.update(TABLE_USERS, values, COL_ID + " = ?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUserRole(long userId, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ROLE, role);
        return db.update(TABLE_USERS, values, COL_ID + " = ?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUserEmail(long userId, String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
        return db.update(TABLE_USERS, values, COL_ID + " = ?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    public List<String> getActiveReportRecipientEmails() {
        List<String> recipients = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_IS_ACTIVE + " = 1 AND " + COL_EMAIL + " <> '' AND (" +
                COL_ROLE + " = ? OR " + COL_ROLE + " = ?)";
        Cursor cursor = db.query(true, TABLE_USERS, new String[]{COL_EMAIL}, selection,
                new String[]{Utils.ROLE_OWNER, Utils.ROLE_MANAGER}, null, null, COL_EMAIL + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                recipients.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recipients;
    }

    public Integer getGlobalLowStockThreshold() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_APP_SETTINGS, new String[]{COL_SETTING_VALUE},
                COL_SETTING_KEY + " = ?", new String[]{SETTING_GLOBAL_LOW_STOCK_THRESHOLD},
                null, null, null);
        Integer threshold = null;
        if (cursor.moveToFirst()) {
            String value = cursor.getString(cursor.getColumnIndexOrThrow(COL_SETTING_VALUE));
            if (value != null && !value.trim().isEmpty()) {
                try {
                    threshold = Integer.parseInt(value.trim());
                } catch (NumberFormatException ignored) {
                    threshold = null;
                }
            }
        }
        cursor.close();
        return threshold;
    }

    public void setGlobalLowStockThreshold(Integer threshold) {
        SQLiteDatabase db = getWritableDatabase();
        if (threshold == null || threshold <= 0) {
            db.delete(TABLE_APP_SETTINGS, COL_SETTING_KEY + " = ?",
                    new String[]{SETTING_GLOBAL_LOW_STOCK_THRESHOLD});
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COL_SETTING_KEY, SETTING_GLOBAL_LOW_STOCK_THRESHOLD);
        values.put(COL_SETTING_VALUE, String.valueOf(threshold));
        db.insertWithOnConflict(TABLE_APP_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean hasDailyReportBeenSent(String reportDate) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_DATE + " = ? AND (" + COL_REPORT_KIND + " = ? OR " + COL_REPORT_KIND + " = ?)";
        Cursor cursor = db.query(TABLE_REPORT_HISTORY, new String[]{COL_ID}, selection,
                new String[]{reportDate, REPORT_KIND_STORE_CLOSE, REPORT_KIND_MIDNIGHT_AUTO},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void recordReportSent(String reportDate, String reportKind) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, reportDate);
        values.put(COL_REPORT_KIND, reportKind);
        values.put(COL_SENT_AT, now());
        db.insert(TABLE_REPORT_HISTORY, null, values);
    }

    private User cursorToUser(Cursor cursor) {
        return new User(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ACTIVE)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }

    public void clearStockTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_STOCK, null, null);
        db.delete(TABLE_SALES, null, null);
        db.close();
    }

    public long addStockItem(String itemName, double costPrice, double sellingPrice, int quantity,
                             String description, String category, String itemSize) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_RECEIVED_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        values.put(COL_SIZE, itemSize);
        return db.insert(TABLE_STOCK, null, values);
    }

    public void restockItem(String name, String size, double costPrice, double sellingPrice,
                            int addedQuantity, String description, String category) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_STOCK, new String[]{COL_QUANTITY, COL_RECEIVED_QUANTITY},
                COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{name, size}, null, null, null);

        int currentQuantity = 0;
        int receivedQuantity = 0;
        if (cursor.moveToFirst()) {
            currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            receivedQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECEIVED_QUANTITY));
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, currentQuantity + addedQuantity);
        values.put(COL_RECEIVED_QUANTITY, receivedQuantity + addedQuantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        db.update(TABLE_STOCK, values, COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{name, size});
    }

    public long addItem(String itemName) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + ITEMS_TABLE_STOCK + " WHERE " + COL_ITEM_NAME + " = ?",
                new String[]{itemName});

        if (cursor.moveToFirst()) {
            cursor.close();
            return -1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        return db.insert(ITEMS_TABLE_STOCK, null, values);
    }

    public StockItem getStockItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        StockItem stockItem = null;
        if (cursor.moveToFirst()) {
            stockItem = cursorToStockItem(cursor);
        }
        cursor.close();
        return stockItem;
    }

    public boolean isStockItemExists(String itemName, String size) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK, new String[]{COL_ID},
                COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{itemName, size}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public long addItemSize(String itemSize) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + ITEMS_SIZE_TABLE_STOCK + " WHERE " + COL_SIZE + " = ?",
                new String[]{itemSize});

        if (cursor.moveToFirst()) {
            cursor.close();
            return -1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_SIZE, itemSize);
        return db.insert(ITEMS_SIZE_TABLE_STOCK, null, values);
    }

    public int updateStockItem(int id, String itemName, String size, double costPrice,
                               double sellingPrice, int quantity, String description, String category) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        values.put(COL_SIZE, size);
        return db.update(TABLE_STOCK, values, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void updateItemQuantity(String name, String size, double costPrice,
                                   double sellingPrice, int newQuantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, newQuantity);
        db.update(TABLE_STOCK, values, COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{name, size});
        db.close();
    }

    public void saveSale(String itemName, double costPrice, double sellingPrice, int quantity,
                         String description, String size, String category) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_COST_PRICE, costPrice);
        values.put(COL_SELLING_PRICE, sellingPrice);
        values.put(COL_QUANTITY, quantity);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_SIZE, size);
        values.put(COL_DATE, currentDateTime);
        values.put(COL_CATEGORY, category);

        db.insert(TABLE_SALES, null, values);
        db.close();
    }

    public List<Items> getAllItems() {
        List<Items> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ITEMS_TABLE_STOCK, null, null, null, null, null, COL_ITEM_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Items item = new Items();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public List<ItemSizes> getAllItemSizes() {
        List<ItemSizes> itemSizes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ITEMS_SIZE_TABLE_STOCK, null, null, null, null, null, COL_SIZE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                ItemSizes itemSize = new ItemSizes();
                itemSize.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                itemSize.setItemSize(cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE)));
                itemSizes.add(itemSize);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return itemSizes;
    }

    public List<StockItem> getAllStockItems() {
        List<StockItem> stockItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK, null, null, null, null, null, COL_ITEM_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                stockItems.add(cursorToStockItem(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stockItems;
    }

    public List<String> getCategories() {
        List<String> categoryList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(true, TABLE_STOCK, new String[]{COL_CATEGORY}, null, null,
                null, null, COL_CATEGORY + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                if (category != null && !category.trim().isEmpty()) {
                    categoryList.add(category);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        categoryList.add(0, "All");
        return categoryList;
    }

    public List<StockItem> getStockItemsByCategory(String category) {
        List<StockItem> stockItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor;
        if ("All".equalsIgnoreCase(category)) {
            cursor = db.query(TABLE_STOCK, null, null, null, null, null, COL_ITEM_NAME + " ASC");
        } else {
            cursor = db.query(TABLE_STOCK, null, COL_CATEGORY + " = ?",
                    new String[]{category}, null, null, COL_ITEM_NAME + " ASC");
        }

        if (cursor.moveToFirst()) {
            do {
                stockItems.add(cursorToStockItem(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stockItems;
    }

    private StockItem cursorToStockItem(Cursor cursor) {
        return new StockItem(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_COST_PRICE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SELLING_PRICE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECEIVED_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE))
        );
    }

    public List<StockItem> getLowStockStockItems() {
        List<StockItem> lowStockItems = new ArrayList<>();
        List<StockItem> stockItems = getAllStockItems();
        Integer globalThreshold = getGlobalLowStockThreshold();

        for (StockItem stockItem : stockItems) {
            int threshold = Utils.resolveLowStockThreshold(stockItem.getReceivedQuantity(), globalThreshold);
            if (stockItem.getQuantity() <= threshold) {
                lowStockItems.add(stockItem);
            }
        }
        return lowStockItems;
    }

    public String getLowStockItems() {
        StringBuilder lowStockReport = new StringBuilder();
        List<StockItem> stockItems = getAllStockItems();
        if (stockItems.isEmpty()) {
            return "The stock database is currently empty.\n";
        }

        List<StockItem> lowStockItems = getLowStockStockItems();
        Integer globalThreshold = getGlobalLowStockThreshold();

        if (!lowStockItems.isEmpty()) {
            lowStockReport.append("Low Stock Alert\n");
            if (globalThreshold != null && globalThreshold > 0) {
                lowStockReport.append("Global threshold: ").append(globalThreshold).append("\n");
            } else {
                lowStockReport.append("Fallback threshold: 10% of received stock\n");
            }

            for (StockItem item : lowStockItems) {
                int threshold = Utils.resolveLowStockThreshold(item.getReceivedQuantity(), globalThreshold);
                lowStockReport.append("- ").append(item.getItemName())
                        .append(" (").append(item.getItemSize()).append(") - ")
                        .append(item.getQuantity()).append(" left")
                        .append(" [threshold ").append(threshold).append("]\n");
            }
        } else {
            lowStockReport.append("All items are sufficiently stocked.\n");
        }
        return lowStockReport.toString();
    }

    public int getCurrentQuantity(String name, String size) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_QUANTITY + " FROM " + TABLE_STOCK +
                        " WHERE " + COL_ITEM_NAME + " = ? AND " + COL_SIZE + " = ?",
                new String[]{name, size});

        int currentQuantity = 0;
        if (cursor.moveToFirst()) {
            currentQuantity = cursor.getInt(0);
        }
        cursor.close();
        return currentQuantity;
    }

    public String getSalesReportForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder reportBuilder = new StringBuilder();
        Cursor cursor = db.query(TABLE_SALES, null, COL_DATE + " = ?",
                new String[]{date}, null, null, null);

        if (cursor.moveToFirst()) {
            reportBuilder.append("Full Report ").append(date).append("\n\n");
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME));
                double sellingPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SELLING_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE));
                reportBuilder.append("- ").append(itemName).append(" (").append(size).append(") - ")
                        .append(quantity).append(" sold, Sold for R").append(sellingPrice).append("\n");
            } while (cursor.moveToNext());
        } else {
            reportBuilder.append("No sales recorded for ").append(date).append(".\n");
        }
        cursor.close();
        return reportBuilder.toString();
    }

    public String exportSalesToCSV(Context context) {
        SQLiteDatabase db = getReadableDatabase();
        String csvFileName = "sales_data.csv";
        File csvFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName);

        try (FileWriter writer = new FileWriter(csvFile)) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SALES, null);
            if (cursor.moveToFirst()) {
                writer.append("Item Name,Cost Price,Selling Price,Quantity,Description,Size,Date,Category\n");
                do {
                    writer.append(cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME))).append(",")
                            .append(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_COST_PRICE)))).append(",")
                            .append(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SELLING_PRICE)))).append(",")
                            .append(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))).append(",")
                            .append(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY))).append("\n");
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return csvFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to generate CSV file.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public double getSumOfSellingPrice(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_SELLING_PRICE + ") FROM " + TABLE_SALES +
                " WHERE " + COL_DATE + " = ?", new String[]{date});
        double totalSellingPrice = 0;
        if (cursor.moveToFirst()) {
            totalSellingPrice = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return totalSellingPrice;
    }

    public int getTotalSales(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_QUANTITY + ") FROM " + TABLE_SALES +
                " WHERE " + COL_DATE + " = ?", new String[]{date});
        int totalItemsSold = 0;
        if (cursor.moveToFirst()) {
            totalItemsSold = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return totalItemsSold;
    }

    public String getMostAppearingItemWithSizeForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COL_ITEM_NAME + ", " + COL_SIZE + ", SUM(" + COL_QUANTITY + ") AS total_quantity " +
                "FROM " + TABLE_SALES +
                " WHERE " + COL_DATE + " = ? " +
                " GROUP BY " + COL_ITEM_NAME + ", " + COL_SIZE +
                " ORDER BY total_quantity DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        String result = null;
        if (cursor.moveToFirst()) {
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME));
            String size = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE));
            result = itemName + ", " + size;
        }
        cursor.close();
        db.close();
        return result;
    }

    public void deleteStockItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_STOCK, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
