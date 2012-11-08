package com.aviras.generic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SynchronizedSimpleDateFormat {

	private static final long serialVersionUID = 1L;

	private SimpleDateFormat sdf = null;

	private SynchronizedSimpleDateFormat() {

	}

	public SynchronizedSimpleDateFormat(String pattern) {
		this();
		sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
	}

	public synchronized String format(java.util.Date d) {
		String sd = sdf.format(d);
		return sd;
	}

	public synchronized java.util.Date parse(String date) throws ParseException {
		return sdf.parse(date);
	}
}