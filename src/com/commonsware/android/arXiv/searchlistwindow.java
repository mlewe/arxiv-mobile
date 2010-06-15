/*
    arXiv Droid - a Free arXiv app for android
    http://www.jdeslippe.com/arxivdroid 

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
import android.widget.Button;
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
import android.view.KeyEvent;
import android.app.Dialog;
import android.app.ProgressDialog;
import java.io.StringReader;

public class searchlistwindow extends ListActivity
{
    private TextView txtinfo;
    private TextView header;
    private String name;
    private String urladdress;
    private String query;
    private String[] titles;
    private String[] dates;
    private String[] links;
    private String[] listtext;
    private String[] descriptions;
    private String[] creators;
    public searchlistwindow thisActivity;
    public ListView list;
    private int nmin=1;
    private int nstep=20;
    private int nmax;
    private int nitems;
    private int ntotalitems;
    private Button nextbutton;
    private Button previousbutton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.searchlist);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        query = myIntent.getStringExtra("keyquery");
        //urladdress = myIntent.getStringExtra("keyurl");

        urladdress = "http://export.arxiv.org/api/query?search_query="+query+"&sortBy=lastUpdatedDate&sortOrder=descending&start="+(nmin-1)+"&max_results="+nstep;

        header=(TextView)findViewById(R.id.theaderlis);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);
        //header.setText(urladdress);

	nextbutton=(Button)findViewById(R.id.nextbutton);
	previousbutton=(Button)findViewById(R.id.previousbutton);

	thisActivity = this;

        txtinfo=(TextView)findViewById(R.id.txt);
        //txtinfo.setText(urladdress);

        getInfoFromXML();

    }

    private void getInfoFromXML() {

	final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true, true);
        setProgressBarIndeterminateVisibility(true);

        //txtinfo.setText("Starting");

	Thread t3 = new Thread() {
        	public void run() {

			waiting(200);
                        txtinfo.post(new Runnable() {
                                public void run() {
                                        txtinfo.setText("Searching");
                        	}
                        });

			try {

				URL url = new URL(urladdress);
        	                SAXParserFactory spf = SAXParserFactory.newInstance();
                	        SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
                                XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                                xr.setContentHandler(myXMLHandler);
				//InputSource temp = new InputSource(url.openStream());
                        	xr.parse(new InputSource(url.openStream()));

                                nitems = myXMLHandler.nitems;
                                ntotalitems = myXMLHandler.ntotalitems;
				final int fnmin = nmin;
				final int fnmax = nmin + nitems - 1;
				final int fntotalitems = ntotalitems;

				if (ntotalitems > fnmax) {
	                                nextbutton.post(new Runnable() {
	                                	public void run() {
							nextbutton.setVisibility(0);
	                                        }
        	                        });
				} else {
	                                nextbutton.post(new Runnable() {
	                                	public void run() {
							nextbutton.setVisibility(8);
	                                        }
        	                        });
				}
				if (nmin > 1) {
	                                nextbutton.post(new Runnable() {
	                                	public void run() {
							previousbutton.setVisibility(0);
	                                        }
        	                        });
				} else {
	                                nextbutton.post(new Runnable() {
	                                	public void run() {
							previousbutton.setVisibility(8);
	                                        }
        	                        });
				}

                                txtinfo.post(new Runnable() {
                                	public void run() {
                                        	txtinfo.setText("Showing "+fnmin+" through "+fnmax+" of "+fntotalitems);
                                        }
                                });

				titles = new String[nitems];
				dates = new String[nitems];
				creators = new String[nitems];
				links = new String[nitems];
				listtext = new String[nitems];
				descriptions = new String[nitems];

				for ( int i = 0 ; i < nitems ; i++) {
					titles[i] = myXMLHandler.titles[i].replaceAll("\n","");
					creators[i] = myXMLHandler.creators[i];
					dates[i] = myXMLHandler.dates[i];
					links[i] = myXMLHandler.links[i];
					descriptions[i] = myXMLHandler.descriptions[i];
					listtext[i] = titles[i];

        				String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"+creators[i]+"\n</begin>";
        				try {
                				SAXParserFactory spf2 = SAXParserFactory.newInstance();
                				SAXParser sp2 = spf2.newSAXParser();
                				XMLReader xr2 = sp2.getXMLReader();
                				XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                				xr2.setContentHandler(myXMLHandler2);
                				xr2.parse(new InputSource(new StringReader( creatort )));
                				for ( int j = 0 ; j < myXMLHandler2.nitems ; j++ ) {
                        				listtext[i] = listtext[i]+" - "+ myXMLHandler2.creators[j];
				                }
				        } catch (Exception e) {
					}
					listtext[i] = listtext[i]+" - "+dates[i];

				}

				handler.sendEmptyMessage(0);

			} catch (Exception e) {
				final Exception ef = e;
                                txtinfo.post(new Runnable() {
                                	public void run() {
                                        	txtinfo.setText("Failed "+ef+" "+urladdress);
                                        }
                                });
			}
		    	dialog.dismiss();
	                handlersize.sendEmptyMessage(0);
		}

  	};
	t3.start();
        //txtinfo.setText("Finishing");

    }

    public void onListItemClick(ListView parent, View v, int position,long id) {
        //selection.setText(items[position]);
        Intent myIntent = new Intent(this,singleitemwindow.class);
        //myIntent.setClassName("com.commonwsare.android.arXiv", "com.commonsware.android.arXiv.rsslistwindow");
        myIntent.putExtra("keytitle", titles[position]);
        myIntent.putExtra("keylink", links[position]);
        myIntent.putExtra("keydescription", descriptions[position]);
        myIntent.putExtra("keycreator", creators[position]);
        myIntent.putExtra("keyname", name);
        startActivity(myIntent);
    }

        private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
			setListAdapter(new ArrayAdapter<String>(thisActivity,
			 R.layout.item, R.id.label,listtext));
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

    public void nextPressed(View button) {
	nmin = nmin + nstep;
        urladdress = "http://export.arxiv.org/api/query?search_query="+query+"&sortBy=lastUpdatedDate&sortOrder=descending&start="+(nmin-1)+"&max_results="+nstep;
        getInfoFromXML();
    }

    public void previousPressed(View button) {
	nmin = nmin - nstep;
        urladdress = "http://export.arxiv.org/api/query?search_query="+query+"&sortBy=lastUpdatedDate&sortOrder=descending&start="+(nmin-1)+"&max_results="+nstep;
        getInfoFromXML();
    }

        private Handler handlersize = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        setProgressBarIndeterminateVisibility(false);
                }
        };


}

