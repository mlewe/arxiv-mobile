/*
    EMusicDownloader - a free Downloader Manager for eMusic purchases
    http://www.jdeslippe.com/EMusicDownloader - based loosely on the
    eMusicJ dekstop app (http://www.kallisti.net.nz/EMusicJ/HomePage).
    This application is not associated with eMusic.com in any way.

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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * SAXParser implementation for eMusic .emx xml file.
 * Based roughly on the SAXParser tutorial at the anddev forum -
 * http://www.anddev.org/parsing_xml_from_the_net_-_using_the_saxparser-t353.html
 * Assumed to be public domain.
 *
 */

public class XMLHandler extends DefaultHandler{

     // Fields

     private boolean in_items = false;
     private boolean in_rdfli = false;
     private boolean in_item = false;
     private boolean in_title = false;
     private boolean in_link = false;
     private boolean in_description = false;
     private boolean in_dccreator = false;
     public int nitems = 0;
     public String[] descriptions;
     public String[] titles;
     public String[] links;
     public String[] creators;

     // Methods

     @Override
     public void startDocument() throws SAXException {
          //Nothing to do
     }

     @Override
     public void endDocument() throws SAXException {
          //Nothing to do
     }

     //Gets be called on opening tags like: <tag>
     @Override
     public void startElement(String namespaceURI, String localName,
               String qName, Attributes atts) throws SAXException {
          if (localName.equals("TRACK")) {
               this.in_TRACK = true;
	       ntrack++;
	       dlurls[ntrack-1]="";
	       dlnames[ntrack-1]="";
          }else if (localName.equals("TRACKCOUNT")) {
               this.in_TRACKCOUNT = true;
          }else if (localName.equals("TRACKURL")) {
               this.in_TRACKURL = true;
          }else if (localName.equals("ALBUM")) {
               this.in_ALBUM = true;
          }else if (localName.equals("ARTIST")) {
               this.in_ARTIST = true;
          }else if (localName.equals("TITLE")) {
               this.in_TITLE = true;
          }else if (localName.equals("ALBUMARTLARGE")) {
               this.in_ALBUMARTLARGE = true;
          }
     }

     //Gets be called on closing tags like: </tag>
     @Override
     public void endElement(String namespaceURI, String localName, String qName)
               throws SAXException {
          if (localName.equals("TRACK")) {
               this.in_TRACK = false;
          }else if (localName.equals("TRACKCOUNT")) {
               this.in_TRACKCOUNT = false;
          }else if (localName.equals("TRACKURL")) {
               this.in_TRACKURL = false;
          }else if (localName.equals("ALBUM")) {
               this.in_ALBUM = false;
          }else if (localName.equals("ARTIST")) {
               this.in_ARTIST = false;
          }else if (localName.equals("TITLE")) {
               this.in_TITLE = false;
          }else if (localName.equals("ALBUMARTLARGE")) {
               this.in_ALBUMARTLARGE = false;
          }
     }

     //Gets be called on the following structure: <tag>characters</tag>
     @Override
     public void characters(char ch[], int start, int length) {
          if(this.in_TRACKCOUNT){
		if (ntrack == 1) {
			//Nothing to do here
		}
     	  } else if (this.in_ALBUM)  {
		if (ntrack == 1) {
			album += new String(ch, start, length);
		}
     	  } else if (this.in_ARTIST)  {
		if (ntrack == 1) {
			artist += new String(ch, start, length);
		}
     	  } else if (this.in_ALBUMARTLARGE)  {
		if (ntrack == 1) {
			albumart += new String(ch, start, length);
		}
     	  } else if (this.in_TRACKURL)  {
		if (ntrack <= 100) {
			dlurls[ntrack-1] += new String(ch, start, length);
		}
     	  } else if (this.in_TITLE)  {
		if (ntrack <= 100) {
			dlnames[ntrack-1] += new String(ch, start, length);
			//We are now adding the suffix in the getFileInfoFromXML method
			//dlnames[ntrack-1] += ".mp3";
		}
	  }
    }
}
