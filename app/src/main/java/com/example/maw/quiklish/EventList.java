package com.example.maw.quiklish;

/**
 * Created by maw on 9/01/2015.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class EventList extends Activity {

    private GestureDetectorCompat mDetector;

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

    // We don't use namespaces
    private static final String ns = null;

    String displayFilePath;
    String displayFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.eventlist);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_EVENTLIST_FILE");
            displayFilePath = extras.getString("DISPLAY_EVENTLIST_FILE_PATH");
        }

        List<EventEntry> events=null;
        int rowcount=0;

        try {
            File eventlistFile = new File(displayFilePath,displayFile);
            FileInputStream fIn = new FileInputStream(eventlistFile);
            events=parseEvents(fIn);
            fIn.close();
            for(EventEntry event  : events) {
                displayEvent(rowcount,event);
                rowcount++;
            }
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("SlideShow", true)) {
            new CountDownTimer(20000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    finishWithNotice("TIMER_DONE");
                }
            }.start();
        }
    }

    public void displayEvent(int row,EventEntry event) {
        switch(row) {
            case 0: setEventText(event,R.id.when1,R.id.what1,R.id.where1); break;
            case 1: setEventText(event,R.id.when2,R.id.what2,R.id.where2); break;
            case 2: setEventText(event,R.id.when3,R.id.what3,R.id.where3); break;
            case 3: setEventText(event,R.id.when4,R.id.what4,R.id.where4); break;
            case 4: setEventText(event,R.id.when5,R.id.what5,R.id.where5); break;
            case 5: setEventText(event,R.id.when6,R.id.what6,R.id.where6); break;
            case 6: setEventText(event,R.id.when7,R.id.what7,R.id.where7); break;
            case 7: setEventText(event,R.id.when8,R.id.what8,R.id.where8); break;
            case 8: setEventText(event,R.id.when9,R.id.what9,R.id.where9); break;
        }
    }

    public void setEventText(EventEntry event, int when,int what,int where) {
        setText(event.when,when);
        setText(event.what,what);
        setText(event.where,where);
    }

    public void setText(String text,int viewid) {
        TextView t=new TextView(this);
        t=(TextView)findViewById(viewid);
        t.setText(text);
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