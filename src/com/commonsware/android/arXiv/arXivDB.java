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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class arXivDB {

    private static final String CREATE_TABLE_FEEDS = "create table if not exists feeds (_id integer primary key autoincrement, "
            + "title text not null, shorttitle text not null, url text not null, count integer not null, unread integer not null, last_update string not null);";
    private static final String CREATE_TABLE_FONTSIZE = "create table if not exists fontsize (_id integer primary key autoincrement, "
            + "fontsizeval integer not null);";
    private static final String FEEDS_TABLE = "feeds";
    private static final String FONTSIZE_TABLE = "fontsize";
    private static final String DATABASE_NAME = "arXiv-V3";
    //private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;

    // Constructor
    public arXivDB(Context ctx) {

        try {
            db = ctx.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE,
                    null);
            db.execSQL(CREATE_TABLE_FEEDS);
            db.execSQL(CREATE_TABLE_FONTSIZE);
        } catch (Exception e) {
        }

    }

    public boolean changeSize(int size) {
        try {

            Cursor c = db.query(FONTSIZE_TABLE, new String[]{"fontsize_id",
                    "fontsize"}, null, null, null, null, null);
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                Long fontsize_id = c.getLong(0);
                db.delete(FONTSIZE_TABLE, "_id="
                        + fontsize_id.toString(), null);
                c.moveToNext();
            }
            c.close();

        } catch (Exception e) {
        }

        ContentValues values = new ContentValues();
        // values.put("fontsize", size.toString());
        values.put("fontsizeval", size);
        return (db.insert(FONTSIZE_TABLE, null, values) > 0);
    }

    public void close() {
        try {
            db.close();
        } catch (Exception e) {
        }
    }

    public boolean deleteFeed(Long feedId) {
        return (db.delete(FEEDS_TABLE, "_id=" + feedId.toString(), null) > 0);
    }

    public List<Feed> getFeeds() {
        ArrayList<Feed> feeds = new ArrayList<Feed>();
        try {

            Cursor c = db.query(FEEDS_TABLE, new String[]{"_id", "title",
                    "shorttitle", "url", "count", "unread"}, null, null, null, null, null);

            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                Feed feed = new Feed();
                feed.feedId = c.getLong(0);
                feed.title = c.getString(1);
                feed.shortTitle = c.getString(2);
                feed.url = c.getString(3);
                feed.count = c.getInt(4);
                feed.unread = c.getInt(5);
                feeds.add(feed);
                c.moveToNext();
            }
            c.close();

        } catch (Exception e) {
        }
        return feeds;
    }

    public int getSize() {
        int size = 0;
        try {
            Cursor c = db.query(FONTSIZE_TABLE, new String[]{"_id",
                    "fontsizeval"}, null, null, null, null, null);

            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                size = c.getInt(1);
                c.moveToNext();
            }

            c.close();

        } catch (Exception e) {
            Log.e("arXivDB", e.toString());
        }
        return size;
    }

    public boolean insertFeed(String title, String shorttitle, String url, int count, int unread) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("shorttitle", shorttitle);
        values.put("url", url);
        values.put("count", count);
        values.put("unread", unread);
        values.put("last_update", 0);
        return (db.insert(FEEDS_TABLE, null, values) > 0);
    }

    public boolean updateFeed(Long feedId, String title, String shorttitle, String url, int count, int unread) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("shorttitle", shorttitle);
        values.put("url", url);
        values.put("count", count);
        values.put("unread", unread);
        return (db.update(FEEDS_TABLE, values, "_id=" + feedId.toString(), null) > 0);
    }
}
