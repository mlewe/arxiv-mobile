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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SingleItemWindow extends SherlockActivity {

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
    private String pdfPath;
    private Boolean vStorage;
    private Boolean vLoop = false;
    private Boolean typeset;
    private ProgressBar progBar;
    private Context thisActivity;
    private arXivDB droidDB;
    private int numberOfAuthors;
    private int fontSize;
    private Handler handlerNoViewer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast
                    .makeText(
                            thisActivity,
                            R.string.install_reader,
                            Toast.LENGTH_SHORT).show();
        }
    };
    private Handler handlerNoStorage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(thisActivity,
                    R.string.no_storage,
                    Toast.LENGTH_SHORT).show();
        }
    };
    private Handler handlerFailed = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(thisActivity, R.string.download_failed,
                    Toast.LENGTH_SHORT).show();
        }
    };
    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBarIndeterminateVisibility(false);
        }
    };

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
                fontSize = fontSize + 2;
                refreshLinLay();
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                return (true);
            case DECREASE_ID:
                fontSize = fontSize - 2;
                refreshLinLay();
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                return (true);
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

        String authortext = author.replace("  ", "");
        authortext = authortext.replace(" ", "+").replace("-", "_");
        authortext = "search_query=au:%22" + authortext + "%22";
        // String urlad =
        // "http://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        String urlad = "http://export.arxiv.org/api/query?search_query="
                + authortext
                + "&sortBy=lastUpdatedDate&sortOrder=descending&s}tart=0&max_results=20";
        // header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", authortext);
        startActivity(myIntent);
    }

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

        thisActivity = this;

        droidDB = new arXivDB(this);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 16;
            droidDB.changeSize(fontSize);
        }
        droidDB.close();

        refreshLinLay();

        if (android.os.Build.VERSION.SDK_INT > 6) {
            setProgressBarIndeterminateVisibility(true);
            printSize();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vLoop = false;
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

        int version = android.os.Build.VERSION.SDK_INT;

        if (version > 6) {

            Thread t = new Thread() {
                public void run() {
                    try {

                        vStorage = false;

                        String storagePath = Environment.getExternalStorageDirectory() + "/arXiv";
                        Log.d("arXiv - ", "Storage path: " + storagePath);

                        File fare = new File(storagePath);
                        boolean success = fare.mkdir();

                        Log.d("arXiv - ", "Storage path: " + success);

                        if (fare.exists()) {
                            pdfPath = storagePath + "/";
                            vStorage = true;
                        } else {
                            File efare = new File("/mnt/sdcard/arXiv");
                            efare.mkdir();
                            if (efare.exists()) {
                                pdfPath = "/mnt/sdcard/arXiv/";
                                vStorage = true;
                            } else {
                                efare = new File("/emmc/arXiv");
                                efare.mkdir();
                                if (efare.exists()) {
                                    pdfPath = "/emmc/arXiv/";
                                    vStorage = true;
                                } else {
                                    efare = new File("/media/arXiv");
                                    efare.mkdir();
                                    if (efare.exists()) {
                                        pdfPath = "/media/arXiv/";
                                        vStorage = true;
                                    }
                                }
                            }
                        }

                        if (vStorage) {

                            vLoop = true;
                            fileSizeTextView.post(new Runnable() {
                                public void run() {
                                    fileSizeTextView.setVisibility(View.GONE);
                                }
                            });
                            progBar.post(new Runnable() {
                                public void run() {
                                    progBar.setVisibility(View.VISIBLE);
                                }
                            });

                            String pdfaddress = link.replace("abs", "pdf");

                            URL u = new URL(pdfaddress);
                            HttpURLConnection c = (HttpURLConnection) u
                                    .openConnection();
                            c.setRequestMethod("GET");
                            c.setDoOutput(true);
                            c.connect();

                            final long ifs = c.getContentLength();
                            InputStream in = c.getInputStream();

                            String filepath = pdfPath;

                            String filename = title.replace(":", "");
                            filename = filename.replace("?", "");
                            filename = filename.replace("*", "");
                            filename = filename.replace("/", "");
                            filename = filename.replace(". ", "");
                            filename = filename.replace("`", "");
                            filename = filename + ".pdf";

                            Boolean vdownload = true;
                            File futureFile = new File(filepath, filename);
                            if (futureFile.exists()) {
                                final long itmp = futureFile.length();
                                if (itmp == ifs && itmp != 0) {
                                    vdownload = false;
                                }
                            }

                            if (vdownload) {
                                FileOutputStream f = new FileOutputStream(
                                        new File(filepath, filename));

                                byte[] buffer = new byte[1024];
                                int len1 = 0;
                                long i = 0;
                                while ((len1 = in.read(buffer)) > 0) {
                                    if (!vLoop) break;
                                    f.write(buffer, 0, len1);
                                    i += len1;
                                    long jt = 100 * i / ifs;
                                    final int j = (int) jt;
                                    progBar.post(new Runnable() {
                                        public void run() {
                                            progBar.setProgress(j);
                                        }
                                    });
                                }
                                f.close();
                            } else {
                                progBar.post(new Runnable() {
                                    public void run() {
                                        progBar.setProgress(100);
                                    }
                                });
                            }

                            if (vLoop) {
                                if (vdownload) {
                                    droidDB = new arXivDB(thisActivity);
                                    String displaytext = title;
                                    for (int i = 0; i < numberOfAuthors; i++) {
                                        displaytext = displaytext + " - "
                                                + authors[i];
                                    }
                                    droidDB.insertHistory(displaytext, filepath
                                            + filename);
                                    droidDB.close();
                                }

                                final File file = new File(filepath + filename);

                                fileSizeTextView.post(new Runnable() {
                                    public void run() {

                                        String[] optionsList = new String[2];
                                        optionsList[0] = "View PDF";
                                        optionsList[1] = "Print PDF";

                                        final AlertDialog d = new AlertDialog.Builder(thisActivity)
                                                .setItems(optionsList, null)
                                                .setIcon(R.drawable.icon)
                                                .setTitle("View or Print PDF?")
                                                .create();

                                        d.show();

                                        //d.getListView().setAdapter(
                                        // new ArrayAdapter<String>(
                                        // thisActivity, android.R.layout.simple_list_item_1, optionsList)
                                        //);

                                        d.getListView().setOnItemClickListener(
                                                new OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(
                                                            AdapterView<?> av, View v, int pos, long id
                                                    ) {

                                                        Intent myIntent;
                                                        if (pos == 0) {
                                                            myIntent = new Intent();
                                                            myIntent.setAction(android.content.Intent.ACTION_VIEW);
                                                        } else {
                                                            myIntent = new Intent(thisActivity, PrintDialogActivity.class);
                                                            myIntent.putExtra("title", "arXiv");
                                                        }
                                                        myIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
                                                        try {
                                                            startActivity(myIntent);
                                                        } catch (ActivityNotFoundException e) {
                                                            handlerNoViewer.sendEmptyMessage(0);
                                                        }

                                                        d.dismiss();
                                                    }
                                                }
                                        );

                                    }
                                });


                            } else {
                                File fd = new File(filepath, filename);
                                fd.delete();
                            }

                        } else {
                            handlerNoStorage.sendEmptyMessage(0);
                        }
                    } catch (Exception e) {
                        Log.d("arxiv", "error " + e);
                        e.printStackTrace();
                        handlerFailed.sendEmptyMessage(0);
                    }
                }
            };
            t.start();

        } else {
            Toast.makeText(this,
                    R.string.android_2_x_required,
                    Toast.LENGTH_SHORT).show();
            String pdfaddress = link.replace("abs", "pdf");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(pdfaddress));
            startActivity(myIntent);
        }

    }

    private void printSize() {
        Thread t4 = new Thread() {
            public void run() {

                try {
                    String pdfaddress = link.replace("abs", "pdf");

                    URL u = new URL(pdfaddress);
                    HttpURLConnection c = (HttpURLConnection) u
                            .openConnection();
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);
                    c.connect();

                    final long ifs = c.getContentLength();
                    c.disconnect();
                    final long jfs = ifs * 100 / 1024 / 1024;
                    final double rfs = (double) jfs / 100.0;
                    fileSizeTextView.post(new Runnable() {
                        public void run() {
                            fileSizeTextView.setText("Size: " + rfs + " MB");
                        }
                    });
                } catch (Exception e) {
                }
                handlerDoneLoading.sendEmptyMessage(0);
            }
        };
        t4.start();
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
            numberOfAuthors = myXMLHandler.numItems;
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
