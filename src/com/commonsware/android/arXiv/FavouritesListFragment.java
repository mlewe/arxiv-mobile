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
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;

import java.util.List;

public class FavouritesListFragment extends SherlockListFragment {

    private ArrayAdapter<Feed> adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ArrayAdapter<Feed>(getActivity(), R.layout.favoritesrow) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView unread = null, title = null;
                View newView = null;
                Feed entry = getItem(position);
                if (convertView != null) {
                    unread = (TextView) convertView.findViewById(R.id.text1);
                    title = (TextView) convertView.findViewById(R.id.text2);
                    if (unread != null && title != null) {
                        newView = convertView;
                    }
                }
                if (newView == null) {
                    newView = getActivity().getLayoutInflater().inflate(R.layout.favoritesrow, parent, false);
                    if (newView == null)
                        return null; // We are beyond any hope here.
                    unread = (TextView) newView.findViewById(R.id.text1);
                    title = (TextView) newView.findViewById(R.id.text2);
                }
                unread.setText(entry.formatUnread());
                title.setText(entry.title);
                return newView;
            }
        };
        setListAdapter(adapter);
        updateFavList();
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if (info == null) return false;
            Feed feed = adapter.getItem(info.position);

            Log.d("Arx", "Opening Database 2");
            arXivDB droidDB = new arXivDB(getActivity());
            droidDB.deleteFeed(feed.feedId);
            adapter.remove(feed);
            droidDB.close();
            Log.d("Arx", "Closed Database 2");

            Thread thread = new Thread() {
                public void run() {
                    try {
                        arXiv a = (arXiv) getActivity();
                        a.updateWidget();
                    } catch (Exception ignored) {

                    }
                }
            };
            thread.start();

        } catch (Exception ignored) {

        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.remove_favorites);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Feed feed = (Feed) getListAdapter().getItem(position);
        if (feed.url.contains("query")) {
            Intent myIntent = new Intent(getActivity(), ArticleList.class);
            myIntent.putExtra("keyquery", feed.shortTitle);
            myIntent.putExtra("keyname", feed.title);
            myIntent.putExtra("keyurl", feed.url);
            startActivity(myIntent);
        } else {
            Intent myIntent = new Intent(getActivity(), RSSListWindow.class);
            myIntent.putExtra("keyname", feed.shortTitle);
            myIntent.putExtra("keyurl", feed.url);
            startActivity(myIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFavList();
    }

    void updateFavList() {
        Log.d("Arx", "Opening Database foo");
        arXivDB droidDB = new arXivDB(getActivity());
        List<Feed> favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx", "Closed Database foo");

        adapter.clear();
        for (Feed entry : favorites) {
            adapter.add(entry);
        }
    }
}