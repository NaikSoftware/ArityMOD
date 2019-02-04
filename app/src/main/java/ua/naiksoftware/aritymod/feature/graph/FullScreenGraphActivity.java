// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.feature.graph;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.javia.arity.Function;

import java.io.File;
import java.util.ArrayList;

import ua.naiksoftware.aritymod.MainActivity;
import ua.naiksoftware.aritymod.R;
import ua.naiksoftware.aritymod.core.ThemedActivity;

public class FullScreenGraphActivity extends ThemedActivity {

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
            grapher = f.arity() == 1 ? new GraphView(this) : new Graph3dView(this);
            grapher.setFunction(f);
        } else {
            grapher = new GraphView(this);
            ((GraphView) grapher).setFunctions(funcs);
        }
        setContentView(R.layout.activity_full_screen_graph);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewGroup container = findViewById(R.id.full_screen_graph_container);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case android.R.id.home:
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
