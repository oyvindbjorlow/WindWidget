package com.vindsiden.windwidget;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import com.vindsiden.windwidget.config.WindWidgetConfig;
import com.vindsiden.windwidget.config.WindWidgetConfigManager;
import com.vindsiden.windwidget.model.BDayWidgetModel;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

/**
 * Receives requests from App Widgets
 */
public class VindsidenActivity extends Activity {
	
	private static final String tag = AppWidgetProvider.class.getName(); // getSimpleName());	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		String customMessage = intent.getStringExtra("MESSAGE");
		
		// Øyvind: added, should be useful if we get to introduce several config objects
		// TODO: change config access code here to access config object with ID matching
		// change: int appWidgetId set as an instance variable to be reachable from the listener(possibly messy idea)?  
		final int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

		//Øyvind: test stateful widgets, see if we can recreate the widget from old state:
	   BDayWidgetModel bwm = BDayWidgetModel.retrieveModel(this, appWidgetId);
     if (bwm == null)
     {
        Log.d(tag,"No widget model found for:" + appWidgetId);
        return; // NB! RETURN STATEMENT TODO CHECK
     }
     
     Button b = (Button ) findViewById(R.id.activityStationIdButton);
     b.setText(bwm.getName());
     
     b.setOnClickListener(new OnClickListener() {    	 			
			@Override
			public void onClick(View v) {
				Button b2 = ((Button) v);
				b2.setText((""+"");
			}
		});

		
		String message = customMessage == null ? "WindWidget oppsett" : customMessage;
		TextView tv = (TextView) findViewById(R.id.windwidget_view);
		tv.setText(message);

		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		int defaultChoice = 1;
		int oldFreq = VindsidenAppWidgetService.config.getFrequenceIntervalInMinutes();
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
		
		Spinner stationIdSpinner = (Spinner) findViewById(R.id.spinner2);			
		// TODO some more robustness here would be nice I suppose (?)
		stationIdSpinner.setSelection(VindsidenAppWidgetService.config.getStationID());
		stationIdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				// TODO Øyvind: Noted som stackoverflow people checked for position > (that is, not >= ) 0 ... 
				// not certain if the Spinner class might be bug prone
				VindsidenAppWidgetService.config.setStationID(position);
			};

			public void onNothingSelected(android.widget.AdapterView<?> arg0) {
				// TODO (?)
			};
		});


		TimePicker timePick2 = (TimePicker) findViewById(R.id.timePicker2);
		initTimepicker(timePick2, VindsidenAppWidgetService.config.getStartTime());

		TimePicker timePick3 = (TimePicker) findViewById(R.id.timePicker3);
		initTimepicker(timePick3, VindsidenAppWidgetService.config.getEndTime());

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
				// TODO Øyvind: Noted som stackoverflow people checked for position > (that is, not >= ) 0 ... not certain if the
				// spinner might be bug prone
				if (position >= 0) {
					String choiceString = (String) parent.getItemAtPosition(position);
					int freq = Integer.valueOf(choiceString);
					if (freq < 1) {
						freq = 5;
					}
					; // paranoia robustness
					VindsidenAppWidgetService.config.setFrequenceIntervalInMinutes(freq);
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
				VindsidenAppWidgetService.config.setStartTime(t);
			}
		});
		timePick3.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Time t = new Time();
				t.hour = hourOfDay;
				t.minute = minute;
				VindsidenAppWidgetService.config.setEndTime(t);
			}
		});

	}

	private void initTimepicker(TimePicker tp, Time t) {
		tp.setIs24HourView(true);
		tp.setCurrentHour(t.hour);
		tp.setCurrentMinute(t.minute);
	}

}