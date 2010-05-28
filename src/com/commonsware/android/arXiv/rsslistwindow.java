package com.commonsware.android.arXiv;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.graphics.Typeface;

public class rsslistwindow extends Activity
{
    private TextView txt;
    private TextView header;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        Intent myIntent = getIntent();
        String name = myIntent.getStringExtra("keyname");
        String url = myIntent.getStringExtra("keyurl");
        url = "http://export.arxiv.org/rss/"+url;

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        header.setText(" "+name);

        txt=(TextView)findViewById(R.id.txt);
        txt.setText(url);


    }
}
