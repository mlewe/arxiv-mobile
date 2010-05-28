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
import android.content.Intent;

public class arXiv extends Activity
{
    private Button btn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btn=(Button)findViewById(R.id.button);
    }

    public void pressedMainButton(View button) {
	//rsslistwindow listwin = new rsslistwindow();
        Intent myIntent = new Intent(this,rsslistwindow.class);
        //myIntent.setClassName("com.commonwsare.android.arXiv", "com.commonsware.android.arXiv.rsslistwindow");
        myIntent.putExtra("keyurl", "Hello, Jack!");
        startActivity(myIntent);
    }

}
