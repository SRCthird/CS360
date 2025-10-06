package me.frigidambiance.projecttwo.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import me.frigidambiance.projecttwo.InventoryDatabase;

public class InventorySyncWorker extends Worker {
    public static final String UNIQUE_PERIODIC_NAME = "inventory-sync-periodic";

    public InventorySyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        try {
            InventoryDatabase db = new InventoryDatabase(getApplicationContext());
            SyncEngine.syncNow(getApplicationContext(), db);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    // --- Helpers to enqueue work ---
    public static void enqueueOneTime(Context ctx) {
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(InventorySyncWorker.class)
                .setConstraints(defaultConstraints())
                .build();
        WorkManager.getInstance(ctx).enqueueUniqueWork(
                "inventory-sync-once",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                req
        );
    }

    public static void ensurePeriodic(Context ctx) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(InventorySyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(defaultConstraints())
                .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
        );
    }

    private static Constraints defaultConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }
}