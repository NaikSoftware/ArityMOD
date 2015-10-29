package ua.naiksoftware.aritymod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
    }
}
