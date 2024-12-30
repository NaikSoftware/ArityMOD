// Copyright (C) 2009 Mihai Preda

package ua.naiksoftware.aritymod;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.javia.arity.Function;

import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;

public class FullScreenGraphActivity extends ThemedActivity implements Toolbar.OnMenuItemClickListener {

    private Grapher grapher;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ArrayList<Function> funcs = MainActivity.graphedFunction;
        if (funcs == null) {
            finish();
            return;
        }
        int size = funcs.size();
        if (size == 1) {
            Function f = funcs.get(0);
            grapher = f.arity() == 1 ? new GraphView(this) : new Graph3dSurfaceView(this);
            grapher.setFunction(f);
        } else {
            grapher = new GraphView(this);
            ((GraphView) grapher).setFunctions(funcs);
        }
        setContentView(R.layout.full_screen_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.graph);
        toolbar.setOnMenuItemClickListener(this);
        //toolbar.setSubtitle(/*functions*/);

        ViewGroup container = (ViewGroup) findViewById(R.id.full_screen_graph_container);
        container.addView(grapher.getView());
    }

    @Override
    protected void onPause() {
        super.onPause();
        grapher.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        grapher.onResume();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.capture_screenshot:
                String fileName = grapher.captureScreenshot();
                if (fileName != null) {
                    Toast.makeText(this, "Screenshot saved as \n" + fileName, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(new File(fileName)), "image/png");
                    startActivity(i);
                }
                break;

            default:
                return false;
        }
        return true;
    }
}
