// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragment;

import ua.naiksoftware.aritymod.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        // TODO: Setup toolbar
    }
}
