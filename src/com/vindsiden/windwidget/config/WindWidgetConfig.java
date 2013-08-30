package com.vindsiden.windwidget.config;


import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.text.format.Time;

public class WindWidgetConfig {

	private static String PREFERENCES_FILE_PREFIX = "WindWidgetPreferences";
	private static String PREF_STATIONID_KEY = "STATION_ID";

	private static String PREF_FREQUENCE_IN_MINUTES_KEY = "FREQ_ID";
	private static String PREF_START_TIME_KEY = "START_TIME_ID";
	private static String PREF_END_TIME_KEY = "END_TIME_ID";

	private static int DEFAULT_STATION_ID = 1;
	private static int DEFAULT_FREQUENCE_IN_MINUTES = 15;

	private static Time DEFAULT_START_TIME; // set by static initializer
	private static Time DEFAULT_END_TIME;

	// static initializer for startTime and endTime defaults
	static {
		Time t = new Time();
		t.set(System.currentTimeMillis());
		t.hour = 9;
		t.minute = 0;
		t.second = 0;
		DEFAULT_START_TIME = t;

		Time t2 = new Time();
		t2.set(System.currentTimeMillis());
		t2.hour = 19;
		t2.minute = 0;
		t2.second = 0;
		DEFAULT_END_TIME = t2;
	}

	public static void setWindStationId(ContextWrapper c, int appWidgetId, int windStationId) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX + appWidgetId, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(WindWidgetConfig.PREF_STATIONID_KEY, windStationId);
		editor.commit();
	}

	public static int getWindStationId(ContextWrapper c, int appWidgetId) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX + appWidgetId, 0);
		return pref.getInt(WindWidgetConfig.PREF_STATIONID_KEY, DEFAULT_STATION_ID); 
	}
		
	// toString() prints the Time in the format:
	//YYYYMMDDTHHMMSS
	//0123456789
	// this is used for save/restore, but only time+minute is really used in the code, so year/month etc is unused.
	public static void setStartTime(ContextWrapper c, Time t) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(WindWidgetConfig.PREF_START_TIME_KEY, t.toString());
		editor.commit();
	}

	public static Time getStartTime(ContextWrapper c) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		return getTimeFromString(
				pref.getString(WindWidgetConfig.PREF_START_TIME_KEY, DEFAULT_START_TIME.toString()));
	}

	public static void setEndTime(ContextWrapper c, Time t) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(WindWidgetConfig.PREF_END_TIME_KEY, t.toString());
		editor.commit();
	}

	public static Time getEndTime(ContextWrapper c) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		return getTimeFromString(
				pref.getString(WindWidgetConfig.PREF_END_TIME_KEY, DEFAULT_END_TIME.toString()));
	}

	
	private static Time getTimeFromString(String timeString) {
		Time t = new Time();		
		t.set(System.currentTimeMillis());
		t.hour = Integer.parseInt(timeString.substring(9, 11));		
		t.minute = Integer.parseInt(timeString.substring(11, 13));
		t.second = 0;
		return t;
	}
	
	public static void setFrequenceIntervalInMinutes(ContextWrapper c, int newInterval) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(WindWidgetConfig.PREF_FREQUENCE_IN_MINUTES_KEY, newInterval);
		editor.commit();
	}

	public static int getFrequenceIntervalInMinutes(ContextWrapper c) {
		SharedPreferences pref = c.getSharedPreferences(WindWidgetConfig.PREFERENCES_FILE_PREFIX, 0);
		return  pref.getInt(WindWidgetConfig.PREF_FREQUENCE_IN_MINUTES_KEY, DEFAULT_FREQUENCE_IN_MINUTES);
	}

	public static long getFrequenceIntervalInMicroseconds(ContextWrapper c) {
		return getFrequenceIntervalInMinutes(c) * 60 * 1000;
	}

}
