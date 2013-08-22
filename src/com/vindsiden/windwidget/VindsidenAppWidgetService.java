package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.vindsiden.windwidget.config.WindWidgetConfig;
import com.vindsiden.windwidget.config.WindWidgetConfigManager;
import com.vindsiden.windwidget.model.Measurement;
import com.vindsiden.windwidget.model.PresentationHelper;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * A work queue processor that handles asynchronous App Widget update requests.
 * 
 * As a "good citizen", using minimal battery to complete tasks, this service is started as a needed, and stops itself
 * when it runs out of work.
 * 
 */
public class VindsidenAppWidgetService extends IntentService {
	private static final String PACKAGE_NAME = VindsidenAppWidgetService.class.getPackage().getName();
	private static final String NEXT_SCHEDULE_URI_POSTFIX = "/next_schedule";
	private static final String WIDGET_PREFIX = "/widget_id/";
	
	static int buckCounter = 0;
	// Larkollen XML - last entry. 
	private static final String URL_PREFIX = "http://www.vindsiden.no/xml.aspx?id=";//"http://www.vindsiden.no/xml.aspx?id=1&last=1";
	private static final String URL_POSTFIX = "&last=1"; 

  private static final String tag = AppWidgetProvider.class.getName(); // getSimpleName());
  
	// alternatives for more data:
	//"http://www.vindsiden.no/xml.aspx?id=1&hours=1";//"http://www.vindsiden.no/xml.aspx?id=1";	

	//todo: support multiple configs (for multiple parallell widgets)
	public static final WindWidgetConfig config = WindWidgetConfig.createADefaultConfig();
	
	/**
	 * Creates an instance with a name defined by the constant
	 */
	public VindsidenAppWidgetService() {
		this(VindsidenAppWidgetService.class.toString());
	}

	/**
	 * @param name
	 *          Used to name the worker thread, important only for debugging.
	 */
	public VindsidenAppWidgetService(String name) {
		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

		int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
		if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
			updateOneAppWidget(appWidgetManager, incomingAppWidgetId);
		} else {
			updateAllAppWidgets(appWidgetManager);
		}
		scheduleNextUpdate();
	}

	/**
	 * Schedules the next app widget update to occur 
	 * previously scheduled app widget update is effectively canceled and replaced by the newly scheduled update.
	 */
	private void scheduleNextUpdate() {
		Intent updateMeasurementIntent = new Intent(this, this.getClass());
		// A content URI for this Intent may be unnecessary. (comment from the example from Programmer Bruce)
		// TODO should this be linked to an appwidget ID as well, in case we have different widgets with different schedules handled by the same service?
		updateMeasurementIntent.setData(Uri.parse("content://" + PACKAGE_NAME + NEXT_SCHEDULE_URI_POSTFIX)); 
		PendingIntent updateMeasurementPendingIntent = PendingIntent.getService(this, 0, updateMeasurementIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		// In effect, the first day update times will be dependent on at what time the user started the app,
		// it could for examle first update at 0903, then 0918, 0933 etc ...
		// yet, the next day, the first measurement should be made at the getStartTime() (0900)
		// and following measurements should be made a predictable interavl increments (0915, 0930)
		Time nextUpdateTime = new Time();		
		nextUpdateTime.set(System.currentTimeMillis() + 
				VindsidenAppWidgetService.config.getFrequenceIntervalInMicroseconds());
		long nextUpdate = nextUpdateTime.toMillis(false);
		
		Time endTimeToday = new Time();
		endTimeToday.set(System.currentTimeMillis());
		endTimeToday.hour = VindsidenAppWidgetService.config.getEndTime().hour;
		endTimeToday.minute = VindsidenAppWidgetService.config.getEndTime().minute;
		
		// schedule next update tomorrow at startTime 
		// if the next update would otherwise have been after our update interval ends for today		
		if (nextUpdateTime.after(endTimeToday)) {
			Time startTimeToday = new Time();
			startTimeToday.set(System.currentTimeMillis());
			startTimeToday.hour = VindsidenAppWidgetService.config.getStartTime().hour;
			startTimeToday.minute = VindsidenAppWidgetService.config.getStartTime().minute;
			
			long oneDayInMils= (1*24*60*60*1000); //add one days worth of miliseconds. (24 hours 60 minutes 60 seconds)
			
			nextUpdate = startTimeToday.toMillis(false) + oneDayInMils;
		}			
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// TODO: check: RTC could specifiy "only update a non-sleeping device?"
		alarmManager.set(AlarmManager.RTC, nextUpdate, updateMeasurementPendingIntent);		
	}

	/**
	 * For each random vindsiden app widget on the user's home screen, updates its display, and registers click handling
	 * for its buttons.
	 */
	private void updateAllAppWidgets(AppWidgetManager appWidgetManager) {
		ComponentName appWidgetProvider = new ComponentName(this, VindsidenAppWidgetProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidgetProvider);
		int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			updateOneAppWidget(appWidgetManager, appWidgetId);
		}
	}

	/**
	 * For the vindsiden app widget with the provided ID, updates its display with a new message, and registers click
	 * handling for its buttons.
	 */
	private void updateOneAppWidget(AppWidgetManager appWidgetManager, int appWidgetId) {

		RemoteViews views = new RemoteViews(PACKAGE_NAME, R.layout.app_widget_layout);
		views.setTextViewText(R.id.passcode_view, "Beskrivelse - txtvarsel?");

		// dra ut data fra XML på vindsiden http://www.vindsiden.no/xml.aspx?id=1
		List<Measurement> measurements;
		try {
			String urlString = URL_PREFIX+ 
					VindsidenAppWidgetService.config.getStationID()+
					URL_POSTFIX;
			Log.d(tag, urlString);
			measurements = (new VindsidenWebXmlReader()).loadXmlFromNetwork(urlString);
		} 
		catch (IOException e) { 
			throw new RuntimeException (getResources().getString(R.string.connection_error)); 
		}
		catch (XmlPullParserException e) { 
			throw new RuntimeException ( getResources().getString(R.string.xml_error));
		}		
		finally { // TODO : any necessary cleanup. 
		} 		
		
		// assume the most recent data is read first from the XML - it probably is, but there's possibility for error here.
		Measurement mostRecentMeasurement = measurements.get(0);
		// add a measure of tolerance (for the case where XML exists, but has readable measurements) 
		if (mostRecentMeasurement == null) { 
			mostRecentMeasurement = new Measurement("", "", "?", "-999"); // -999 triggers "?" as direction
		}
		// sett en veldig enkel knapp gfx (char basert pt) for å indikere vindstyrke og retning
		StringBuffer windText = new StringBuffer("");
		windText.append(PresentationHelper.getWindStrengthString(mostRecentMeasurement.getWindAvg()));
		windText.append("\n"+PresentationHelper.getWindDirectionString(mostRecentMeasurement.getDirectionAvg()));
		windText.append("\n"+"@"+mostRecentMeasurement.getStationID());
		//debug: add counter
		windText.append("-" + buckCounter++);
		views.setTextViewText(R.id.widgetButton, windText);
		
		setProcessWidgetClickIntent(views, appWidgetId, "aMessage");
	    
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	
	  /** 
	   * Configures button clicks to pass the current message of the 
	   * parent app widget to the Activity.
	   */
	  private void setProcessWidgetClickIntent(RemoteViews views, int appWidgetId, String newMessage)
	  {
	    Intent intent = new Intent(this, VindsidenActivity.class);
	    intent.setData(Uri.parse("content://" + PACKAGE_NAME + WIDGET_PREFIX + appWidgetId));
	    intent.putExtra("ARGUMENT", newMessage); // not really used anymore. a debug option.	  
	    intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
	    PendingIntent pendingIntent =
	        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
	  }

}