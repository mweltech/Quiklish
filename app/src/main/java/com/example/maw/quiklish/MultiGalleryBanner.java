package com.example.maw.quiklish;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Xml;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by maw on 29/06/2015.
 */
public class MultiGalleryBanner extends Activity implements Animation.AnimationListener {

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

    private GestureDetectorCompat mDetector;
    private int imagecount;
    private int eventcount;
    private List<EventEntry> eventList;
    private File[] galleryFiles;
    private List<Bitmap> galleryImages;
    private boolean updatePicture = true;
    private boolean allDone = false;

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

        setContentView(R.layout.multi_gallery_banner);

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
            bannerFile = extras.getString("DISPLAY_BANNER_FILE");
            bannerFilePath = extras.getString("DISPLAY_BANNER_FILE_PATH");
            eventList=openEventsFile(bannerFilePath,bannerFile);
            eventcount = 0;
            // Note - need eventList to exist before we open the Gallery
            openGallery(imageFilePath,eventList.size());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateGallery();
        updateBanner();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    }


    public void onAnimationEnd(Animation animation) {
        if(eventcount>=eventList.size()) {  // finish once we have displayed all the events....
            //TextView textView = (TextView) findViewById(R.id.mGalleryBannerWhat);
            //textView.setText(" ");  // clear the textView - otherwise last text flashes up on resume...
            finishWithNotice("GALLERY_DONE");
        }
        else {
            updateGallery();
            updateBanner();
        }
    }
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    public void openGallery(String galleryPath,int maxEvents) {
        File f = new File(galleryPath);
        ImageView imageView;
        imageView = (ImageView) findViewById(R.id.mGalleryImgLeft); // get the bounds for imageview -- do all three if diff sizes?
        imagecount = 0;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if(f.exists()) {
            galleryFiles = f.listFiles();
            galleryImages = new ArrayList();
            for (int i=0;i<maxEvents*3;i++) {  // 3 pictures per event so * 3
                int j = getRandomNumber(galleryFiles.length);
                Bitmap bmp = decodeSampledBitmapFromFile(galleryFiles[j].getAbsolutePath(), size.x / 3, size.y / 2);
                galleryImages.add(bmp);
            }
        }
    }

    public void updateGallery() {
        setGalleryAnimation(R.id.mGalleryImgLeft,0,2000,7000,2000,false);
        setGalleryAnimation(R.id.mGalleryImgCenter,2000,2000,7000,2000,false);
        setGalleryAnimation(R.id.mGalleryImgRight,4000,2000,7000,2000,true);
    }

    public int getRandomNumber(int max) {
        Random r = new Random();
        return r.nextInt(max);
    }

    //public void setGalleryAnimation(int imageViewID,int imageNumber,int startOffset, int fadeInDuration,int timeInBetween,int fadeOutDuration,boolean setListener) {
    public void setGalleryAnimation(int imageViewID,int startOffset, int fadeInDuration,int timeInBetween,int fadeOutDuration,boolean setListener) {
        if(imagecount>=galleryImages.size() || imagecount<0) {
            imagecount = 0;
        }
        ImageView imageView;
        imageView = (ImageView) findViewById(imageViewID);
        imageView.setImageDrawable(null);  // must call this otherwise image does not update!!
        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        imageView.setImageBitmap(galleryImages.get(imagecount));
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setStartOffset(startOffset);
        fadeIn.setDuration(fadeInDuration);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(startOffset+fadeInDuration + timeInBetween);
        fadeOut.setDuration(fadeOutDuration);
        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);
        if (setListener) {
            animation.setAnimationListener(this);
        }
        imagecount++;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
    }

    public void updateBanner() {
//        if(eventcount>=eventList.size() || eventcount<0) {
//            eventcount = 0;
//        }
        EventEntry ee=eventList.get(eventcount);
        TextView textView = (TextView) findViewById(R.id.mGalleryBannerWhat);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        textView.setVisibility(View.INVISIBLE);
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

