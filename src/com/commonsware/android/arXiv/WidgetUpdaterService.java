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

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class WidgetUpdaterService extends Service {
    private final IBinder binder = new LocalBinder();
    private final FeedUpdater feedUpdater = new FeedUpdater(new Handler(), this);

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(feedUpdater);
    }

    public void handleCommand() {
        getContentResolver().registerContentObserver(Feeds.CONTENT_URI, true, feedUpdater);
    }

    public ContentObserver getFeedUpdater() {
        return feedUpdater;
    }

    public class LocalBinder extends Binder {
        WidgetUpdaterService getService() {
            return WidgetUpdaterService.this;
        }
    }

    private class FeedUpdater extends ContentObserver {
        private Context context;

        public FeedUpdater(Handler handler, Context context) {
            super(handler);
            this.context = context;
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
            AppWidgetManager a = AppWidgetManager.getInstance(context);
            if (a == null)
                return;
            int[] ids = a.getAppWidgetIds(new ComponentName(context, ArxivAppWidgetProvider.class));
            if (ids.length == 0)
                return;
            Intent intent = new Intent(context, ArxivAppWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            intent.putExtra("fromObserver", true);
            context.sendBroadcast(intent);
        }
    }
}
