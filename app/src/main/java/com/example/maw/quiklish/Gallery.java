package com.example.maw.quiklish;

/**
 * Created by maw on 30/12/2014.
 */

import java.io.File;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Gallery extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery);

        imageView=(ImageView)findViewById(R.id.galleryImg1);

        File imgFile;

        String displayFilePath;
        String displayFile;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_IMAGE_FILE");
            displayFilePath = extras.getString("DISPLAY_IMAGE_FILE_PATH");
        }
        else {
            displayFile = "EzyDisplay.jpeg";
            displayFilePath = getFilesDir().getPath();
        }

        //imgFile = new File(getFilesDir(),displayFile);
        imgFile = new File(displayFilePath,displayFile);

        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                finish();
                //mTextField.setText("done!");
            }
        }.start();
    }

}
