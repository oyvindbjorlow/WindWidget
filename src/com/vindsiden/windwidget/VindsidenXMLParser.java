package com.vindsiden.windwidget;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import com.vindsiden.windwidget.model.Measurement;


public class VindsidenXMLParser {

	// Tags for vindsiden XML
	private static final String LVL1_TAG = "Data";
	private static final String LVL2_TAG = "Measurement";
	private static final String DIRECTION_AVG_MEASUREMENT = "DirectionAvg";
	private static final String WIND_AVG_MEASUREMENT = "WindAvg";
	private static final String MEASUREMENT_TIME = "Time";
	private static final String STATION_ID = "StationID";
	private static final String NAME_SPACE = null; // note: namespace usage is also set explicitly in the parse() method.

	public List<Measurement> parse(StringReader sr) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser(); // ExpatPullParser
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false); // no namespaces
		
		parser.setInput(sr); // debug, worked fine: new StringReader
													// ("<Data><Measurement><StationID>1</StationID><Time>2013-08-14T12:35:45-07:00</Time>    <WindAvg>2.5</WindAvg>    <DirectionAvg>4</DirectionAvg></Measurement></Data>"));		
		//parser.skipToTag(p, XmlPullParser.START_TAG);
		parser.nextTag();  // tror denne bug'et da vindsiden begnyte returnere <?xml version="1.0" encoding="utf-8"?> øverst.
		return readFeed(parser);
	}

	
	public List<Measurement> parse(InputStream inputStream) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser(); // ExpatPullParser
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false); // no namespaces hardcode
		
		parser.setInput(inputStream, null); // NOTE: here we explicitly pass in null as the value for encoding
		System.out.println(parser.getInputEncoding()); // reports UTF-8 when vindsiden xml is prefixed <?xml version="1.0" encoding="utf-8"?>
		parser.nextTag();
		return readFeed(parser);
	}

	
	private List<Measurement> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		List<Measurement> entries = new ArrayList<Measurement>();		
		parser.require(XmlPullParser.START_TAG, NAME_SPACE, LVL1_TAG);
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals(LVL2_TAG)) { 
				entries.add(readMeasurement(parser));
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	// Parses the contents of an entry. If it encounters specific tags, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the tag.
	private Measurement readMeasurement(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, NAME_SPACE, LVL2_TAG);
		String stationID = "", time = "", windAvg = "", directionAvg = "";

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

			if (name.equals(STATION_ID)) {
				stationID = readStringTag(parser, STATION_ID);
			} else if (name.equals(MEASUREMENT_TIME)) {
				time = readStringTag(parser, MEASUREMENT_TIME);
			} else if (name.equals(WIND_AVG_MEASUREMENT)) {
				windAvg = readStringTag(parser, WIND_AVG_MEASUREMENT);
			} else if (name.equals(DIRECTION_AVG_MEASUREMENT)) {
				directionAvg = readStringTag(parser, DIRECTION_AVG_MEASUREMENT);
			} else {
				skip(parser);
			}
		}
		return new Measurement(stationID, time, windAvg, directionAvg);
	}

	// Processes a tag - specified by argument - in the single measurement.
	private String readStringTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAME_SPACE, tagName);
		String result = readText(parser);
		parser.require(XmlPullParser.END_TAG, NAME_SPACE, tagName);
		return result;
	}

	// For a text tag, extracts its text value.
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