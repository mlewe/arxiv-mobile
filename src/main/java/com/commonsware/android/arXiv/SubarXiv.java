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


import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SubarXiv extends ActionBarActivity {
    //UI-Views
    private String name;
    private String[] items;
    private String[] urls;
    private String[] shortItems;
    private int mySourcePref = 0;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mySourcePref = prefs.getInt("source", 0);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(name);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, new SubarXivFragment());
        ft.commit();
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

    public static class SubarXivFragment extends ListFragment {
        private String name;
        private String[] items;
        private String[] urls;
        private String[] shortItems;
        private int mySourcePref = 0;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SubarXiv activity = (SubarXiv) getActivity();
            name = activity.name;
            items = activity.items;
            urls = activity.urls;
            shortItems = activity.shortItems;
            mySourcePref = activity.mySourcePref;

            setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items));
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if (info == null) return false;
            ContentValues cv = new ContentValues();
            int id;
            if (mySourcePref == 0) {
                String tempquery = "search_query=cat:" + urls[info.position] + ((info.position == 0) ? "*" : "");
                String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                        + "&sortBy=submittedDate&sortOrder=ascending";
                cv.put(Feeds.TITLE, shortItems[info.position]);
                cv.put(Feeds.SHORTTITLE, tempquery);
                cv.put(Feeds.URL, tempurl);
                cv.put(Feeds.UNREAD, -1);
                cv.put(Feeds.COUNT, -1);
                id = R.string.added_to_favorites;
            } else {
                cv.put(Feeds.TITLE, shortItems[info.position] + " (RSS)");
                cv.put(Feeds.SHORTTITLE, shortItems[info.position]);
                cv.put(Feeds.URL, urls[info.position]);
                cv.put(Feeds.UNREAD, -2);
                cv.put(Feeds.COUNT, -2);
                id = R.string.added_to_favorites_rss;
            }
            cv.put(Feeds.LAST_UPDATE, 0);
            new AsyncQueryHandler(getActivity().getContentResolver()) {
                @Override
                protected void onInsertComplete(int id, Object cookie, Uri uri) {
                    Toast.makeText(getActivity(), id, Toast.LENGTH_SHORT).show();
                }
            }.startInsert(id, null, Feeds.CONTENT_URI, cv);
            return true;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.add(0, 1000, 0, R.string.add_favorites);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            if (mySourcePref == 0) {
                Intent myIntent = new Intent(getActivity(), ArticleList.class);
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
                Intent myIntent = new Intent(getActivity(), RSSListWindow.class);
                myIntent.putExtra("keyname", shortItems[position]);
                myIntent.putExtra("keyurl", urls[position]);
                startActivity(myIntent);
            }
        }
    }
}
