/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe
    Copyright (C) 2013 Marius Lewerenz

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

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SingleItemWindow extends ActionBarActivity {

    public static final int SHARE_ID = Menu.FIRST + 1;
    public static final int INCREASE_ID = Menu.FIRST + 2;
    public static final int DECREASE_ID = Menu.FIRST + 3;
    //UI-Views
    private TextView titleTextView;
    private TextView abstractTextView;
    private WebView abstractWebView;
    private TextView idTextView;
    private TextView fileSizeTextView;
    private String name;
    private String title;
    private String description;
    private String creator;
    private String[] authors;
    private String link;
    private Boolean typeset;
    private ProgressBar progBar;
    private int fontSize;
    private long enqueue;
    private DownloadManager downloadManager;

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case SHARE_ID:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "arXiv Article");
                i.putExtra(Intent.EXTRA_TEXT, title + " " + link);
                startActivity(Intent.createChooser(i, getString(R.string.share)));
                return (true);
            case INCREASE_ID:
                setFontSize(fontSize + 2);
                return true;
            case DECREASE_ID:
                setFontSize(fontSize - 2);
                return true;
        }
        return (false);
    }

    public void authorPressed(View v) {
        String author;
        if (v.getTag() != null && v.getTag() instanceof String)
            author = (String) v.getTag();
        else return;

        Intent myIntent = new Intent(this, ArticleList.class);
        myIntent.putExtra("keyname", author);

        String authortext = author.replaceFirst("^\\s*(\\p{Lu}).*\\s(\\S+)\\s*$", "$2_$1");
        authortext = Utils.deAccent(authortext);
        authortext = authortext.replace("-", "_");
        // String urlad =
        // "http://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        String urlad = "http://export.arxiv.org/api/query?search_query=au:+" + authortext
                + "&sortBy=lastUpdatedDate&sortOrder=descending&s}tart=0&max_results=20";
        // header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", "search_query=au:+" + authortext);
        startActivity(myIntent);
    }

    private void setFontSize(int size) {
        if (size > 20) size = 20;
        if (size < 12) size = 12;
        fontSize = size;
        refreshLinLay();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt("fontSize", size);
        if (Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        fontSize = prefs.getInt("fontSize", 16);
        typeset = (prefs.getBoolean("typeset", true) && description.contains("$"));

        if (typeset) {
            setContentView(R.layout.singleitem_html);
            abstractWebView = (WebView) findViewById(R.id.abstract_webview);
            abstractWebView.getSettings().setJavaScriptEnabled(true);

            // Workaround for older android versions, see:
            //  https://groups.google.com/forum/?hl=en#!topic/android-developers/cW19T5rCO6M
            abstractWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        } else {
            setContentView(R.layout.singleitem_plain);

            // this stuff should probably go into the loader
            description = description.replaceAll("\\s+", " ");
            description = description.replaceAll("^\\s+", "");
            description = description.replaceAll("\\s*<p>\\s*", "");
            description = description.replaceAll("\\s*</p>\\s*", "\n");

            abstractTextView = (TextView) findViewById(R.id.abstract_text);
        }

        ActionBar ab = getSupportActionBar();
        ab.setTitle(name);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        progBar = (ProgressBar) findViewById(R.id.pbar);
        fileSizeTextView = (TextView) findViewById(R.id.tsize);
        titleTextView = (TextView) findViewById(R.id.title_text);
        idTextView = (TextView) findViewById(R.id.id_text);

        refreshLinLay();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)
                        && intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0) == enqueue) {
                    unregisterReceiver(this);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = downloadManager.query(query);
                    if (c != null && c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String s = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            Uri uri = Uri.parse(s);
                            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                            viewIntent.setDataAndType(uri, "application/pdf");
                            viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            try {
                                startActivity(viewIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(SingleItemWindow.this, R.string.install_reader, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, SHARE_ID, Menu.NONE, R.string.share);
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
    }

    public void pressedPDFButton(View button) {
        String s = link.replace("abs", "pdf") + ".pdf";
        String file = s.substring(s.lastIndexOf("/") + 1);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
        request.setTitle(title);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, file);
        enqueue = downloadManager.enqueue(request);
    }

    public void refreshLinLay() {
        titleTextView.setText(title);
        titleTextView.setTextSize(fontSize);

        // The Below Gets the Authors Names
        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                + creator + "\n</begin>";
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            XMLHandlerCreator myXMLHandler = new XMLHandlerCreator();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(new StringReader(creatort)));
            authors = new String[myXMLHandler.numItems];
            LinearLayout authorLL = (LinearLayout) findViewById(R.id.authorlist);
            authorLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            for (int i = 0; i < myXMLHandler.numItems; i++) {
                authors[i] = myXMLHandler.creators[i] + "  ";
                TextView temptv = (TextView) inflater.inflate(R.layout.author, null);
                if (temptv != null) {
                    temptv.setText("   " + authors[i]);
                    temptv.setTextSize(fontSize);
                    temptv.setTag(authors[i]);
                    authorLL.addView(temptv);
                }
            }

        } catch (Exception e) {
            authors = new String[0];
        }

        if (typeset) {
            abstractWebView.getSettings().setDefaultFontSize(fontSize);
            abstractWebView.loadDataWithBaseURL("http://bar", "<style type='text/css'>*{color:white;background-color:" +
                    String.format("#%06x", (0xFFFFFF & getResources().getColor(R.color.back4))) + ";}</style>" +
                    "<script type='text/x-mathjax-config'>" +
                    "MathJax.Hub.Config({ " +
                    "showMathMenu: false, " +
                    "jax: ['input/TeX','output/HTML-CSS'], " +
                    "extensions: ['tex2jax.js'], " +
                    "tex2jax: { inlineMath: [['$','$']] }, " +
                    "TeX: { extensions: ['AMSmath.js','AMSsymbols.js','noErrors.js','noUndefined.js'] } " +
                    "});</script>" +
                    "<script type='text/javascript' src='file:///android_asset/MathJax/MathJax.js'></script>" +
                    "<body style='margin:0;padding:0;'>" +
                    description + "</body>", "text/html", "utf-8", "");
        } else {
            abstractTextView.setText(description);
            abstractTextView.setTextSize(fontSize);
        }

        idTextView.setText("arxiv-id: " + link.substring(link.lastIndexOf("/") + 1));
        idTextView.setTextSize(fontSize);
    }

}
