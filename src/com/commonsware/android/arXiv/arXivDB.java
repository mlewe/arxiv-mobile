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
	private static final String FEEDS_TABLE = "feeds";
	private static final String DATABASE_NAME = "arXiv";
	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	public arXivDB(Context ctx) {

		try {
			db = ctx.openOrCreateDatabase("DATABASE_NAME", ctx.MODE_PRIVATE, null);
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

	public boolean deleteFeed(Long feedId) {
		return (db.delete(FEEDS_TABLE, "feed_id=" + feedId.toString(), null) > 0);
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
}
