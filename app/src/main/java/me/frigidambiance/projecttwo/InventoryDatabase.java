package me.frigidambiance.projecttwo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "inventory.db";
    private static final int DB_VERSION = 1;

    // Schema
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COL_ID = "id";
    public static final String COL_ITEM = "item";
    public static final String COL_LOCATION = "location";

    public InventoryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createSql =
                "CREATE TABLE " + TABLE_INVENTORY + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ITEM + " TEXT NOT NULL, " +
                        COL_LOCATION + " TEXT NOT NULL" +
                        ")";
        db.execSQL(createSql);

        // Preload sample data
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        InventoryItem[] seed = new InventoryItem[]{
                new InventoryItem("Espresso Beans - House Blend", "Back Room Shelf A"),
                new InventoryItem("Vanilla Syrup", "Bar Area - Syrup Rack"),
                new InventoryItem("Oat Milk (4-pack)", "Cold Storage"),
                new InventoryItem("12oz Hot Cups", "Front Counter Storage"),
                new InventoryItem("Chocolate Sauce", "Bar Area - Toppings Shelf"),
                new InventoryItem("Whipped Cream Canisters", "Cold Storage"),
                new InventoryItem("Filter Paper - Large", "Back Room Drawer 3"),
                new InventoryItem("Caramel Drizzle", "Bar Area - Syrup Rack"),
                new InventoryItem("To-Go Lids - 16oz", "Front Counter Cabinet"),
                new InventoryItem("Green Tea Bags", "Back Room Shelf B")
        };

        for (InventoryItem item : seed) {
            ContentValues values = toContentValues(item);
            db.insert(TABLE_INVENTORY, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // --- CRUD using the model ---

    /** Inserts and returns the new rowId; also sets the id on the provided item if successful. */
    public long insertItem(InventoryItem item) {
        long rowId = getWritableDatabase().insert(TABLE_INVENTORY, null, toContentValues(item));
        if (rowId != -1) {
            item.setId((int) rowId);
        }
        return rowId;
    }

    /** Convenience overload for quick adds. */
    public long insertItem(String itemName, String location) {
        return insertItem(new InventoryItem(itemName, location));
    }

    /** Updates by id contained in the model. Returns true if any row updated. */
    public boolean updateItem(InventoryItem item) {
        if (item.getId() == null) return false;
        ContentValues values = toContentValues(item);
        // Do not attempt to overwrite the id column
        values.remove(COL_ID);
        int rows = getWritableDatabase().update(
                TABLE_INVENTORY,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(item.getId())}
        );
        return rows > 0;
    }

    /** Updates by explicit id. */
    public boolean updateItem(int id, String itemName, String location) {
        return updateItem(new InventoryItem(id, itemName, location));
    }

    public boolean deleteItem(int id) {
        return getWritableDatabase()
                .delete(TABLE_INVENTORY, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    /** Gets all items (including id). */
    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = new ArrayList<>();
        final String sql = "SELECT " + COL_ID + ", " + COL_ITEM + ", " + COL_LOCATION +
                " FROM " + TABLE_INVENTORY + " ORDER BY " + COL_ITEM + " ASC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) {
                items.add(fromCursor(c));
            }
        }
        return items;
    }


    /** Finds one by id; null if not found. */
    @Nullable
    public InventoryItem getItemById(int id) {
        final String sql = "SELECT " + COL_ID + ", " + COL_ITEM + ", " + COL_LOCATION +
                " FROM " + TABLE_INVENTORY + " WHERE " + COL_ID + "=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    /** Finds the first match by exact name; null if not found. */
    @Nullable
    public InventoryItem getItemByName(String itemName) {
        final String sql = "SELECT " + COL_ID + ", " + COL_ITEM + ", " + COL_LOCATION +
                " FROM " + TABLE_INVENTORY + " WHERE " + COL_ITEM + "=? LIMIT 1";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{itemName})) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public int getIdByItem(String itemName) {
        InventoryItem item = getItemByName(itemName);
        return item != null && item.getId() != null ? item.getId() : -1;
    }

    // --- Mapping helpers ---
    private static ContentValues toContentValues(InventoryItem item) {
        ContentValues values = new ContentValues();
        if (item.getId() != null) {
            values.put(COL_ID, item.getId());
        }
        values.put(COL_ITEM, item.getItem());
        values.put(COL_LOCATION, item.getLocation());
        return values;
    }

    private static InventoryItem fromCursor(Cursor c) {
        int id = c.getInt(0);
        String item = c.getString(1);
        String location = c.getString(2);
        return new InventoryItem(id, item, location);
    }
}
