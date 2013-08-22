package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import com.vindsiden.windwidget.config.WindWidgetConfig;
import com.vindsiden.windwidget.config.WindWidgetConfigManager;
import com.vindsiden.windwidget.model.BDayWidgetModel;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Handles intents for Vindsiden App Widget updates and other lifecycle events. Delegates app widget updates to
 * VindsidenAppWidgetService.
 */
public class VindsidenAppWidgetProvider extends AppWidgetProvider {

	private static final String tag = AppWidgetProvider.class.getName(); // getSimpleName());

	/**
	 * Kalles alltid når en intent mottas - vi bør antagelig ikke endre noe mer på denne. {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Special app widget delete handling added for bug in Android 1.5, as described at
		// http://groups.google.com/group/android-developers/browse_thread/thread/365d1ed3aac30916?pli=1
		@SuppressWarnings("deprecation")
		// using this deprecated field seems to be a requirement to support min SDK 3 (otherwise min SDK 4 is required, it
		// seems. no biggie I guess)
		String sdk = android.os.Build.VERSION.SDK;
		String release = android.os.Build.VERSION.RELEASE;
		String action = intent.getAction();
		if ((sdk.equals("3") || release.equals("1.5")) && ACTION_APPWIDGET_DELETED.equals(action)) {
			int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
			if (appWidgetId != INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}

	/**
	 * Kalles når hver instans lages og kalles på nytt hvis provider_config.xml fila spesifiserer frekvensintervall Vår
	 * xml fil spesifiserer IKKE et slikt intervall, så jeg antar pt denne bare kalles hver gang en ny appwidget instans
	 * legges til container. {@inheritDoc}
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		int appWidgetId = INVALID_APPWIDGET_ID;
		// Jeff Sharkey's Sky app portends a null check is necessary, though the
		// API documentation provides no indication that null is possible.
		if (appWidgetIds != null) {
			int N = appWidgetIds.length;
			if (N == 1) {
				appWidgetId = appWidgetIds[0];
			}
		}

		Log.d(tag, "onUpdate called");
		Log.d(tag, "Number of widgets:" + appWidgetIds.length);

		// assume my WindWidgetConfigManager class is not garbage collected,
		// and use it to save some config data (I would normally have put that data in a AppWidget implementation,
		// but that seems to go against the grain the way Android thinks? not willing to save state in buttons/view/layout
		// components :p
		// Using the class and static methos to pass config params is rather dirty, but that's the way it is
		// 'til refactoring comes ...
		/*
		 * for (int id : appWidgetIds) { WindWidgetConfigManager.addConfigAt(id, WindWidgetConfig.createADefaultConfig()); }
		 */

		BDayWidgetModel bwm = BDayWidgetModel.retrieveModel(context, appWidgetId);
		if (bwm == null) {
			Log.d(tag, "No widget model found for:" + appWidgetId);
			// return; //oeb: dangers of copying code - this must be rewritten, no bad return statemnt here!
		}

		// oeb: I assume this can be done from a service (with knowledge of appwidgetid) instead.
		// ConfigureBDayWidgetActivity.updateAppWidget(context, appWidgetManager, bwm);

		Intent intent = new Intent(context, VindsidenAppWidgetService.class);
		intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
		context.startService(intent);
	}

	/**
	 * Kalles hver gang en appwidget slettes fra sin host.
	 * 
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		// code here for deleting config files
		int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			if (i != INVALID_APPWIDGET_ID) {
				BDayWidgetModel.retrieveModel(context, appWidgetIds[i]); // save state before widget is removed
			}
		}
	}

	// kalles når første instans av widget legges på homescreen - init a store I suppose?
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
		Log.d(tag, "onEnabled called");
		BDayWidgetModel.clearAllPreferences(context);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.ai.android.BDayWidget", ".BDayWidgetProvider"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	// Kalles når den siste instans av widget fjernes fra homescreen - free up store etc I suppose?
	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Log.d(tag, "onDisabled called");
		BDayWidgetModel.clearAllPreferences(context);
		PackageManager pm = context.getPackageManager();
		// oeb: HMMM??? BØR IKKE DETTE VÆRE DISABLED I SÅ FALL? SKJØNNER IKKE.
		pm.setComponentEnabledSetting(new ComponentName("com.ai.android.BDayWidget", ".BDayWidgetProvider"),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

}