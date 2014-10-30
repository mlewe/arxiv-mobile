/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe
    Copyright (C) 2013 Marius Lewerenz

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package com.commonsware.android.arXiv;

import android.app.DownloadManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

public class DownloadsActivity extends ActionBarActivity {
    public static final int CLEAR_ID = Menu.FIRST + 1;
    private DownloadsFragment downloadsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout fl = new FrameLayout(this);
        fl.setId(android.R.id.content);
        setContentView(fl);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("History");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        downloadsFragment = new DownloadsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(android.R.id.content, downloadsFragment);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, CLEAR_ID, Menu.NONE, R.string.clear_history);
    }

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case CLEAR_ID:
                deleteFiles();
                return (true);
        }
        return (false);
    }

    private void deleteFiles() {
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor c = dm.query(new DownloadManager.Query());
        if (c != null) {
            long[] ids = new long[c.getCount()];
            int position = 0;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ids[position] = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
                c.moveToNext();
                position++;
            }
            dm.remove(ids);
        }
        Toast.makeText(DownloadsActivity.this, R.string.deleted_history, Toast.LENGTH_SHORT).show();
        finish();
    }
}
