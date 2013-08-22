package com.vindsiden.windwidget.config;

import android.text.format.Time;

public class WindWidgetConfig {
	private int stationID, widgetID;
	// private String stationName; //unused per se

	private Time startTime, endTime;
	private int frequenceIntervalInMinutes;

	public WindWidgetConfig(int stationID, int widgetID, Time startTime, Time endTime, int frequenceIntervalInMinutes) {
		setStationID(stationID);
		setWidgetID(widgetID);
		setStartTime(startTime);
		setEndTime(endTime);
		setFrequenceIntervalInMinutes(frequenceIntervalInMinutes);
	}

	public static WindWidgetConfig createADefaultConfig() {
		Time t = new Time();
		t.set(System.currentTimeMillis());
		t.hour = 9;
		t.minute = 0;
		t.second = 0;

		Time t2 = new Time();
		t2.set(System.currentTimeMillis());		
		t2.hour = 19;
		t2.minute = 0;
		t2.second = 0;

		return new WindWidgetConfig(1, 1, t, t2, 15); // Hardcode: freq.15min, station 1, widget1 // bugs v 5 min default?!
	}

	public int getStationID() {
		return stationID;
	}

	public void setStationID(int stationID) {
		this.stationID = stationID;
	}

	public int getWidgetID() {
		return widgetID;
	}

	public void setWidgetID(int widgetID) {
		this.widgetID = widgetID;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public int getFrequenceIntervalInMinutes() {
		return frequenceIntervalInMinutes;
	}

	public void setFrequenceIntervalInMinutes(int frequenceIntervalInMinutes) {
		this.frequenceIntervalInMinutes = frequenceIntervalInMinutes;
	}

	public long getFrequenceIntervalInMicroseconds() {
		return getFrequenceIntervalInMinutes() * 60 * 1000;
	}

}
