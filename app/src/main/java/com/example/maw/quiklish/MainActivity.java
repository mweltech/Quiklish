package com.example.maw.quiklish;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

//package com.example.maw.quiklish;

public class MainActivity extends ActionBarActivity implements DownloadListener {

    static final String CURRENT_STATE = "CurrentState";
    static final String CURRENT_DISPLAYED_ITEM = "CurrentDisplayedItem";
    //static final String SD_PATH = "/storage/emulated/0/Pictures/EzyDisplay";


    static final int GET_SETTINGS   = 1;
    static final int DOWNLOAD_NOW   = 2;
    static final int DOWNLOADS_DONE = 3;

    private Intent movie_activity;
    private Intent gallery_activity;
    private Intent eventlist_activity;
    private int current_state;
    private List<DisplayItem> displayitems;
    private Integer current_displayed_item;

    @Override
     protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // remove title

        setContentView(R.layout.activity_main);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            current_state = savedInstanceState.getInt(CURRENT_STATE);
            current_displayed_item = savedInstanceState.getInt(CURRENT_DISPLAYED_ITEM);
        } else {
            //current_state = DOWNLOAD_NOW;
            current_state = GET_SETTINGS;
            displayitems = null;
            current_displayed_item = 0;
        }
        movie_activity = new Intent(this, Movies.class);
        gallery_activity = new Intent(this, Gallery.class);
        eventlist_activity = new Intent(this, EventList.class);
        final Button button = (Button) findViewById(R.id.goBabyGo);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText e = new EditText(MainActivity.this);
                e = (EditText)findViewById(R.id.channel);
                InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                // Perform action on click
                DownloadFiles(e.getText().toString());
            }
        });

    }

    @Override
    public void isFinished() {
        TextView t=new TextView(this);
        t=(TextView)findViewById(R.id.status1);
        t.setText("Downloads all done.");
        current_state=DOWNLOADS_DONE;
        runTheShow();

    }

    @Override
    protected void onStart() {
        super.onStart();

        switch(current_state) {
            case GET_SETTINGS:
                //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
                current_state=DOWNLOAD_NOW;
                // dont break -- fall through to DownloadFiles();
            case DOWNLOAD_NOW:
                //DownloadFiles();
                break;
        }
    }

    private void DownloadFiles(String channel) {

        FTPDownload ftp = new FTPDownload();
        TextView t = new TextView(this);
        t = (TextView) findViewById(R.id.status1);
        ftp.setStatusDisplay(t);
        //File extPath = getDir("Quiklish", MODE_PRIVATE);
        //ftp.setPath(extPath);
        ftp.setDownloadListener(this);

        //ftp.execute(new String[]{prefFTPServer, "/"+prefFTPDirectory});
        //ftp.execute(new String[]{"166.62.2.1","/sites/default/files/private/users/mweltech"});
//        if(prefRemoteDirectory.charAt(prefRemoteDirectory.length()-1)!='/') {
//            prefRemoteDirectory += "/";
//        }
//        if(prefRemoteDirectory.charAt(prefLocalDirectory.length()-1)!='/') {
//            prefRemoteDirectory += "/";
//        }

        String prefServer = getPreferencesString("Server");
        String prefRemoteDirectory = getPreferencesString("RemoteDirectory");
        //String prefChannel = getPreferencesString("Channel");
        String prefLocalDirectory = getPreferencesString("LocalDirectory");
        prefRemoteDirectory=fixDirectoryPath(prefRemoteDirectory);
        prefLocalDirectory=fixDirectoryPath(prefLocalDirectory);
        //ftp.execute(new String[]{prefServer,prefRemoteDirectory+prefChannel,prefLocalDirectory});
        ftp.execute(new String[]{prefServer,prefRemoteDirectory+channel,prefLocalDirectory});
    }

    private String getPreferencesString(String prefName) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String prefString = settings.getString(prefName,"");
        return prefString;
    }

    private String fixDirectoryPath(String directoryPath) {
        if(directoryPath.charAt(directoryPath.length()-1)!='/') {
            directoryPath += "/";
        }
        return directoryPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            //if (resultCode == RESULT_OK) {

                finishActivity(0);
                runTheShow();
            //}
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(CURRENT_STATE, current_state);
        savedInstanceState.putInt(CURRENT_DISPLAYED_ITEM, current_displayed_item);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readDisplayItems() {
        try {
            String prefLocalDirectory = getPreferencesString("LocalDirectory");
            prefLocalDirectory=fixDirectoryPath(prefLocalDirectory);
            //FileInputStream config_file = new FileInputStream(getDir("Quicklish", MODE_PRIVATE)+"/ezydisplaydata.xml");
            FileInputStream config_file = new FileInputStream(prefLocalDirectory+"ezydisplaydata.xml");
            DisplayListReader display_list_reader = new DisplayListReader();
            display_list_reader.readListFromFile(config_file);
            displayitems = display_list_reader.getList();
            config_file.close();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void runTheShow() {
        String prefLocalDirectory = getPreferencesString("LocalDirectory");
        prefLocalDirectory=fixDirectoryPath(prefLocalDirectory);
        readDisplayItems();
        if(displayitems!=null) {
            if(current_displayed_item>displayitems.size()-1) {
                current_displayed_item = 0;
            }
            if(displayitems.get(current_displayed_item).item_type.equals("picture")) {
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE");
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE",displayitems.get(current_displayed_item).file);
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE_PATH");
                //gallery_activity.putExtra("DISPLAY_IMAGE_FILE_PATH",getDir("Quicklish", MODE_PRIVATE).getAbsolutePath());
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(gallery_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("movie")) {
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE");
                movie_activity.putExtra("DISPLAY_MOVIE_FILE", displayitems.get(current_displayed_item).file);
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE_PATH");
                //movie_activity.putExtra("DISPLAY_MOVIE_FILE_PATH",getDir("Quicklish", MODE_PRIVATE).getAbsolutePath());
                movie_activity.putExtra("DISPLAY_MOVIE_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(movie_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("eventlist")) {
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE");
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE", displayitems.get(current_displayed_item).file);
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE_PATH");
                //eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE_PATH",getDir("Quicklish", MODE_PRIVATE).getAbsolutePath());
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(eventlist_activity,0);
            }
        }
    }


    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
