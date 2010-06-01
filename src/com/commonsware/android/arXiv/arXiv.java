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
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.ListView;
import android.app.ListActivity;
import android.view.ContextMenu;
import android.widget.AdapterView;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.List;

public class arXiv extends Activity implements AdapterView.OnItemClickListener
{
    private Button btn;
    private TextView header;
    private ListView catlist;
    private ListView favlist;
    private arXivDB droidDB;
    private int vflag=1;

    private List<Feed> favorites;

    String[] items={"Astrophysics", "Condensed Matter", "Computer Science", "General Relativity", "High Energy Experiment", "High Energy Lattice", "High Energy Phenomenology", "High Energy Theory", "Mathematics", "Mathematical Physics", "Misc Physics", "Nonlinear Sciences", "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology", "Quantitative Finance","Quantum Physics","Statistics"};
    String[] shortitems={"Astrophysics", "Condensed Matter", "Computer Science", "General Relativity", "H.E. Experiment", "H.E. Lattice", "Phenomenology", "H.E. Theory", "Mathematics", "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.", "Nuclear Theory", "Quant. Biology", "Quant. Finance","Quantum Physics","Statistics"};
    String[] urls={"astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex", "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics", "nlin", "nucl-ex", "nucl-th","q-bio","q-fin","quant-ph","stat"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //btn=(Button)findViewById(R.id.button);

	Resources res = getResources();

        header=(TextView)findViewById(R.id.theader);
        catlist=(ListView)findViewById(R.id.catlist);
        favlist=(ListView)findViewById(R.id.favlist);
	catlist.setOnItemClickListener(this);

        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

	TabHost tabs=(TabHost)findViewById(R.id.tabhost);
	tabs.setup();
	TabHost.TabSpec spec=tabs.newTabSpec("tag1");
	spec.setContent(R.id.catlist);
	spec.setIndicator("Categories",res.getDrawable(R.drawable.cat));
	tabs.addTab(spec);
	spec=tabs.newTabSpec("tag2");
	spec.setContent(R.id.favlist);
	spec.setIndicator("Favorites",res.getDrawable(R.drawable.fav));
	tabs.addTab(spec);

        catlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,items));
	registerForContextMenu(catlist);

	droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

        List<String> lfavorites = new ArrayList<String>();
	for (Feed feed : favorites) {
		lfavorites.add(feed.title);
        }

        favlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,lfavorites));
	registerForContextMenu(favlist);


    }

    //public void pressedMainButton(View button) {
    //    Intent myIntent = new Intent(this,rsslistwindow.class);
    //    myIntent.putExtra("keyurl", "Hello, Jack!");
    //    startActivity(myIntent);
    //}

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
	//long id = catlist.getAdapter().getItemId(info.position);
	if (view.getId() == R.id.favlist) {
		menu.add(0, 1000, 0, "Remove From Favorites");
		vflag = 0;
	} else {
		menu.add(0, 1000, 0, "Add to Favorites");
		vflag = 1;
	}
    }

    public boolean onContextItemSelected (MenuItem item) {

	AdapterView.AdapterContextMenuInfo info;
	try {
		info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	} catch (ClassCastException e) {
    		//Log.e(TAG, "bad menuInfo", e);
    		return false;
	}
	long id = catlist.getAdapter().getItemId(info.position);
	//header.setText(" "+info.position);

	droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

	//String tempt = urls[info.position]+" ";

	int icount = 0;
	if (vflag == 0) {
		for (Feed feed : favorites) {
			//tempt = tempt + feed.url + " ";
			if (icount == info.position) {
				boolean vcomplete = droidDB.deleteFeed(feed.feedId);
			}
		icount++;
		}
       	} else {
		boolean vcomplete = droidDB.insertFeed(items[info.position],urls[info.position]);
	}

	//header.setText(tempt);

        favorites = droidDB.getFeeds();

        List<String> lfavorites = new ArrayList<String>();
	for (Feed feed : favorites) {
		lfavorites.add(feed.title);
        }

        favlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,lfavorites));

	return true;
    }

}
