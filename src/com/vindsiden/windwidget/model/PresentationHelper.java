 package com.vindsiden.windwidget.model;
/*
 * A simple class with static methods to provide a simple representation
 * of wind direction and strength.
 */
public class PresentationHelper {
	
	
	
	// Assume a 200x200 coordinate grid with origo and +/-100 at its extremes.
	// Assume a degree input value of 0 is Northern wind (an arrow pointing straight down from origo
	// This means we'll draw on a lot less than our available space, and we'll need to
	// adjust this for actual size of canvas ...
	private static int xModifier = -100;
	private static int yModifier = -100;
	
	
	public static Float getWindStrength(String windStrength) {
		return new Float(Float.valueOf(windStrength)); // . , sanity might be in order ...		
	}
	
	public static boolean isValidDirection (String degree) {
		int degreeInt = Integer.valueOf(degree);		
		return ((degreeInt > -180) && (degreeInt < 360)); // check these limits with cg/Øystein, I suppose
	}
	
	
	//degrees to rotate the default image (currently showing a pure Southern wind, necessitatying 180 degrees offset here)
	public static int getDegreesRotation (String degreeMeasurement) {
		return Integer.valueOf(degreeMeasurement)+180;
	}
	public static int xPosition(String degree) {
		int degreeInt = Integer.valueOf(degree);		
		return (new Double(Math.sin(Math.toRadians(degreeInt))*xModifier)).intValue();
	}
	public static int yPosition(String degree) {
		int degreeInt = Integer.valueOf(degree);		
		return (new Double(Math.cos(Math.toRadians(degreeInt))*yModifier)).intValue();
	}
	
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
