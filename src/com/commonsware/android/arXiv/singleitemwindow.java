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
import android.view.Window;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.BufferedInputStream;
import android.view.ViewGroup.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.widget.Toast;

public class singleitemwindow extends Activity implements View.OnClickListener
{
    private LinearLayout linlay;
    private ScrollView sv;
    private Button pbtn;
    private TextView txttitle;
    private TextView txtabs;
    private TextView header;
    private TextView tsize;
    private String name;
    private String title;
    private String description;
    private String creator;
    private String link;
    private String pdfpath;
    private Boolean vstorage;
    private String[] authors;
    private Boolean vloop=false;
    private ProgressBar pbar;
    private Context thisactivity;
    private arXivDB droidDB;
    private int nauthors;

    public static final int SHARE_ID = Menu.FIRST+1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.singleitem);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");

        pbar=(ProgressBar)findViewById(R.id.pbar);              // Progressbar for download
        pbtn=(Button)findViewById(R.id.pdfbutton);

        tsize=(TextView)findViewById(R.id.tsize);

        header=(TextView)findViewById(R.id.theadersi);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(name);

	description = description.replace("\n","");
	description = description.replace("<p>","");
	description = description.replace("</p>","");

	TextView txttitle = new TextView(this);
	TextView txtabs = new TextView(this);

        txttitle.setText(title);
        txttitle.setTextSize(15);
	txttitle.setPadding(5,5,5,5);
	txttitle.setTextColor(0xffffffff);

	sv = (ScrollView)findViewById(R.id.SV);

	thisactivity = this;

        LinearLayout linlay = new LinearLayout(this);
        linlay.setOrientation(1);
	linlay.addView(txttitle);

        txtabs.setText("Abstract: "+description);
	txtabs.setPadding(5,5,5,5);
	txtabs.setTextSize(14);
	txtabs.setTextColor(0xffffffff);

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
		nauthors = myXMLHandler.nitems;
		for ( int i = 0 ; i < myXMLHandler.nitems ; i++ ) {
			authors[i] = myXMLHandler.creators[i]+"  ";
                        TextView temptv = new TextView(this);
			temptv.setText(" "+authors[i]);
			temptv.setClickable(true);
			//temptv.setSelected(true);
			temptv.setId(i+1000);
			temptv.setOnClickListener(this);
			temptv.setPadding(5,5,5,5);
			temptv.setTextSize(14);
			temptv.setTextColor(0xffffffff);
			linlay.addView(temptv);
                        View rulerin = new View(this);
                        //rulerin.setBackgroundColor(0xFF696969);
                        rulerin.setBackgroundColor(0xFF3f3b3b);
                        linlay.addView(rulerin, new LayoutParams( 320, 1));
		}

	} catch (Exception e) {
	        //header.setText(" "+e+" "+creatort);
		authors = new String[0];
	}

	linlay.addView(txtabs);
        //setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_1,authors));

	sv.addView(linlay);

	int version = android.os.Build.VERSION.SDK_INT;

	if ( version > 6) {
                setProgressBarIndeterminateVisibility(true);
		printSize();
	}

    }

    public void pressedPDFButton(View button) {

	int version = android.os.Build.VERSION.SDK_INT;

	if ( version > 6) {

        //Intent myIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(pdfaddress));
        //startActivity(myIntent);
	//pbar.setVisibility(0);

        Thread t = new Thread() {
        	public void run() {

			try {

				vstorage = false;

				File fare = new File("/sdcard/arXiv");
               			fare.mkdir();

		                if (fare.exists()) {
                		        pdfpath="/sdcard/arXiv/";
		                        vstorage=true;
				} else {
					File efare = new File("/emmc/arXiv");
        	       			efare.mkdir();
			                if (efare.exists()) {
        	        		       pdfpath="/sdcard/arXiv/";
			                       vstorage=true;
					}
				}

				if (vstorage) {

				vloop = true;
                       		tsize.post(new Runnable() {
                        		public void run() {
						tsize.setVisibility(8);
                        		}
                        	});
                       		pbar.post(new Runnable() {
                        		public void run() {
						pbar.setVisibility(0);
                        		}
                        	});

			        String pdfaddress = link.replace("abs","pdf");

				URL u = new URL(pdfaddress);
                		HttpURLConnection c = (HttpURLConnection) u.openConnection();
                		c.setRequestMethod("GET");
                		c.setDoOutput(true);
                		c.connect();

                		final long ifs = c.getContentLength();
                		final long jfs = ifs*100/1024/1024;
                		final double rfs = (double) jfs/100.0;

                		InputStream in = c.getInputStream();

				String filepath=pdfpath;
				String filename=title.replace(":","");
				filename=filename.replace("?","");
				filename=filename.replace("*","");
				filename=filename.replace("/","");
				filename=filename.replace(". ","");
                		filename=filename+".pdf";

				Boolean vdownload = true;
                                File futureFile = new File(filepath,filename);
                                if (futureFile.exists()) {
                                        final long itmp = futureFile.length();
                                        if (itmp == ifs && itmp != 0) {
						vdownload = false;
                                	}
                                }

				if (vdownload) {
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
				} else {
                        		pbar.post(new Runnable() {
                        			public void run() {
                                			pbar.setProgress(100);
                        			}
                        		});
				}

				if ( vloop ) {
					if ( vdownload) {
					        droidDB = new arXivDB(thisactivity);
						String displaytext = title;
						for (int i =0; i < nauthors; i++) {
							displaytext = displaytext + " - " + authors[i];
						}
						droidDB.insertHistory(displaytext,filepath+filename);
						droidDB.close();
					}

					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File file = new File(filepath+filename);
					intent.setDataAndType(Uri.fromFile(file), "application/pdf");

					try {
						startActivity(intent);
			                } catch (ActivityNotFoundException e) {
		                                handler.sendEmptyMessage(0);
					}
				} else {
					File fd = new File(filepath,filename);
					fd.delete();
				}

				} else {
	                                handler2.sendEmptyMessage(0);
				}
			} catch (Exception e) {
	                	handler3.sendEmptyMessage(0);
			}

		}
	};
        t.start();
	
	} else {
		Toast.makeText(thisactivity, "Android 2.x required to download PDF in app",
                 Toast.LENGTH_SHORT).show();
		String pdfaddress = link.replace("abs","pdf");
                Intent myIntent = new Intent(Intent.ACTION_VIEW,
                 Uri.parse(pdfaddress));
                 startActivity(myIntent);
	}

    }

    public void onClick(View v) {
	final int position = v.getId()-1000;

        //pbtn.post(new Runnable() {
        //	public void run() {
        //        	pbtn.setText(""+position);
        //        }
        //});

        Intent myIntent = new Intent(this,searchlistwindow.class);
        myIntent.putExtra("keyname", authors[position]);

	String authortext=authors[position].replace("  ","");
	authortext=authortext.replace(" ","+");
	authortext="search_query=au:%22"+authortext+"%22";
	//String urlad = "http://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
	String urlad = "http://export.arxiv.org/api/query?search_query="+authortext+"&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
	//header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", authortext);
        startActivity(myIntent);

    }

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
                menu.add(Menu.NONE, SHARE_ID, Menu.NONE, "Share Article");
        }


        private boolean applyMenuChoice(MenuItem item) {
                switch (item.getItemId()) {
                        case SHARE_ID:
				Intent i=new Intent(android.content.Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, "arXiv Article");
				i.putExtra(Intent.EXTRA_TEXT,title+" "+link);
				startActivity(Intent.createChooser(i, "Share Article"));
				return(true);
		}
		return(false);
	}

        private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
			Toast.makeText(thisactivity, "You must install a PDF Reader from the Market.  Try AdobeReader.",
                         Toast.LENGTH_SHORT).show();
                }
        };

        private Handler handler2 = new Handler() {
                @Override
                public void handleMessage(Message msg) {
			Toast.makeText(thisactivity, "Neither /sdcard or /emmc available to download PDF.",
                         Toast.LENGTH_SHORT).show();
                }
        };

        private Handler handler3 = new Handler() {
                @Override
                public void handleMessage(Message msg) {
			Toast.makeText(thisactivity, "Error: Could not download PDF.",
                         Toast.LENGTH_SHORT).show();
                }
        };

        @Override
        public void onDestroy() {
                super.onDestroy();
                vloop = false;
        }


	private void printSize() {
	        Thread t4 = new Thread() {
        		public void run() {

				try {
				        String pdfaddress = link.replace("abs","pdf");

					URL u = new URL(pdfaddress);
        	        		HttpURLConnection c = (HttpURLConnection) u.openConnection();
                			c.setRequestMethod("GET");
                			c.setDoOutput(true);
                			c.connect();

	                		final long ifs = c.getContentLength();
					c.disconnect();
        	        		final long jfs = ifs*100/1024/1024;
                			final double rfs = (double) jfs/100.0;
                       			tsize.post(new Runnable() {
                        			public void run() {
							tsize.setText("Size: "+rfs+" MB");
                        			}
                        		});
				} catch (Exception e) {
				}
		                handlersize.sendEmptyMessage(0);
			}
		};
       		t4.start();
	}

        private Handler handlersize = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        setProgressBarIndeterminateVisibility(false);
                }
        };

}
