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

import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.util.Xml;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class EventList extends Activity {

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayFile = extras.getString("DISPLAY_EVENTLIST_FILE");
            displayFilePath = extras.getString("DISPLAY_EVENTLIST_FILE_PATH");
        }
        else {
            //displayFile = "EzyDisplay.jpeg";
            //displayFilePath = getFilesDir().getPath();
        }

        List<EventEntry> events=null;
        int rowcount=0;

        try {
            //FileInputStream fIn = openFileInput("ezydisplaydata.xml");
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

    public void displayEvent(int row,EventEntry event) {
        switch(row) {
            case 0: setEventText(event,R.id.when1,R.id.what1,R.id.where1); break;
            case 1: setEventText(event,R.id.when2,R.id.what2,R.id.where2); break;
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

    public void makeTestFile() {
        FileOutputStream fOut;
        try { // catches IOException below
            fOut = openFileOutput("ezydisplaydata.xml",MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write("<eventlist><title>Upcoming events...</title>" +
                    "<event>" +
                    "<when>7:00pm</when>" +
                    "<what>Days of Our Lives</what>" +
                    "<where>Room A</where>" +
                    "</event>" +
                    "<event>" +
                    "<when>8:00pm</when>" +
                    "<what>Dating Naked</what>" +
                    "<where>Room B</where>" +
                    "</event>" +
                    "</eventlist>");
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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


}