// Copyright (C) 2009-2010 Mihai Preda

package ua.naiksoftware.aritymod;

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
