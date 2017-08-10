package com.kpstv.quickurl;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pddstudio.urlshortener.URLShortener;

import net.glxn.qrgen.android.QRCode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static com.kpstv.quickurl.DatabaseHelper.DATABASE_NAME;
import static junit.framework.Assert.assertTrue;


public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    InterstitialAd mInterstitialAd;
    private List<Spacecraft> movieList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapter mAdapter;
    private IntentIntegrator qrScan;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public String shortup,bitly;
    private static final String PRIVATE_PREF = "myapp";
    private static final String VERSION_KEY = "version_number";
    public static final int REQUEST_CODE = 100;
    ImageView image;
    TextView act;
    Button newbutton;
    EditText urlenter;
    DatabaseHelper myDb;
    public static int n = 0;
    int c = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        SharedPreferences SP = getSharedPreferences("settings", Context.MODE_PRIVATE);
        final boolean Showd = SP.getBoolean("longid", false);
        myDb = new DatabaseHelper(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        qrScan = new IntentIntegrator(this);
        act = (TextView) findViewById(R.id.activity) ;
        final String prefList = SP.getString("PREF_LIST", "1");
        if (!isNetworkStatusAvialable(getApplicationContext())) {
               Snackbar.make(findViewById(R.id.coorder1), "Check your Internet Connection !", Snackbar.LENGTH_SHORT)
                                       .show();

        }
        final  boolean showd = SP.getBoolean("showbox", false);
        recyclerView.setHasFixedSize(true);
        newbutton = (Button) findViewById(R.id.newbutton);
        newbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                fab.close(true);
                urlenter.setText("");
                if (showd){ urlenter.setVisibility(View.VISIBLE);}
                urlenter.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(urlenter, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        mAdapter = new RecyclerAdapter(movieList);

        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItem(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        if (Build.VERSION.SDK_INT >= 21) {
            checkAndRequestPermissions();
        }






        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                        fab.close(true);
                        startAD();
                        Spacecraft movie = movieList.get(position);
                         Intent i = new Intent(MainActivity.this , Activity2.class);
                        i.putExtra("key", movie.getGenre());
                        startActivity(i);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                        fab.close(true);
                       final Spacecraft movie = movieList.get(position);
final String temp = movie.getGenre().toString();
final String temp1 = movie.getTitle().toString();

                        AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                        builder.setIcon(android.R.drawable.ic_dialog_info);
                        builder.setMessage(Html.fromHtml("<b>Long url: </b>" +temp1 + "<br><br><b>Short url: </b>"  + temp))
                                .setTitle(Html.fromHtml("<font color='#ffffff'>Url Info</font>"))
                                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        shareIntent.setType("text/plain");
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getGenre().toString());
                                        startActivity(Intent.createChooser(shareIntent, "Share link using"));

                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Copied to Clipboard", temp);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                            }
                        }).setNeutralButton("QR Code", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               if (Showd) {shortup=movie.getTitle().toString();} else {shortup=movie.getGenre().toString();}
                                startAD();
                                newdialog();
                            }
                        });

                        builder.create().show();


                    }
                })
        );

        setSupportActionBar(toolbar);urlenter = (EditText) findViewById(R.id.enterurl);
        if (showd){urlenter.setVisibility(View.GONE);}
        urlenter.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                SharedPreferences SP = getSharedPreferences("settings", Context.MODE_PRIVATE);
                final String prefList = SP.getString("PREF_LIST", "1");
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                    fab.close(true);
                    switch (prefList){
                        case "1":
                            if (!isNetworkStatusAvialable(getApplicationContext())) {
                                Snackbar.make(findViewById(R.id.coorder1), "Check your Internet Connection !", Snackbar.LENGTH_SHORT)
                                        .show();

                            } else {
                                shortUrl(urlenter.getText().toString());
                                startAD();
                            }

                            break;
                        case  "2":
                            startAD();
                            break;

                    }
                    return true;
                }
                return false;
            }
        });
        MobileAds.initialize(getApplicationContext(), getString(R.string.InterstitialAds_ADBMOD));
        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab_add);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                fab.close(true);
                urlenter.setText("");
                if (showd){ urlenter.setVisibility(View.VISIBLE);}
                urlenter.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(urlenter, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab_settings);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                fab.close(true);
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(myIntent);
            }
        });
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab_scam);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
                fab.close(true);
                qrScan.initiateScan();

            }
        });
        String link = null;
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            link = intent.getDataString();
        } else if (getIntent().getStringExtra("link") != null) {
            link = getIntent().getStringExtra("link");
        }
        if (link != null) {
            urlenter.setText(link.toString());
            if (showd){ urlenter.setVisibility(View.VISIBLE);}
            urlenter.requestFocus();
        }

        prepareMovieData();
        startAD();
        ShowInfo();
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
    private void ShowInfo() {
        SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        int currentVersionNumber = 0;

        int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception e) {
        }

        if (currentVersionNumber > savedVersionNumber) {
            showDialog();

            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Note")
                .setMessage("You Can disable Ads from Settings, if you find it annoying!")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }
    private void showInterstitial() {
        SharedPreferences SP = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean show = SP.getBoolean("show_ads",true);
        if (show) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }
    }
    //ad
    public void startAD() {
        c=0;
        SharedPreferences SP = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean show = SP.getBoolean("show_ads", true);
        if (show) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    showInterstitial();
                }
            });
        }
    }
    public static String getResponseFromUrl(String url) {
        String xml = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xml;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      final  IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    Toast.makeText(getApplicationContext(), obj.getString("name") + " " + obj.getString("address"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Scan Result");
                    builder.setMessage(result.getContents());
                    builder.setPositiveButton("Visit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this , Activity2.class);
                            i.putExtra("key",result.getContents().toString());
                            startActivity(i);
                        }
                    }).setNegativeButton("Share", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, result.getContents().toString());
                            startActivity(Intent.createChooser(shareIntent, "Share link using"));

                            dialog.dismiss();
                        }
                    }).setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert1 = builder.create();
                    alert1.show();
                   // Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private  boolean checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public void newdialog(){
        Bitmap myBitmap = QRCode.from(shortup).withSize(350, 350).withColor(0xFFFFFFFF, 0xFF444444).bitmap();
         image = new ImageView(MainActivity.this);
        image.setImageBitmap(myBitmap);
           AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                   builder.setView(image)
                   .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           saveimage();

                       }
                   }).setNegativeButton("Share", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   shareimage ();
               }
           }).setNeutralButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
               }
           });


           builder.create().show();

    }
    public void shareimage () {
        try {
            image.buildDrawingCache();
            final Bitmap bitmap = image.getDrawingCache();
            if (bitmap == null) {
                Toast.makeText(getApplicationContext(), "Something Went Wrong, Please Try Again!1", Toast.LENGTH_SHORT).show();
                return;
            }
            final File dir = new File(Environment.getExternalStorageDirectory(), super.getResources().getString(R.string.externalstorage));

            if (!dir.exists()) {
                dir.mkdirs();
            }

            final File img = new File(dir, "image.png");
            if (img.exists()) {
                img.delete();
            }
            final OutputStream outStream = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(img));
            startActivity(Intent.createChooser(share,"Share via"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Something Went Wrong, Please Try Again!2", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveimage () {
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(input);
        adb.setTitle(Html.fromHtml("<font color='#ffffff'>Save Name</font>"));
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        adb.setNegativeButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String savefile=input.getText().toString();
                    image.buildDrawingCache();
                    final Bitmap bitmap = image.getDrawingCache();
                    if (bitmap == null) {
                        Toast.makeText(getApplicationContext(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final File dir = new File(Environment.getExternalStorageDirectory(), getString(R.string.externalstorage));

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File file = new File(Environment.getExternalStorageDirectory(), savefile + ".png");


                    if (file.exists()) {
                                     Toast.makeText(getApplicationContext(), "File already exist!", Toast.LENGTH_SHORT).show();
                    } else {
                        final File img = new File(dir, savefile + ".png");
                        if (img.exists()) {
                            img.delete();
                        }
                        final OutputStream outStream = new FileOutputStream(img);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        Toast.makeText(getApplicationContext(), "Saved in Sdcard/DCIM/" + savefile + ".png", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Something Went Wrong, Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        adb.show();



    }

    public void addData(String shorturl, String year){
    boolean isInserted = myDb.insertData(urlenter.getText().toString(),
            shorturl,
            year);

}
    public void clearData() {
        movieList.clear(); //clear list
        //let your adapter know about the changes and reload view.
    int s = movieList.size();
        for(int i=1;i==s; i++){
            String d = Integer.toString(i);
            myDb.deleteData(d);
        }
        getApplicationContext().deleteDatabase(DATABASE_NAME);
        mAdapter.notifyDataSetChanged();
        if (movieList.isEmpty()){act.setVisibility(View.VISIBLE); newbutton.setVisibility(View.VISIBLE);} else{act.setVisibility(View.GONE);newbutton.setVisibility(View.GONE);}
    }
    private void prepareMovieData() {

        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            // show message
                        return;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
          /*  buffer.append("Id :"+ res.getString(0)+"\n");
            buffer.append("Name :"+ res.getString(1)+"\n");
            buffer.append("Surname :"+ res.getString(2)+"\n");
            buffer.append("Marks :"+ res.getString(3)+"\n\n");*/
           Spacecraft movie = new Spacecraft(res.getString(1), res.getString(2),res.getString(3));
            movieList.add(movie);
        }
        mAdapter.notifyDataSetChanged();
        if (movieList.isEmpty()){act.setVisibility(View.VISIBLE); newbutton.setVisibility(View.VISIBLE);} else{act.setVisibility(View.GONE); newbutton.setVisibility(View.GONE);}
    }

    public void shortUrl(String Url) {
        String longUrl = Url;
        URLShortener.shortUrl(longUrl, new URLShortener.LoadingCallback() {
            ProgressDialog progress = new ProgressDialog(MainActivity.this);
            @Override
            public void startedLoading() {
                //outputLink.setText("Loading...");
                progress.setMessage("Getting Url");
                progress.show();
            }

            @Override
            public void finishedLoading(@Nullable String shortUrl) {
                progress.dismiss();
                //make sure the string is not null
                final String temp;
            if(shortUrl != null) {
                temp = shortUrl;
                addData(temp, getCurrentTime().toString());
                Spacecraft movie = new Spacecraft(urlenter.getText().toString(), temp, getCurrentTime().toString());
               movieList.add(movie);
                if (movieList.isEmpty()){act.setVisibility(View.VISIBLE);newbutton.setVisibility(View.VISIBLE);} else{act.setVisibility(View.GONE);newbutton.setVisibility(View.GONE);}
            }  else temp = "Unable to generate Link!";
                int kp = mAdapter.getItemCount();
                AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setMessage(temp)
                        .setTitle(Html.fromHtml("<font color='#ffffff'>Shortened Url</font>"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied to Clipboard", temp);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                    }
                });


                builder.create().show();
            }
        });

    }

    @Override
    public void onBackPressed() {

        FloatingActionMenu fab = (FloatingActionMenu) findViewById(R.id.menu);
        if (fab.isOpened()) {
            fab.close(true);
        } else {
            SharedPreferences SP = getSharedPreferences("settings", Context.MODE_PRIVATE);
            final boolean showexit = SP.getBoolean("showbox", false);
            final boolean showexit1 = SP.getBoolean("exit_val", true);
          if(showexit){if (urlenter.isShown()) {
              urlenter.setVisibility(View.GONE);
          } else {
              if (showexit1) {
                  AlertDialog.Builder adb = new AlertDialog.Builder(this);
                  adb.setTitle(Html.fromHtml("<font color='#000000'>Warning</font>"));
                  adb.setIcon(android.R.drawable.ic_dialog_alert);
                  adb.setMessage("Are you Sure ?");
                  adb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {

                      }
                  });
                  adb.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          finish();
                      }
                  });
                  adb.show();
              } else {finish();}
          }
            } else {
if (showexit1) {
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(Html.fromHtml("<font color='#ffffff'>Warning</font>"));
    adb.setIcon(android.R.drawable.ic_dialog_alert);
    adb.setMessage("Are you Sure ?");
    adb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });
    adb.setNegativeButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    });
    adb.show();
} else {finish();}
            }
        }
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_bar) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(Html.fromHtml("<font color='#ffffff'>Warning</font>"));
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setMessage("This will earse all your Shortened Url");
            adb.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            adb.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   clearData();
                }
            });
            adb.show();
            return true;
        }
        if (id==R.id.paste){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            String pasteData = "";
            if (!(clipboard.hasPrimaryClip())) { } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            } else {
                ClipData.Item item1 = clipboard.getPrimaryClip().getItemAt(0);
                pasteData = item1.getText().toString();

            }
            urlenter.setText(pasteData);
            urlenter.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(urlenter, InputMethodManager.SHOW_IMPLICIT);
                        return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handleResult(Result rawResult) {
        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)

        // show the scanner result into dialog box.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();
        mScannerView.resumeCameraPreview(this);
    }
}
