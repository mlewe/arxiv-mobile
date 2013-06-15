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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;

public class FavouritesListFragment extends SherlockListFragment {
    private String[] unreadList;
    private String[] favoritesList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        unreadList = new String[1];
        unreadList[0] = "1";
        favoritesList = new String[1];
        favoritesList[0] = "Test";

        setListAdapter(new myCustomAdapter());
    }

    class myCustomAdapter extends ArrayAdapter {

        myCustomAdapter() {
            super(getActivity(), R.layout.favoritesrow, favoritesList);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ViewHolder holder;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(com.commonsware.android.arXiv.R.layout.favoritesrow, parent, false);
                holder = new ViewHolder();
                holder.text1 = (TextView) row.findViewById(com.commonsware.android.arXiv.R.id.text1);
                holder.text2 = (TextView) row.findViewById(com.commonsware.android.arXiv.R.id.text2);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.text1.setText(unreadList[position]);
            holder.text2.setText(favoritesList[position]);
            return (row);

        }

        public class ViewHolder {
            public TextView text1;
            public TextView text2;
        }

    }

}