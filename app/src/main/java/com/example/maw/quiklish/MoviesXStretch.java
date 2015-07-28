package com.example.maw.quiklish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by maw on 14/07/2015.
 */
public class MoviesXStretch extends Activity {

    public class MyVideoView extends VideoView {

        private int mVideoWidth;
        private int mVideoHeight;

        public MyVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public MyVideoView(Context context) {
            super(context);
        }

        public void setVideoSize(int width, int height) {
            mVideoWidth = width;
            mVideoHeight = height;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
            int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth;
                } else if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                }
            }

            setMeasuredDimension(10*width/12, 10*height/12);
        }
    }

    //MyVideoView myVideoView;
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
        setContentView(R.layout.moviexstretch);
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
//        setContentView(R.layout.moviexstretch);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.videoXStretch1);
        MyVideoView myVideoView = new MyVideoView(this);
        layout.addView(myVideoView);
        File movieFile = new File(displayFilePath,displayFile);
        //myVideoView = (VideoView)findViewById(R.id.videoView1);
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
