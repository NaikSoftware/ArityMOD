// Copyright (C) 2009 Mihai Preda
package ua.naiksoftware.aritymod;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ListDefs extends ListActivity {

    private Defs defs;
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defs = MainActivity.defs;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, defs.lines);
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.defs, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.clear_defs).setEnabled(defs.size() > 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.clear_defs:
                defs.clear();
                defs.save();
                adapter.notifyDataSetInvalidated();
                break;

            default:
                return false;
        }
        return true;
    }
}
