package ua.naiksoftware.aritymod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("dark_theme", false)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
    }
}
