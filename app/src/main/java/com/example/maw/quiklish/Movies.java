package com.example.maw.quiklish;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by maw on 29/12/2014.
 */
public class Movies extends Activity {
    VideoView myVideoView;
    String displayFilePath;
    String displayFile;
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // stop device going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.movies);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_MOVIE_FILE");
            displayFilePath = extras.getString("DISPLAY_MOVIE_FILE_PATH");
        }
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
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
                finishWithNotice("MOVIE_DONE");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void finishWithNotice(String notice) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FINISH_REASON", notice);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if(velocityX<0) {
                finishWithNotice("USER_FLING_RIGHT");
            }
            else {
                finishWithNotice("USER_FLING_LEFT");
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            finishWithNotice("USER_DOUBLETAP");
            return true;
        }
    }
}
