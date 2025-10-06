package me.frigidambiance.projecttwo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InventoryItem {
    private Integer id;       // nullable for new (unsaved) items
    private String item;
    private String location;


    private @Nullable String serverId;
    private @NonNull String clientId;
    private long updatedAt;
    private @Nullable Long deletedAt;
    private boolean dirty;

    public InventoryItem(@Nullable Integer id, @NonNull String item, @NonNull String location) {
        this.id = id;
        this.item = item;
        this.location = location;

        this.serverId = null;
        this.clientId = java.util.UUID.randomUUID().toString();
        this.updatedAt = System.currentTimeMillis();
        this.deletedAt = null;
        this.dirty = false;
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

    @Nullable public String getServerId() { return serverId; }
    public void setServerId(@Nullable String serverId) { this.serverId = serverId; }

    @NonNull public String getClientId() { return clientId; }
    public void setClientId(@NonNull String clientId) { this.clientId = clientId; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Nullable public Long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(@Nullable Long deletedAt) { this.deletedAt = deletedAt; }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }

    @Override public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", item='" + item + "'" +
                ", location='" + location + "'" +
                ", serverId=" + serverId +
                ", clientId='" + clientId + "'" +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                ", dirty=" + dirty +
                '}';
    }
}