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
import android.content.Intent;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.graphics.Typeface;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.net.Uri;
import java.net.*;
import android.widget.ListView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.webkit.WebView;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.view.View;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.BufferedInputStream;
import android.view.ViewGroup.LayoutParams;

public class singleitemwindow extends Activity implements View.OnClickListener
{
    private LinearLayout linlay;
    private ScrollView sv;
    private WebView wv;
    private Button pbtn;
    private TextView txt;
    private TextView txt2;
    private TextView txt3;
    private TextView header;
    private String name;
    private String title;
    private String description;
    private String creator;
    private String link;
    private String[] authors;
    private Boolean vloop=true;
    private ProgressBar pbar;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singleitem);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");

        pbar=(ProgressBar)findViewById(R.id.pbar);              // Progressbar for download
        pbtn=(Button)findViewById(R.id.pdfbutton);

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);

	description = description.replace("\n","");
	description = description.replace("<p>","");
	description = description.replace("</p>","");

	TextView txt = new TextView(this);
	TextView txt3 = new TextView(this);

        txt.setText(title);
	txt.setPadding(5,5,5,5);

	sv = (ScrollView)findViewById(R.id.SV);

        LinearLayout linlay = new LinearLayout(this);
        linlay.setOrientation(1);
	linlay.addView(txt);

        //wv=(WebView)findViewById(R.id.wv);
	//wv.setBackgroundColor(0);

        //txt2= new TextView;
        //txt.setText(creator);
	//wv.loadData(creator, "text/html", "utf-8");

        txt3.setText("Abstract: "+description);
	txt3.setPadding(5,5,5,5);
	txt3.setTextSize(13);

	//The Below Gets the Authors Names
	String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"+creator+"\n</begin>";
	try {
	        SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXParser sp = spf.newSAXParser();
        	XMLReader xr = sp.getXMLReader();
        	XMLHandlerCreator myXMLHandler = new XMLHandlerCreator();
        	xr.setContentHandler(myXMLHandler);
        	xr.parse(new InputSource(new StringReader( creatort )));
		authors = new String[myXMLHandler.nitems];
		for ( int i = 0 ; i < myXMLHandler.nitems ; i++ ) {
			authors[i] = myXMLHandler.creators[i]+"  ";
                        TextView temptv = new TextView(this);
			temptv.setText(" "+authors[i]);
			temptv.setClickable(true);
			//temptv.setSelected(true);
			temptv.setId(i+1000);
			temptv.setOnClickListener(this);
			temptv.setPadding(5,5,5,5);
			temptv.setTextSize(13);
			linlay.addView(temptv);
                        View rulerin = new View(this);
                        //rulerin.setBackgroundColor(0xFF696969);
                        rulerin.setBackgroundColor(0xFF3f3b3b);
                        linlay.addView(rulerin, new LayoutParams( 320, 1));
		}

	} catch (Exception e) {
	        txt.setText(" "+e+" "+creatort);
		authors = new String[0];
	}


	linlay.addView(txt3);
        //setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_1,authors));

	sv.addView(linlay);
	//txt.setText("Hello");

    }

    public void pressedPDFButton(View button) {

        //Intent myIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(pdfaddress));
        //startActivity(myIntent);
	//txt2.setText(pdfaddress);
	pbar.setVisibility(0);

        Thread t = new Thread() {
        	public void run() {


			try {
				File fare = new File("/sdcard/arXiv");
               			fare.mkdir();

			        String pdfaddress = link.replace("abs","pdf");

				URL u = new URL(pdfaddress);
                		HttpURLConnection c = (HttpURLConnection) u.openConnection();
                		c.setRequestMethod("GET");
                		c.setDoOutput(true);
                		c.connect();
                		final long ifs = c.getContentLength();

				//txt2.setText(" "+ifs);

                		final long jfs = ifs*100/1024/1024;
                		final double rfs = (double) jfs/100.0;
                		InputStream in = c.getInputStream();

				String filepath="/sdcard/arXiv/";
                		String filename="tmp.pdf";

				FileOutputStream f = new FileOutputStream(new File(filepath,filename));

                		byte[] buffer = new byte[1024];
                		int len1 = 0;
                		long i = 0;
                		while ( (len1 = in.read(buffer)) > 0 ) {
                			if (vloop == false) {
						break;
                        		}
                        		f.write(buffer,0,len1);
                        		i+=len1;
                        		long jt = 100*i/ifs;
                        		final int j = (int) jt;
                        		pbar.post(new Runnable() {
                        			public void run() {
                                			pbar.setProgress(j);
                        			}
                        		});
				}
				f.close();

				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				File file = new File(filepath+filename);
				intent.setDataAndType(Uri.fromFile(file), "application/pdf");
				startActivity(intent);

			} catch (Exception e) {
				final Exception ef = e;
                      		txt2.post(new Runnable() {
                        		public void run() {
						txt2.setText(" "+ef);
					}
				});
			}

		}
	};
        t.start();
    }

    public void onClick(View v) {
	final int iswitch = v.getId();

        pbtn.post(new Runnable() {
        	public void run() {
                	pbtn.setText(" "+iswitch);
                }
        });
    }

}
