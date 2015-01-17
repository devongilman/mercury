package it.skarafaz.mercury.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import it.skarafaz.mercury.R;
import it.skarafaz.mercury.adapter.ServerPagerAdapter;
import it.skarafaz.mercury.data.LoadConfigTaskResult;
import it.skarafaz.mercury.manager.ConfigManager;


public class MainActivity extends ActionBarActivity {
    private ProgressBar progress;
    private LinearLayout empty;
    private TextView message;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_main);
        progress = (ProgressBar) findViewById(R.id.progress);
        empty = (LinearLayout) findViewById(R.id.empty);
        message = (TextView) findViewById(R.id.message);
        pager = (ViewPager) findViewById(R.id.pager);
        reload();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:
                reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reload() {
        new AsyncTask<Void, Void, LoadConfigTaskResult>() {
            @Override
            protected void onPreExecute() {
                progress.setVisibility(View.VISIBLE);
                empty.setVisibility(View.INVISIBLE);
                pager.setVisibility(View.INVISIBLE);
            }

            @Override
            protected LoadConfigTaskResult doInBackground(Void... params) {
                return ConfigManager.getInstance().load();
            }

            @Override
            protected void onPostExecute(LoadConfigTaskResult result) {
                if (ConfigManager.getInstance().getServers().size() > 0) {
                    ServerPagerAdapter adapter = new ServerPagerAdapter(getSupportFragmentManager(), ConfigManager.getInstance().getServers());
                    pager.setAdapter(adapter);
                    pager.setVisibility(View.VISIBLE);
                    if (result == LoadConfigTaskResult.ERRORS_FOUND) {
                        Toast.makeText(MainActivity.this, getString(R.string.errors_found), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    message.setText(getEmptyMessage(result));
                    empty.setVisibility(View.VISIBLE);
                }
                progress.setVisibility(View.INVISIBLE);
            }
        }.execute();
    }

    private String getEmptyMessage(LoadConfigTaskResult result) {
        String message = "";
        switch (result) {
            case SUCCESS:
                message = String.format(getString(R.string.empty_config_dir), ConfigManager.getInstance().getConfigDir());
                break;
            case ERRORS_FOUND:
                message = getString(R.string.errors_found);
                break;
            case CANNOT_READ_EXT_STORAGE:
                message = getString(R.string.cannot_read_ext_storage);
                break;
            case CANNOT_CREATE_CONFIG_DIR:
                message = String.format(getString(R.string.cannot_create_config_dir), ConfigManager.getInstance().getConfigDir());
                break;
        }
        return message;
    }
}
