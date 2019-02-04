// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.core

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import ua.naiksoftware.aritymod.R

abstract class ThemedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppTheme_Dark)
        }

        super.onCreate(savedInstanceState)
    }
}
