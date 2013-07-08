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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Method;
import java.util.List;

public class ArxivAppWidgetProvider extends AppWidgetProvider {

    private static final Class[] mRemoveAllViewsSignature = new Class[]{
            int.class};
    private static final Class[] mAddViewSignature = new Class[]{
            int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];
    private RemoteViews views;
    private List<Feed> favorites;
    private FeedUpdater feedUpdater;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final AppWidgetManager myAppWidgetManager = appWidgetManager;
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, arXiv.class);
        String typestring = "widget";
        intent.putExtra("keywidget", typestring);
        intent.setData((Uri.parse("foobar://" + SystemClock.elapsedRealtime())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener to the button
        views = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget);
        views.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);

        try {
            mRemoveAllViews = RemoteViews.class.getMethod("removeAllViews",
                    mRemoveAllViewsSignature);
            mRemoveAllViewsArgs[0] = R.id.mainlayout;
            mRemoveAllViews.invoke(views, mRemoveAllViewsArgs);
            //views.removeAllViews(R.id.mainlayout);
        } catch (Exception ef) {
        }

        arXivDB droidDB = new arXivDB(context);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("arXiv", "Updating widget - size " + favorites.size());

        int count = 0;
        if (favorites.size() > 0) {
            for (Feed feed : favorites) {
                if (feed.url.contains("query")) {
                    count++;
                }
            }
        }
        if (count > 0) {
            for (Feed feed : favorites) {
                if (feed.url.contains("query")) {
                    RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
                    Log.d("arXiv", "Updating widget " + feed.shortTitle + " " + feed.count + " " + feed.unread);
                    tempViews.setTextViewText(R.id.number, feed.formatUnread());
                    tempViews.setTextViewText(R.id.favtext, feed.title);
                    try {
                        mAddView = RemoteViews.class.getMethod("addView",
                                mAddViewSignature);
                        mAddViewArgs[0] = R.id.mainlayout;
                        mAddViewArgs[1] = tempViews;
                        mAddView.invoke(views, mAddViewArgs);
                        //views.addView(R.id.mainlayout, tempViews);
                    } catch (Exception ef) {
                        views.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+");
                    }

                }
            }

        } else {
            RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
            tempViews.setTextViewText(R.id.number, "-");
            tempViews.setTextViewText(R.id.favtext,
                    "No favorite categories or searches set, or incompatible source preference set in all favorites.");
            try {
                mAddView = RemoteViews.class.getMethod("addView",
                        mAddViewSignature);
                mAddViewArgs[0] = R.id.mainlayout;
                mAddViewArgs[1] = tempViews;
                mAddView.invoke(views, mAddViewArgs);
                //views.addView(R.id.mainlayout, tempViews);
            } catch (Exception ef) {
                views.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+");
            }
        }
        // Perform this loop procedure for each App Widget that belongs to this provider
        // Tell the AppWidgetManager to perform an update on the current App Widget
        for (final int appWidgetId : appWidgetIds) {
            myAppWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        ContentResolver cr = context.getContentResolver();
        feedUpdater = new FeedUpdater(new Handler(), context);
        cr.registerContentObserver(Feeds.CONTENT_URI, true, feedUpdater);
    }

    @Override
    public void onDisabled(Context context) {
        ContentResolver cr = context.getContentResolver();
        try {
            cr.unregisterContentObserver(feedUpdater);
        } catch (IllegalStateException ignored) {
            // Do Nothing.  Observer has already been unregistered.
        }
    }

    private class FeedUpdater extends ContentObserver {
        private Context context;

        public FeedUpdater(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            AppWidgetManager a = AppWidgetManager.getInstance(context);
            if (a == null)
                return;
            int[] ids = a.getAppWidgetIds(new ComponentName(context, ArxivAppWidgetProvider.class));
            if (ids.length == 0)
                return;
            Intent intent = new Intent(context, ArxivAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }
    }
}
