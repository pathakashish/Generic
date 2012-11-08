package com.aviras.generic;

import java.util.HashMap;

import android.app.Application;
import android.graphics.Typeface;

public class MyApplication extends Application {

	private static MyApplication singleton;
	private HashMap<String, Typeface> fonts = new HashMap<String, Typeface>();

	public static MyApplication getInstance() {
		return singleton;
	}

	public void onCreate() {
		super.onCreate();
		singleton = this;
		DatabaseHelper.init(getApplicationContext());
		if (!Log.isInDebugMode()) {
			Log.setUncaughtExceptionHandler(getApplicationContext());
		}
	}

	public void addCustomFont(String name, Typeface f) {
		fonts.put(name, f);
	}

	public Typeface getCustomFont(String ttfName) {
		Typeface f = fonts.get(ttfName);
		if (f == null) {
			f = Typeface.createFromAsset(getAssets(), ttfName);
			if (f == null) {
				throw new RuntimeException("Font not found!");
			}
			fonts.put(ttfName, f);
		}
		return f;
	}
}
