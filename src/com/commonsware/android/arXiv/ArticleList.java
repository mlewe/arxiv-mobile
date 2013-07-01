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
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ArticleList extends SherlockFragmentActivity {
    private ArticleListFragment articleListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout fl = new FrameLayout(this);
        fl.setId(android.R.id.content);
        setContentView(fl);

        Intent myIntent = getIntent();
        String name = myIntent.getStringExtra("keyname");
        String query = myIntent.getStringExtra("keyquery");
        String url = myIntent.getStringExtra("keyurl");

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return articleListFragment.getContent();
    }

    public static class Item {
        public String title, updatedDate, publishedDate, category, authors, link, description, text2;
    }
}