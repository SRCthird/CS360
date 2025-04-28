package me.frigidambiance.projecttwo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "inventory.db";
    private static final int DB_VERSION = 1;

    public InventoryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, item TEXT, location TEXT)");

        // Preload sample data
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        String[][] data = {
                {"Espresso Beans - House Blend", "Back Room Shelf A"},
                {"Vanilla Syrup", "Bar Area - Syrup Rack"},
                {"Oat Milk (4-pack)", "Cold Storage"},
                {"12oz Hot Cups", "Front Counter Storage"},
                {"Chocolate Sauce", "Bar Area - Toppings Shelf"},
                {"Whipped Cream Canisters", "Cold Storage"},
                {"Filter Paper - Large", "Back Room Drawer 3"},
                {"Caramel Drizzle", "Bar Area - Syrup Rack"},
                {"To-Go Lids - 16oz", "Front Counter Cabinet"},
                {"Green Tea Bags", "Back Room Shelf B"}
        };

        for (String[] item : data) {
            ContentValues values = new ContentValues();
            values.put("item", item[0]);
            values.put("location", item[1]);
            db.insert("inventory", null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS inventory");
        onCreate(db);
    }

    public boolean deleteItem(int id) {
        return getWritableDatabase().delete("inventory", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean insertItem(String item, String location) {
        ContentValues values = new ContentValues();
        values.put("item", item);
        values.put("location", location);
        return getWritableDatabase().insert("inventory", null, values) != -1;
    }

    public boolean updateItem(int id, String item, String location) {
        ContentValues values = new ContentValues();
        values.put("item", item);
        values.put("location", location);
        return getWritableDatabase().update("inventory", values, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public List<HashMap<String, String>> getAllItems() {
        List<HashMap<String, String>> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT item, location FROM inventory", null);

        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("item", cursor.getString(0));
            map.put("location", cursor.getString(1));
            itemList.add(map);
        }
        cursor.close();
        return itemList;
    }

    public int getIdByItem(String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM inventory WHERE item = ?", new String[]{itemName});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

}
