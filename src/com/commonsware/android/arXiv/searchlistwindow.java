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

import java.io.StringReader;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ComponentName;
import android.appwidget.AppWidgetManager;

import android.content.Context;
import android.app.PendingIntent;
import android.widget.RemoteViews;

import android.os.SystemClock;
import android.net.Uri;

import java.lang.reflect.Method;

public class SearchListWindow extends ListActivity {

    public SearchListWindow thisActivity;

    //UI-Views
    private TextView txtInfo;
    private TextView header;
    public ListView list;
    private Button favoriteButton;
    private Button nextButton;
    private Button previousButton;

    private Feed favFeed;
    private String name;
    private String catName;
    private String urlAddress;
    private String urlInput;
    private String query;
    private String[] titles;
    private String[] categories;
    private String[] updatedDates;
    private String[] publishedDates;
    private String[] links;
    private String[] listText;
    private String[] descriptions;
    private String[] creators;
    private int iFirstResultOnPage = 1;
    private int nResultsPerPage = 30;
    private int numberOfResultsOnPage;
    private int numberOfTotalResults;
    private int fontSize;
    private Boolean vCategory;
    private Boolean vFavorite=false;

    private arXivDB droidDB;

    public static final int INCREASE_ID = Menu.FIRST + 1;
    public static final int DECREASE_ID = Menu.FIRST + 2;

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
     int.class};
    private static final Class[] mAddViewSignature = new Class[] {
     int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];

    private Handler handlerSetList = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (fontSize < 12) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item10, R.id.label, listText));
            } else if (fontSize == 12) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item12, R.id.label, listText));
            } else if (fontSize == 14) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item, R.id.label, listText));
            } else if (fontSize == 16) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item16, R.id.label, listText));
            } else if (fontSize == 18) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item18, R.id.label, listText));
            } else if (fontSize == 20) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item20, R.id.label, listText));
            } else if (fontSize > 20) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                        R.layout.item22, R.id.label, listText));
            }
        }
    };

    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBarIndeterminateVisibility(false);
        }
    };

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
        case INCREASE_ID:
            if (fontSize < 22) {
                if (fontSize < 10) {
                    fontSize = 10;
                }
                fontSize = fontSize + 2;
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                handlerSetList.sendEmptyMessage(0);
            }
            return (true);
        case DECREASE_ID:
            if (fontSize > 10) {
                if (fontSize > 22) {
                    fontSize = 22;
                }
                fontSize = fontSize - 2;
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                handlerSetList.sendEmptyMessage(0);
            }
            return (true);
        }
        return (false);
    }

    public void favoritePressed(View button) {
        droidDB = new arXivDB(this);
        droidDB.insertFeed(name, query, urlInput, numberOfTotalResults);
        Toast.makeText(this, R.string.added_to_favorites,
                Toast.LENGTH_SHORT).show();
        droidDB.close();
        Thread t9 = new Thread() {
            public void run() {
                updateWidget();
            }
        };
        t9.start();
    }

    private void getInfoFromXML() {

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                getString(R.string.loading), true, true);
        setProgressBarIndeterminateVisibility(true);

        Thread t3 = new Thread() {
            public void run() {

                waiting(200);
                txtInfo.post(new Runnable() {
                    public void run() {
                        txtInfo.setText(R.string.searching);
                    }
                });

                try {

                    URL url = new URL(urlAddress);
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                    xr.setContentHandler(myXMLHandler);
                    xr.parse(new InputSource(url.openStream()));

                    numberOfResultsOnPage = myXMLHandler.numItems;
                    numberOfTotalResults = myXMLHandler.numTotalItems;
                    final int fnmin = iFirstResultOnPage;
                    final int fnmax = iFirstResultOnPage + numberOfResultsOnPage - 1;
                    final int fntotalitems = numberOfTotalResults;

                    if (!vFavorite) {
                        favoriteButton.post(new Runnable() {
                            public void run() {
                                favoriteButton.setVisibility(0);
                            }
                        });
                    }

                    if (numberOfTotalResults > fnmax) {
                        nextButton.post(new Runnable() {
                            public void run() {
                                nextButton.setVisibility(0);
                            }
                        });
                    } else {
                        nextButton.post(new Runnable() {
                            public void run() {
                                nextButton.setVisibility(8);
                            }
                        });
                    }
                    if (iFirstResultOnPage > 1) {
                        previousButton.post(new Runnable() {
                            public void run() {
                                previousButton.setVisibility(0);
                            }
                        });
                    } else {
                        previousButton.post(new Runnable() {
                            public void run() {
                                previousButton.setVisibility(8);
                            }
                        });
                    }

                    txtInfo.post(new Runnable() {
                        public void run() {
                            txtInfo.setText("Showing " + fnmin + " through "
                                    + fnmax + " of " + fntotalitems);
                        }
                    });

                    titles = new String[numberOfResultsOnPage];
                    updatedDates = new String[numberOfResultsOnPage];
                    publishedDates = new String[numberOfResultsOnPage];
                    creators = new String[numberOfResultsOnPage];
                    links = new String[numberOfResultsOnPage];
                    listText = new String[numberOfResultsOnPage];
                    descriptions = new String[numberOfResultsOnPage];
                    categories = new String[numberOfResultsOnPage];

                    for (int i = 0; i < numberOfResultsOnPage; i++) {
                        titles[i] = myXMLHandler.titles[i]
                                .replaceAll("\n", " ").replaceAll(" +"," ");
                        creators[i] = myXMLHandler.creators[i];
                        updatedDates[i] = myXMLHandler.updatedDates[i];
                        publishedDates[i] = myXMLHandler.publishedDates[i];
                        categories[i] = myXMLHandler.categories[i];
                        links[i] = myXMLHandler.links[i];
                        descriptions[i] = myXMLHandler.descriptions[i]
                                .replaceAll("\n", " ");
                        ;
                        listText[i] = titles[i]+"\n";

                        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                                + creators[i] + "\n</begin>";
                        try {
                            SAXParserFactory spf2 = SAXParserFactory
                                    .newInstance();
                            SAXParser sp2 = spf2.newSAXParser();
                            XMLReader xr2 = sp2.getXMLReader();
                            XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                            xr2.setContentHandler(myXMLHandler2);
                            xr2.parse(new InputSource(
                                    new StringReader(creatort)));
                            listText[i] = listText[i] + "-Authors: "
                              + myXMLHandler2.creators[0];
                            for (int j = 1; j < myXMLHandler2.numItems; j++) {
                                listText[i] = listText[i] + ", "
                                        + myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                        if (updatedDates[i].equals(publishedDates[i])) {
                            listText[i] = listText[i] + "\n-Published: " + publishedDates[i].replace("T"," ").replace("Z","");
                        } else {
                            listText[i] = listText[i] + "\n-Updated: " + updatedDates[i].replace("T"," ").replace("Z","");
                            listText[i] = listText[i] + "\n-Published: " + publishedDates[i].replace("T"," ").replace("Z","");
                        }
                        if (!query.contains(categories[i]) && vCategory) {
                            listText[i] = listText[i] + "\n-Cross-Ref: "+categories[i];
                        } else if (!vCategory) {
                            listText[i] = listText[i] + "\n-Category: "+categories[i];
                        }
                    }

                    handlerSetList.sendEmptyMessage(0);

                    if (vFavorite && favFeed.count != numberOfTotalResults && numberOfTotalResults > 0) {
                        try {
                            droidDB = new arXivDB(thisActivity);
                            droidDB.updateFeed(favFeed.feedId,favFeed.title,favFeed.shortTitle,favFeed.url,numberOfTotalResults);
                            droidDB.close();
                            favFeed.count = numberOfTotalResults;
                            updateWidget();
                        } catch (Exception enf) {
                        }
                    }

                    dialog.dismiss();
                    handlerDoneLoading.sendEmptyMessage(0);

                } catch (Exception e) {

                    final Exception ef = e;
                    txtInfo.post(new Runnable() {
                        public void run() {
                            //txtInfo.setText(R.string.couldnt_parse);
                            txtInfo.setText("Error "+ef);
                        }
                    });

                    dialog.dismiss();
                    handlerDoneLoading.sendEmptyMessage(0);

                }
            }
        };
        t3.start();
    }

    public void nextPressed(View button) {
        iFirstResultOnPage = iFirstResultOnPage + nResultsPerPage;
        urlAddress = "http://export.arxiv.org/api/query?" + query
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start="
                + (iFirstResultOnPage - 1) + "&max_results=" + nResultsPerPage;
        getInfoFromXML();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.searchlist);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        query = myIntent.getStringExtra("keyquery");
        urlInput = myIntent.getStringExtra("keyurl");

        urlAddress = "http://export.arxiv.org/api/query?" + query
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start="
                + (iFirstResultOnPage - 1) + "&max_results=" + nResultsPerPage;

        Log.d("arXiv - ", urlAddress);

        if (query.contains("cat:")) {
          vCategory=true;
        } else {
          vCategory=false;
        }

        header = (TextView) findViewById(R.id.theaderlis);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);

        nextButton = (Button) findViewById(R.id.nextbutton);
        previousButton = (Button) findViewById(R.id.previousbutton);
        favoriteButton = (Button) findViewById(R.id.favoritebutton);

        thisActivity = this;

        txtInfo = (TextView) findViewById(R.id.txt);

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 16;
            try {
                droidDB.changeSize(fontSize);
            } catch (Exception ef) {
            }
        }
        //See if this is a favorite
        List<Feed> favorites = droidDB.getFeeds();
        for (Feed feed : favorites) {
            if (query.equals(feed.shortTitle)) {
                favFeed=feed;
                vFavorite=true;
            }
        }
        droidDB.close();

        getInfoFromXML();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent myIntent = new Intent(this, SingleItemWindow.class);
        myIntent.putExtra("keytitle", titles[position]);
        myIntent.putExtra("keylink", links[position]);
        myIntent.putExtra("keydescription", descriptions[position]);
        myIntent.putExtra("keycreator", creators[position]);
        myIntent.putExtra("keyname", name);
        startActivity(myIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
    }

    public void updateWidget() {
        // Get the layout for the App Widget and attach an on-click listener to the button
        Context context = getApplicationContext();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.arxiv_appwidget);
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, arXiv.class);
        String typestring = "widget";
        intent.putExtra("keywidget",typestring);
        intent.setData((Uri.parse("foobar://"+SystemClock.elapsedRealtime())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);

        droidDB = new arXivDB(thisActivity);
        List<Feed> favorites = droidDB.getFeeds();
        droidDB.close();

        String favText = "";

        if (favorites.size() > 0) {
            try {
                mRemoveAllViews = RemoteViews.class.getMethod("removeAllViews",
                 mRemoveAllViewsSignature);
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout);
                mRemoveAllViews.invoke(views, mRemoveAllViewsArgs);

                //views.removeAllViews(R.id.mainlayout);
                
            } catch (Exception ef) {
            }
            for (Feed feed : favorites) {

                if (feed.url.contains("query")) {

                    String urlAddressTemp = "http://export.arxiv.org/api/query?" + feed.shortTitle
                            + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1";

                    int numberOfTotalResults = 0;
                    try {
                        URL url = new URL(urlAddressTemp);
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
                        tempViews.setTextViewText(R.id.number, ""+newArticles);
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
                ComponentName thisWidget = new ComponentName(thisActivity, ArxivAppWidgetProvider.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(thisActivity);
                manager.updateAppWidget(thisWidget, views);
            }
        }

    }

    public void previousPressed(View button) {
        iFirstResultOnPage = iFirstResultOnPage - nResultsPerPage;
        urlAddress = "http://export.arxiv.org/api/query?" + query
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start="
                + (iFirstResultOnPage - 1) + "&max_results=" + nResultsPerPage;
        getInfoFromXML();
    }

    private void waiting(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }

}
