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

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CategoriesListFragment extends ListFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    static String[] items = {"Astrophysics", "Condensed Matter", "Computer Science",
            "General Relativity", "HEP Experiment", "HEP Lattice",
            "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Mathematical Physics", "Misc Physics", "Nonlinear Sciences",
            "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology",
            "Quantitative Finance", "Quantum Physics", "Statistics"};
    static int[] itemsFlag = {1, 2, 3, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 7, 8, 0, 9};
    static String[] shortItems = {"Astrophysics", "Condensed Matter",
            "Computer Science", "General Relativity", "HEP Experiment",
            "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.",
            "Nuclear Theory", "Quant. Biology", "Quant. Finance",
            "Quantum Physics", "Statistics"};
    static String[] urls = {"astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex",
            "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics",
            "nlin", "nucl-ex", "nucl-th", "q-bio", "q-fin", "quant-ph", "stat"};
    static String[] asItems = {"Astrophysics All",
            "Cosmology and Extragalactic Astrophysics",
            "Earth & Planetary Astrophysics", "Galaxy Astrophysics",
            "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics"};
    static String[] asURLs = {"astro-ph", "astro-ph.CO", "astro-ph.EP",
            "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR"};
    static String[] asShortItems = {"Astrophysics All",
            "Cosm. & Ext-Gal. Astrophysics", "Earth & Planetary Astrophysics",
            "Galaxy Astrophysics", "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics"};
    static String[] cmItems = {"Condensed Matter All",
            "Disordered Systems and Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity"};
    static String[] cmURLs = {"cond-mat", "cond-mat.dis-nn", "cond-mat.mtrl-sci",
            "cond-mat.mes-hall", "cond-mat.other", "cond-mat.quant-gas",
            "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el",
            "cond-mat.supr-con"};
    static String[] cmShortItems = {"Cond. Matter All",
            "Disord. Systems & Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity"};
    static String[] csItems = {"Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Computational Engineering, Finance and Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation"};
    static String[] csURLs = {"cs", "cs.AR", "cs.AI", "cs.CL", "cs.CC", "cs.CE",
            "cs.CG", "cs.GT", "cs.CV", "cs.CY", "cs.CR", "cs.DS", "cs.DB",
            "cs.DL", "cs.DM", "cs.DC", "cs.FL", "cs.GL", "cs.GR", "cs.HC",
            "cs.IR", "cs.IT", "cs.LG", "cs.LO", "cs.MS", "cs.MA", "cs.MM",
            "cs.NI", "cs.NE", "cs.NA", "cs.OS", "cs.OH", "cs.PF", "cs.PL",
            "cs.RO", "cs.SE", "cs.SD", "cs.SC"};
    static String[] csShortItems = {"Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Comp. Eng., Fin. & Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation"};
    static String[] mtItems = {"Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry"};
    static String[] mtURLs = {"math", "math.AG", "math.AT", "math.AP", "math.CT",
            "math.CA", "math.CO", "math.AC", "math.CV", "math.DG", "math.DS",
            "math.FA", "math.GM", "math.GN", "math.GT", "math.GR", "math.HO",
            "math.IT", "math.KT", "math.LO", "math.MP", "math.MG", "math.NT",
            "math.NA", "math.OA", "math.OC", "math.PR", "math.QA", "math.RT",
            "math.RA", "math.SP", "math.ST", "math.SG"};
    static String[] mtShortItems = {"Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry"};
    static String[] mpItems = {"Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics"};
    static String[] mpURLs = {"physics", "physics.acc-ph", "physics.ao-ph",
            "physics.atom-ph", "physics.atm-clus", "physics.bio-ph",
            "physics.chem-ph", "physics.class-ph", "physics.comp-ph",
            "physics.data-an", "physics.flu-dyn", "physics.gen-ph",
            "physics.geo-ph", "physics.hist-ph", "physics.ins-det",
            "physics.med-ph", "physics.optics", "physics.ed-ph",
            "physics.soc-ph", "physics.plasm-ph", "physics.pop-ph",
            "physics.space-ph"};
    static String[] mpShortItems = {"Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics"};
    static String[] nlItems = {"Nonlinear Sciences All",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons"};
    static String[] nlURLs = {"nlin", "nlin.AO", "nlin.CG", "nlin.CD", "nlin.SI",
            "nlin.PS"};
    static String[] nlShortItems = {"Nonlinear Sciences",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons"};
    static String[] qbItems = {"Quant. Biology All", "Biomolecules", "Cell Behavior",
            "Genomics", "Molecular Networks", "Neurons and Cognition",
            "Quant. Biology Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs"};
    static String[] qbURLs = {"q-bio", "q-bio.BM", "q-bio.CB", "q-bio.GN",
            "q-bio.MN", "q-bio.NC", "q-bio.OT", "q-bio.PE", "q-bio.QM",
            "q-bio.SC", "q-bio.TO"};
    static String[] qbShortItems = {"Quant. Bio. All", "Biomolecules",
            "Cell Behavior", "Genomics", "Molecular Networks",
            "Neurons and Cognition", "QB Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs"};
    static String[] qfItems = {"Quant. Finance All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure"};
    static String[] qfURLs = {"q-fin", "q-fin.CP", "q-fin.GN", "q-fin.PM",
            "q-fin.PR", "q-fin.RM", "q-fin.ST", "q-fin.TR"};
    static String[] qfShortItems = {"Quant. Fin. All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure"};
    static String[] stItems = {"Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory"};
    static String[] stURLs = {"stat", "stat.AP", "stat.CO", "stat.ML", "stat.ME",
            "stat.TH"};
    static String[] stShortItems = {"Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory"};
    private int mySourcePref = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items));
        registerForContextMenu(getListView());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mySourcePref = prefs.getInt("source", 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return false;
        ContentValues cv = new ContentValues();
        int id;
        if (mySourcePref == 0) {
            String tempquery = "search_query=cat:" + urls[info.position] + "*";
            String tempurl = "http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            cv.put(Feeds.TITLE, shortItems[info.position]);
            cv.put(Feeds.SHORTTITLE, tempquery);
            cv.put(Feeds.URL, tempurl);
            cv.put(Feeds.UNREAD, -1);
            cv.put(Feeds.COUNT, -1);
            id = R.string.added_to_favorites;
        } else {
            cv.put(Feeds.TITLE, shortItems[info.position] + " (RSS)");
            cv.put(Feeds.SHORTTITLE, shortItems[info.position]);
            cv.put(Feeds.URL, urls[info.position]);
            cv.put(Feeds.UNREAD, -2);
            cv.put(Feeds.COUNT, -2);
            id = R.string.added_to_favorites_rss;
        }
        cv.put(Feeds.LAST_UPDATE, 0);
        new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onInsertComplete(int id, Object cookie, Uri uri) {
                Toast.makeText(getActivity(), id, Toast.LENGTH_SHORT).show();
            }
        }.startInsert(id, null, Feeds.CONTENT_URI, cv);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.add_favorites);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (itemsFlag[position] == 0) {
            if (mySourcePref == 1) {
                Intent myIntent = new Intent(getActivity(), RSSListWindow.class);
                myIntent.putExtra("keyname", shortItems[position]);
                myIntent.putExtra("keyurl", urls[position]);
                startActivity(myIntent);
            } else {
                Intent myIntent = new Intent(getActivity(), ArticleList.class);
                myIntent.putExtra("keyname", shortItems[position]);
                String tempquery = "search_query=cat:" + urls[position] + "*";
                myIntent.putExtra("keyquery", tempquery);
                String tempurl = "http://export.arxiv.org/api/query?"
                        + tempquery
                        + "&sortBy=submittedDate&sortOrder=ascending";
                myIntent.putExtra("keyurl", tempurl);
                startActivity(myIntent);
            }
        } else {
            Intent myIntent = new Intent(getActivity(), SubarXiv.class);
            myIntent.putExtra("keyname", shortItems[position]);

            switch (itemsFlag[position]) {
                case 1:
                    myIntent.putExtra("keyitems", asItems);
                    myIntent.putExtra("keyurls", asURLs);
                    myIntent.putExtra("keyshortitems", asShortItems);
                    break;
                case 2:
                    myIntent.putExtra("keyitems", cmItems);
                    myIntent.putExtra("keyurls", cmURLs);
                    myIntent.putExtra("keyshortitems", cmShortItems);
                    break;
                case 3:
                    myIntent.putExtra("keyitems", csItems);
                    myIntent.putExtra("keyurls", csURLs);
                    myIntent.putExtra("keyshortitems", csShortItems);
                    break;
                case 4:
                    myIntent.putExtra("keyitems", mtItems);
                    myIntent.putExtra("keyurls", mtURLs);
                    myIntent.putExtra("keyshortitems", mtShortItems);
                    break;
                case 5:
                    myIntent.putExtra("keyitems", mpItems);
                    myIntent.putExtra("keyurls", mpURLs);
                    myIntent.putExtra("keyshortitems", mpShortItems);
                    break;
                case 6:
                    myIntent.putExtra("keyitems", nlItems);
                    myIntent.putExtra("keyurls", nlURLs);
                    myIntent.putExtra("keyshortitems", nlShortItems);
                    break;
                case 7:
                    myIntent.putExtra("keyitems", qbItems);
                    myIntent.putExtra("keyurls", qbURLs);
                    myIntent.putExtra("keyshortitems", qbShortItems);
                    break;
                case 8:
                    myIntent.putExtra("keyitems", qfItems);
                    myIntent.putExtra("keyurls", qfURLs);
                    myIntent.putExtra("keyshortitems", qfShortItems);
                    break;
                case 9:
                    myIntent.putExtra("keyitems", stItems);
                    myIntent.putExtra("keyurls", stURLs);
                    myIntent.putExtra("keyshortitems", stShortItems);
                    break;
            }
            startActivity(myIntent);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("source"))
            mySourcePref = sharedPreferences.getInt("source", 0);
    }
}