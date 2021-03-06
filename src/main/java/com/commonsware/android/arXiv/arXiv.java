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

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class arXiv extends ActionBarActivity {

    public static final int ABOUT_ID = Menu.FIRST + 1;
    public static final int HISTORY_ID = Menu.FIRST + 2;
    public static final int PREF_ID = Menu.FIRST + 3;
    public static final int DONATE_ID = Menu.FIRST + 4;
    public static final int SEARCH_ID = Menu.FIRST + 5;
    //UI-Views
    private ViewPager viewPager;
    private MenuItem submenu;
    private Menu menu;

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case ABOUT_ID:
                String str = getString(R.string.about_text);
                TextView wv = new TextView(this);
                wv.setPadding(16, 0, 16, 16);
                wv.setText(str);

                ScrollView scwv = new ScrollView(this);
                scwv.addView(wv);

                Dialog dialog = new Dialog(this) {
                    public boolean onKeyDown(int keyCode, KeyEvent event) {
                        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT)
                            this.dismiss();
                        return true;
                    }
                };
                dialog.setTitle(R.string.about_arxiv_droid);
                dialog.addContentView(scwv, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                dialog.show();
                return (true);
            case HISTORY_ID:
                Intent myIntent = new Intent(this, DownloadsActivity.class);
                startActivity(myIntent);
                return (true);
            case PREF_ID:
                if (Build.VERSION.SDK_INT >= 11) {
                    startActivity(new Intent(this, EditPreferences.class));
                } else {
                    startActivity(new Intent(this, EditPreferencesCompat.class));
                }
                return (true);
            case DONATE_ID:
                Intent goToMarket = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.jd.android.arXiv"));
                try {
                    startActivity(goToMarket);
                } catch (Exception ef) {
                    Toast.makeText(this, "Market Not Installed", Toast.LENGTH_SHORT).show();
                }
                return (true);
            case SEARCH_ID:
                Intent search = new Intent(this, SearchWindow.class);
                startActivity(search);
                return (true);
        }
        return (false);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainnew);

        final ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        viewPager = (ViewPager) findViewById(R.id.mainviewpager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        final ActionBar.Tab catlistTab = ab.newTab().setText("Categories");
        final ActionBar.Tab favlistTab = ab.newTab().setText("Favorites");

        ActionBar.TabListener tl = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                if (tab == catlistTab)
                    viewPager.setCurrentItem(0);
                else
                    viewPager.setCurrentItem(1);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        ab.addTab(catlistTab.setTabListener(tl));
        ab.addTab(favlistTab.setTabListener(tl));

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    ab.selectTab(catlistTab);
                else
                    ab.selectTab(favlistTab);
            }
        });

        Intent intent = getIntent();
        if ("widget".equals(intent.getStringExtra("widget")))
            viewPager.setCurrentItem(1, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, SEARCH_ID, Menu.NONE, R.string.search)
                .setIcon(R.drawable.ic_action_action_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        populateMenu(menu);
        this.menu = menu;
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (menu != null && submenu != null) {
                    menu.performIdentifierAction(submenu.getItemId(), 0);
                    return true;
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, ABOUT_ID, Menu.NONE, R.string.about_arxiv_droid);
        menu.add(Menu.NONE, HISTORY_ID, Menu.NONE, R.string.view_history);
        menu.add(Menu.NONE, PREF_ID, Menu.NONE, R.string.preferences);
        menu.add(Menu.NONE, DONATE_ID, Menu.NONE, R.string.donate);
    }

    static class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0)
                return new CategoriesListFragment();
            else
                return new FavouritesListFragment();
        }
    }
}
