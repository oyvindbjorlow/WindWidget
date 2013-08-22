package com.vindsiden.windwidget.model;

import java.util.Map;

public interface IWidgetModelSaveContract {

	// Given a key and value
	// set the equivalent member of the object to that value
	public void setValueForPref(String key, String value);

	// In a derived class tell
	// what should be the name of the
	// shared preferences file
	// Typically points to the class name of the
	// widget provider
	public String getPrefname();

	// As a derived class return
	// the data members of the object to be saved
	// in a map. You can choose to save only a subset
	// of your members. Some members or attributes
	// could be hardcoded, or transient and need not be saved.
	public Map<String, String> getPrefsToSave();

	// You will implement to initialize
	// your invariants or transients
	public void init();
}
