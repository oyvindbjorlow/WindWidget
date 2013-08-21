package com.vindsiden.windwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import com.vindsiden.windwidget.model.Measurement;

@SuppressWarnings("unused")
public class VindsidenWebXmlReader {

	private static final int CONNECT_TIMEOUT = 15000 /* milliseconds */;
	private static final int READ_TIMEOUT = 10000 /* milliseconds */;

	// NOT IMPLEMENTED!!! NB! just dumb constants for now!
	// todo: get these from phone settings and preferences at some point ...
	// Whether there is a Wi-Fi connection.
	private static boolean wifiConnected = true; // false;
	// Whether there is a mobile connection.
	private static boolean mobileConnected = false;
	// Whether the display should be refreshed.
	public static boolean refreshDisplay = true;

	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";

	// Øyvind - specify a value. for now: WIFI only, not pay-per-byte mobile internet. TODO impelment usage.
	// public static String sPref = null;
	public static String sPref = WIFI;

	public List<Measurement> loadXmlFromNetwork(String url) throws XmlPullParserException, IOException {

		List<Measurement> result;

		if ((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
			result = loadXmlFromNetworkNow(url);
		} else if ((sPref.equals(WIFI)) && (wifiConnected)) {
			result = loadXmlFromNetworkNow(url);
		} else {
			throw new IOException("Argh. No net, or no xml, or no measurement.");
		}

		return result;
	}

	// Uploads XML from, parses it
	private List<Measurement> loadXmlFromNetworkNow(String urlString) throws XmlPullParserException, IOException {
		// Instantiate the parser
		List<Measurement> result;
		InputStream stream = null;
		VindsidenXMLParser vindsidenXmlParser = new VindsidenXMLParser();
		try {
			stream = downloadUrl(urlString);

			// Alternative implementation, which I guess worked when vindsiden  
			// returned XML WITHOUT <?xml version="1.0" encoding="utf-8"?> in its header
			// NOTE: the current implementation did NOT then, so this is a bug prone area of code.
			//    More robust code here or at vindsiden.com could be needed.
			/*
			 * String stringedVindsidenXML = readStream(stream); result = vindsidenXmlParser.parse(new
			 * StringReader(stringedVindsidenXML));
			 */

			result = vindsidenXmlParser.parse(stream);

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return result;
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	private InputStream downloadUrl(String urlString) throws IOException {
		URL url = new URL(urlString); // Øyvind: java.net specification added
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		return conn.getInputStream();
	}

	// Not in use at present - but it might be needed in the future, ref 
	// whether <?xml version="1.0" encoding="utf-8"?> is present in XML header or not
	// (bug med encoding detection?)
	// BufferedStreamReader skjønner trolig encoding bedre i det tilfellet enn alternativene som først er i bruk nå.
	private String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuffer l = new StringBuffer("");
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				l.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return l.toString();
	}

}
