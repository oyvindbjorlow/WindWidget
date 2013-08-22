package com.vindsiden.windwidget.model;

import android.appwidget.AppWidgetProvider;
import android.util.Log;

public class Measurement {
	/**
	 * A single measurement of the wind - normally created from Vindsiden.com XML.
	 * {@inheritDoc}
	 */

	private final String tag = AppWidgetProvider.class.getName(); // getSimpleName());
	
	private final String stationID, time, windAvg, directionAvg;

	public Measurement(String stationID, String time, String windAvg, String directionAvg) {
		Log.d(tag,"Measurement created: Station: "+stationID+" time "+time+" windAvg: "+windAvg+" dir: "+directionAvg);
		this.stationID = stationID;
		this.time = time;
		this.windAvg = windAvg;
		this.directionAvg = directionAvg;
	}

	public String getStationID() {
		return stationID;
	}

	public String getTime() {
		return time;
	}

	public String getWindAvg() {
		return windAvg;
	}

	public String getDirectionAvg() {
		return directionAvg;
	}
}