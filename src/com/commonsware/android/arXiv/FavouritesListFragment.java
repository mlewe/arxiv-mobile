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

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;

public class FavouritesListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.favoritesrow, null,
                new String[]{Feeds.TITLE, Feeds.UNREAD},
                new int[]{R.id.text2, R.id.text1}, 0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.text1) {
                    if (cursor.getColumnIndex(Feeds.UNREAD) == columnIndex) {
                        ((TextView) view).setText(Feeds.formatUnread(cursor.getInt(columnIndex)));
                        return true;
                    }
                }
                return false;
            }
        });
        getLoaderManager().restartLoader(0, null, this);
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return false;
        final Uri feedUri = ContentUris.withAppendedId(Feeds.CONTENT_URI, info.id);
        new Thread() {
            @Override
            public void run() {
                getActivity().getContentResolver().delete(feedUri, null, null);
            }
        }.start();
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.remove_favorites);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = adapter.getCursor();
        c.moveToPosition(position);
        String shortTitle = c.getString(c.getColumnIndex(Feeds.SHORTTITLE));
        String title = c.getString(c.getColumnIndex(Feeds.TITLE));
        String url = c.getString(c.getColumnIndex(Feeds.URL));
        if (url.contains("query")) {
            Intent intent = new Intent(getActivity(), ArticleList.class);
            intent.putExtra("keyquery", shortTitle);
            intent.putExtra("keyname", title);
            intent.putExtra("keyurl", url);
            intent.putExtra("favorite", true);
            intent.putExtra("feedId", id);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), RSSListWindow.class);
            intent.putExtra("keyname", shortTitle);
            intent.putExtra("keyurl", url);
            startActivity(intent);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (getListAdapter() == null)
            setListAdapter(adapter);
        adapter.changeCursor(cursor);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), Feeds.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }
}