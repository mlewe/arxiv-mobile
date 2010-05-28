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
        String url = myIntent.getStringExtra("keyurl");

        header=(TextView)findViewById(R.id.theader);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

        txt=(TextView)findViewById(R.id.txt);
        txt.setText(url);


    }
}
