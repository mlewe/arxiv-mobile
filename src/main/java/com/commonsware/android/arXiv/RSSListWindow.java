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

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.net.URL;

public class RSSListWindow extends SherlockListActivity {

    public static final int INCREASE_ID = Menu.FIRST + 1;
    public static final int DECREASE_ID = Menu.FIRST + 2;
    public static final int FAVORITE_ID = Menu.FIRST + 3;
    private TextView txt;
    private String name;
    private String urlAddress;
    private String query;
    private String[] titles;
    private String[] links;
    private String[] listText;
    private String[] listText2;
    private String[] descriptions;
    private String[] creators;
    private int fontSize;
    private Boolean favorite = false;
    private Boolean loaded = false;
    private Handler handlerSetList = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            setListAdapter(new myCustomAdapter());

        }
    };
    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loaded = true;
            setProgressBarIndeterminateVisibility(false);
        }
    };

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case INCREASE_ID:
                setFontSize(fontSize + 2);
                return true;
            case DECREASE_ID:
                setFontSize(fontSize - 2);
                return true;
            case FAVORITE_ID:
                favoritePressed(null);
                return true;
        }
        return (false);
    }

    private void getInfoFromXML() {

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true, true);
        setProgressBarIndeterminateVisibility(true);

        Thread t2 = new Thread() {
            public void run() {
                try {

                    waiting(200);

                    txt.post(new Runnable() {
                        public void run() {
                            txt.setText("Loading");
                        }
                    });

                    URL url = new URL(urlAddress);
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    XMLHandlerRSS myXMLHandler = new XMLHandlerRSS();
                    xr.setContentHandler(myXMLHandler);

                    Boolean vcompleted = true;

                    try {
                        xr.parse(new InputSource(url.openStream()));
                    } catch (Exception saxe) {
                        if (myXMLHandler.numItems == 0) {
                            txt.post(new Runnable() {
                                public void run() {
                                    txt.setText(R.string.couldnt_parse);
                                }
                            });
                            vcompleted = false;
                        }
                    }

                    int nitems = myXMLHandler.numItems;
                    final String tdate = myXMLHandler.date.replace("T", " ").replace("Z", "");
                    final int nitemst = nitems;

                    if (vcompleted) {
                        txt.post(new Runnable() {
                            public void run() {
                                txt.setText(nitemst
                                        + " new submissions.\nRefreshed: "
                                        + tdate);
                            }
                        });
                    }

                    if (nitemst != myXMLHandler.icount) {
                        nitems = myXMLHandler.icount;
                        final int nitemst2 = nitems;
                        txt.post(new Runnable() {
                            public void run() {
                                txt
                                        .setText(nitemst
                                                + " new submissions.  Refreshed: "
                                                + tdate
                                                + "\nError in feed - only showing first "
                                                + nitemst2 + " results.");
                            }
                        });
                    }

                    titles = new String[nitems];
                    creators = new String[nitems];
                    links = new String[nitems];
                    listText = new String[nitems];
                    listText2 = new String[nitems];
                    descriptions = new String[nitems];

                    for (int i = 0; i < nitems; i++) {
                        titles[i] = myXMLHandler.titles[i].replaceAll(
                                "(.arXiv.*)", "");
                        //String category = "";
                        String category = myXMLHandler.titles[i].replaceAll(".*\\[", "").replace("])", "").replace("] UPDATED)", "");
                        creators[i] = myXMLHandler.creators[i];
                        links[i] = myXMLHandler.links[i];
                        descriptions[i] = myXMLHandler.descriptions[i];
                        listText[i] = titles[i];
                        listText2[i] = "";

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
                            listText2[i] = listText2[i] + "-Authors: "
                                    + myXMLHandler2.creators[0];
                            for (int j = 1; j < myXMLHandler2.numItems; j++) {
                                //listText[i] = listText[i] + " - "
                                listText2[i] = listText2[i] + ", "
                                        + myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                        if (myXMLHandler.titles[i].contains("UPDATED")) {
                            listText2[i] = listText2[i] + "\n-Updated";
                        }
                        if (!query.contains(category)) {
                            listText2[i] = listText2[i] + "\n-Cross-Ref: " + category;
                        }
                    }

                    handlerSetList.sendEmptyMessage(0);

                } catch (Exception e) {

                    final Exception ef = e;
                    txt.post(new Runnable() {
                        public void run() {
                            txt.setText("Failed " + ef);
                        }
                    });

                }

                dialog.dismiss();
                handlerDoneLoading.sendEmptyMessage(0);
            }
        };
        t2.start();
    }

    public void favoritePressed(View button) {
        ContentValues cv = new ContentValues();
        cv.put(Feeds.TITLE, name + " (RSS)");
        cv.put(Feeds.SHORTTITLE, name);
        cv.put(Feeds.URL, query);
        cv.put(Feeds.UNREAD, -2);
        cv.put(Feeds.COUNT, -2);
        cv.put(Feeds.LAST_UPDATE, 0);
        new AsyncQueryHandler(this.getContentResolver()) {
            @Override
            protected void onInsertComplete(int id, Object cookie, Uri uri) {
                Toast.makeText(getBaseContext(), id, Toast.LENGTH_SHORT).show();
            }
        }.startInsert(R.string.added_to_favorites_rss, null, Feeds.CONTENT_URI, cv);
        favorite = true;
        supportInvalidateOptionsMenu();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.searchlist);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        query = myIntent.getStringExtra("keyurl");
        urlAddress = "http://export.arxiv.org/rss/" + query;

        favorite = myIntent.getBooleanExtra("favorite", false);
        if (favorite)
            supportInvalidateOptionsMenu();

        ActionBar ab = getSupportActionBar();
        ab.setTitle(name);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        txt = (TextView) findViewById(R.id.txt);

        fontSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("fontSize", 16);

        getInfoFromXML();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
        if (!favorite) {
            menu.add(Menu.NONE, FAVORITE_ID, Menu.NONE, "Add to Favorites");
        }
    }

    private void setFontSize(int size) {
        if (size > 20) size = 20;
        if (size < 12) size = 12;
        fontSize = size;
        if (loaded) {
            handlerSetList.sendEmptyMessage(0);
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("fontSize", size);
        if (Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }

    private void waiting(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }

    class myCustomAdapter extends ArrayAdapter<String> {

        myCustomAdapter() {
            super(RSSListWindow.this, R.layout.searchrow, listText);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.searchrow, parent, false);
                holder = new ViewHolder();
                holder.text1 = (TextView) row.findViewById(R.id.text1);
                holder.text2 = (TextView) row.findViewById(R.id.text2);
                holder.linLay = (LinearLayout) row.findViewById(R.id.linlay);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.text1.setText(listText[position]);
            holder.text1.setTextSize(fontSize);
            holder.text2.setText(listText2[position]);
            holder.text2.setTextSize(fontSize - 2);
            holder.linLay.setBackgroundResource((position % 2 == 0) ? R.color.back2 : R.color.back4);
            return (row);

        }

        public class ViewHolder {
            public TextView text1;
            public TextView text2;
            public LinearLayout linLay;
        }

    }

}
