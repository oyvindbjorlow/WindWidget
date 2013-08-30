package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import com.vindsiden.windwidget.config.WindWidgetConfig;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * Receives requests from App Widgets
 */
public class VindsidenActivity extends Activity {

	int appWidgetId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		String customMessage = intent.getStringExtra("MESSAGE");

		appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

		int widgetStationID = WindWidgetConfig.getWindStationId(this, appWidgetId);

		Spinner stationIdSpinner = (Spinner) findViewById(R.id.spinner2);
		// TODO some more robustness here would be nice I suppose (?)
		// stationIdSpinner.setSelection(VindsidenAppWidgetService.config.getStationID());
		// use this is preferences bugs:
		// widgetStationID = VindsidenAppWidgetService.config.getStationID());
		stationIdSpinner.setSelection(widgetStationID);
		stationIdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				// TODO Øyvind: Noted som stackoverflow people checked for position > (that is, not >= ) 0 ...
				// not certain if the Spinner class might be bug prone
				WindWidgetConfig.setWindStationId(VindsidenActivity.this, appWidgetId, position);

			};

			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				// TODO (?)
			};
		});

		String message = customMessage == null ? "WindWidget oppsett" : customMessage;
		TextView tv = (TextView) findViewById(R.id.windwidget_view);
		tv.setText(message);

		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		int defaultChoice = 1;
		int oldFreq = WindWidgetConfig.getFrequenceIntervalInMinutes(this);
		// TODO some robustness here would be nice I suppose - freq values are defined both in XML and hardcoded here as of
		// now.
		if (oldFreq == 5) {
			defaultChoice = 0;
		}
		;
		if (oldFreq == 15) {
			defaultChoice = 1;
		}
		;
		if (oldFreq == 30) {
			defaultChoice = 2;
		}
		;
		if (oldFreq == 60) {
			defaultChoice = 3;
		}
		;
		spinner.setSelection(defaultChoice);

		TimePicker timePick2 = (TimePicker) findViewById(R.id.timePicker2);
		initTimepicker(timePick2, WindWidgetConfig.getStartTime(this));

		TimePicker timePick3 = (TimePicker) findViewById(R.id.timePicker3);
		initTimepicker(timePick3, WindWidgetConfig.getEndTime(this));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				// TODO Øyvind: Noted som stackoverflow people checked for position > (that is, not >= ) 0 ... not certain if
				// the spinner might be bug prone
				if (position >= 0) {
					String choiceString = (String) parent.getItemAtPosition(position);
					int freq = Integer.valueOf(choiceString);
					if (freq < 1) {
						freq = 5;
					}
					; // paranoia robustness
					WindWidgetConfig.setFrequenceIntervalInMinutes(VindsidenActivity.this, freq);

				}
			};

			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				// TODO (?)
			};
		});

		timePick2.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Time t = new Time();
				t.hour = hourOfDay;
				t.minute = minute;
				WindWidgetConfig.setStartTime(VindsidenActivity.this, t);
			}
		});
		timePick3.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Time t = new Time();
				t.hour = hourOfDay;
				t.minute = minute;
				WindWidgetConfig.setEndTime(VindsidenActivity.this, t);
			}
		});

	}

	private void initTimepicker(TimePicker tp, Time t) {
		tp.setIs24HourView(true);
		tp.setCurrentHour(t.hour);
		tp.setCurrentMinute(t.minute);
	}

}