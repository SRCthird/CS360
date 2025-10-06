package me.frigidambiance.projecttwo;

import android.app.Application;

import me.frigidambiance.projecttwo.net.ApiClient;
import me.frigidambiance.projecttwo.sync.InventorySyncWorker;

public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApiClient.setBaseUrl("https://mongo.frigidambiance.me:8080/");

        InventorySyncWorker.ensurePeriodic(this);
    }
}