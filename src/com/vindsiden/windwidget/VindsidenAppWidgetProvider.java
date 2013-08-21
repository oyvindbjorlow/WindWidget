package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_DELETED;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Handles intents for Vindsiden App Widget updates and other lifecycle events. 
 * Delegates app widget updates to VindsidenAppWidgetService.
 */
public class VindsidenAppWidgetProvider extends AppWidgetProvider {
	/**
	 * {@inheritDoc}
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

		Intent intent = new Intent(context, VindsidenAppWidgetService.class);
		intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);

		context.startService(intent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Special app widget delete handling added for bug in Android 1.5, as described at
		// http://groups.google.com/group/android-developers/browse_thread/thread/365d1ed3aac30916?pli=1
		@SuppressWarnings("deprecation")
		// using this deprecated field seems to be a requirement to support min
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
}