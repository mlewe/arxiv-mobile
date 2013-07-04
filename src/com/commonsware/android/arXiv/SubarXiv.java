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


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

public class SubarXiv extends SherlockListActivity {
    //UI-Views
    private String name;
    private String[] items;
    private String[] urls;
    private String[] shortItems;
    private int mySourcePref = 0;

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        arXivDB droidDB = new arXivDB(this);

        if (mySourcePref == 0) {
            String tempquery = "search_query=cat:" + urls[info.position];
            if (info.position == 0) {
                tempquery = tempquery + "*";
            }
            String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            droidDB.insertFeed(shortItems[info.position],
                    tempquery, tempurl, -1, -1);
            arXiv.updateWidget(this);
        } else {
            String tempquery = urls[info.position];
            String tempurl = tempquery;
            droidDB.insertFeed(shortItems[info.position] + " (RSS)", shortItems[info.position], tempurl, -2, -2);
            Toast.makeText(this, R.string.added_to_favorites_rss,
                    Toast.LENGTH_SHORT).show();
        }
        droidDB.close();

        return true;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submain);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        urls = myIntent.getStringArrayExtra("keyurls");
        items = myIntent.getStringArrayExtra("keyitems");
        shortItems = myIntent.getStringArrayExtra("keyshortitems");

        ActionBar ab = getSupportActionBar();
        ab.setTitle(name);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
        registerForContextMenu(getListView());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mySourcePref = Integer.parseInt(prefs.getString("sourcelist", "0"));

    }

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenuInfo menuInfo) {
        menu.add(0, 1000, 0, R.string.add_favorites);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        if (mySourcePref == 0) {
            Intent myIntent = new Intent(this, ArticleList.class);
            myIntent.putExtra("keyname", shortItems[position]);
            String tempquery = "search_query=cat:" + urls[position];
            if (position == 0) {
                tempquery = tempquery + "*";
            }
            myIntent.putExtra("keyquery", tempquery);
            String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            myIntent.putExtra("keyurl", tempurl);
            startActivity(myIntent);
        } else {
            Intent myIntent = new Intent(this, RSSListWindow.class);
            myIntent.putExtra("keyname", shortItems[position]);
            myIntent.putExtra("keyurl", urls[position]);
            startActivity(myIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
