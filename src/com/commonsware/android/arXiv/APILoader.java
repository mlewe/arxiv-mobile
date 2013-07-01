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

import android.content.Context;
import android.util.Log;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class APILoader extends arXivLoader {
    private boolean dataIsReady = false, error = false;
    private int firstResult, totalCount;
    private String url, query, errorMsg;
    private List<ArticleList.Item> list = null;

    public APILoader(Context context, String url, String query, int firstResult) {
        super(context);
        this.url = url;
        this.query = query;
        this.firstResult = firstResult;
    }

    public boolean hasError() {
        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public List<ArticleList.Item> loadInBackground() {
        try {
            URL _url = new URL(url);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(_url.openStream()));

            int numberOfResultsOnPage = myXMLHandler.numItems;
            totalCount = myXMLHandler.numTotalItems;
            int numberOfTotalResults = myXMLHandler.numTotalItems;
            final int fnmin = firstResult;
            final int fnmax = firstResult + numberOfResultsOnPage - 1;
            final int fntotalitems = numberOfTotalResults;

//            if (numberOfTotalResults > fnmax) {
//                nextButton.post(new Runnable() {
//                    public void run() {
//                        nextButton.setVisibility(View.VISIBLE);
//                    }
//                });
//            } else {
//                nextButton.post(new Runnable() {
//                    public void run() {
//                        nextButton.setVisibility(View.GONE);
//                    }
//                });
//            }
//            } else {
//                previousButton.post(new Runnable() {
//                    public void run() {
//                        previousButton.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//
//            txtInfo.post(new Runnable() {
//                public void run() {
//                    txtInfo.setText("Showing " + fnmin + " through "
//                            + fnmax + " of " + fntotalitems);
//                }
//            });

            list = new ArrayList<ArticleList.Item>(numberOfResultsOnPage);

            for (int i = 0; i < numberOfResultsOnPage; i++) {
                ArticleList.Item item = new ArticleList.Item();
                item.title = myXMLHandler.titles[i].replaceAll("\n", " ").replaceAll(" +", " ");
                item.authors = myXMLHandler.creators[i];
                item.updatedDate = myXMLHandler.updatedDates[i];
                item.publishedDate = myXMLHandler.publishedDates[i];
                item.category = myXMLHandler.categories[i];
                item.link = myXMLHandler.links[i];
                item.description = myXMLHandler.descriptions[i].replaceAll("\n", " ");
                item.text2 = "";

                String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>" + item.authors + "\n</begin>";

                try {
                    SAXParserFactory spf2 = SAXParserFactory
                            .newInstance();
                    SAXParser sp2 = spf2.newSAXParser();
                    XMLReader xr2 = sp2.getXMLReader();
                    XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                    xr2.setContentHandler(myXMLHandler2);
                    xr2.parse(new InputSource(
                            new StringReader(creatort)));
                    item.text2 += "-Authors: " + myXMLHandler2.creators[0];
                    for (int j = 1; j < myXMLHandler2.numItems; j++) {
                        item.text2 += ", " + myXMLHandler2.creators[j];
                    }
                } catch (Exception e) {
                }
                if (item.updatedDate.equals(item.publishedDate)) {
                    item.text2 += "\n-Published: " + item.publishedDate.replace("T", " ").replace("Z", "");
                } else {
                    item.text2 += "\n-Updated: " + item.updatedDate.replace("T", " ").replace("Z", "");
                    item.text2 += "\n-Published: " + item.publishedDate.replace("T", " ").replace("Z", "");
                }
                if (!query.contains(item.category) && query.contains("cat:")) {
                    item.text2 += "\n-Cross-Ref: " + item.category;
                } else if (!query.contains("cat:")) {
                    item.text2 += "\n-Category: " + item.category;
                }
                list.add(item);
            }

//            if (vFavorite && favFeed.count != numberOfTotalResults && numberOfTotalResults > 0) {
//                try {
//                    droidDB = new arXivDB(thisActivity);
//                    int unread = 0;
//                    droidDB.updateFeed(favFeed.feedId, favFeed.title, favFeed.shortTitle, favFeed.url, numberOfTotalResults, unread);
//                    droidDB.close();
//                    favFeed.count = numberOfTotalResults;
//                    updateWidget();
//                } catch (Exception enf) {
//                }
//            }
//
            error = false;
        } catch (Exception e) {
            Log.e("Arx", "error loading", e);
            error = true;
            errorMsg = e.getMessage();
        }
        if (list == null)
            list = new ArrayList<ArticleList.Item>(0);
        dataIsReady = true;
        return list;
    }

    @Override
    protected void onStartLoading() {
        if (dataIsReady) {
            deliverResult(list);
        } else {
            forceLoad();
        }
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }
}
