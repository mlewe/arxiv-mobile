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
import android.widget.AdapterView;
import android.view.View;
import android.view.KeyEvent;
import android.app.Dialog;
import android.app.ProgressDialog;
import java.io.StringReader;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;

public class subarXiv extends Activity implements AdapterView.OnItemClickListener
{
    private TextView txt;
    private TextView header;
    private String name;
    private String urladdress;
    private String[] items;
    private String[] urls;
    private String[] shortitems;
    public rsslistwindow thisActivity;
    public ListView list;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submain);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        urls = myIntent.getStringArrayExtra("keyurls");
        items = myIntent.getStringArrayExtra("keyitems");
        shortitems = myIntent.getStringArrayExtra("keyshortitems");

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        list=(ListView)findViewById(R.id.list);

        header.setText(" "+name);

        txt=(TextView)findViewById(R.id.txt);
        //txt.setText(urladdress);

	//list.setAdapter(new ArrayAdapter<String>(this,
	// R.layout.item, R.id.label,items));

        list.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,items));

        list.setOnItemClickListener(this);
        registerForContextMenu(list);

    }

    public void onItemClick(AdapterView<?> a, View v, int position,long id) {
        //selection.setText(items[position]);
        Intent myIntent = new Intent(this,rsslistwindow.class);
        //myIntent.setClassName("com.commonwsare.android.arXiv", "com.commonsware.android.arXiv.rsslistwindow");
        myIntent.putExtra("keyname", shortitems[position]);
        myIntent.putExtra("keyurl", urls[position]);
        startActivity(myIntent);
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info;
        try {
                info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
                //Log.e(TAG, "bad menuInfo", e);
                return;
        }
        menu.add(0, 1000, 0, "Add to Favorites");

    }

    public boolean onContextItemSelected (MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;
        try {
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
                //Log.e(TAG, "bad menuInfo", e);
                return false;
        }
        //long id = catlist.getAdapter().getItemId(info.position);

        arXivDB droidDB = new arXivDB(this);

        boolean vcomplete = droidDB.insertFeed(items[info.position],shortitems[info.position],urls[info.position]);
	return true;

    }

}
