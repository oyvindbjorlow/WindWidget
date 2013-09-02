package com.vindsiden.windwidget.model;

/*
 * A simple class with static methods to provide a simple representation
 * of wind direction and strength.
 */
public class PresentationHelper {

	public static Float getWindStrength(String windStrengthString) {
		Float windStrength;
		try {
			windStrength = Float.valueOf(windStrengthString);
		} catch (NumberFormatException e) {
			windStrength = Float.valueOf(0); // for robustness, just provide a value, we assume "0" best signifies
																				// "no strength measured".
		}
		return windStrength;
	}

	/* assumes isValidDireciotn */
	public static int getDirectionInt(String degreeString) {
		int degreeInt;
		try {
			degreeInt = Integer.valueOf(degreeString);
		} catch (NumberFormatException e) {
			degreeInt = Integer.valueOf(-999);
		}
		return degreeInt;
	}

	public static boolean isValidDirection(String degree) {
		int degreeInt;
		try {
			degreeInt = Integer.valueOf(degree);
		} catch (NumberFormatException e) {
			degreeInt = Integer.valueOf(-999);
		}
		return ((degreeInt > -180) && (degreeInt < 360)); // check these limits with cg/Øystein, I suppose
	}

	public static String getWindDirectionString(String directionAvg) {
		// @formatter:off
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
		// @formatter:on
		return directionString;
	}

	public static String getWindStrengthString(String windAvg) {
		return windAvg; // No conversion done at present.
	}

}
