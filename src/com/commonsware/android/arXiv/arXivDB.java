package com.commonsware.android.arXiv;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.IOException;

public class arXivDB {

	private static final String CREATE_TABLE_FEEDS = "create table feeds (feed_id integer primary key autoincrement, "
			+ "title text not null, shorttitle text not null, url text not null);";
	private static final String CREATE_TABLE_HISTORY = "create table history (history_id integer primary key autoincrement, "
			+ "displaytext text not null, url text not null);";
	private static final String CREATE_TABLE_FONTSIZE = "create table fontsize (fontsize_id integer primary key autoincrement, "
			+ "fontsizeval integer not null);";
	private static final String FEEDS_TABLE = "feeds";
	private static final String HISTORY_TABLE = "history";
	private static final String FONTSIZE_TABLE = "fontsize";
	private static final String DATABASE_NAME = "arXiv";
	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public arXivDB(Context ctx) {

		try {
			db = ctx.openOrCreateDatabase("DATABASE_NAME", ctx.MODE_PRIVATE, null);
			db.execSQL(CREATE_TABLE_FONTSIZE);
			ContentValues values = new ContentValues();
			values.put("fontsizeval", 14);
			db.insert(FONTSIZE_TABLE, null, values);
			db.execSQL(CREATE_TABLE_HISTORY);
			db.execSQL(CREATE_TABLE_FEEDS);
		} catch ( Exception e) {
		}

		//try {
		//	db = ctx.openDatabase(DATABASE_NAME, null);
		//} catch (FileNotFoundException e) {
		//	try {
		//		db = ctx.createDatabase(DATABASE_NAME, DATABASE_VERSION, 0,null);
		//		db.execSQL(CREATE_TABLE_FEEDS);
		//	} catch (FileNotFoundException e1) {
		//		db = null;
		//	}
		//}
	}

	public boolean insertFeed(String title, String shorttitle, String url) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("shorttitle", shorttitle);
		values.put("url", url);
		return (db.insert(FEEDS_TABLE, null, values) > 0);
	}

	public boolean insertHistory(String displaytext, String url) {
		ContentValues values = new ContentValues();
		values.put("displaytext", displaytext);
		values.put("url", url);
		return (db.insert(HISTORY_TABLE, null, values) > 0);
	}

	public boolean changeSize(int size) {
		try {
			Cursor c = db.query(FONTSIZE_TABLE, new String[] { "fontsize_id", "fontsize"}, null, null, null, null, null);

			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Long fontsize_id = c.getLong(0);
				db.delete(FONTSIZE_TABLE, "fontsize_id=" + fontsize_id.toString(), null);
				c.moveToNext();
			}

		} catch (SQLException e) {
			//Log.e("NewsDroid", e.toString());
		}

		ContentValues values = new ContentValues();
		//values.put("fontsize", size.toString());
		values.put("fontsizeval", size);
		return (db.insert(FONTSIZE_TABLE, null, values) > 0);
	}

	public boolean deleteFeed(Long feedId) {
		return (db.delete(FEEDS_TABLE, "feed_id=" + feedId.toString(), null) > 0);
	}

	public boolean deleteHistory(Long historyId) {
		return (db.delete(HISTORY_TABLE, "history_id=" + historyId.toString(), null) > 0);
	}

	public void close() {
		db.close();
	}

	public List<Feed> getFeeds() {
		ArrayList<Feed> feeds = new ArrayList<Feed>();
		try {

			Cursor c = db.query(FEEDS_TABLE, new String[] { "feed_id", "title", "shorttitle",
					"url" }, null, null, null, null, null);

			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Feed feed = new Feed();
				feed.feedId = c.getLong(0);
				feed.title = c.getString(1);
				feed.shorttitle = c.getString(2);
				feed.url = c.getString(3);
				feeds.add(feed);
				c.moveToNext();
			}

		} catch (SQLException e) {
			//Log.e("NewsDroid", e.toString());
		}

		return feeds;
	}

	public List<History> getHistory() {
		ArrayList<History> historys = new ArrayList<History>();
		try {

			Cursor c = db.query(HISTORY_TABLE, new String[] { "history_id", "displaytext",
					"url" }, null, null, null, null, null);

			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				History history = new History();
				history.historyId = c.getLong(0);
				history.displaytext = c.getString(1);
				history.url = c.getString(2);
				historys.add(history);
				c.moveToNext();
			}

		} catch (SQLException e) {
			//Log.e("NewsDroid", e.toString());
		}

		return historys;
	}

	public int getSize() {
		int size = 0;
		try {
			Cursor c = db.query(FONTSIZE_TABLE, new String[] { "fontsize_id", "fontsizeval"}, null, null, null, null, null);

			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				size = c.getInt(1);
				c.moveToNext();
			}

		} catch (Exception e) {
			Log.e("arXivDB", e.toString());
		}

		return size;
	}


}
