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
import android.widget.EditText;
import android.widget.Spinner;
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
import android.widget.AdapterView;
import android.text.TextWatcher;
import android.text.Editable;

public class searchwindow extends Activity implements AdapterView.OnItemSelectedListener, TextWatcher
{
    private TextView txt;
    private TextView header;
    private String query;
    private String query1;
    private String[] items={"Author","Title"};
    private int iselected1=0;
    private EditText field1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        header=(TextView)findViewById(R.id.theaderse);
        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");

        header.setTypeface(face);
        header.setText("Search");

	Spinner spin1=(Spinner)findViewById(R.id.spinner1);
	spin1.setOnItemSelectedListener(this);

	ArrayAdapter<String> aa=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items);
	aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spin1.setAdapter(aa);

        field1=(EditText)findViewById(R.id.field1);
	field1.addTextChangedListener(this);

    }

	public void onItemSelected(AdapterView<?> parent,
		View v, int position, long id) {
		iselected1=position;
		//selection.setText(items[position]);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	public void onTextChanged(CharSequence s, int start, int before,int count) {
		//selection.setText(edit.getText());
		String tempt = field1.getText().toString();
		if (iselected1 == 0) {
			query1="au:%22"+tempt.replace(" ","+")+"%22";
		} else if (iselected1 == 1) {
			query1="ti:%22"+tempt.replace(" ","+")+"%22";
		}
	}

	public void beforeTextChanged(CharSequence s, int start,int count, int after) {
		// needed for interface, but not used
	}

	public void afterTextChanged(Editable s) {
		// needed for interface, but not used
	}

	public void pressedSearchButton(View button) {
        	Intent myIntent = new Intent(this,searchlistwindow.class);
		String tittext = "Search Results";
	        myIntent.putExtra("keyname", tittext);
		String query = query1;
	        String urlad = "http://export.arxiv.org/api/query?search_query="+query+"&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
	        myIntent.putExtra("keyurl", urlad);
        	startActivity(myIntent);

	}

}
