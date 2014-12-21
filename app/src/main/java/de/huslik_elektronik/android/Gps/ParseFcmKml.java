/*
 * (c) 2014 by Joachim Weishaupt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.huslik_elektronik.android.Gps;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ParseFcmKml {

    private int version = 1;        // kmlBuilder Version, actual Version 1


    private ArrayList<GpsFrame> list;

    private XmlPullParserFactory xmlFactoryObject;

    public void load(Context c, String filename) {
        InputStream is;
        try {
            File f = new File(c.getExternalFilesDir(null).getAbsolutePath(), filename);
            is = new FileInputStream(f);
            //is = c.getAssets().open("fcm_2.xml");
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myParser.setInput(is, null);
            parseFcmKML(myParser);
            is.close();
        } catch (Exception e) {
            Log.e("xmlParser", e.toString());
        }
    }

    public void load(String kml) {
        InputStream is;
        try {
            is = new ByteArrayInputStream(kml.getBytes());
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myParser.setInput(is, null);
            parseFcmKML(myParser);
            is.close();
        } catch (Exception e) {
            Log.e("xmlParser", e.toString());
        }
    }

    private void parseFcmKML(XmlPullParser myParser) {

        String lat = "0", lon = "0", height = "0";
        String vx = "0", vy = "0", vz = "0";
        String dx = "0", dy = "0", dz = "0";
        GpsFrame gpsframe;
        list = new ArrayList<>();
        int event;
        //String text = null;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        //text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals(GpsFrame.Frame)) {
                            float _lat = Float.parseFloat(lat);
                            float _lon = Float.parseFloat(lon);
                            float _h = Float.parseFloat(height);
                            float _vx = Float.parseFloat(vx);
                            float _vy = Float.parseFloat(vy);
                            float _vz = Float.parseFloat(vz);
                            float _dx = Float.parseFloat(dx);
                            float _dy = Float.parseFloat(dy);
                            float _dz = Float.parseFloat(dz);
                            gpsframe = new GpsFrame(_lon, _lat, _h, _vx, _vy, _vz, _dx, _dy, _dz, 0);
                            list.add(gpsframe);
                        } else if (name.equals(GpsFrame.Position)) {
                            lon = myParser.getAttributeValue(null, "lon");
                            lat = myParser.getAttributeValue(null, "lat");
                            height = myParser.getAttributeValue(null, "height");
                        } else if (name.equals(GpsFrame.Speed)) {
                            vx = myParser.getAttributeValue(null, "vx");
                            vy = myParser.getAttributeValue(null, "vy");
                            vz = myParser.getAttributeValue(null, "vz");
                        } else if (name.equals(GpsFrame.DistanceToHome)) {
                            dx = myParser.getAttributeValue(null, "dx");
                            dy = myParser.getAttributeValue(null, "dy");
                            dz = myParser.getAttributeValue(null, "dz");
                        } else if (name.equals(kmlBuilder.version)) {
                            String sVersion = myParser.getAttributeValue(null, "value");
                            version = Integer.parseInt(sVersion);
                        } else {
                            break;
                        }
                }
                event = myParser.next();
            }

        } catch (Exception e) {
            Log.e("xmlParser", e.toString());
        }
    }

    public ArrayList<GpsFrame> getFlightLog() {
        return (list);
    }


}

