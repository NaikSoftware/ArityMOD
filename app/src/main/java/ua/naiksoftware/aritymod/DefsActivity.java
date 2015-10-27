package ua.naiksoftware.aritymod;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ua.naiksoftware.utils.ParcelableBinder;

public class DefsActivity extends AppCompatActivity implements
        DefsFragment.OnFragmentInteractionListener, Toolbar.OnMenuItemClickListener {

    public static final String PARAM_DEFS = "param_defs";

    private Defs defs;
    private DefsFragment defsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(getString(R.string.user_defs));
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.defs);
        toolbar.setOnMenuItemClickListener(this);

        defs = ((ParcelableBinder<Defs>)getIntent().getParcelableExtra(PARAM_DEFS)).getObj();
        defsFragment = DefsFragment.newInstance(defs.lines);
        getSupportFragmentManager().beginTransaction().replace(R.id.defs_fragment,
                defsFragment).commit();
    }

    @Override
    public void onFragmentInteraction(String def) {
        Toast.makeText(DefsActivity.this, def, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.clear_defs:
                defs.clear();
                defs.save();
                defsFragment.getListView().invalidateViews();
                break;

            default:
                return false;
        }
        return true;
    }
}