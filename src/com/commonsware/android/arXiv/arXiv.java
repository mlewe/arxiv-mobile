/*
    arXiv Droid - a Free arXiv app for android
    http://www.jdeslippe.com/arxivdroid

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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.ListView;
import android.app.ListActivity;
import android.view.ContextMenu;
import android.widget.AdapterView;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.List;
import android.os.Build.VERSION;

public class arXiv extends Activity implements AdapterView.OnItemClickListener
{
    private Button btn;
    private TextView header;
    private ListView catlist;
    private ListView favlist;
    private arXivDB droidDB;
    private int vflag=1;

    private List<Feed> favorites;

    String[] items={"Astrophysics", "Condensed Matter", "Computer Science", "General Relativity", "HEP Experiment", "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics", "Mathematical Physics", "Misc Physics", "Nonlinear Sciences", "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology", "Quantitative Finance","Quantum Physics","Statistics"};
    int[] itemsflag={1, 2, 3, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 7, 8, 0, 9};
    String[] shortitems={"Astrophysics", "Condensed Matter", "Computer Science", "General Relativity", "HEP Experiment", "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics", "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.", "Nuclear Theory", "Quant. Biology", "Quant. Finance","Quantum Physics","Statistics"};
    String[] urls={"astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex", "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics", "nlin", "nucl-ex", "nucl-th","q-bio","q-fin","quant-ph","stat"};

    String[] asitems={"Astrophysics All", "Cosmology and Extragalactic Atrophysics", "Earth & Planetary Astrophysics", "Galaxy Astrophysics", "HE Astrophysical Phenomena", "Instrumentation and Methods for Astrophysics", "Solar and Stellar Astrophysics"};
    String[] asurls={"astro-ph", "astro-ph.CO", "astro-ph.EP", "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR"};
    String[] asshortitems={"Astrophysics All", "Cosmology and Extragalactic Atrophysics", "Earth & Planetary Astrophysics", "Galaxy Astrophysics", "HE Astrophysical Phenomena", "Instrumentation and Methods for Astrophysics", "Solar and Stellar Astrophysics"};

    String[] cmitems={"Condensed Matter All", "Disordered Systems and Neural Networks", "Materials Science", "Mesoscale and Nanoscale Physics", "Other Condensed Matter", "Quantum Gases", "Soft Condensed Matter", "Statistical Mechancics", "Strongly Correlated Electrons", "Superconductivity"};
    String[] cmurls={"cond-mat", "cond-mat.dis-nn", "cond-mat.mtrl-sci", "cond-mat.mes-hall", "cond-mat.other", "cond-mat.quant-gas", "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el", "cond-mat.supr-con"};
    String[] cmshortitems={"Condensed Matter All", "Disordered Systems and Neural Networks", "Materials Science", "Mesoscale and Nanoscale Physics", "Other Condensed Matter", "Quantum Gases", "Soft Condensed Matter", "Statistical Mechancics", "Strongly Correlated Electrons", "Superconductivity"};

    String[] csitems={"Computer Science All","Architecture","Artificial Intelligence","Computation and Language","Computational Complexity","Computational Engineering, Finance and Science","Computational Geometry","CS and Game Theory","Computer Vision and Pattern Recognition","Computers and Society","Cryptography and Security","Data Structures and Algorithms","Databases","Digital Libraries","Discrete Mathematics","Distributed, Parallel, and Cluster Computing","Formal Languages and Automata Theory","General Literature","Graphics","Human-Computer Interaction","Informal Retrieval","Information Theory","Learning","Logic in Computer Science","Mathematical Software","Multiagent Systems","Multimedia","Networking and Internet Architecture","Neural adn Evolutionary Computing","Numerical Analysis","Operating Systems","Other Computer Science","Performance","Programming Languages","Robotics","Software Engineering","Sound","Symbolic Computation"};
    String[] csurls={"cs","cs.AR","cs.AI","cs.CL","cs.CC","cs.CE","cs.CG","cs.GT","cs.CV","cs.CY","cs.CR","cs.DS","cs.DB","cs.DL","cs.DM","cs.DC","cs.FL","cs.GL","cs.GR","cs.HC","cs.IR","cs.IT","cs.LG","cs.LO","cs.MS","cs.MA","cs.MM","cs.NI","cs.NE","cs.NA","cs.OS","cs.OH","cs.PF","cs.PL","cs.RO","cs.SE","cs.SD","cs.SC"};
    String[] csshortitems={"Computer Science All","Architecture","Artificial Intelligence","Computation and Language","Computational Complexity","Computational Engineering, Finance and Science","Computational Geometry","CS and Game Theory","Computer Vision and Pattern Recognition","Computers and Society","Cryptography and Security","Data Structures and Algorithms","Databases","Digital Libraries","Discrete Mathematics","Distributed, Parallel, and Cluster Computing","Formal Languages and Automata Theory","General Literature","Graphics","Human-Computer Interaction","Informal Retrieval","Information Theory","Learning","Logic in Computer Science","Mathematical Software","Multiagent Systems","Multimedia","Networking and Internet Architecture","Neural adn Evolutionary Computing","Numerical Analysis","Operating Systems","Other Computer Science","Performance","Programming Languages","Robotics","Software Engineering","Sound","Symbolic Computation"};

    String[] mtitems={"Math All","Algebraic Geometry","Algebraic Topology","Analysis of PDEs","Category Theory","Classical Analysis of ODEs","Combinatorics","Commutative Algebra","Complex Variables","Differential Geometry","Dynamical Systems","Functional Analysis","General Mathematics","General Topology","Geometric Topology","Group Theory","Math History and Overview","Information Theory","K-Theory and Homology","Logic","Mathematical Physics","Metric Geomeotry","Number Theory","Numerical Analysis","Operator Algebras","Optimization and Control","Probability","Quantum Algebra","Representation Theory","Rings and Algebras","Spectral Theory","Statistics","Symplectic Geometry"};
    String[] mturls={"math","math.AG","math.AT","math.AP","math.CT","math.CA","math.CO","math.AC","math.CV","math.DG","math.DS","math.FA","math.GM","math.GN","math.GT","math.GR","math.HO","math.IT","math.KT","math.LO","math.MP","math.MG","math.NT","math.NA","math.OA","math.OC","math.PR","math.QA","math.RT","math.RA","math.SP","math.ST","math.SG"};
    String[] mtshortitems={"Math All","Algebraic Geometry","Algebraic Topology","Analysis of PDEs","Category Theory","Classical Analysis of ODEs","Combinatorics","Commutative Algebra","Complex Variables","Differential Geometry","Dynamical Systems","Functional Analysis","General Mathematics","General Topology","Geometric Topology","Group Theory","Math History and Overview","Information Theory","K-Theory and Homology","Logic","Mathematical Physics","Metric Geomeotry","Number Theory","Numerical Analysis","Operator Algebras","Optimization and Control","Probability","Quantum Algebra","Representation Theory","Rings and Algebras","Spectral Theory","Statistics","Symplectic Geometry"};

    String[] mpitems={"Physics (Misc) All","Accelerator Physics","Atmospheric and Oceanic Physics","Atomic Physics","Atomic and Molecular Clusters","Biologcial Physics","Chemical Physics","Classical Physics","Computational Physics","Data Analysis, Statistics, and Probability","Fluid Dynamics","General Physics","Geophysics","History of Physics","Instrumentation and Dectors","Medical Physics","Optics","Physics Education","Physics and Society","Plasma Physics","Popular Physics","Space Physics"};
    String[] mpurls={"physics","physics.acc-ph","physics.ao-ph","physics.atom-ph","physics.atm-clus","physics.bio-ph","physics.chem-ph","physics.class-ph","physics.comp-ph","physics.data-an","physics.flu-dyn","physics.gen-ph","physics.geo-ph","physics.hist-ph","physics.ins-det","physics.med-ph","physics.optics","physics.ed-ph","physics.soc-ph","physics.plasm-ph","physics.pop-ph","physics.space-ph"};
    String[] mpshortitems={"Physics (Misc) All","Accelerator Physics","Atmospheric and Oceanic Physics","Atomic Physics","Atomic and Molecular Clusters","Biologcial Physics","Chemical Physics","Classical Physics","Computational Physics","Data Analysis, Statistics, and Probability","Fluid Dynamics","General Physics","Geophysics","History of Physics","Instrumentation and Dectors","Medical Physics","Optics","Physics Education","Physics and Society","Plasma Physics","Popular Physics","Space Physics"};

    String[] nlitems={"Nonlinear Sciences All","Adaptation and Self-Organizing Systems","Cellular Automata and Lattice Gases","Chaotic Dynamics","Exactly Solvable and Integrable Systems","Pattern Formation and Solitons"};
    String[] nlurls={"nlin","nlin.AO","nlin.CG","nlin.CD","nlin.SI","nlin.PS"};
    String[] nlshortitems={"Nonlinear Sciences","Adaptation and Self-Organizing Systems","Cellular Automata and Lattice Gases","Chaotic Dynamics","Exactly Solvable and Integrable Systems","Pattern Formation and Solitons"};

    String[] qbitems={"Quant. Biology All","Biomolecules","Cell Behavior","Genomics","Molecular Networks","Neurons and Cognition","Quant. Biology Other","Populations and Evolutions","Quantitative Methods","Subcellular Processes","Tissues and Organs"};
    String[] qburls={"q-bio","q-bio.BM","q-bio.CB","q-bio.GN","q-bio.MN","q-bio.NC","q-bio.OT","q-bio.PE","q-bio.QM","q-bio.SC","q-bio.TO"};
    String[] qbshortitems={"Quant. Bio. All","Biomolecules","Cell Behavior","Genomics","Molecular Networks","Neurons and Cognition","QB Other","Populations and Evolutions","Quantitative Methods","Subcellular Processes","Tissues and Organs"};

    String[] qfitems={"Quant. Finance All","Computational Finance","General Finance","Portfolio Management","Pricing and Securities","Risk Management","Statistical Finance","Trading and Market Microstructure"};
    String[] qfurls={"q-fin","q-fin.CP","q-fin.GN","q-fin.PM","q-fin.PR","q-fin.RM","q-fin.ST","q-fin.TR"};
    String[] qfshortitems={"Quant. Fin. All","Computational Finance","General Finance","Portfolio Management","Pricing and Securities","Risk Management","Statistical Finance","Trading and Market Microstructure"};

    String[] stitems={"Statistics All","Applications","Computation","Machine Learning","Methodology","Theory"};
    String[] sturls={"stat","stat.AP","stat.CO","stat.ML","stat.ME","stat.TH"};
    String[] stshortitems={"Statistics","Applications","Computation","Machine Learning","Methodology","Theory"};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	int version = android.os.Build.VERSION.SDK_INT;

	if (version > 6) {
	        setContentView(R.layout.mainnew);
	} else {
	        setContentView(R.layout.main);
	}

        //btn=(Button)findViewById(R.id.button);

	Resources res = getResources();

        header=(TextView)findViewById(R.id.theader);
        catlist=(ListView)findViewById(R.id.catlist);
        favlist=(ListView)findViewById(R.id.favlist);
	catlist.setOnItemClickListener(this);
	favlist.setOnItemClickListener(this);

        Typeface face=Typeface.createFromAsset(getAssets(), "fonts/LiberationSans.ttf");
        header.setTypeface(face);

	TabHost tabs=(TabHost)findViewById(R.id.tabhost);
	tabs.setup();
	TabHost.TabSpec spec=tabs.newTabSpec("tag1");
	spec.setContent(R.id.catlist);
	spec.setIndicator("Categories",res.getDrawable(R.drawable.cat));
	tabs.addTab(spec);
	spec=tabs.newTabSpec("tag2");
	spec.setContent(R.id.favlist);
	spec.setIndicator("Favorites",res.getDrawable(R.drawable.fav));
	tabs.addTab(spec);

        catlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,items));
	registerForContextMenu(catlist);

	droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

        List<String> lfavorites = new ArrayList<String>();
	for (Feed feed : favorites) {
		lfavorites.add(feed.title);
        }

        favlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,lfavorites));
	registerForContextMenu(favlist);

	//header.setText(""+version);

    }

    //public void pressedMainButton(View button) {
    //    Intent myIntent = new Intent(this,rsslistwindow.class);
    //    myIntent.putExtra("keyurl", "Hello, Jack!");
    //    startActivity(myIntent);
    //}

    public void onItemClick(AdapterView<?> a, View v, int position,long id) {

	if (a.getId() == R.id.favlist) {
		//header.setText("IN FAVLIST");

		String tempname = "";
		String tempurl = "";

		droidDB = new arXivDB(this);
        	favorites = droidDB.getFeeds();

		int icount = 0;
		for (Feed feed : favorites) {
			if (icount == position) {
				tempname = feed.shorttitle;
				tempurl = feed.url;
			}
			icount++;
		}

		//header.setText(tempname+tempurl);

	        Intent myIntent = new Intent(this,rsslistwindow.class);
        	myIntent.putExtra("keyname", tempname);
        	myIntent.putExtra("keyurl", tempurl);
        	startActivity(myIntent);
	} else {
		//header.setText("NOT IN FAVLIST");
		if (itemsflag[position] == 0) {
		        Intent myIntent = new Intent(this,rsslistwindow.class);
        		myIntent.putExtra("keyname", shortitems[position]);
        		myIntent.putExtra("keyurl", urls[position]);
        		startActivity(myIntent);
		} else {
		        Intent myIntent = new Intent(this,subarXiv.class);
        		myIntent.putExtra("keyname", shortitems[position]);

			switch (itemsflag[position]) {

				case 1:
		        		myIntent.putExtra("keyitems", asitems);
        				myIntent.putExtra("keyurls", asurls);
        				myIntent.putExtra("keyshortitems", asshortitems);
					break;

				case 2:
		        		myIntent.putExtra("keyitems", cmitems);
        				myIntent.putExtra("keyurls", cmurls);
        				myIntent.putExtra("keyshortitems", cmshortitems);
					break;

				case 3:
		        		myIntent.putExtra("keyitems", csitems);
        				myIntent.putExtra("keyurls", csurls);
        				myIntent.putExtra("keyshortitems", csshortitems);
					break;

				case 4:
		        		myIntent.putExtra("keyitems", mtitems);
        				myIntent.putExtra("keyurls", mturls);
        				myIntent.putExtra("keyshortitems", mtshortitems);
					break;

				case 5:
		        		myIntent.putExtra("keyitems", mpitems);
        				myIntent.putExtra("keyurls", mpurls);
        				myIntent.putExtra("keyshortitems", mpshortitems);
					break;

				case 6:
		        		myIntent.putExtra("keyitems", nlitems);
        				myIntent.putExtra("keyurls", nlurls);
        				myIntent.putExtra("keyshortitems", nlshortitems);
					break;

				case 7:
		        		myIntent.putExtra("keyitems", qbitems);
        				myIntent.putExtra("keyurls", qburls);
        				myIntent.putExtra("keyshortitems", qbshortitems);
					break;

				case 8:
		        		myIntent.putExtra("keyitems", qfitems);
        				myIntent.putExtra("keyurls", qfurls);
        				myIntent.putExtra("keyshortitems", qfshortitems);
					break;

				case 9:
		        		myIntent.putExtra("keyitems", stitems);
        				myIntent.putExtra("keyurls", sturls);
        				myIntent.putExtra("keyshortitems", stshortitems);
					break;


			}

        		startActivity(myIntent);
		}
	}
    }

    //public void onItemClick(AdapterView<?> a, View v, int position,long id) {
    //    Intent myIntent = new Intent(this,rsslistwindow.class);
    //    myIntent.putExtra("keyname", shortitems[position]);
    //    myIntent.putExtra("keyurl", urls[position]);
    //    startActivity(myIntent);
    //}

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

	AdapterView.AdapterContextMenuInfo info;
	try {
    		info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	} catch (ClassCastException e) {
    		//Log.e(TAG, "bad menuInfo", e);
    		return;
	}
	//long id = catlist.getAdapter().getItemId(info.position);
	if (view.getId() == R.id.favlist) {
		menu.add(0, 1000, 0, "Remove From Favorites");
		vflag = 0;
	} else {
		menu.add(0, 1000, 0, "Add to Favorites");
		vflag = 1;
	}
    }

    public boolean onContextItemSelected (MenuItem item) {

	AdapterView.AdapterContextMenuInfo info;
	try {
		info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	} catch (ClassCastException e) {
    		//Log.e(TAG, "bad menuInfo", e);
    		return false;
	}
	long id = catlist.getAdapter().getItemId(info.position);
	//header.setText(" "+info.position);

	droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

	//String tempt = urls[info.position]+" ";

	int icount = 0;
	if (vflag == 0) {
		for (Feed feed : favorites) {
			//tempt = tempt + feed.url + " ";
			if (icount == info.position) {
				boolean vcomplete = droidDB.deleteFeed(feed.feedId);
			}
			icount++;
		}
       	} else {
		boolean vcomplete = droidDB.insertFeed(items[info.position],shortitems[info.position],urls[info.position]);
	}

	//header.setText(tempt);

        favorites = droidDB.getFeeds();

        List<String> lfavorites = new ArrayList<String>();
	for (Feed feed : favorites) {
		lfavorites.add(feed.title);
        }

        favlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,lfavorites));

	return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();

	droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

        List<String> lfavorites = new ArrayList<String>();
	for (Feed feed : favorites) {
		lfavorites.add(feed.title);
        }

        favlist.setAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1,lfavorites));

    }


}
