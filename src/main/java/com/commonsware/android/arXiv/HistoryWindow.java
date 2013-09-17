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

import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;

public class HistoryWindow extends SherlockFragmentActivity {
    public static final int CLEAR_ID = Menu.FIRST + 1;
    private HistoryListFragment historyListFragment;

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

        historyListFragment = new HistoryListFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(android.R.id.content, historyListFragment);
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

        new AsyncQueryHandler(this.getContentResolver()) {
            @Override
            protected void onInsertComplete(int id, Object cookie, Uri uri) {
                Toast.makeText(HistoryWindow.this, id, Toast.LENGTH_SHORT).show();
            }
        }.startDelete(R.string.deleted_history, null, History.CONTENT_URI, null, null);
    }

    private class HistoryListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private SimpleCursorAdapter adapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new SimpleCursorAdapter(getActivity(), R.layout.item, null,
                    new String[]{History.DISPLAYTEXT}, new int[]{R.id.label}, 0);
            setListAdapter(adapter);
            getLoaderManager().restartLoader(0, null, this);
        }

        @Override
        public void onListItemClick(ListView parent, View v, int position, long id) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);

            Cursor cursor = adapter.getCursor();
            cursor.moveToPosition(position);
            File file = new File(cursor.getString(cursor.getColumnIndex(History.URL)));
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.install_reader, Toast.LENGTH_SHORT).show();
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
            return new CursorLoader(getActivity(), History.CONTENT_URI, null, null, null, null);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            adapter.changeCursor(null);
        }
    }
}
