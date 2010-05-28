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
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.app.ListActivity;

public class arXiv extends ListActivity
{
    private Button btn;
    private TextView header;
    String[] items={"Astrophysics", "Condensed Matter", "Quantum Physics"};
    String[] urls={"Astrophysics", "cond-mat", "Quantum Physics"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btn=(Button)findViewById(R.id.button);

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        setListAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,items));

    }

    public void pressedMainButton(View button) {
	//rsslistwindow listwin = new rsslistwindow();
        Intent myIntent = new Intent(this,rsslistwindow.class);
        //myIntent.setClassName("com.commonwsare.android.arXiv", "com.commonsware.android.arXiv.rsslistwindow");
        myIntent.putExtra("keyurl", "Hello, Jack!");
        startActivity(myIntent);
    }

    public void onListItemClick(ListView parent, View v, int position,long id) {
        //selection.setText(items[position]);
        Intent myIntent = new Intent(this,rsslistwindow.class);
        //myIntent.setClassName("com.commonwsare.android.arXiv", "com.commonsware.android.arXiv.rsslistwindow");
        myIntent.putExtra("keyname", items[position]);
        myIntent.putExtra("keyurl", urls[position]);
        startActivity(myIntent);
    }

}
