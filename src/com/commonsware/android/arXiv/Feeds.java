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

import android.net.Uri;
import android.provider.BaseColumns;

public final class Feeds implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://" + arXivDBContentProvider.AUTHORITY + "/" + arXivDBContentProvider.FEEDS_TABLE);
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.arXiv." + arXivDBContentProvider.FEEDS_TABLE;
    public static final String _ID = "_id";
    public static final String TITLE = "title";
    public static final String SHORTTITLE = "shorttitle";
    public static final String URL = "url";
    public static final String COUNT = "count";
    public static final String UNREAD = "unread";
    public static final String LAST_UPDATE = "last_update";

    Feeds() {
    }
}
