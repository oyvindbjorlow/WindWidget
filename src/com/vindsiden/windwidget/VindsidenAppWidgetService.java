package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.vindsiden.windwidget.config.WindWidgetConfig;
import com.vindsiden.windwidget.model.Measurement;
import com.vindsiden.windwidget.model.PresentationHelper;
import com.vindsiden.windwidget.model.WindWidgetStations;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
	private static final String WIDGET_URI_PREFIX = "/widget_id/";

	// robustness: if XML doesn't include any Measurements we can provide this pohny measurement. -999 triggers "?" as
	// direction
	private static final Measurement PHONY_MEASUREMENT = new Measurement("", "", "?", "-999");
	private static final String tag = AppWidgetProvider.class.getName(); // getSimpleName());

	// alternatives for more data:
	// "http://www.vindsiden.no/xml.aspx?id=1&hours=1";//"http://www.vindsiden.no/xml.aspx?id=1";

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
			// dette trigges i hvert fal fra Providers onUpdate (ved innlegg av ny widget). antagelig aldri ellers?
			updateOneAppWidget(appWidgetManager, incomingAppWidgetId);
		} else {
			// dette trigges av pendingIntent fyrt fra scheduleNextUpdate()
			updateAllAppWidgets(appWidgetManager);
		}
		scheduleNextUpdate();
	}

	/**
	 * Schedules the next app widget update to occur previously scheduled app widget update is effectively canceled and
	 * replaced by the newly scheduled update.
	 */
	private void scheduleNextUpdate() {
		Intent updateMeasurementIntent = new Intent(this, this.getClass());
		// A content URI for this Intent may be unnecessary. (comment from the example from Programmer Bruce)
		updateMeasurementIntent.setData(Uri.parse("content://" + PACKAGE_NAME + NEXT_SCHEDULE_URI_POSTFIX));
		PendingIntent updateMeasurementPendingIntent = PendingIntent.getService(this, 0, updateMeasurementIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// In effect, the first day update times will be dependent on at what time the user started the app,
		// it could for examle first update at 0903, then 0918, 0933 etc ...
		// yet, the next day, the first measurement should be made at the getStartTime() (0900)
		// and following measurements should be made a predictable interavl increments (0915, 0930)
		// TODO also, I assume the real frequence here is (FREQ+processing time for downloading and showing 1 measurement)
		// but that's acceptable for now.
		Time nextUpdateTime = new Time();
		Log.d(tag, "scheduleNextUpdate called at approx: " + System.currentTimeMillis());
		nextUpdateTime.set(System.currentTimeMillis() + WindWidgetConfig.getFrequenceIntervalInMicroseconds(this));
		long nextUpdate = nextUpdateTime.toMillis(false);

		Time endTimeToday = new Time();
		endTimeToday.set(System.currentTimeMillis());
		Time configEndTime = WindWidgetConfig.getEndTime(this);
		endTimeToday.hour = configEndTime.hour;
		endTimeToday.minute = configEndTime.minute;

		// schedule next update tomorrow at startTime
		// if the next update would otherwise have been after our update interval ends for today
		if (nextUpdateTime.after(endTimeToday)) {
			Time startTimeToday = new Time();
			startTimeToday.set(System.currentTimeMillis());
			Time configStartTime = WindWidgetConfig.getStartTime(this);
			startTimeToday.hour = configStartTime.hour;
			startTimeToday.minute = configStartTime.minute;

			// add one days worth of miliseconds. (24 hours 60 minutes 60seconds)
			long oneDayInMils = (1 * 24 * 60 * 60 * 1000);

			nextUpdate = startTimeToday.toMillis(false) + oneDayInMils;
		}

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// RTC does not waken up the device, but the intent is triggered once the device reawakens from another trigger
		// fra doc: If there is already an alarm scheduled for the same IntentSender, it will first be canceled.
		alarmManager.set(AlarmManager.RTC, nextUpdate, updateMeasurementPendingIntent);
		Log.d(tag, "scheduleNextUpdate set the alarm for: " + nextUpdate);

	}

	/**
	 * For each random vindsiden app widget on the user's home screen, updates its display, and registers click handling
	 * for its buttons.
	 */
	private void updateAllAppWidgets(AppWidgetManager appWidgetManager) {
		ComponentName appWidgetProvider = new ComponentName(this, VindsidenAppWidgetProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidgetProvider);
		int N = appWidgetIds.length;

		StringBuffer idsToUpdate = new StringBuffer();
		for (int oneId : appWidgetIds) {
			idsToUpdate.append(" " + oneId);
		}
		Log.d(tag, "updateAllAppWidgets called for set: " + idsToUpdate);

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
		Log.d(tag, "updateOneAppWidgets called for id: " + appWidgetId);
		RemoteViews views = new RemoteViews(PACKAGE_NAME, R.layout.app_widget_layout);
		views.setTextViewText(R.id.header_view, "Beskrivelse - txtvarsel?");

		int widgetStationID = WindWidgetConfig.getWindStationId(this, appWidgetId);

		List<Measurement> measurements = null;

		try {
			String urlString = WindWidgetConfig.getVindsidenUrlPrefix() + widgetStationID
					+ WindWidgetConfig.getVindsidenUrlPostfix();
			Log.d(tag, urlString);
			measurements = (new VindsidenWebXmlReader()).loadXmlFromNetwork(urlString);
		} catch (IOException e) {
			Log.d(tag, "An IO exception occured. Stack follows: ");
			Log.d(tag, e.getStackTrace().toString());
			// not certain how robust throwing a runtime exception is, might break stuff with recurrence etc!
			// throw new RuntimeException(getResources().getString(R.string.connection_error));
		} catch (XmlPullParserException e) {
			Log.d(tag, "An XmlPullParserException occured. Stack follows: ");
			Log.d(tag, e.getStackTrace().toString());
			// throw new RuntimeException(getResources().getString(R.string.xml_error));
		}

		// add a measure of tolerance for the following cases:
		// 1: no measurements were returned (network or XML or parser error or somesuch)
		// 2: where XML exists, but has readable measurements)
		// || operator should avoid any null pointer exceptions for case 2: (2nd operand only evaluted if measurements !=
		// null)
		// (we had problems with the alarm/scheduler seemingly dying if network had been turned off for a while,
		// a theory is it was caused by a nullpointer exception that could happen here in the old code)
		// size () <1 check added, as we've seen measurements != null yet get(0) throwing runtime exc. runtime.
		Measurement mostRecentMeasurement;
		if ((measurements == null) || (measurements.size() < 1) || (measurements.get(0) == null)) {
			// make sure we always have some data in a Measurement object here.
			mostRecentMeasurement = PHONY_MEASUREMENT;
			// not sure we should use this PHONY_MEASUREMENT anymore. instead, we could gray out the
			// widget(s) where no valid measurement was found
			// Set a different background color to signify "no update, due to no connection, invalid measurement or somesuch"
			views.setInt(R.id.imageButton1, "setBackgroundColor",  0x00999999); //0x00000000); this was all black, no opaque
			
			
		} else {
			views.setInt(R.id.imageButton1, "setBackgroundColor", 0x00FFFFFF);
			// assume the most recent data is read first from the XML - it probably is, but there's possibility for error
			mostRecentMeasurement = measurements.get(0);

			/*
			 * // sett en veldig enkel knapp gfx (char basert pt) for å indikere vindstyrke og retning StringBuffer windText =
			 * new StringBuffer("");
			 * windText.append(PresentationHelper.getWindStrengthString(mostRecentMeasurement.getWindAvg()));
			 * windText.append("\n" + PresentationHelper.getWindDirectionString(mostRecentMeasurement.getDirectionAvg()));
			 * 
			 * Time t = new Time(); t.set(System.currentTimeMillis()); windText.append("\n" + // t.monthDay+"." + t.month+" "+
			 * // t.hour+""+ // t.minute + "@" + mostRecentMeasurement.getStationID());
			 * 
			 * views.setTextViewText(R.id.widgetButton, windText);
			 */

			// Very simply gfx support: First, choose a predrawn arrow based on strength
			int arrowPng = PresentationHelper.getWindStrengthDrawable(PresentationHelper
					.getWindStrength(mostRecentMeasurement.getWindAvg()));

			// rotate the predawn arrow depending on measured direction:
			if (PresentationHelper.isValidDirection(mostRecentMeasurement.getDirectionAvg())) {
				Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), arrowPng);
				Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight() + 40,
						Bitmap.Config.ARGB_8888);
				Canvas tempCanvas = new Canvas(bmResult);
				tempCanvas.rotate(PresentationHelper.getDirectionInt(mostRecentMeasurement.getDirectionAvg()),
						bmpOriginal.getWidth() / 2, bmpOriginal.getHeight() / 2); // focal point is center of orig image (not+40
																																			// height)
				tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);

				Canvas tempCanvas2 = new Canvas(bmResult); // the other canvas won the bmp was rotated, so we create another.
				Paint paint = new Paint();
				paint.setColor(Color.WHITE);
				paint.setTextSize(20f);
				paint.setAntiAlias(true);
				paint.setFakeBoldText(true);
				paint.setShadowLayer(6f, 0, 0, Color.BLACK);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextAlign(Paint.Align.LEFT);
				tempCanvas2.drawText(PresentationHelper.getWindStrengthString(mostRecentMeasurement.getWindAvg()) + "ms "
				// +"@"+ mostRecentMeasurement.getStationID()
						, 0, bmResult.getHeight() - 20, // 20,
						// used 20 for drawing close to top of button
						paint);
				tempCanvas2.drawText(WindWidgetStations.getStationNameForStationId(widgetStationID), 0, bmResult.getHeight(),
						paint);

				views.setBitmap(R.id.imageButton1, "setImageBitmap", bmResult);
			} else {
				// for now, just draw a logo if no valid direction
				views.setBitmap(R.id.imageButton1, "setImageBitmap",
						BitmapFactory.decodeResource(getResources(), R.drawable.icon));
			}
		}

		setProcessWidgetClickIntent(views, appWidgetId, "aMessage");

		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Configures button clicks to pass the current message of the parent app widget to the Activity.
	 */
	private void setProcessWidgetClickIntent(RemoteViews views, int appWidgetId, String newMessage) {
		Intent intent = new Intent(this, VindsidenActivity.class);
		intent.setData(Uri.parse("content://" + PACKAGE_NAME + WIDGET_URI_PREFIX + appWidgetId));
		intent.putExtra("ARGUMENT", newMessage); // not really used anymore. a debug option.
		intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
		views.setOnClickPendingIntent(R.id.imageButton1, pendingIntent);
	}

}