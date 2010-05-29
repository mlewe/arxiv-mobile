package com.commonsware.android.arXiv;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.graphics.Typeface;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.net.Uri;
import java.net.*;

public class rsslistwindow extends Activity
{
    private TextView txt;
    private TextView header;
    private String urladdress;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        Intent myIntent = getIntent();
        String name = myIntent.getStringExtra("keyname");
        String url = myIntent.getStringExtra("keyurl");
        urladdress = "http://export.arxiv.org/rss/"+url;

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(" "+name);

        txt=(TextView)findViewById(R.id.txt);
        txt.setText(urladdress);

        getInfoFromXML();

    }

    private void getInfoFromXML() {

	Thread t2 = new Thread() {
        	public void run() {

			try {

				URL url = new URL(urladdress);
        	                SAXParserFactory spf = SAXParserFactory.newInstance();
                	        SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
                                XMLHandler myXMLHandler = new XMLHandler();
                                xr.setContentHandler(myXMLHandler);
                        	xr.parse(new InputSource(url.openStream()));

                                int nitems = myXMLHandler.nitems;

			} catch (Exception e) {
			}
		}

  	};
	t2.start();

    }

}
