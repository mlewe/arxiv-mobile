package com.commonsware.android.arXiv;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class EditPreferences extends SherlockPreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Preferences");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

