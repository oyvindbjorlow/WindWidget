package com.vindsiden.windwidget.model;

public class WindWidgetStation {
	int index;
	int stationId;
	String stationName;
	
	public int getIndex() {
		return index;
	}

	public int getStationId() {
		return stationId;
	}

	public String getStationName() {
		return stationName;
	}

	
	WindWidgetStation(int index, int stationId, String stationName) {
		this.index = index;
		this.stationId = stationId;
		this.stationName = stationName;			
	}
	
}
