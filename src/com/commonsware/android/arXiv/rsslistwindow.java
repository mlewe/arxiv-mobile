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
import android.view.KeyEvent;
import android.app.Dialog;
import android.app.ProgressDialog;

public class rsslistwindow extends ListActivity
{
    private TextView txt;
    private TextView header;
    private String name;
    private String urladdress;
    private String[] titles;
    private String[] links;
    private String[] descriptions;
    private String[] creators;
    public rsslistwindow thisActivity;
    public ListView list;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        String url = myIntent.getStringExtra("keyurl");
        urladdress = "http://export.arxiv.org/rss/"+url;

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(" "+name);

	thisActivity = this;

        txt=(TextView)findViewById(R.id.txt);
        //txt.setText(urladdress);

        getInfoFromXML();

    }

    private void getInfoFromXML() {

	final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

	Thread t2 = new Thread() {
        	public void run() {

			try {

                                txt.post(new Runnable() {
                                	public void run() {
                                        	txt.setText("Starting");
                                        }
                                });

				URL url = new URL(urladdress);
        	                SAXParserFactory spf = SAXParserFactory.newInstance();
                	        SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
                                XMLHandler myXMLHandler = new XMLHandler();
                                xr.setContentHandler(myXMLHandler);
                        	xr.parse(new InputSource(url.openStream()));

                                final int nitems = myXMLHandler.nitems;

                                txt.post(new Runnable() {
                                	public void run() {
                                        	txt.setText("Most Recent: ");
                                        }
                                });

				titles = new String[nitems];
				creators = new String[nitems];
				links = new String[nitems];
				descriptions = new String[nitems];
				for ( int i = 0 ; i < nitems ; i++) {
					titles[i] = myXMLHandler.titles[i];
					creators[i] = myXMLHandler.creators[i];
					links[i] = myXMLHandler.links[i];
					descriptions[i] = myXMLHandler.descriptions[i];
				}

				handler.sendEmptyMessage(0);

			} catch (Exception e) {
				final Exception ef = e;
                                txt.post(new Runnable() {
                                	public void run() {
                                        	txt.setText("Failed "+ef);
                                        }
                                });
			}
		    	dialog.dismiss();
		}
  	};
	t2.start();

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
			 R.layout.item, R.id.label,titles));
		}
	};

}
