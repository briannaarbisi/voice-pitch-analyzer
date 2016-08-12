package de.lilithwittmann.voicepitchanalyzer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import de.lilithwittmann.voicepitchanalyzer.R;

public class SettingsActivity extends AppCompatActivity
{
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                Log.i(LOG_TAG, "back pressed");
                this.onBackPressed();
            }

            case R.id.action_record:
            {
                startActivity(new Intent(this, RecordingActivity.class));
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void saveSettings() {
        SharedPreferences prefs =
                getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Switch webToggle = (Switch) findViewById(R.id.webPageSwitch);
        prefs.edit().putBoolean(getString(R.string.settings_use_web_page), webToggle.isChecked()).apply();

        EditText webText = (EditText) findViewById(R.id.editWebPageText);
        String url = webText.getText().toString();
        String lastSavedUrl = prefs.getString(getString(R.string.settings_web_url), getString(R.string.default_web_url));

        // clear stored web view scroll position if url is changed
        if (!lastSavedUrl.equalsIgnoreCase(url))
            prefs.edit().putString(getString(R.string.web_scroll_position), "0,0").apply();

        prefs.edit().putString(getString(R.string.settings_web_url), url).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        saveSettings();
    }
}
