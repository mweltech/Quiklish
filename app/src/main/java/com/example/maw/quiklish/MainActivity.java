package com.example.maw.quiklish;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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

// Changes:

//


public class MainActivity extends ActionBarActivity implements DownloadListener {

    SharedPreferences settings;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    static final String CURRENT_STATE = "CurrentState";
    static final String CURRENT_DISPLAYED_ITEM = "CurrentDisplayedItem";

    static final int GO_PREVIOUS    = 1;
    static final int GO_NEXT        = 2;

    static final int GET_SETTINGS   = 1;
    static final int DOWNLOAD_NOW   = 2;
    static final int DOWNLOADS_DONE = 3;

    private Intent movie_activity;
    private Intent gallery_activity;
    private Intent eventlist_activity;
    private Intent gallery_banner_activity;
    private Intent multi_gallery_banner_activity;
    private int current_state;
    private List<DisplayItem> displayitems;
    private Integer current_displayed_item;
    private boolean cancel_startup_timer = false;

    @Override
     protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // remove title

        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                //Log.d("LISTENING! - Pref changed for: ", key );
                if(key.equals("StartupOnBoot")) {
                    setStartOnBoot(prefs.getBoolean(key,false));
                }
            }
        };

        settings.registerOnSharedPreferenceChangeListener(listener);


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
        //movie_activity = new Intent(this, MoviesXStretch.class);
        gallery_activity = new Intent(this, Gallery.class);
        eventlist_activity = new Intent(this, EventList.class);
        gallery_banner_activity = new Intent(this, GalleryBanner.class);
        multi_gallery_banner_activity = new Intent(this, MultiGalleryBanner.class);

        //select last/default channel
        String prefChannel = getPreferencesString("Channel");
        if(prefChannel.length()>0) {
            EditText e = new EditText(MainActivity.this);
            e = (EditText) findViewById(R.id.channel);
            e.setText(prefChannel);
        }

        final Button go_button = (Button) findViewById(R.id.goBabyGo);
        go_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tuneInChannel();
            }
        });

        final TextView channel_selection = (TextView) findViewById(R.id.channel);
        channel_selection.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN)) {

                        tuneInChannel();

                }
                return true;
            }
        });

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                if(cancel_startup_timer==true) {
                    cancel();
                }
            }

            public void onFinish() {
                tuneInChannel();
            }
        }.start();
    }

    public void tuneInChannel() {
        current_displayed_item = 0;
        cancel_startup_timer = true;
        EditText e = new EditText(MainActivity.this);
        e = (EditText)findViewById(R.id.channel);
        // hide the keyboard
        InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        // check channel is not blank and get the data
        if(e.getText().toString().length()>0) {
           saveChannelAsDefault(e.getText().toString());
           DownloadFiles(e.getText().toString());
        }
    }

    public void saveChannelAsDefault(String channel) {
        setPreferencesString("Channel",channel);
    }

    @Override
    public void isFinished() {
        TextView t=new TextView(this);
        t=(TextView)findViewById(R.id.status1);
        t.setText("Downloads all done.");
        current_state=DOWNLOADS_DONE;
        runTheShow(GO_NEXT);
    }

    @Override
    protected void onStart() {
        super.onStart();

        switch(current_state) {
            case GET_SETTINGS:
                //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
                current_state=DOWNLOAD_NOW;
                // don't break -- fall through to DownloadFiles();
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
        ftp.setDownloadListener(this);
        String prefServer = getPreferencesString("Server");
        String prefRemoteDirectory = getDirectory("RemoteDirectory");
        String prefLocalDirectory = getDirectory("LocalDirectory");
        String prefUsername = getPreferencesString("Username");
        String prefPassword = getPreferencesString("Password");
        //String prefGallery = getGallery();
        String prefGallery = getDirectory("Gallery");
        ftp.execute(new String[]{prefServer,prefRemoteDirectory,prefLocalDirectory,prefUsername,prefPassword,prefGallery});
    }

    private String getPreferencesString(String prefName) {
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String prefString = settings.getString(prefName,"");
        return prefString;
    }

    private void setPreferencesString(String prefName, String value) {
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(prefName, value);
        editor.commit();
    }

    private String fixDirectoryPath(String directoryPath) {
        if(directoryPath.charAt(directoryPath.length()-1)!='/') {
            directoryPath += "/";
        }
        return directoryPath;
    }

    public void setStartOnBoot(boolean startOnBoot) {
        ComponentName receiver = new ComponentName(MainActivity.this, StartMyAppAtBootReceiver.class);
        PackageManager pm = getPackageManager();
        if(startOnBoot==true) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String reason = data.getStringExtra("FINISH_REASON");
                // only move to next item if the timer finishes normally...
                if(reason.equals("TIMER_DONE") || reason.equals("GALLERY_DONE") || reason.equals("MOVIE_DONE") || reason.equals("USER_FLING_LEFT") || reason.equals("USER_FLING_RIGHT")) {
                    finishActivity(0);
                    if(reason.equals("USER_FLING_LEFT")) {
                        runTheShow(GO_PREVIOUS);
                    }
                    else {
                        runTheShow(GO_NEXT);
                    }
                }
            }
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

    private String getDirectory(String prefName) {
        String prefDirectory = getPreferencesString(prefName);
        prefDirectory=fixDirectoryPath(prefDirectory);
        prefDirectory=fixDirectoryPath(prefDirectory+getPreferencesString("Channel"));
        return prefDirectory;
    }

    private void readDisplayItems() {
        try {
            String prefLocalDirectory = getDirectory("LocalDirectory");
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

    private void runTheShow(int direction) {
        String prefLocalDirectory = getDirectory("LocalDirectory");
        String prefGallery = getDirectory("Gallery");
        readDisplayItems();
        if(displayitems!=null) {
            if(direction==GO_NEXT) {
                if (current_displayed_item > displayitems.size() - 1) {
                    current_displayed_item = 0;
                }
            }
            else {
                current_displayed_item--; // we are currently pointing to next so dec by 2
                current_displayed_item--;
                if (current_displayed_item < 0) {
                    current_displayed_item = displayitems.size() - 1;
                }
            }
            if(displayitems.get(current_displayed_item).item_type.equals("picture")) {
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE");
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE",displayitems.get(current_displayed_item).file);
                gallery_activity.removeExtra("DISPLAY_IMAGE_FILE_PATH");
                gallery_activity.putExtra("DISPLAY_IMAGE_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(gallery_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("movie")) {
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE");
                movie_activity.putExtra("DISPLAY_MOVIE_FILE", displayitems.get(current_displayed_item).file);
                movie_activity.removeExtra("DISPLAY_MOVIE_FILE_PATH");
                movie_activity.putExtra("DISPLAY_MOVIE_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(movie_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("eventlist")) {
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE");
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE", displayitems.get(current_displayed_item).file);
                eventlist_activity.removeExtra("DISPLAY_EVENTLIST_FILE_PATH");
                eventlist_activity.putExtra("DISPLAY_EVENTLIST_FILE_PATH",prefLocalDirectory);
                current_displayed_item++;
                startActivityForResult(eventlist_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("gallery_banner")) {
                gallery_banner_activity.removeExtra("DISPLAY_BANNER_FILE");
                gallery_banner_activity.putExtra("DISPLAY_BANNER_FILE", displayitems.get(current_displayed_item).file);
                gallery_banner_activity.removeExtra("DISPLAY_BANNER_FILE_PATH");
                gallery_banner_activity.putExtra("DISPLAY_BANNER_FILE_PATH",prefLocalDirectory);
                gallery_banner_activity.removeExtra("DISPLAY_GALLERY_FILE_PATH");
                gallery_banner_activity.putExtra("DISPLAY_GALLERY_FILE_PATH",prefGallery);
                current_displayed_item++;
                startActivityForResult(gallery_banner_activity,0);
            }
            else if(displayitems.get(current_displayed_item).item_type.equals("multi_gallery_banner")) {
                multi_gallery_banner_activity.removeExtra("DISPLAY_BANNER_FILE");
                multi_gallery_banner_activity.putExtra("DISPLAY_BANNER_FILE", displayitems.get(current_displayed_item).file);
                multi_gallery_banner_activity.removeExtra("DISPLAY_BANNER_FILE_PATH");
                multi_gallery_banner_activity.putExtra("DISPLAY_BANNER_FILE_PATH",prefLocalDirectory);
                multi_gallery_banner_activity.removeExtra("DISPLAY_GALLERY_FILE_PATH");
                multi_gallery_banner_activity.putExtra("DISPLAY_GALLERY_FILE_PATH",prefGallery);
                current_displayed_item++;
                startActivityForResult(multi_gallery_banner_activity,0);
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
