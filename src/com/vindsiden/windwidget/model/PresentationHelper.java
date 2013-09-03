package com.vindsiden.windwidget.model;

import com.vindsiden.windwidget.R;

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

	public static int getWindStrengthDrawable(float windStrength ) {
		// @formatter:off
		int arrowPng = R.drawable.zero;		
		if (windStrength == Float.NaN) {}		 // keep 0 as the image.
		if (windStrength > 0) 				{arrowPng = R.drawable.arrow;}				
		if (windStrength >= 2.5)	 		{arrowPng = R.drawable.arrow2;}		
		if (windStrength >= 5) 				{arrowPng = R.drawable.arrow5;}		
		if (windStrength >= 7.5)	 		{arrowPng = R.drawable.arrow7;}
		if (windStrength >= 10) 			{arrowPng = R.drawable.arrow10;}  
		if (windStrength >= 12.5) 		{arrowPng = R.drawable.arrow12;}				
		if (windStrength >= 15) 			{arrowPng = R.drawable.arrow15;}		
		if (windStrength >= 17.5) 		{arrowPng = R.drawable.arrow17;}		
		if (windStrength >= 20) 			{arrowPng = R.drawable.arrow20;} // this gfx is the "max wind" gfx of this simple version
		// @formatter:on
		return arrowPng;
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
