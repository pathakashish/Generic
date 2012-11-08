package com.aviras.generic;

import java.util.Calendar;

public class DateTimeUtils {

	public static final SynchronizedSimpleDateFormat df_yyyy_MM_dd = new SynchronizedSimpleDateFormat(
			"yyyy-MM-dd");
	public static final SynchronizedSimpleDateFormat df_yyyy_MM_dd_HH_mm = new SynchronizedSimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	public static final SynchronizedSimpleDateFormat df_yyyy_MM_dd_HH_mm_ss = new SynchronizedSimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static final SynchronizedSimpleDateFormat df_HH_mm = new SynchronizedSimpleDateFormat(
			"HH:mm");
	public static final SynchronizedSimpleDateFormat df_hh_mm_a = new SynchronizedSimpleDateFormat(
			"hh:mm a");
	public static final SynchronizedSimpleDateFormat df_MM_dd_yyyy = new SynchronizedSimpleDateFormat(
			"MM/dd/yyyy");
	public static final SynchronizedSimpleDateFormat df_EEEE_MMM_dd = new SynchronizedSimpleDateFormat(
			"EEEE, MMM dd");
	public static final SynchronizedSimpleDateFormat df_EEEE_MMM_dd_hh_mm_a = new SynchronizedSimpleDateFormat(
			"EEEE, MMM dd hh:mm a");
	public static final SynchronizedSimpleDateFormat df_EEEE_MMM_dd_yyyy = new SynchronizedSimpleDateFormat(
			"EEEE - MMM dd, yyyy");
	public static final SynchronizedSimpleDateFormat df_MMM_dd = new SynchronizedSimpleDateFormat(
			"MMM dd");
	public static final SynchronizedSimpleDateFormat df_MMM_yyyy = new SynchronizedSimpleDateFormat(
			"MMMM yyyy");
	public static final SynchronizedSimpleDateFormat df_dd_MMM_yyyy = new SynchronizedSimpleDateFormat(
			"dd MMM yyyy");
	public static final SynchronizedSimpleDateFormat df_dd_MMMM = new SynchronizedSimpleDateFormat(
			"dd MMMM");

	public static void getStartAndEndDatesForWeek(Calendar currentDate,
			Calendar startDate, Calendar endDate) {
		Calendar cal = (Calendar) currentDate.clone();
		int i = 0;
		while (i < 8 && Calendar.SUNDAY != cal.get(Calendar.DAY_OF_WEEK)) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			i++;
		}
		if (i == 0) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		// Remove below line if week is needed to be started on Monday
		cal.add(Calendar.DAY_OF_MONTH, 1);
		startDate.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		startDate.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		startDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));

		cal.add(Calendar.DAY_OF_MONTH, 6);

		endDate.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
		endDate.set(Calendar.MONTH, cal.get(Calendar.MONTH));
		endDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));
	}
}
