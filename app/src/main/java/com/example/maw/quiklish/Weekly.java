package com.example.maw.quiklish;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Html;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
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

/**
 * Created by maw on 28/07/2015.
 */
public class Weekly extends Activity {

    private GestureDetectorCompat mDetector;

    public class WeekEvent {
        public final String when;
        public final String what;
        public final String tellMeMore;

        private WeekEvent(String when, String what, String tellMeMore) {
            this.when = when;
            this.what = what;
            this.tellMeMore = tellMeMore;
        }
    }

    public class WeekEntry {
        public final WeekEvent mon;
        public final WeekEvent tue;
        public final WeekEvent wed;
        public final WeekEvent thu;
        public final WeekEvent fri;
        public final WeekEvent sat;
        public final WeekEvent sun;

        private WeekEntry(WeekEvent mon, WeekEvent tue, WeekEvent wed, WeekEvent thu, WeekEvent fri, WeekEvent sat, WeekEvent sun) {
            this.mon = mon;
            this.tue = tue;
            this.wed = wed;
            this.thu = thu;
            this.fri = fri;
            this.sat = sat;
            this.sun = sun;
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
        // stop device going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.weekly);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int columnWidth = (size.x-7*4)/7;  //padding is 2 so need 4 per cell
        int titleHeight = ((size.y-6*4)/7)*2/3;
        int rowHeight   = (size.y-titleHeight-6*4)/6;  //padding is 2 so need 4 per cell
        //int textFields[] = {
        //    R.id.weekMonTitle,R.id.weekTueTitle,R.id.weekWedTitle,R.id.weekThuTitle,R.id.weekFriTitle,R.id.weekSatTitle,R.id.weekSunTitle,
        //    R.id.weekMon1When,R.id.weekTue1When,R.id.weekWed1When,R.id.weekThu1When,R.id.weekFri1When,R.id.weekSat1When,R.id.weekSun1When,
        //    R.id.weekMon1What,R.id.weekTue1What,R.id.weekWed1What,R.id.weekThu1What,R.id.weekFri1What,R.id.weekSat1What,R.id.weekSun1What,
        //    R.id.weekMon1TellMeMore,R.id.weekTue1TellMeMore,R.id.weekWed1TellMeMore,R.id.weekThu1TellMeMore,R.id.weekFri1TellMeMore,R.id.weekSat1TellMeMore,R.id.weekSun1TellMeMore,
        //};

        //GridLayout g=(GridLayout)findViewById(R.id.weekGrid);
        //GridLayout.LayoutParams params = (GridLayout.LayoutParams) g.getLayoutParams();
        //params.width = (screenWidth/g.getColumnCount()) -params.rightMargin - params.leftMargin;
        //g.setLayoutParams(params);
        TextView v = null;
        v = (TextView)findViewById(R.id.weekMonTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekTueTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekWedTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekThuTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekFriTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekSatTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);
        v = (TextView)findViewById(R.id.weekSunTitle); v.setWidth(columnWidth); v.setHeight(titleHeight/2);

        v = (TextView)findViewById(R.id.weekMon1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat1); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun1); v.setWidth(columnWidth); v.setHeight(rowHeight);

        v = (TextView)findViewById(R.id.weekMon2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat2); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun2); v.setWidth(columnWidth); v.setHeight(rowHeight);

        v = (TextView)findViewById(R.id.weekMon3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat3); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun3); v.setWidth(columnWidth); v.setHeight(rowHeight);

        v = (TextView)findViewById(R.id.weekMon4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat4); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun4); v.setWidth(columnWidth); v.setHeight(rowHeight);

        v = (TextView)findViewById(R.id.weekMon5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat5); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun5); v.setWidth(columnWidth); v.setHeight(rowHeight);

        v = (TextView)findViewById(R.id.weekMon6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekTue6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekWed6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekThu6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekFri6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSat6); v.setWidth(columnWidth); v.setHeight(rowHeight);
        v = (TextView)findViewById(R.id.weekSun6); v.setWidth(columnWidth); v.setHeight(rowHeight);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_WEEKLY_FILE");
            displayFilePath = extras.getString("DISPLAY_WEEKLY_FILE_PATH");
        }

        List<WeekEntry> events=null;
        int rowcount=0;

        try {
            File eventlistFile = new File(displayFilePath,displayFile);
            FileInputStream fIn = new FileInputStream(eventlistFile);
            events=parseEvents(fIn);
            fIn.close();
            for(WeekEntry event  : events) {
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

    public void displayEvent(int row,WeekEntry event) {
        switch(row) {
            case 0:
                setEventText(event.mon,R.id.weekMon1,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue1,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed1,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu1,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri1,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat1,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun1,Color.parseColor("#000030"));
                break;
            case 1:
                setEventText(event.mon,R.id.weekMon2,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue2,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed2,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu2,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri2,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat2,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun2,Color.parseColor("#000030"));
                break;
            case 2:
                setEventText(event.mon,R.id.weekMon3,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue3,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed3,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu3,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri3,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat3,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun3,Color.parseColor("#000030"));
                break;
            case 3:
                setEventText(event.mon,R.id.weekMon4,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue4,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed4,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu4,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri4,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat4,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun4,Color.parseColor("#000030"));
                break;
            case 4:
                setEventText(event.mon,R.id.weekMon5,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue5,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed5,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu5,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri5,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat5,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun5,Color.parseColor("#000030"));
                break;
            case 5:
                setEventText(event.mon,R.id.weekMon6,Color.parseColor("#000030"));
                setEventText(event.tue,R.id.weekTue6,Color.parseColor("#003030"));
                setEventText(event.wed,R.id.weekWed6,Color.parseColor("#303030"));
                setEventText(event.thu,R.id.weekThu6,Color.parseColor("#000030"));
                setEventText(event.fri,R.id.weekFri6,Color.parseColor("#003030"));
                setEventText(event.sat,R.id.weekSat6,Color.parseColor("#303030"));
                setEventText(event.sun,R.id.weekSun6,Color.parseColor("#000030"));
                break;
        }
    }

    public void setEventText(WeekEvent event,int eventField,int bgcolor) {
        String eventString;
        eventString = "";
        if(event.when!=null)       { eventString  = "<b>"+event.when+"</b>"; }
        if(event.what!=null)       { eventString += "<br/><b>"+event.what+"</b>"; }
        if(event.tellMeMore!=null) { eventString += "<br/><small>"+event.tellMeMore+"</small>"; }
        setText(Html.fromHtml(eventString),eventField,bgcolor);
    }

    public void setText(android.text.Spanned text,int viewid,int bgcolor) {
        TextView t=new TextView(this);
        t=(TextView)findViewById(viewid);
        t.setText(text);
        if(text.length()>0) { t.setBackgroundColor(bgcolor); }
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

        parser.require(XmlPullParser.START_TAG, ns, "weeklylist");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("week")) {
                events.add(readWeek(parser));
            } else {
                skip(parser);
            }
        }
        return events;
    }

    private WeekEntry readWeek(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "week");
        WeekEvent mon = null;
        WeekEvent tue = null;
        WeekEvent wed = null;
        WeekEvent thu = null;
        WeekEvent fri = null;
        WeekEvent sat = null;
        WeekEvent sun = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("mon")) {
                mon = readSlot("mon",parser);
            } else if (name.equals("tue")) {
                tue = readSlot("tue",parser);
            } else if (name.equals("wed")) {
                wed = readSlot("wed",parser);
            } else if (name.equals("thu")) {
                thu = readSlot("thu",parser);
            } else if (name.equals("fri")) {
                fri = readSlot("fri",parser);
            } else if (name.equals("sat")) {
                sat = readSlot("sat",parser);
            } else if (name.equals("sun")) {
                sun = readSlot("sun",parser);
            } else {
                skip(parser);
            }
        }
        return new WeekEntry(mon, tue, wed, thu, fri, sat, sun);
    }

    private WeekEvent readSlot(String day, XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, day);
        String when = null;
        String what = null;
        String tellMeMore = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("when")) {
                when = readTag("when",parser);
            }
            else if (name.equals("what")) {
                what = readTag("what",parser);
            }
            else if (name.equals("tellmemore")) {
                tellMeMore = readTag("tellmemore",parser);
            } else {
                skip(parser);
            }
        }
        return new WeekEvent(when,what,tellMeMore);
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
