package ua.naiksoftware.aritymod;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
//import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle state) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppTheme_Dark);
        }


        super.onCreate(state);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(getString(R.string.settings));
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPreferencesFromResource(R.xml.settings);

        //getSupportFragmentManager().beginTransaction()
        //        .replace(R.id.settings_fragment, new SettingsFragment()).commit();
    }
}
