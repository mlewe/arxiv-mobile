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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PrintDialogActivity extends Activity {
    private static final String PRINT_DIALOG_URL =
            "http://www.google.com/cloudprint/dialog.html";
    private static final String JS_INTERFACE = "AndroidPrintDialog";
    private static final String ZXING_URL = "http://zxing.appspot.com";
    private static final int ZXING_SCAN_REQUEST = 65743;
    /**
     * Post message that is sent by Print Dialog web page when the printing
     * dialog needs to be closed.
     */
    private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";
    /**
     * Intent that started the action.
     */
    Intent cloudPrintIntent;
    /**
     * Web view element to show the printing dialog in.
     */
    private WebView dialogWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.print_dialog);
        dialogWebView = (WebView) findViewById(R.id.webview);
        cloudPrintIntent = this.getIntent();

        WebSettings settings = dialogWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        dialogWebView.setWebViewClient(new PrintDialogWebClient());
        dialogWebView.addJavascriptInterface(
                new PrintDialogJavaScriptInterface(), JS_INTERFACE);

        dialogWebView.loadUrl(PRINT_DIALOG_URL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        if (requestCode == ZXING_SCAN_REQUEST && resultCode == RESULT_OK) {
            dialogWebView.loadUrl(intent.getStringExtra("SCAN_RESULT"));
        }
    }

    final class PrintDialogJavaScriptInterface {
        public String getType() {
            return "dataUrl";
        }

        public String getTitle() {
            return cloudPrintIntent.getExtras().getString("title");
        }

        public String getContent() {
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream is = contentResolver.openInputStream(
                        cloudPrintIntent.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int n = is.read(buffer);
                while (n >= 0) {
                    baos.write(buffer, 0, n);
                    n = is.read(buffer);
                }
                is.close();
                baos.flush();

                String contentBase64 = Base64.encodeToString(
                        baos.toByteArray(), Base64.DEFAULT);
                return "data:" + cloudPrintIntent.getType() + ";base64," +
                        contentBase64;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        public void onPostMessage(String message) {
            if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
                finish();
            }
        }
    }

    private final class PrintDialogWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(ZXING_URL)) {
                Intent intentScan = new Intent(
                        "com.google.zxing.client.android.SCAN");
                intentScan.putExtra("SCAN_MODE", "QR_CODE_MODE");
                try {
                    startActivityForResult(intentScan, ZXING_SCAN_REQUEST);
                } catch (ActivityNotFoundException error) {
                    view.loadUrl(url);
                }
            } else {
                view.loadUrl(url);
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (PRINT_DIALOG_URL.equals(url)) {
                // Submit print document.
                view.loadUrl("javascript:printDialog.setPrintDocument("
                        + "printDialog.createPrintDocument(window."
                        + JS_INTERFACE + ".getType(),window." + JS_INTERFACE
                        + ".getTitle(),window." + JS_INTERFACE
                        + ".getContent()))");

                // Add post messages listener.
                view.loadUrl("javascript:window.addEventListener('message'," +
                        "function(evt){window." + JS_INTERFACE +
                        ".onPostMessage(evt.data)}, false)");
            }
        }
    }
}
