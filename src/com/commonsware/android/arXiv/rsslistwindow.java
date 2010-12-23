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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.widget.TextView;
import android.graphics.Typeface;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.net.Uri;
import java.net.*;
import android.widget.ListView;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.Window;
import android.app.ProgressDialog;
import java.io.StringReader;
import android.view.Menu;
import android.view.MenuItem;

public class RSSListWindow extends ListActivity {
    private TextView txt;
    private TextView header;
    private String name;
    private String urlAddress;
    private String[] titles;
    private String[] links;
    private String[] listText;
    private String[] descriptions;
    private String[] creators;
    private int fontSize;
    private arXivDB droidDB;

    public RSSListWindow thisActivity;
    public ListView list;

    public static final int INCREASE_ID = Menu.FIRST+1;
    public static final int DECREASE_ID = Menu.FIRST+2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.list);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        String url = myIntent.getStringExtra("keyurl");
        urlAddress = "http://export.arxiv.org/rss/"+url;

        header=(TextView)findViewById(R.id.theaderli);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);

        thisActivity = this;

        txt=(TextView)findViewById(R.id.txt);

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        droidDB.close();

        getInfoFromXML();

    }

    private void getInfoFromXML() {

        final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true,true);
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
                    XMLHandler myXMLHandler = new XMLHandler();
                    xr.setContentHandler(myXMLHandler);

                    Boolean vcompleted = true;

                    try {
                        xr.parse(new InputSource(url.openStream()));
                    } catch (Exception saxe) {
                        if (myXMLHandler.nitems == 0) {
                            txt.post(new Runnable() {
                	        public void run() {
                        	    txt.setText("Couldn't Parse - No Network Connection to Server?");
                                }
                            });
                            vcompleted = false;
                        }
                    }

                    int nitems = myXMLHandler.nitems;
                    final String tdate = myXMLHandler.date;
                    final int nitemst = nitems;

                    if (vcompleted) {
                        txt.post(new Runnable() {
                            public void run() {
                                txt.setText(nitemst+" new submissions.  Refreshed: "+tdate);
                            }
                        });
                    }

                    if (nitemst != myXMLHandler.icount) {
	                nitems = myXMLHandler.icount;
                        final int nitemst2 = nitems;
                        txt.post(new Runnable() {
        	            public void run() {
                	        txt.setText(nitemst+" new submissions.  Refreshed: "+tdate+"\nError in feed - only showing first "+nitemst2+" results.");
                            }
                        });
                    }

                    titles = new String[nitems];
                    creators = new String[nitems];
                    links = new String[nitems];
                    listText = new String[nitems];
                    descriptions = new String[nitems];

                    for ( int i = 0 ; i < nitems ; i++) {
                        titles[i] = myXMLHandler.titles[i].replaceAll("(.arXiv.*)","");
                        creators[i] = myXMLHandler.creators[i];
                        links[i] = myXMLHandler.links[i];
                        descriptions[i] = myXMLHandler.descriptions[i];
                        listText[i] = titles[i];

                        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"+creators[i]+"\n</begin>";
                        try {
                            SAXParserFactory spf2 = SAXParserFactory.newInstance();
                            SAXParser sp2 = spf2.newSAXParser();
                            XMLReader xr2 = sp2.getXMLReader();
                            XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                            xr2.setContentHandler(myXMLHandler2);
                            xr2.parse(new InputSource(new StringReader( creatort )));
                            for ( int j = 0 ; j < myXMLHandler2.nitems ; j++ ) {
                                listText[i] = listText[i]+" - "+ myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                    }

                    handlerSetList.sendEmptyMessage(0);

                } catch (Exception e) {

                    final Exception ef = e;
                    txt.post(new Runnable() {
                        public void run() {
                            //txt.setText("Failed "+ef);
                        }
                    });

                }

                dialog.dismiss();
                handlerDoneLoading.sendEmptyMessage(0);
            }
        };
        t2.start();
    }

    public void onListItemClick(ListView parent, View v, int position,long id) {
        Intent myIntent = new Intent(this,SingleItemWindow.class);
        myIntent.putExtra("keytitle", titles[position]);
        myIntent.putExtra("keylink", links[position]);
        myIntent.putExtra("keydescription", descriptions[position]);
        myIntent.putExtra("keycreator", creators[position]);
        myIntent.putExtra("keyname", name);
        startActivity(myIntent);
    }

    private Handler handlerSetList = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (fontSize < 12) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item10, R.id.label,listText));
            } else if (fontSize == 12) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item12, R.id.label,listText));
            } else if (fontSize == 14) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item, R.id.label,listText));
            } else if (fontSize == 16) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item16, R.id.label,listText));
            } else if (fontSize == 18) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item18, R.id.label,listText));
            } else if (fontSize == 20) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item20, R.id.label,listText));
            } else if (fontSize > 20) {
                setListAdapter(new ArrayAdapter<String>(thisActivity,
                 R.layout.item22, R.id.label,listText));
            }
        }
    };

    private static void waiting (int n){
        long t0, t1;
        t0 =  System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        }
        while (t1 - t0 < n);
    }

    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBarIndeterminateVisibility(false);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return(applyMenuChoice(item) ||
        super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
         menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
    }

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
        case INCREASE_ID:
            if (fontSize < 22) {
                if (fontSize < 10) {
                    fontSize = 10;
                }
                fontSize = fontSize+2;
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                handlerSetList.sendEmptyMessage(0);
            }
            return(true);
        case DECREASE_ID:
            if (fontSize > 10) {
                if (fontSize > 22) {
                    fontSize = 22;
                }
                fontSize = fontSize-2;
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                handlerSetList.sendEmptyMessage(0);
            }
            return(true);
        }
        return(false);
    }

}
