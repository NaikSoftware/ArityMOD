package ua.naiksoftware.aritymod;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * App settings screen
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }
}
