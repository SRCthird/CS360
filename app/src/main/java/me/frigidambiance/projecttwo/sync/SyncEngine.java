package me.frigidambiance.projecttwo.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.frigidambiance.projecttwo.InventoryDatabase;
import me.frigidambiance.projecttwo.InventoryItem;
import me.frigidambiance.projecttwo.net.ApiClient;
import me.frigidambiance.projecttwo.net.BulkResponse;
import me.frigidambiance.projecttwo.net.ChangesResponse;
import me.frigidambiance.projecttwo.net.InventoryApi;
import me.frigidambiance.projecttwo.net.InventoryDto;
import me.frigidambiance.projecttwo.net.InventoryMapper;
import me.frigidambiance.projecttwo.net.DateIso;
import retrofit2.Response;

public final class SyncEngine {
    private static final String TAG = "SyncEngine";
    private static final String PREFS = "inventory_sync_prefs";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";

    private SyncEngine() {}

    public static void syncNow(Context ctx, InventoryDatabase db) throws IOException {
        InventoryApi api = ApiClient.getInventoryApi(ctx);
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        List<InventoryItem> dirty = db.getDirtyItems();
        if (!dirty.isEmpty()) {
            List<InventoryDto> payload = new ArrayList<>(dirty.size());
            for (InventoryItem it : dirty) payload.add(InventoryMapper.toDto(it));

            Response<BulkResponse> resp = api.bulkUpsert(payload).execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new IOException("bulkUpsert failed: code=" + resp.code());
            }
            BulkResponse body = resp.body();

            if (body.upserts != null) {
                for (InventoryDto d : body.upserts) {
                    InventoryItem local = db.findByServerOrClientId(d._id, d.clientId);
                    if (local == null) {
                        InventoryItem it = new InventoryItem(d.item != null ? d.item : "", d.location != null ? d.location : "");
                        InventoryMapper.applyFromDto(it, d);
                        db.insertFromServer(it);
                    } else {
                        InventoryMapper.applyFromDto(local, d);
                        db.saveMerged(local);
                    }
                }
            }

            if (body.serverTime != null && !body.serverTime.isEmpty()) {
                prefs.edit().putString(KEY_LAST_SYNC_TIME, body.serverTime).apply();
            }
        }

        String since = prefs.getString(KEY_LAST_SYNC_TIME, "1970-01-01T00:00:00.000Z");
        Response<ChangesResponse> changesResp = api.getChanges(since).execute();
        if (!changesResp.isSuccessful() || changesResp.body() == null) {
            throw new IOException("getChanges failed: code=" + changesResp.code());
        }
        ChangesResponse cr = changesResp.body();

        if (cr.changes != null) {
            for (InventoryDto d : cr.changes) {
                InventoryItem local = db.findByServerOrClientId(d._id, d.clientId);
                if (local == null) {
                    InventoryItem it = new InventoryItem(d.item != null ? d.item : "", d.location != null ? d.location : "");
                    InventoryMapper.applyFromDto(it, d);
                    db.insertFromServer(it);
                    continue;
                }

                long serverUpdated = d.updatedAt != null ? DateIso.parseIsoUtc(d.updatedAt) : 0L;
                long localUpdated = local.getUpdatedAt();

                Long serverDeleted = d.deletedAt == null ? null : DateIso.parseIsoUtc(d.deletedAt);
                Long localDeleted = local.getDeletedAt();

                boolean serverDeletedNewer = serverDeleted != null && (localDeleted == null || serverDeleted > localDeleted);
                boolean serverNewer = serverUpdated > localUpdated;

                if (serverDeletedNewer) {
                    local.setDeletedAt(serverDeleted);
                    local.setDirty(false);
                    db.saveMerged(local);
                } else if (serverNewer && (localDeleted == null)) {
                    InventoryMapper.applyFromDto(local, d);
                    db.saveMerged(local);
                } else {
                    Log.d(TAG, "Kept local version for clientId=" + local.getClientId());
                }
            }
        }

        if (cr.serverTime != null && !cr.serverTime.isEmpty()) {
            prefs.edit().putString(KEY_LAST_SYNC_TIME, cr.serverTime).apply();
        }
    }
}