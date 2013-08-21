package com.vindsiden.windwidget.model;
/*
 * A simple class with static methods to provide a simple representation
 * of wind direction and strength.
 */
public class PresentationHelper {
	public static String getWindDirectionString(String directionAvg) {
		String directionString = "O";  // signifies no wind
		int dirInt = Integer.valueOf(directionAvg);
		if (dirInt < -90) 	{directionString = "?";}		// signifies a
		if (dirInt >= -90) 	{directionString = ">";}    // signifies western wind (a simple arrow pointing east) 
		if (dirInt >= -72) 	{directionString = "v>";}
		if (dirInt >= -23) 	{directionString = "v";}
		if (dirInt >= +23) 	{directionString = "<v";}
		if (dirInt >= +72) 	{directionString = "<";}
		if (dirInt >= +112)	{directionString = "<^";}
		if (dirInt >= +158) {directionString = "^";}
		if (dirInt >= +202)	{directionString = "^>";}
		if (dirInt >= +247)	{directionString = ">";}
		if (dirInt >= +271)	{directionString = "v>";}  // 273 actually reported so broadening the range a bit		
		if (dirInt >= +295)	{directionString = "?";}
		
		return directionString;
	}
	
	public static String getWindStrengthString(String windAvg) {
		return windAvg; // No conversion done at present.
	}



}
