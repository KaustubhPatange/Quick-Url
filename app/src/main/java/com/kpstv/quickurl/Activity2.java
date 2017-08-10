package com.kpstv.quickurl;

import android.app.ActionBar;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Activity2 extends AppCompatActivity {
    private ProgressBar progress;
    public WebView mwebview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        final String message = b.getString("key", "");
        setContentView(R.layout.activity_2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (!isNetworkStatusAvialable(getApplicationContext())) {
             Snackbar.make(findViewById(R.id.coorder), "Check your Internet Connection !", Snackbar.LENGTH_LONG)
                    .setAction("Refresh", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mwebview.loadUrl(message);
                        }
                    })

                    .show();

        }
        mwebview = (WebView) findViewById(R.id.cwebview);
        WebSettings webSettings = mwebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mwebview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mwebview.getSettings().setCacheMode(webSettings.LOAD_CACHE_ELSE_NETWORK);
        mwebview.getSettings().setAppCacheEnabled(true);
        // mwebview.getSettings().setSupportMultipleWindows(true);
        mwebview.getSettings().setAllowFileAccess(true);
        mwebview.getSettings().setBuiltInZoomControls(true);
        mwebview.getSettings().setDisplayZoomControls(false);
        mwebview.getSettings().setLoadWithOverviewMode(true);
        mwebview.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int newProgress){
                progress.setProgress(newProgress);
                if(newProgress == 100){
                    progress.setVisibility(View.GONE);
                }
            }
        });
        //  mwebview.getSettings().setUseWideViewPort(true);
        // mwebview.loadData(mresult, "text/html; charset=UTF-8", null);
        mwebview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        progress = (ProgressBar) findViewById(R.id.pb);
        progress.setMax(100);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);
        mwebview.setWebViewClient(new WebViewClientDemo());
        mwebview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Download Started!", Toast.LENGTH_SHORT).show();
            }
        });
        mwebview.loadUrl(message);
    }
    public static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if (netInfos.isConnected())
                    return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mwebview.canGoBack()){
            mwebview.goBack();
        } else {
            super.onBackPressed();
        }

    }

    private class WebViewClientDemo extends WebViewClient {
        @Override

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            // Do something on page loading started
            // Visible the progressbar
            setTitle("Loading..");
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            setTitle(mwebview.getTitle());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Share) {
            String tmp = mwebview.getUrl();
            String test = mwebview.getTitle();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(test + "<br><br>" + tmp).toString());
            startActivity(Intent.createChooser(shareIntent, "Share Post using"));
            return true;
        }
if (id==R.id.webinfo){
    WebView test3 = (WebView) findViewById(R.id.cwebview);
    String test = test3.getTitle();
    final String tmp = test3.getUrl();
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle("Web Details");
    adb.setIcon(android.R.drawable.ic_dialog_info);
    adb.setMessage(Html.fromHtml("<b>" + "Title: " + "</b>" + test + "<br>" + "<b>" + "Url: " + "</b>" + tmp));
    adb.setPositiveButton("Copy Url", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("", tmp.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
        }
    });
    adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });
    adb.show();
    return  true;
}
if (id==R.id.openvia){
    String tmp = mwebview.getUrl();
    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
            Uri.parse(tmp));
    startActivity(browserIntent);
    return true;
}
        return super.onOptionsItemSelected(item);
    }
}
