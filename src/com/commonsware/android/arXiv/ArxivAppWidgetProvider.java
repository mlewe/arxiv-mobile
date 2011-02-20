/*
    arXiv Droid - a Free arXiv app for android
    http://launchpad.net/arxivdroid

    Copyright (C) 2010 Jack Deslippe

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

import java.net.URL;
import android.os.SystemClock;
import android.net.Uri;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Intent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.app.PendingIntent;
import android.widget.RemoteViews;
import android.graphics.Typeface;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Method;

public class ArxivAppWidgetProvider extends AppWidgetProvider {

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
     int.class};
    private static final Class[] mAddViewSignature = new Class[] {
     int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, arXiv.class);
            String typestring = "widget";
            intent.putExtra("keywidget",typestring);
            intent.setData((Uri.parse("foobar://"+SystemClock.elapsedRealtime())));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget);
            views.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);

            try {
                mRemoveAllViews = RemoteViews.class.getMethod("removeAllViews",
                 mRemoveAllViewsSignature);
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout);
                mRemoveAllViews.invoke(views, mRemoveAllViewsArgs);
                //views.removeAllViews(R.id.mainlayout);
            } catch (Exception ef) {
            }

            arXivDB droidDB = new arXivDB(context);
            List<Feed> favorites = droidDB.getFeeds();
            droidDB.close();

            String favText = "";

            Log.d("arXiv","Updating widget - size "+favorites.size());

            int iCounter = 0;
            if (favorites.size() > 0) {
                for (Feed feed : favorites) {
                    if (feed.url.contains("query")) {
                        iCounter++;
                    }
                }
            }

            if (iCounter > 0) {

                for (Feed feed : favorites) {

                    if (feed.url.contains("query")) {

                        String urlAddress = "http://export.arxiv.org/api/query?" + feed.shortTitle
                            + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1";

                        int numberOfTotalResults = 0;
                        try {
                            URL url = new URL(urlAddress);
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();
                            XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                            xr.setContentHandler(myXMLHandler);
                            xr.parse(new InputSource(url.openStream()));
                            numberOfTotalResults = myXMLHandler.numTotalItems;
                        } catch (Exception ef) {
                        }

                        RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
                        favText = feed.title;
                        if (feed.count > -1) {
                            int newArticles = numberOfTotalResults-feed.count;
                            if (newArticles >= 0) {
                                tempViews.setTextViewText(R.id.number, ""+newArticles);
                            } else {
                                tempViews.setTextViewText(R.id.number, "0");
                            }
                            //if (feed.title.contains("ondensed")) {
                            //    tempViews.setTextViewText(R.id.number, ""+200);
                            //}
                        } else {
                            tempViews.setTextViewText(R.id.number, "0");
                        }
                        tempViews.setTextViewText(R.id.favtext, favText);

                        try {
                            mAddView = RemoteViews.class.getMethod("addView",
                             mAddViewSignature);
                            mAddViewArgs[0] = Integer.valueOf(R.id.mainlayout);
                            mAddViewArgs[1] = tempViews;
                            mAddView.invoke(views, mAddViewArgs);
                            //views.addView(R.id.mainlayout, tempViews);
                        } catch (Exception ef) {
                            views.setTextViewText(R.id.subheading,"Widget only supported on Android 2.1+");
                        }

                    }
                }
            } else {
                RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget_item);
                favText = "No favorite categories or searches set, or incompatible source preference set in all favorites.";
                tempViews.setTextViewText(R.id.number, "-");
                tempViews.setTextViewText(R.id.favtext, favText);
                try {
                    mAddView = RemoteViews.class.getMethod("addView",
                     mAddViewSignature);
                    mAddViewArgs[0] = Integer.valueOf(R.id.mainlayout);
                    mAddViewArgs[1] = tempViews;
                    mAddView.invoke(views, mAddViewArgs);
                    //views.addView(R.id.mainlayout, tempViews);
                } catch (Exception ef) {
                    views.setTextViewText(R.id.subheading,"Widget only supported on Android 2.1+");
                }
            }

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
