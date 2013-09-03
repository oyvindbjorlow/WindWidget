package com.vindsiden.windwidget.model;

import java.util.ArrayList;

public class WindWidgetStations {
	private static ArrayList<WindWidgetStation> stationList;

	static {
		stationList = new ArrayList<WindWidgetStation>();
		stationList.add(0, new WindWidgetStation(0, 1, "Larkollen"));
		stationList.add(1, new WindWidgetStation(1, 3, "Verket"));
		stationList.add(2 ,new WindWidgetStation(2, 105, "Hvittingfoss"));
		stationList.add(3, new WindWidgetStation(3, 6, "Hovden"));
		stationList.add(4 ,new WindWidgetStation(4, 9, "Grøtfjord"));
		stationList.add(5, new WindWidgetStation(5, 12, "Finnlandsfjellet"));
		stationList.add(6, new WindWidgetStation(6, 14, "Breivikeidet"));
		stationList.add(7, new WindWidgetStation(7, 15, "Sommarøy"));
		stationList.add(8, new WindWidgetStation(8, 1003, "Eiken"));
		stationList.add(9, new WindWidgetStation(9, 106, "Vinje"));
		stationList.add(10, new WindWidgetStation(10, 107, "Skinnvollen"));
		stationList.add(11, new WindWidgetStation(11, 1004, "Lofthus - Nosi"));
		stationList.add(12, new WindWidgetStation(12, 21, "Kroken"));
		stationList.add(13, new WindWidgetStation(13, 20, "Takvannet"));
		stationList.add(14, new WindWidgetStation(14, 23, "Ersfjordeidet"));
		stationList.add(15, new WindWidgetStation(15, 51, "Hvasser"));
		stationList.add(16, new WindWidgetStation(16, 52, "Moss Havn"));
		stationList.add(17, new WindWidgetStation(17, 53, "Randaberg"));
		stationList.add(18, new WindWidgetStation(18, 56, "Steilene"));
		stationList.add(19, new WindWidgetStation(19, 58, "Rolfstangen"));

	}
	
	public static ArrayList<WindWidgetStation> getStationList() {
		return stationList;
	}
	

}
