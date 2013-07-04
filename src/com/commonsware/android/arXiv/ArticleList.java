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
            case FAVORITE_ID:
                addFavorite();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return articleListFragment.getContent();
    }

    public void addFavorite() {
        arXivDB droidDB = new arXivDB(this);
        droidDB.insertFeed(name, query, url, -1, -1);
        droidDB.close();
        Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        favorite = true;
        supportInvalidateOptionsMenu();
        arXiv.updateWidget(this);
    }

    public static class Item {
        public String title, updatedDate, publishedDate, category, authors, link, description, text2;
    }
}