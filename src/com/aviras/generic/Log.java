package com.aviras.generic;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class Log {

	private static final String PREFS_CRASH = null;
	private static final String EXTRA_CRASH_LOG = null;

	private static boolean V = true;
	private static boolean E = true;
	private static boolean I = true;
	private static boolean D = true;
	private static boolean W = true;

	static {
		if (BuildConfig.DEBUG) {
		}
	}

	public static boolean isInDebugMode() {
		return BuildConfig.DEBUG;
	}

	public static void logVerbose(boolean v) {
		V = v;
	}

	public static void logError(boolean e) {
		E = e;
	}

	public static void logInfo(boolean i) {
		I = i;
	}

	public static void logDebug(boolean d) {
		D = d;
	}

	public static void logWarning(boolean w) {
		W = w;
	}

	public static void v(String tag, String msg) {
		if (BuildConfig.DEBUG && V) {
			// && android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
			android.util.Log.v(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (BuildConfig.DEBUG && I) {
			android.util.Log.i(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (BuildConfig.DEBUG && D) {
			// && android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
			android.util.Log.d(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.w(tag, msg);
		} else if (W) {
			android.util.Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.e(tag, msg);
		} else if (E) {
			android.util.Log.e(tag, msg);
		}
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (BuildConfig.DEBUG && V) {
			// && android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
			android.util.Log.v(tag, msg, tr);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (BuildConfig.DEBUG && I) {
			android.util.Log.i(tag, msg, tr);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (BuildConfig.DEBUG && D) {
			// && android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
			android.util.Log.d(tag, msg, tr);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			android.util.Log.w(tag, msg, tr);
		} else if (W) {
			android.util.Log.w(tag, msg, tr);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			android.util.Log.e(tag, msg, tr);
		} else if (E) {
			android.util.Log.e(tag, msg, tr);
		}
	}

	public static void sendCrashLogIfAvailable(Context context) {
		final Context applicationContext = context.getApplicationContext();
		if (!TextUtils.isEmpty(applicationContext.getSharedPreferences(
				PREFS_CRASH, Context.MODE_PRIVATE).getString(EXTRA_CRASH_LOG,
				""))) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					String log = applicationContext.getSharedPreferences(
							PREFS_CRASH, Context.MODE_PRIVATE).getString(
							EXTRA_CRASH_LOG, "");
					android.util.Log.v(
							applicationContext.getString(R.string.app_name),
							"Sending crashlog...");
					android.util.Log.v(
							applicationContext.getString(R.string.app_name), ""
									+ log);
					long timestamp = new Date().getTime();
					String key = IoUtils.getMd5For(timestamp
							+ applicationContext.getString(R.string.app_name)
							+ "Sph!nx");
					String to = "anand@sphinx-solution.com";
					String versionName = "";
					try {
						PackageInfo packageInfo = applicationContext
								.getPackageManager().getPackageInfo(
										applicationContext.getPackageName(), 0);
						versionName = packageInfo.versionName;
					} catch (NameNotFoundException e1) {
						e1.printStackTrace();
					}
					String urlstring = "http://www.sphinx-solution.com/bugnotifier.php";
					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("log", log));
						nameValuePairs
								.add(new BasicNameValuePair("app_name",
										applicationContext
												.getString(R.string.app_name)));
						nameValuePairs.add(new BasicNameValuePair(
								"app_version", versionName));
						nameValuePairs.add(new BasicNameValuePair("timestamp",
								timestamp + ""));
						nameValuePairs.add(new BasicNameValuePair("key", key));
						nameValuePairs.add(new BasicNameValuePair("to", to));
						IoUtils.post(applicationContext, urlstring,
								nameValuePairs);
						android.util.Log.v(
								applicationContext.getString(R.string.app_name),
								"Log sent!");
						applicationContext
								.getSharedPreferences(PREFS_CRASH,
										Context.MODE_PRIVATE).edit()
								.putString(EXTRA_CRASH_LOG, "").commit();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public static void setUncaughtExceptionHandler(Context context) {
		final Context applicationContext = context.getApplicationContext();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@SuppressLint("NewApi")
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				final DisplayMetrics dm = applicationContext.getResources()
						.getDisplayMetrics();
				StackTraceElement[] arr = e.getStackTrace();
				String report = e.toString() + "<br />";
				report += "-------------------------------<br />";
				SimpleDateFormat df_yyyy_MM_dd_HH_mm = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm", Locale.ENGLISH);
				report += "Time: "
						+ df_yyyy_MM_dd_HH_mm.format(Calendar.getInstance()
								.getTime()) + "<br />";
				report += "-------------------------------<br />";
				report += "--------- Device ---------<br />";
				report += "Brand: " + Build.BRAND + "<br />";
				report += "Device: " + Build.DEVICE + "<br />";
				report += "Id: " + Build.ID + "<br />";
				report += "Product: " + Build.PRODUCT + "<br />";
				report += "Model: " + Build.MODEL + "<br />";
				report += "Resolution: " + dm.widthPixels + "x"
						+ dm.heightPixels + "<br />";
				report += "Density: " + dm.density + "<br />";
				report += "densityDPI: " + dm.densityDpi + "<br />";
				report += "scaledDensity: " + dm.scaledDensity + "<br />";
				report += "xdpi: " + dm.xdpi + "<br />";
				report += "ydpi: " + dm.ydpi + "<br />";
				report += "Display language: "
						+ Locale.getDefault().getDisplayLanguage() + "<br />";
				report += "ISO3 language: "
						+ Locale.getDefault().getISO3Language() + "<br />";
				report += "Country: " + Locale.getDefault().getCountry()
						+ "<br />";
				report += "ISO2 Country: "
						+ Locale.getDefault().getISO3Country() + "<br />";

				try {
					report += "Memory class: "
							+ (((ActivityManager) applicationContext
									.getSystemService(Context.ACTIVITY_SERVICE))
									.getMemoryClass()) + " MB<br />";
				} catch (Exception e1) {
					report += "Memory class: " + "NA<br />";
				}
				try {
					report += "Large memory class: "
							+ (((ActivityManager) applicationContext
									.getSystemService(Context.ACTIVITY_SERVICE))
									.getLargeMemoryClass()) + " MB<br />";
				} catch (Exception e1) {
					report += "Large memory class: " + "NA<br />";
				}
				report += "Free memory: "
						+ ((Runtime.getRuntime().freeMemory() / 1024) / 1024)
						+ " MB<br />";
				report += "Max memory available to app: "
						+ ((Runtime.getRuntime().maxMemory() / 1024) / 1024)
						+ " MB<br />";

				report += "-------------------------------<br />";
				report += "--------- Firmware ---------<br />";
				report += "SDK: " + Build.VERSION.SDK_INT + "<br />";
				report += "Release: " + Build.VERSION.RELEASE + "<br />";
				report += "Incremental: " + Build.VERSION.INCREMENTAL
						+ "<br />";
				report += "-------------------------------<br />";
				report += "--------- " + applicationContext.getPackageName()
						+ " ---------<br />";
				try {
					PackageInfo packageInfo = applicationContext
							.getPackageManager().getPackageInfo(
									applicationContext.getPackageName(), 0);
					report += "Version Code: "
							+ String.valueOf(packageInfo.versionCode)
							+ "<br />";
					report += "Version name: " + packageInfo.versionName
							+ "<br />";
				} catch (NameNotFoundException e1) {
					e1.printStackTrace();
					report += "Cannot load application Version!";
				}
				report += "-------------------------------<br />";
				report += "--------- Stack trace ---------<br />";
				for (int i = 0; i < arr.length; i++) {
					report += "&#09;" + arr[i].toString() + "<br />";
				}
				report += "-------------------------------<br />";
				// If the exception was thrown in a background thread inside
				// AsyncTask, then the actual exception can be found with
				// getCause
				report += "--------- Cause ---------<br />";
				Throwable cause = e.getCause();
				if (cause != null) {
					report += cause.toString() + "<br />";
					arr = cause.getStackTrace();
					for (int i = 0; i < arr.length; i++) {
						report += "&#09;" + arr[i].toString() + "<br />";

					}
				}
				// final String reportData = report;
				applicationContext
						.getSharedPreferences(PREFS_CRASH, Context.MODE_PRIVATE)
						.edit().putString(EXTRA_CRASH_LOG, report).commit();
				Log.e("Report ::", report);
				InternalActivityStack.finishAllOtherActivities(new Class[] {});
				System.exit(0);
			}
		});
	}
}
