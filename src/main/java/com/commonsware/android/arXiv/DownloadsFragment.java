package com.commonsware.android.arXiv;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;


public class DownloadsFragment extends SherlockListFragment {

    private String[] fromColumns = {
            DownloadManager.COLUMN_TITLE
    };
    private int[] toFields = {
            android.R.id.text1
    };
    private SimpleCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query q = new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
        adapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                dm.query(q),
                fromColumns,
                toFields,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Cursor c = adapter.getCursor();
        c.moveToPosition(position);

        String s = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(s), "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.install_reader, Toast.LENGTH_SHORT).show();
        }
    }

}
