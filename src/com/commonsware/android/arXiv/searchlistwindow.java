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

public class SearchListWindow extends ListActivity {

    public SearchListWindow thisActivity;
    
    //UI-Views
    private TextView txtInfo;
    private TextView header;
    public ListView list;
    private Button nextButton;
    private Button previousButton;
    
    private String name;
    private String urlAddress;
    private String urlInput;
    private String query;
    private String[] titles;
    private String[] dates;
    private String[] links;
    private String[] listText;
    private String[] descriptions;
    private String[] creators;
    private int iFirstResultOnPage = 1;
    private int nResultsPerPage = 20;
    private int numberOfResultsOnPage;
    private int numberOfTotalResults;
    private int fontSize;

    private arXivDB droidDB;

    public static final int INCREASE_ID = Menu.FIRST + 1;
    public static final int DECREASE_ID = Menu.FIRST + 2;

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
        droidDB.insertFeed(name, query, urlInput);
        Toast.makeText(this, R.string.added_to_favorites,
                Toast.LENGTH_SHORT).show();
        droidDB.close();
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
                    dates = new String[numberOfResultsOnPage];
                    creators = new String[numberOfResultsOnPage];
                    links = new String[numberOfResultsOnPage];
                    listText = new String[numberOfResultsOnPage];
                    descriptions = new String[numberOfResultsOnPage];

                    for (int i = 0; i < numberOfResultsOnPage; i++) {
                        titles[i] = myXMLHandler.titles[i]
                                .replaceAll("\n", " ");
                        creators[i] = myXMLHandler.creators[i];
                        dates[i] = myXMLHandler.dates[i];
                        links[i] = myXMLHandler.links[i];
                        descriptions[i] = myXMLHandler.descriptions[i]
                                .replaceAll("\n", " ");
                        ;
                        listText[i] = titles[i];

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
                            for (int j = 0; j < myXMLHandler2.numItems; j++) {
                                listText[i] = listText[i] + " - "
                                        + myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                        listText[i] = listText[i] + " - " + dates[i];
                    }

                    handlerSetList.sendEmptyMessage(0);

                } catch (Exception e) {

                    final Exception ef = e;
                    txtInfo.post(new Runnable() {
                        public void run() {
                            txtInfo.setText(R.string.couldnt_parse);
                        }
                    });

                }
                dialog.dismiss();
                handlerDoneLoading.sendEmptyMessage(0);
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

        header = (TextView) findViewById(R.id.theaderlis);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);

        nextButton = (Button) findViewById(R.id.nextbutton);
        previousButton = (Button) findViewById(R.id.previousbutton);

        thisActivity = this;

        txtInfo = (TextView) findViewById(R.id.txt);

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 14;
            droidDB.changeSize(fontSize);
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
