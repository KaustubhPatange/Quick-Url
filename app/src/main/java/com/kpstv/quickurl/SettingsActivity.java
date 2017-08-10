package com.kpstv.quickurl;

/**
 * Created by kp on 19/7/17.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kp on 8/7/17.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment {
        public static int i = 0;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName("settings");
            addPreferencesFromResource(R.xml.settings);
            Preference list = findPreference("open");
            list.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/KaustubhPatange/Quick-Url"));
                    startActivity(browserIntent);
                    return true;
                }
            });
           final ListPreference preflist = (ListPreference) findPreference("PREF_LIST");
            Preference version = findPreference("ver");
            Preference change = findPreference("chn");
            Preference email = findPreference("email");
            email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Email();
                    return true;
                }
            });
            change.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://kpstvhub.com/support/"));
                    startActivity(browserIntent);
                    return true;
                }
            });
            version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    i = i + 1;
                    if (i==5){
                        Toast.makeText(getActivity(),"Developed by KP", Toast.LENGTH_SHORT).show();
                    }
                    if (i==6){
                        Toast.makeText(getActivity(),"View My Other Projects, Press 3", Toast.LENGTH_SHORT).show();
                    }
                    if (i==7){
                        Toast.makeText(getActivity(),"View My Other Projects, Press 2", Toast.LENGTH_SHORT).show();
                    }
                    if (i==8){
                        Toast.makeText(getActivity(),"View My Other Projects, Press 1", Toast.LENGTH_SHORT).show();
                    }
                    if (i>=9){
                        if (i==9){
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://kpstvhub.com"));
                            startActivity(browserIntent);
                        }
                        i=0;
                    }
                  return true;
                }
            });


        }
        public void Email() {
            String[] TO = {"developerkp16@gmail.com"};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);

            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email to Kaustubh Patange"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}