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
import java.io.StringReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.content.ActivityNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class historywindow extends ListActivity
{
    private TextView header;
    public ListView list;
    private List<History> historys;
    private arXivDB droidDB;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        header=(TextView)findViewById(R.id.theaderhs);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText("History");

        //txt=(TextView)findViewById(R.id.txt);
        //txt.setText(urladdress);

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();
        droidDB.close();

        List<String> lhistory = new ArrayList<String>();
        for (History history : historys) {
                lhistory.add(history.displaytext);
        }

        setListAdapter(new ArrayAdapter<String>(this,
         R.layout.item, R.id.label,lhistory));

        //setListAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_list_item_1,lhistory));

    }

    public void onListItemClick(ListView parent, View v, int position,long id) {

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

	String filename="";

	int icount = 0;
        for (History history : historys) {
		if (icount == position) {
			filename=history.url;
		}
        }

        File file = new File(filename);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");

        try {
        	startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }

        startActivity(intent);
    }

}
