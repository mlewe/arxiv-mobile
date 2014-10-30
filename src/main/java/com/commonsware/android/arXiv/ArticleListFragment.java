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
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleListFragment extends ListFragment
        implements arXivLoader.arXivLoaderCallbacks, AbsListView.OnScrollListener {
    private int firstResult = 1, resultsPerLoad = 30;
    private int currentFirstVisibleItem, currentVisibleItemCount, currentScrollState, totalCount;
    private long feedId;
    private int fontSize = 16;
    private String name, url, query, sortBy;
    private ArrayAdapter<ArticleList.Item> adapter;
    private ArticleList.Item[] content;
    private View footer;
    private Boolean error = false;
    private View errorStrip;
    private TextView errorMsg;
    private arXivLoader.arXivLoaderManager loaderManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortBy = prefs.getString("sortBy", "lastUpdatedDate");
        fontSize = prefs.getInt("fontSize", 16);

        footer = getActivity().getLayoutInflater().inflate(R.layout.activity_circle, null);
        errorStrip = getActivity().getLayoutInflater().inflate(R.layout.error_strip, null);
        View icon = errorStrip.findViewById(R.id.refresh);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getListView().addFooterView(footer);
                getListView().removeFooterView(errorStrip);
                loaderManager.restartLoader(0, ArticleListFragment.this);
            }
        });
        errorMsg = (TextView) errorStrip.findViewById(R.id.errorMsg);

        getListView().addFooterView(footer);
        getListView().setOnScrollListener(this);

        Intent intent = getActivity().getIntent();
        name = intent.getStringExtra("keyname");
        query = intent.getStringExtra("keyquery");
        url = intent.getStringExtra("keyurl");
        feedId = intent.getLongExtra("feedId", -1);

        loaderManager = new arXivLoader.arXivLoaderManager(getLoaderManager());

        Object o = getActivity().getLastCustomNonConfigurationInstance();
        if (o != null && o instanceof ArticleList.Item[]) {
            adapter = new ArticleAdapter((ArticleList.Item[]) o);
            setListAdapter(adapter);
            getListView().removeFooterView(footer);
            firstResult = adapter.getCount() + 1;
            totalCount = savedInstanceState.getInt("totalCount");
            error = savedInstanceState.getBoolean("error");
            if (error) {
                errorMsg.setText(savedInstanceState.getCharSequence("errorMsg"));
                getListView().addFooterView(errorStrip);
            }
        } else {
            adapter = new ArticleAdapter();
            loaderManager.initLoader(0, this);
        }
    }

    @Override
    public arXivLoader onCreateLoader(int i) {
        String urlAddress = "http://export.arxiv.org/api/query?" + query
                + "&sortBy=" + sortBy + "&sortOrder=descending&start="
                + (firstResult - 1) + "&max_results=" + resultsPerLoad;
        return new APILoader(getActivity(), urlAddress, query, firstResult);
    }

    @Override
    public void onLoaderReset(arXivLoader itemLoader) {

    }

    @Override
    public void onLoadFinished(arXivLoader itemLoader, List<ArticleList.Item> list) {
        for (ArticleList.Item item : list)
            adapter.add(item);
        if (getListAdapter() != adapter)
            setListAdapter(adapter);
        firstResult = adapter.getCount() + 1;
        getListView().removeFooterView(footer);
        error = itemLoader.hasError();
        if (error) {
            errorMsg.setText(itemLoader.getErrorMsg());
            getListView().addFooterView(errorStrip);
        } else {
            totalCount = itemLoader.getTotalCount();
            if (feedId != -1) {
                final ContentValues cv = new ContentValues();
                cv.put(Feeds.UNREAD, 0);
                cv.put(Feeds.COUNT, totalCount);
                cv.put(Feeds.LAST_UPDATE, System.currentTimeMillis());
                final Uri feedUri = ContentUris.withAppendedId(Feeds.CONTENT_URI, feedId);
                new Thread() {
                    @Override
                    public void run() {
                        getActivity().getContentResolver().update(feedUri, cv, null, null);
                        getActivity().getContentResolver().notifyChange(feedUri, null);
                    }
                }.start();
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (position >= adapter.getCount()) return;
        ArticleList.Item item = adapter.getItem(position);
        Intent myIntent = new Intent(getActivity(), SingleItemWindow.class);
        myIntent.putExtra("keytitle", item.title);
        myIntent.putExtra("keylink", item.link);
        myIntent.putExtra("keydescription", item.description);
        myIntent.putExtra("keycreator", item.authors);
        myIntent.putExtra("keyname", name);
        startActivity(myIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("totalCount", totalCount);
        if (error) {
            outState.putBoolean("error", error);
            outState.putCharSequence("errorMsg", errorMsg.getText());
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        this.isScrollCompleted();
    }

    private void isScrollCompleted() {
        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE
                && currentFirstVisibleItem + currentVisibleItemCount == firstResult - 1
                && getListView().getFooterViewsCount() == 0) {
            if (firstResult < totalCount) {
                getListView().addFooterView(footer);
                loaderManager.restartLoader(0, this);
            }
        }
    }

    public ArticleList.Item[] getContent() {
        content = new ArticleList.Item[adapter.getCount()];
        for (int i = 0; i < adapter.getCount(); i++) {
            content[i] = adapter.getItem(i);
        }
        return content;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int size) {
        fontSize = size;
        getListView().invalidateViews();
    }

    private class ArticleAdapter extends ArrayAdapter<ArticleList.Item> {
        private ArticleAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_1);
        }

        private ArticleAdapter(ArticleList.Item[] objects) {
            super(getActivity(), android.R.layout.simple_list_item_1,
                    new ArrayList<ArticleList.Item>(Arrays.asList(objects)));
            // We need to create a new List here, to be able to use add().
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.searchrow, parent, false);
                holder = new ViewHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.linLay = (LinearLayout) row.findViewById(R.id.linlay);
                row.setTag(holder);
            } else
                holder = (ViewHolder) row.getTag();
            holder.text1.setText(getItem(position).title);
            holder.text1.setTextSize(fontSize);
            holder.text2.setText(getItem(position).text2);
            holder.text2.setTextSize(fontSize - 2);
            holder.linLay.setBackgroundResource((position % 2 == 0) ? R.color.back2 : R.color.back4);
            return row;
        }

        class ViewHolder {
            public TextView text1, text2;
            public LinearLayout linLay;
        }
    }
}
