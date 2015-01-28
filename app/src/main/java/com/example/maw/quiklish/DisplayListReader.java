package com.example.maw.quiklish;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by maw on 5/01/2015.
 */

public class DisplayListReader {

    // We don't use namespaces
    private static final String ns = null;

    public List<DisplayItem> displayitems;

    //public void DisplayListReader(FileInputStream config_file) {
    public void readListFromFile(FileInputStream config_file) {
        int rowcount=0;

        try {
            displayitems=parseEvents(config_file);
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

    public List<DisplayItem> getList() {
        return displayitems;
    }

    public List parseEvents(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readDisplayItemList(parser);
        } finally {
            in.close();
        }
    }

    private List readDisplayItemList(XmlPullParser parser) throws XmlPullParserException, IOException {
        List events = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "displayitem_list");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("displayitem")) {
                events.add(readDisplayItem(parser));
            } else {
                skip(parser);
            }
        }
        return events;
    }

    private DisplayItem readDisplayItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "displayitem");
        String item_type = null;
        String filename = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item_type")) {
                item_type = readTag("item_type",parser);
            } else if (name.equals("filename")) {
                filename = readTag("filename",parser);
            } else {
                skip(parser);
            }
        }
        return new DisplayItem(item_type, filename);
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
