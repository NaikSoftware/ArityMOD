// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.graph;

import android.view.View;

import org.javia.arity.Function;

interface Grapher {
    String SCREENSHOT_DIR = "/screenshots";
    void setFunction(Function f);
    void onPause();
    void onResume();
    View getView();
    String captureScreenshot();
}
