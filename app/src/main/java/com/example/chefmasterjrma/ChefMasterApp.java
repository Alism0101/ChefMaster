package com.example.chefmasterjrma;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ChefMasterApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme preference globally
        SharedPreferences prefs = getSharedPreferences("THEME_PREFS", MODE_PRIVATE);
        int mode = prefs.getInt("NIGHT_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
