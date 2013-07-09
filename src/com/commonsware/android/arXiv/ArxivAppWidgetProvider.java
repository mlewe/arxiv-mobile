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
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Method;

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

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Cursor cursor = context.getContentResolver()
                .query(Feeds.CONTENT_URI, new String[]{Feeds.TITLE, Feeds.UNREAD},
                        Feeds.TITLE + " not like '%RSS%'", null, null);
        int count = cursor.getCount();
        Log.d("arXiv", "Updating widget - size " + count);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (final int appWidgetId : appWidgetIds) {
            // Get the layout for the App Widget and attach an on-click listener
            Intent intent = new Intent(context, arXiv.class);
            intent.putExtra("widget", "widget");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
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
            if (count > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
                    String title = cursor.getString(cursor.getColumnIndex(Feeds.TITLE));
                    int unread = cursor.getInt(cursor.getColumnIndex(Feeds.UNREAD));
                    Log.d("arXiv", "Updating widget " + title + " " + unread);
                    tempViews.setTextViewText(R.id.number, Feeds.formatUnread(unread));
                    tempViews.setTextViewText(R.id.favtext, title);
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
                    cursor.moveToNext();
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
            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        cursor.close();
    }

    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, WidgetUpdaterService.class));
    }

    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, WidgetUpdaterService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // if we're not receiving an Update or get an Update from our own ContentObserver progress normally.
        if (!AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())
                || intent.getBooleanExtra("fromObserver", false))
            super.onReceive(context, intent);
        else { // else fire a change notification, so the unread count gets updated.
            context.startService(new Intent(context, WidgetUpdaterService.class));
            IBinder iBinder = peekService(context, new Intent(context, WidgetUpdaterService.class));
            ContentObserver observer = null;
            if (iBinder != null)
                observer = ((WidgetUpdaterService.LocalBinder) iBinder).getService().getFeedUpdater();
            context.getContentResolver().notifyChange(Feeds.CONTENT_URI, observer);
        }
    }
}
