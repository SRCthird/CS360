package me.frigidambiance.projecttwo.net;

import me.frigidambiance.projecttwo.InventoryItem;

public final class InventoryMapper {
    private InventoryMapper() {}

    public static InventoryDto toDto(InventoryItem it) {
        InventoryDto d = new InventoryDto();
        d._id = it.getServerId();
        d.clientId = it.getClientId();
        d.item = it.getItem();
        d.location = it.getLocation();
        d.updatedAt = DateIso.toIsoUtc(it.getUpdatedAt());
        d.deletedAt = it.getDeletedAt() == null ? null : DateIso.toIsoUtc(it.getDeletedAt());
        return d;
    }

    public static void applyFromDto(InventoryItem it, InventoryDto d) {
        it.setServerId(d._id);
        if (d.clientId != null && !d.clientId.isEmpty()) it.setClientId(d.clientId);
        if (d.item != null) it.setItem(d.item);
        if (d.location != null) it.setLocation(d.location);
        if (d.updatedAt != null) it.setUpdatedAt(DateIso.parseIsoUtc(d.updatedAt));
        it.setDeletedAt(d.deletedAt == null ? null : DateIso.parseIsoUtc(d.deletedAt));
        it.setDirty(false);
    }
}
