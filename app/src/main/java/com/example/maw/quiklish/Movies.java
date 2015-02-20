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
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.movies);
        //VideoView myVideoView = (VideoView)findViewById(R.id.videoView1);
        //myVideoView.setVideoPath("/storage/emulated/0/Pictures/EzyDisplay/EZYDISPLAY.m4v");
        //myVideoView.setVideoPath(movie_file);
        //myVideoView.setMediaController(new MediaController(this));
        //myVideoView.requestFocus();
        //myVideoView.start();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_MOVIE_FILE");
            displayFilePath = extras.getString("DISPLAY_MOVIE_FILE_PATH");
        }
        else {
            //displayFile = "EzyDisplay.jpeg";
            //displayFilePath = getFilesDir().getPath();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.movies);
        File movieFile = new File(displayFilePath,displayFile);
        myVideoView = (VideoView)findViewById(R.id.videoView1);
        String sPathToMovie = movieFile.getAbsolutePath();
        myVideoView.setVideoPath(sPathToMovie);
        //myVideoView.setMediaController(new MediaController(this));
        myVideoView.requestFocus();
        myVideoView.start();

        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                //myVideoView.start();
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
