/*
    arXiv Droid - a Free arXiv app for android
    http://launchpad.net/arxivdroid

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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SubarXiv extends Activity implements
        AdapterView.OnItemClickListener {

    //UI-Views
    private TextView headerTextView;
    public ListView list;
    
    private String name;
    private String[] items;
    private String[] urls;
    private String[] shortItems;

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        arXivDB droidDB = new arXivDB(this);

        String tempquery = "search_query=cat:" + urls[info.position];
        if (info.position == 0) {
            tempquery = tempquery + "*";
        }
        String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                + "&sortBy=submittedDate&sortOrder=ascending";
        droidDB.insertFeed(shortItems[info.position],
                tempquery, tempurl);

        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submain);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        urls = myIntent.getStringArrayExtra("keyurls");
        items = myIntent.getStringArrayExtra("keyitems");
        shortItems = myIntent.getStringArrayExtra("keyshortitems");

        headerTextView = (TextView) findViewById(R.id.theadersm);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        headerTextView.setTypeface(face);

        list = (ListView) findViewById(R.id.listsm);

        headerTextView.setText(name);

        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));

        list.setOnItemClickListener(this);
        registerForContextMenu(list);
    }

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        menu.add(0, 1000, 0, R.string.add_favorites);
    }

    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

        Intent myIntent = new Intent(this, SearchListWindow.class);
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
    }

}
