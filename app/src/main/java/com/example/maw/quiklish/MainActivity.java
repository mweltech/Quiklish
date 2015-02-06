package com.example.maw.quiklish;

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


    static final Integer DOWNLOAD_NOW = 1;
    static final Integer DOWNLOADS_DONE = 2;

    private Intent movie_activity;
    private Intent gallery_activity;
    private Intent eventlist_activity;
    private Integer current_state;
    private List<DisplayItem> displayitems;
    private Integer current_displayed_item;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();





        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            current_state = savedInstanceState.getInt(CURRENT_STATE);
            current_displayed_item = savedInstanceState.getInt(CURRENT_DISPLAYED_ITEM);
        } else {
            current_state = DOWNLOAD_NOW;
            displayitems = null;
            current_displayed_item = 0;
        }
        setContentView(R.layout.activity_main);
        movie_activity = new Intent(this, Movies.class);
        gallery_activity = new Intent(this, Gallery.class);
        eventlist_activity = new Intent(this, EventList.class);
        //current_state=JUST_STARTED;
        Integer i=0;
        i++;

    }

    @Override
    public void isFinished() {
        TextView t=new TextView(this);
        t=(TextView)findViewById(R.id.status1);
        t.setText("Downloads all done.");
        current_state=DOWNLOADS_DONE;
        runTheShow();

        //movie_activity = new Intent(this, Movies.class);
        //startActivityForResult(movie_activity,0);
        //startActivity(movie_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get data from settings activity in this case the language
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String prefFTPServer = settings.getString("FTPServer","0.0.0.0");
        String prefFTPDirectory = settings.getString("FTPDirectory","/");

        if(current_state==DOWNLOAD_NOW) {
            FTPDownload ftp = new FTPDownload();
            TextView t = new TextView(this);
            t = (TextView) findViewById(R.id.status1);
            ftp.setStatusDisplay(t);
            File extPath = getDir("Quicklish", MODE_PRIVATE);
            ftp.setPath(extPath);
            ftp.setDownloadListener(this);



            ftp.execute(new String[]{prefFTPServer, "/"+prefFTPDirectory});
        }

        movie_activity = new Intent(this, Movies.class);
        startActivityForResult(movie_activity,0);
        movie_activity = new Intent(this, Gallery.class);
        startActivityForResult(gallery_activity,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            //if (resultCode == RESULT_OK) {
            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.

            // Do something with the contact here (bigger example below)

            finishActivity(0);
            runTheShow();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readDisplayItems() {
        try {
            FileInputStream config_file = new FileInputStream(getDir("Quicklish", MODE_PRIVATE)+"/ezydisplaydata.xml");
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
        readDisplayItems();
        if(displayitems!=null) {
            if(current_displayed_item>displayitems.size()-1) {
                current_displayed_item = 0;
            }
            if(displayitems.get(current_displayed_item).item_type.equals("picture")) {
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE");
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE",displayitems.get(current_displayed_item).file);
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE_PATH");
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE_PATH",getDir("Quicklish", MODE_PRIVATE));
                current_displayed_item++;
                startActivityForResult(gallery_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("movie")) {
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE");
                movie_activity.putExtra("DISPLAY_MOVIE_FILE",displayitems.get(current_displayed_item).file);
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE_PATH");
                movie_activity.putExtra("DISPLAY_MOVIE_FILE_PATH",getDir("Quicklish", MODE_PRIVATE));
                current_displayed_item++;
                startActivityForResult(movie_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("eventlist")) {
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE");
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE",displayitems.get(current_displayed_item).file);
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE_PATH");
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE_PATH",getDir("Quicklish", MODE_PRIVATE));
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
