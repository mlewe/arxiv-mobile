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

import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.util.Log;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;

public class arXivDBContentProvider extends ContentProvider {

    static final String AUTHORITY = "com.commonsware.android.arXiv.arXivDBContentProvider";
    private static final String DATABASE_NAME = "arXiv-V3";
    private static final String CREATE_TABLE_FEEDS = "create table if not exists feeds (_id integer primary key autoincrement, "
            + "title text not null, shorttitle text not null, url text not null, count integer not null, unread integer not null, last_update integer not null);";
    private static final String CREATE_TABLE_HISTORY = "create table if not exists history (_id integer primary key autoincrement, "
            + "displaytext text not null, url text not null);";
    private static final String CREATE_TABLE_FONTSIZE = "create table if not exists fontsize (_id integer primary key autoincrement, "
            + "fontsizeval integer not null);";
    static final String FEEDS_TABLE = "feeds";
    private static final String HISTORY_TABLE = "history";
    private static final String FONTSIZE_TABLE = "fontsize";
    private static final int FEEDS = 1;
    private static final int FEEDS_ID = 2;
    private static final int HISTORY = 3;
    private static final int HISTORY_ID = 4;
    private static final int DATABASE_VERSION = 4;
    private static final UriMatcher sUriMatcher;
    private DataBaseHelper dbHelper;
    private ContentResolver cr;

    @Override
    synchronized public Cursor query(Uri uri,
                                     String[] projection,
                                     String selection,
                                     String[] selectionArgs,
                                     String sortOrder) {
        String TABLE;
        switch (sUriMatcher.match(uri)) {
            case FEEDS_ID:
                if (selection == null) selection = "";
                selection += Feeds._ID + "=" + uri.getLastPathSegment();
            case FEEDS:
                TABLE = FEEDS_TABLE;
                break;
            case HISTORY_ID:
                if (selection == null) selection = "";
                selection += History._ID + "=" + uri.getLastPathSegment();
            case HISTORY:
                TABLE = HISTORY_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db == null) return null;
        Cursor c = db.query(TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(cr, uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                return Feeds.CONTENT_TYPE;
            case HISTORY:
                return History.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    synchronized public Uri insert(Uri uri,
                                   ContentValues values) {
        String TABLE;
                Uri URI;
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                TABLE = FEEDS_TABLE;
                URI = Feeds.CONTENT_URI;
                break;
            case HISTORY:
                TABLE = HISTORY_TABLE;
                URI = History.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null) return null;
        long rowId = db.insert(TABLE, null, values);
        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(URI, rowId);
            cr.notifyChange(uri, null);
            return newUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    synchronized public int update(Uri uri,
                                   ContentValues values,
                                   String selection,
                                   String[] selectionArgs) {
        String TABLE;
        switch (sUriMatcher.match(uri)) {
            case FEEDS_ID:
                if (selection == null) selection = "";
                selection += Feeds._ID + "=" + uri.getLastPathSegment();
            case FEEDS:
                TABLE = FEEDS_TABLE;
                break;
            case HISTORY_ID:
                if (selection == null) selection = "";
                selection += History._ID + "=" + uri.getLastPathSegment();
            case HISTORY:
                TABLE = HISTORY_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null) return -1;
        int count = db.update(TABLE, values, selection, selectionArgs);
        return count;
    }

    @Override
    synchronized public int delete(Uri uri,
                                   String selection,
                                   String[] selectionArgs) {
        String TABLE;
        switch (sUriMatcher.match(uri)) {
            case FEEDS_ID:
                if (selection == null) selection = "";
                selection += Feeds._ID + "=" + uri.getLastPathSegment();
            case FEEDS:
                TABLE = FEEDS_TABLE;
                break;
            case HISTORY_ID:
                if (selection == null) selection = "";
                selection += History._ID + "=" + uri.getLastPathSegment();
            case HISTORY:
                TABLE = HISTORY_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db == null) return -1;
        int count = db.delete(TABLE, selection, selectionArgs);
        cr.notifyChange(uri, null);
        return count;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context == null) return false;
        dbHelper = new DataBaseHelper(context);
        cr = context.getContentResolver();
        if (cr == null) return false;
        HandlerThread ht = new HandlerThread("foo");
        ht.start();
        cr.registerContentObserver(Feeds.CONTENT_URI, true, new FeedUpdater(new Handler(ht.getLooper()), cr));
        return true;
    }

    private class FeedUpdater extends ContentObserver {
        private ContentResolver cr;
        public FeedUpdater(Handler handler, ContentResolver cr) {
            super(handler);
            this.cr = cr;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Cursor c = cr.query(
                    Feeds.CONTENT_URI,
                    new String[]{Feeds._ID, Feeds.UNREAD, Feeds.COUNT, Feeds.URL, Feeds.SHORTTITLE},
                    Feeds.LAST_UPDATE + "<=" + (System.currentTimeMillis()-1800000),
                    null, null);
            if (c == null) return;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                boolean unreadChanged = false;
                ContentValues cv = new ContentValues();
                String url = c.getString(3);
                String shorttitle =  c.getString(4);
                if (url.contains("query")) {
                    String urlAddress = "http://export.arxiv.org/api/query?" + shorttitle
                            + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1";
                    int unread = 0;
                    try {
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                        xr.setContentHandler(myXMLHandler);
                        xr.parse(new InputSource(new URL(urlAddress).openStream()));
                        unread = myXMLHandler.numTotalItems - c.getInt(2);
                        cv.put(Feeds.UNREAD, unread);
                    } catch (Exception ef) {
                        Log.d("arXiv", "Caught Exception " + ef);
                    }
                    Log.d("Arx", "updated unread entry: " + shorttitle + ": " + unread + " new articles");
                    unreadChanged = (unread > c.getInt(1));
                }
                cv.put(Feeds.LAST_UPDATE, System.currentTimeMillis());
                Uri changeUri = ContentUris.withAppendedId(Feeds.CONTENT_URI, c.getInt(0));
                cr.update(changeUri, cv, null, null);
                if (unreadChanged)
                    cr.notifyChange(changeUri, this);
                c.moveToNext();
            }
            c.close();
        }
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, FEEDS_TABLE, FEEDS);
        sUriMatcher.addURI(AUTHORITY, FEEDS_TABLE + "/#", FEEDS_ID);
        sUriMatcher.addURI(AUTHORITY, HISTORY_TABLE, HISTORY);
        sUriMatcher.addURI(AUTHORITY, HISTORY_TABLE + "/#", HISTORY_ID);
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {
        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_FEEDS);
            db.execSQL(CREATE_TABLE_HISTORY);
            db.execSQL(CREATE_TABLE_FONTSIZE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // just deleting everything here
            db.execSQL("drop table if exists " + FEEDS_TABLE);
            db.execSQL("drop table if exists " + HISTORY_TABLE);
            db.execSQL("drop table if exists " + FONTSIZE_TABLE);
            onCreate(db);
        }
    }

    public static final class History implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + HISTORY_TABLE);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.arXiv."+ HISTORY_TABLE;
        public static final String _ID = "_id";
        public static final String DISPLAYTEXT = "displaytext";
        public static final String URL = "url";

        private History() {
        }
    }

}
