package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import java.util.ArrayList;

import com.vindsiden.windwidget.config.WindWidgetConfig;
import com.vindsiden.windwidget.model.WindWidgetStation;
import com.vindsiden.windwidget.model.WindWidgetStations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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

		// StationID SPINNER
		Spinner stationIdSpinner = (Spinner) findViewById(R.id.spinner2);
		ArrayList<String> spinnerArray = new ArrayList<String>();
		for (WindWidgetStation w : WindWidgetStations.getStationList()) {
			spinnerArray.add(w.getStationName());
		}
		ArrayAdapter<String> spinnerArrayAdapter = 
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
		stationIdSpinner.setAdapter(spinnerArrayAdapter);
		
		stationIdSpinner.setSelection(
				WindWidgetStations.getIndexForStationID(WindWidgetConfig.getWindStationId(this,appWidgetId)));

		stationIdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				// TODO Øyvind: Noted som stackoverflow people checked for position > (that is, not >= ) 0 ...
				// not certain if the Spinner class might be bug prone
				WindWidgetConfig.setWindStationId(VindsidenActivity.this, appWidgetId,
						WindWidgetStations.getStationIdForIndex(position));
			};

			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
			};
		});

		String message = customMessage == null ? "WindWidget oppsett" : customMessage;
		TextView tv = (TextView) findViewById(R.id.windwidget_view);
		tv.setText(message);

		
		// FREQUENCE SPINNER
		// TODO some robustness here would be nice I suppose - freq values are defined both in XML and hardcoded here
		Spinner freqSpinner = (Spinner) findViewById(R.id.spinner1);
		int defaultChoice = 1;
		int oldFreq = WindWidgetConfig.getFrequenceIntervalInMinutes(this);
		// @formatter:off
		if (oldFreq == 5) {defaultChoice = 0;};
		if (oldFreq == 15) {defaultChoice = 1;};
		if (oldFreq == 30) {defaultChoice = 2;};
		if (oldFreq == 60) {defaultChoice = 3;};
		// @formatter:on
		freqSpinner.setSelection(defaultChoice);

		// TIME PICKERS START/ENDTIMES
		TimePicker timePick2 = (TimePicker) findViewById(R.id.timePicker2);
		initTimepicker(timePick2, WindWidgetConfig.getStartTime(this));

		TimePicker timePick3 = (TimePicker) findViewById(R.id.timePicker3);
		initTimepicker(timePick3, WindWidgetConfig.getEndTime(this));

		freqSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
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