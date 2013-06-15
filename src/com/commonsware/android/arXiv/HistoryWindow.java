/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe

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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryWindow extends SherlockListActivity {

    public static final int CLEAR_ID = Menu.FIRST + 1;
    //UI-Views
    public ListView list;
    private List<History> historys;
    private arXivDB droidDB;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("History");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();
        droidDB.close();

        List<String> lhistory = new ArrayList<String>();
        for (History history : historys) {
            lhistory.add(history.displayText);
        }

        setListAdapter(new ArrayAdapter<String>(this, R.layout.item,
                R.id.label, lhistory));
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String filename = "";

        int icount = 0;
        for (History history : historys) {
            if (icount == position) {
                filename = history.url;
            }
            icount++;
        }

        File file = new File(filename);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }

        startActivity(intent);
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
        File dir = new File("/sdcard/arXiv");

        String[] children = dir.list();
        if (children != null) {
            for (String filename : children) {
                File f = new File("/sdcard/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        File dir2 = new File("/emmc/arXiv");
        String[] children2 = dir2.list();
        if (children2 != null) {
            for (String filename : children2) {
                File f = new File("/emmc/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        dir2 = new File("/media/arXiv");
        children2 = dir2.list();
        if (children2 != null) {
            for (String filename : children2) {
                File f = new File("/media/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();

        for (History history : historys) {
            droidDB.deleteHistory(history.historyId);
        }
        droidDB.close();

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();
        droidDB.close();

        List<String> lhistory = new ArrayList<String>();
        for (History history : historys) {
            lhistory.add(history.displayText);
        }

        setListAdapter(new ArrayAdapter<String>(this, R.layout.item,
                R.id.label, lhistory));

        Toast.makeText(this, R.string.deleted_history, Toast.LENGTH_SHORT).show();
    }

}
