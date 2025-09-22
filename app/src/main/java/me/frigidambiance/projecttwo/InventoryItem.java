package me.frigidambiance.projecttwo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InventoryItem {
    private Integer id;       // nullable for new (unsaved) items
    private String item;
    private String location;

    public InventoryItem(@Nullable Integer id, @NonNull String item, @NonNull String location) {
        this.id = id;
        this.item = item;
        this.location = location;
    }

    public InventoryItem(@NonNull String item, @NonNull String location) {
        this(null, item, location);
    }

    @Nullable public Integer getId() { return id; }
    public void setId(@Nullable Integer id) { this.id = id; }

    @NonNull public String getItem() { return item; }
    public void setItem(@NonNull String item) { this.item = item; }

    @NonNull public String getLocation() { return location; }
    public void setLocation(@NonNull String location) { this.location = location; }

    @Override public String toString() {
        return "InventoryItem{id=" + id + ", item='" + item + "', location='" + location + "'}";
    }
}