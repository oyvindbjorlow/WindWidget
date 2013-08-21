package com.vindsiden.windwidget.config;

import java.util.ArrayList;

public class WindWidgetConfigManager {
	private static ArrayList<WindWidgetConfig> configList = new ArrayList<WindWidgetConfig>();
	
	public static WindWidgetConfig getConfigFor(int id) {
		WindWidgetConfig c = null;
		
		if (configList.get(id)!= null) {
			c = configList.get(id);
		}
		
		return c;
	}
	public static void addConfigAt(int id, WindWidgetConfig c) {
		configList.set(id,c);		
	}
	
	public static void removeConfigAt(int id) {
		configList.set(id, null);
	}

}
