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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ArticleList extends SherlockFragmentActivity {
    public static final int INCREASE_ID = Menu.FIRST + 1;
    public static final int DECREASE_ID = Menu.FIRST + 2;
    public static final int FAVORITE_ID = Menu.FIRST + 3;
    private ArticleListFragment articleListFragment;
    private String name;
    private String query;
    private String url;
    private Boolean favorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout fl = new FrameLayout(this);
        fl.setId(android.R.id.content);
        setContentView(fl);

        Intent intent = getIntent();
        name = intent.getStringExtra("keyname");
        query = intent.getStringExtra("keyquery");
        url = intent.getStringExtra("keyurl");
        favorite = intent.getBooleanExtra("favorite", false);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(name);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            articleListFragment = new ArticleListFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(android.R.id.content, articleListFragment);
            ft.commit();
        } else {
            Fragment f = fm.findFragmentById(android.R.id.content);
            if (f instanceof ArticleListFragment)
                articleListFragment = (ArticleListFragment) f;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
        if (!favorite)
            menu.add(Menu.NONE, FAVORITE_ID, Menu.NONE, "Add to Favorites");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case INCREASE_ID:
                setFontSize(articleListFragment.getFontSize() + 2);
                return true;
            case DECREASE_ID:
                setFontSize(articleListFragment.getFontSize() - 2);
                return true;
            case FAVORITE_ID:
                addFavorite();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFontSize(int size) {
        if (size > 20) size = 20;
        if (size < 12) size = 12;
        articleListFragment.setFontSize(size);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("fontSize", size);
        if (Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return articleListFragment.getContent();
    }

    public void addFavorite() {
        ContentValues cv = new ContentValues();
        cv.put(Feeds.TITLE, name);
        cv.put(Feeds.SHORTTITLE, query);
        cv.put(Feeds.URL, url);
        cv.put(Feeds.UNREAD, -1);
        cv.put(Feeds.COUNT, -1);
        cv.put(Feeds.LAST_UPDATE, 0);
        new AsyncQueryHandler(this.getContentResolver()) {
            @Override
            protected void onInsertComplete(int id, Object cookie, Uri uri) {
                Toast.makeText(getBaseContext(), id, Toast.LENGTH_SHORT).show();
            }
        }.startInsert(R.string.added_to_favorites, null, Feeds.CONTENT_URI, cv);
        favorite = true;
        supportInvalidateOptionsMenu();
    }

    public static class Item {
        public String title, updatedDate, publishedDate, category, authors, link, description, text2;
    }
}