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
    public static final String COL_SERVER_ID = "server_id";
    public static final String COL_CLIENT_ID = "client_id";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_DELETED_AT = "deleted_at";
    public static final String COL_DIRTY = "dirty";

    public InventoryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context.getApplicationContext();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createSql =
                "CREATE TABLE " + TABLE_INVENTORY + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ITEM + " TEXT NOT NULL, " +
                    COL_LOCATION + " TEXT NOT NULL," +
                    COL_SERVER_ID + " TEXT, " +
                    COL_CLIENT_ID + " TEXT, " +
                    COL_UPDATED_AT + " INTEGER, " +
                    COL_DELETED_AT + " INTEGER, " +
                    COL_DIRTY + " INTEGER NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(createSql);
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_inventory_client ON " + TABLE_INVENTORY + "(" + COL_CLIENT_ID + ")");

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
            ContentValues values = toContentValues(item, false);
            db.insert(TABLE_INVENTORY, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + " ADD COLUMN " + COL_SERVER_ID + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + " ADD COLUMN " + COL_CLIENT_ID + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + " ADD COLUMN " + COL_UPDATED_AT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + " ADD COLUMN " + COL_DELETED_AT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY + " ADD COLUMN " + COL_DIRTY + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_inventory_client ON " + TABLE_INVENTORY + "(" + COL_CLIENT_ID + ")");
            // backfill updated_at to "now" for existing rows; client_id will be filled lazily on read/write
            db.execSQL("UPDATE " + TABLE_INVENTORY + " SET " + COL_UPDATED_AT + " = strftime('%s','now')*1000 WHERE " + COL_UPDATED_AT + " IS NULL");
        }
    }

    // --- CRUD using the model ---

    /** Inserts and returns the new rowId; also sets the id on the provided item if successful. */
    public long insertItem(InventoryItem item) {
        long now = System.currentTimeMillis();
        item.setUpdatedAt(now);
        item.setDeletedAt(null);
        item.setDirty(true);
        if (item.getClientId().isEmpty()) {
            item.setClientId(java.util.UUID.randomUUID().toString());
        }
        long rowId = getWritableDatabase().insert(TABLE_INVENTORY, null, toContentValues(item, false));

        if (rowId != -1) {
            me.frigidambiance.projecttwo.sync.InventorySyncWorker.enqueueOneTime(
                    getContextSafe()
            );
        }
        return rowId;
    }

    private Context getContextSafe() { return appContext; }

    /** Convenience overload for quick adds. */
    public long insertItem(String itemName, String location) {
        return insertItem(new InventoryItem(itemName, location));
    }

    /** Updates by id contained in the model. Returns true if any row updated. */
    public boolean updateItem(InventoryItem item) {
        if (item.getId() == null) return false;
        item.setUpdatedAt(System.currentTimeMillis());
        item.setDirty(true);
        ContentValues values = toContentValues(item, false);
        int rows = getWritableDatabase().update(
                TABLE_INVENTORY, values, COL_ID + "=?", new String[]{String.valueOf(item.getId())});
        return rows > 0;
    }

    /** Updates by explicit id. */
    public boolean updateItem(int id, String itemName, String location) {
        InventoryItem it = getItemById(id);
        if (it == null) return false;
        it.setItem(itemName);
        it.setLocation(location);
        return updateItem(it);
    }

    public boolean softDeleteItem(int id) {
        InventoryItem it = getItemById(id);
        if (it == null) return false;
        it.setDeletedAt(System.currentTimeMillis());
        it.setUpdatedAt(System.currentTimeMillis());
        it.setDirty(true);
        ContentValues values = new ContentValues();
        values.put(COL_DELETED_AT, it.getDeletedAt());
        values.put(COL_UPDATED_AT, it.getUpdatedAt());
        values.put(COL_DIRTY, 1);
        int rows = getWritableDatabase().update(TABLE_INVENTORY, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        boolean ok = rows > 0;
        if (ok) me.frigidambiance.projecttwo.sync.InventorySyncWorker.enqueueOneTime(getContextSafe());
        return ok;
    }
    private Context appContext;
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

    public List<InventoryItem> getAllItems(boolean includeDeleted) {
        List<InventoryItem> items = new ArrayList<>();
        final String where = includeDeleted ? "" : (" WHERE " + COL_DELETED_AT + " IS NULL");
        final String sql = "SELECT " + columns() +
                " FROM " + TABLE_INVENTORY +
                where +
                " ORDER BY " + COL_ITEM + " ASC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) items.add(fromCursor(c));
        }
        return items;
    }

    /** Finds one by id; null if not found. */
    @Nullable
    public InventoryItem getItemById(int id) {
        final String sql = "SELECT " + columns() + " FROM " + TABLE_INVENTORY + " WHERE " + COL_ID + "=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    /** Finds the first match by exact name; null if not found. */
    @Nullable
    public InventoryItem getItemByName(String itemName) {
        final String sql = "SELECT " + columns() +
                " FROM " + TABLE_INVENTORY +
                " WHERE " + COL_ITEM + "=? AND " + COL_DELETED_AT + " IS NULL LIMIT 1";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{itemName})) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public List<InventoryItem> getDirtyItems() {
        List<InventoryItem> items = new ArrayList<>();
        final String sql = "SELECT " + columns() + " FROM " + TABLE_INVENTORY + " WHERE " + COL_DIRTY + " = 1";
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {
            while (c.moveToNext()) items.add(fromCursor(c));
        }
        return items;
    }

    @Nullable
    public InventoryItem findByServerOrClientId(@Nullable String serverId, @Nullable String clientId) {
        String where;
        List<String> args = new ArrayList<>();
        if (serverId != null && !serverId.isEmpty()) {
            where = COL_SERVER_ID + "=?";
            args.add(serverId);
        } else if (clientId != null && !clientId.isEmpty()) {
            where = COL_CLIENT_ID + "=?";
            args.add(clientId);
        } else {
            return null;
        }
        final String sql = "SELECT " + columns() + " FROM " + TABLE_INVENTORY + " WHERE " + where + " LIMIT 1";
        try (Cursor c = getReadableDatabase().rawQuery(sql, args.toArray(new String[0]))) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    public boolean saveMerged(InventoryItem it) {
        it.setDirty(false);
        ContentValues v = toContentValues(it, false);
        int rows = getWritableDatabase().update(
                TABLE_INVENTORY, v, COL_ID + "=?", new String[]{String.valueOf(it.getId())});
        return rows > 0;
    }

    public long insertFromServer(InventoryItem it) {
        it.setDirty(false);
        if (it.getClientId().isEmpty()) {
            it.setClientId(java.util.UUID.randomUUID().toString());
        }
        long rowId = getWritableDatabase().insert(TABLE_INVENTORY, null, toContentValues(it, false));
        if (rowId != -1) it.setId((int) rowId);
        return rowId;
    }

    public int getIdByItem(String itemName) {
        InventoryItem item = getItemByName(itemName);
        return item != null && item.getId() != null ? item.getId() : -1;
    }

    // --- Mapping helpers ---
    private static ContentValues toContentValues(InventoryItem item, boolean includeId) {
        ContentValues values = new ContentValues();
        if (includeId && item.getId() != null) values.put(COL_ID, item.getId());
        values.put(COL_ITEM, item.getItem());
        values.put(COL_LOCATION, item.getLocation());
        values.put(COL_SERVER_ID, item.getServerId());
        values.put(COL_CLIENT_ID, item.getClientId());
        values.put(COL_UPDATED_AT, item.getUpdatedAt());
        if (item.getDeletedAt() == null) values.putNull(COL_DELETED_AT);
        else values.put(COL_DELETED_AT, item.getDeletedAt());
        values.put(COL_DIRTY, item.isDirty() ? 1 : 0);
        return values;
    }

    private static String columns() {
        return COL_ID + "," + COL_ITEM + "," + COL_LOCATION + "," +
                COL_SERVER_ID + "," + COL_CLIENT_ID + "," +
                COL_UPDATED_AT + "," + COL_DELETED_AT + "," + COL_DIRTY;
    }

    private static InventoryItem fromCursor(Cursor c) {
        int idx = 0;
        int id = c.getInt(idx++);
        String item = c.getString(idx++);
        String location = c.getString(idx++);
        String serverId = c.isNull(idx) ? null : c.getString(idx); idx++;
        String clientId = c.isNull(idx) ? null : c.getString(idx); idx++;
        long updatedAt = c.isNull(idx) ? System.currentTimeMillis() : c.getLong(idx); idx++;
        Long deletedAt = c.isNull(idx) ? null : c.getLong(idx); idx++;
        boolean dirty = c.getInt(idx) == 1;

        InventoryItem it = new InventoryItem(id, item, location);
        it.setServerId(serverId);
        if (clientId == null || clientId.isEmpty()) {
            clientId = java.util.UUID.randomUUID().toString();
        }
        it.setClientId(clientId);
        it.setUpdatedAt(updatedAt);
        it.setDeletedAt(deletedAt);
        it.setDirty(dirty);
        return it;
    }
}
