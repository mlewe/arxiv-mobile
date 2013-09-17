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
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import java.util.List;

public abstract class arXivLoader extends AsyncTaskLoader<List<ArticleList.Item>> {
    public arXivLoader(Context context) {
        super(context);
    }

    abstract public int getTotalCount();

    abstract public String getErrorMsg();

    abstract public boolean hasError();

    public static class arXivLoaderManager implements LoaderManager.LoaderCallbacks<List<ArticleList.Item>> {
        private LoaderManager loaderManager;
        private arXivLoaderCallbacks callbacks;
        public arXivLoaderManager(LoaderManager loaderManager) {
            this.loaderManager = loaderManager;
        }

        public arXivLoader initLoader(int i, arXivLoaderCallbacks callbacks) {
            this.callbacks = callbacks;
            Loader loader = loaderManager.initLoader(i, null, this);
            if (loader instanceof arXivLoader)
                return (arXivLoader) loader;
            else return null;
        }

        public arXivLoader restartLoader(int i, arXivLoaderCallbacks callbacks) {
            this.callbacks = callbacks;
            Loader loader = loaderManager.restartLoader(i, null, this);
            if (loader instanceof arXivLoader)
                return (arXivLoader) loader;
            else return null;
        }

        @Override
        public Loader<List<ArticleList.Item>> onCreateLoader(int i, Bundle bundle) {
            return callbacks.onCreateLoader(i);
        }

        @Override
        public void onLoadFinished(Loader<List<ArticleList.Item>> listLoader, List<ArticleList.Item> items) {
            if (listLoader instanceof arXivLoader)
                callbacks.onLoadFinished((arXivLoader) listLoader, items);
        }

        @Override
        public void onLoaderReset(Loader<List<ArticleList.Item>> listLoader) {
            if (listLoader instanceof arXivLoader)
                callbacks.onLoaderReset((arXivLoader) listLoader);
        }
    }

    public interface arXivLoaderCallbacks {
        arXivLoader onCreateLoader(int i);

        void onLoadFinished(arXivLoader listLoader, List<ArticleList.Item> items);

        void onLoaderReset(arXivLoader listLoader);
    }
}
