package com.example.maw.quiklish;

/**
 * Created by maw on 8/06/2015.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GalleryBanner extends Activity implements Animation.AnimationListener {

    public class EventEntry {
        public final String when;
        public final String what;
        public final String where;

        private EventEntry(String when, String what, String where) {
            this.when = when;
            this.what = what;
            this.where = where;
        }
    }

    private ImageView imageView;
    private GestureDetectorCompat mDetector;
    private int imagecount=0;
    private int eventcount=0;
    private List<EventEntry> eventList;
    private File[] galleryFiles;
    private List<Bitmap> galleryImages;
    private boolean updatePicture = true;

    // We don't use namespaces
    private static final String ns = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // stop device going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.gallery_banner);

        imageView=(ImageView)findViewById(R.id.galleryImg1);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        File imgFile;

        String imageFilePath;
        String imageFile;
        String bannerFilePath;
        String bannerFile;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //displayFile = extras.getString("DISPLAY_IMAGE_FILE");
            imageFile = "EzyDisplay.jpeg";
            imageFilePath = extras.getString("DISPLAY_GALLERY_FILE_PATH");
            openGallery(imageFilePath);
            bannerFile = extras.getString("DISPLAY_BANNER_FILE");
            bannerFilePath = extras.getString("DISPLAY_BANNER_FILE_PATH");
            eventList=openEventsFile(bannerFilePath,bannerFile);
        }
        else {
            imageFile = "EzyDisplay.jpeg";
            imageFilePath = getFilesDir().getPath();
        }

        imgFile = new File(imageFilePath,imageFile);

        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
        // set banner scrolling

        TextView tv = (TextView) this.findViewById(R.id.bannerWhat);
        tv.setSelected(true);  // Set focus to the textview
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateGallery();
        updateBanner();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    }


    public void onAnimationEnd(Animation animation) {
        updateGallery();
        updateBanner();
    }
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    public void openGallery(String galleryPath) {
        File f = new File(galleryPath);
        if(f.exists()) {
            galleryFiles = f.listFiles();
            galleryImages = new ArrayList();
            for (int i=0;i<galleryFiles.length;i++) {
                Bitmap bmp = BitmapFactory.decodeFile(galleryFiles[i].getAbsolutePath());
                galleryImages.add(bmp);
            }
        }
    }

    public void updateGallery() {
        imageView=(ImageView)findViewById(R.id.galleryImg1);
        if(imagecount>=galleryImages.size() || imagecount<0) {
            imagecount = 0;
        }
        int fadeInDuration = 2000; // Configure time values here
        int timeBetween = 11000;
        int fadeOutDuration = 2000;

        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        imageView.setImageBitmap(galleryImages.get(imagecount));

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);
        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);
        animation.setAnimationListener(this);
        imagecount++;
    }

    public void updateBanner() {
        if(eventcount>=eventList.size() || eventcount<0) {
            eventcount = 0;
        }
        EventEntry ee=eventList.get(eventcount);
        TextView textView = (TextView) findViewById(R.id.bannerWhat);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        textView.setText(ee.when+"       "+ee.what+"       "+ee.where);
        textView.measure(0, 0);
        int textWidth = textView.getMeasuredWidth();
        Animation mAnimation = new TranslateAnimation(screenWidth, -textWidth, 0, 0);
        mAnimation.setDuration(15000);    // Set custom duration.
        mAnimation.setStartOffset(500);     // Set custom offset.
        textView.startAnimation(mAnimation);
        eventcount++;
    }

    public List<EventEntry> openEventsFile(String filePath,String fileName) {
        List<EventEntry> events = null;

        try {
            File eventlistFile = new File(filePath, fileName);
            FileInputStream fIn = new FileInputStream(eventlistFile);
            events = parseEvents(fIn);
            fIn.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return events;
    }

    public List parseEvents(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readEvents(parser);
        } finally {
            in.close();
        }
    }

    private List readEvents(XmlPullParser parser) throws XmlPullParserException, IOException {
        List events = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "eventlist");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("event")) {
                events.add(readEvent(parser));
            } else {
                skip(parser);
            }
        }
        return events;
    }

    private EventEntry readEvent(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "event");
        String when = null;
        String what = null;
        String where = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("when")) {
                when = readTag("when",parser);
            } else if (name.equals("what")) {
                what = readTag("what",parser);
            } else if (name.equals("where")) {
                where = readTag("where",parser);
            } else {
                skip(parser);
            }
        }
        return new EventEntry(when, what, where);
    }

    private String readTag(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return title;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
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
