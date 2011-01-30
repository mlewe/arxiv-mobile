/*
    arXiv Droid - a Free arXiv app for android
    http://launchpad.net/arxivdroid

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SingleItemWindow extends Activity implements View.OnClickListener {

    //UI-Views
    private LinearLayout linLay;
    private ScrollView scrollView;
    private TextView titleTextView;
    private TextView abstractTextView;
    private TextView headerTextView;
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
    private ProgressBar progBar;
    private Context thisActivity;
    private arXivDB droidDB;
    private int numberOfAuthors;
    private int fontSize;

    public static final int SHARE_ID = Menu.FIRST + 1;
    public static final int INCREASE_ID = Menu.FIRST + 2;
    public static final int DECREASE_ID = Menu.FIRST + 3;

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

    public void onClick(View v) {
        final int position = v.getId() - 1000;

        Intent myIntent = new Intent(this, SearchListWindow.class);
        myIntent.putExtra("keyname", authors[position]);

        String authortext = authors[position].replace("  ", "");
        authortext = authortext.replace(" ", "+").replace("-", "_");
        authortext = "search_query=au:%22" + authortext + "%22";
        // String urlad =
        // "http://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        String urlad = "http://export.arxiv.org/api/query?search_query="
                + authortext
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        // header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", authortext);
        startActivity(myIntent);

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.singleitem);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");

        progBar = (ProgressBar) findViewById(R.id.pbar); // Progressbar for
                                                         // download

        fileSizeTextView = (TextView) findViewById(R.id.tsize);

        headerTextView = (TextView) findViewById(R.id.theadersi);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/LiberationSans.ttf");
        headerTextView.setTypeface(face);

        headerTextView.setText(name);

        description = description.replace("\n", "");
        description = description.replace("<p>", "");
        description = description.replace("</p>", "");

        titleTextView = new TextView(this);
        abstractTextView = new TextView(this);

        thisActivity = this;

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 16;
            droidDB.changeSize(fontSize);
        }
        droidDB.close();

        scrollView = (ScrollView) findViewById(R.id.SV);

        refreshLinLay();

        int version = android.os.Build.VERSION.SDK_INT;

        if (version > 6) {
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

                        File fare = new File("/sdcard/arXiv");
                        fare.mkdir();

                        if (fare.exists()) {
                            pdfPath = "/sdcard/arXiv/";
                            vStorage = true;
                        } else {
                            File efare = new File("/emmc/arXiv");
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

                        if (vStorage) {

                            vLoop = true;
                            fileSizeTextView.post(new Runnable() {
                                public void run() {
                                    fileSizeTextView.setVisibility(8);
                                }
                            });
                            progBar.post(new Runnable() {
                                public void run() {
                                    progBar.setVisibility(0);
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
                                    if (vLoop == false) {
                                        break;
                                    }
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

                                Intent intent = new Intent();
                                intent
                                        .setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(filepath + filename);
                                intent.setDataAndType(Uri.fromFile(file),
                                        "application/pdf");

                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    handlerNoViewer.sendEmptyMessage(0);
                                }

                            } else {
                                File fd = new File(filepath, filename);
                                fd.delete();
                            }

                        } else {
                            handlerNoStorage.sendEmptyMessage(0);
                        }
                    } catch (Exception e) {
                        handlerFailed.sendEmptyMessage(0);
                    }
                }
            };
            t.start();

        } else {
            Toast.makeText(thisActivity,
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
        titleTextView.setPadding(5, 5, 5, 5);
        titleTextView.setTextColor(0xffffffff);

        try {
            linLay.removeAllViews();
        } catch (Exception e) {
        }

        linLay = new LinearLayout(this);
        linLay.setOrientation(1);
        linLay.addView(titleTextView);

        abstractTextView.setText("Abstract: " + description);
        abstractTextView.setPadding(5, 5, 5, 5);
        abstractTextView.setTextSize(fontSize);
        abstractTextView.setTextColor(0xffffffff);

        // The Below Gets the Authors Names
        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                + creator + "\n</begin>";
        try {

            Resources res = getResources();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            XMLHandlerCreator myXMLHandler = new XMLHandlerCreator();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(new StringReader(creatort)));
            authors = new String[myXMLHandler.numItems];
            numberOfAuthors = myXMLHandler.numItems;
            for (int i = 0; i < myXMLHandler.numItems; i++) {
                authors[i] = myXMLHandler.creators[i] + "  ";
                TextView temptv = new TextView(this);
                temptv.setText(" " + authors[i]);
                temptv.setClickable(true);
                temptv.setFocusable(true);
                temptv
                        .setBackgroundDrawable(res
                                .getDrawable(android.R.drawable.list_selector_background));
                temptv.setId(i + 1000);
                temptv.setOnClickListener(this);
                temptv.setPadding(5, 5, 5, 5);
                temptv.setTextSize(fontSize);
                temptv.setTextColor(0xffffffff);
                linLay.addView(temptv);
                View rulerin = new View(this);
                rulerin.setBackgroundColor(0xFF3f3b3b);
                linLay.addView(rulerin, new LayoutParams(320, 1));
            }

        } catch (Exception e) {
            authors = new String[0];
        }

        linLay.addView(abstractTextView);

        try {
            scrollView.removeAllViews();
        } catch (Exception e) {
        }
        scrollView.addView(linLay);
    }

}
