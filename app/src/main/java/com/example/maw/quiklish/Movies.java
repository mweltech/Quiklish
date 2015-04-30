package com.example.maw.quiklish;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by maw on 29/12/2014.
 */
public class Movies extends Activity {
    VideoView myVideoView;
    String displayFilePath;
    String displayFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_MOVIE_FILE");
            displayFilePath = extras.getString("DISPLAY_MOVIE_FILE_PATH");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.movies);
        File movieFile = new File(displayFilePath,displayFile);
        myVideoView = (VideoView)findViewById(R.id.videoView1);
        String sPathToMovie = movieFile.getAbsolutePath();
        myVideoView.setVideoPath(sPathToMovie);
        myVideoView.requestFocus();
        myVideoView.start();

        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
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

}
