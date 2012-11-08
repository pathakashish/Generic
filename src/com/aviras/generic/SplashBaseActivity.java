package com.aviras.generic;

import java.util.Calendar;

import android.os.AsyncTask;

public abstract class SplashBaseActivity extends BaseActivity {

	private static final long SPLASH_TIME_IN_MILLIS = 1000;
	private TimeConsumingTask tcTask = null;

	@Override
	protected void onPause() {
		super.onPause();
		try {
			tcTask.cancel(true);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		tcTask = new TimeConsumingTask();
		tcTask.execute();
	}

	private class TimeConsumingTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			boolean waited = false;
			long startTime = Calendar.getInstance().getTimeInMillis();
			doInBackGround();
			long endTime = Calendar.getInstance().getTimeInMillis();
			long waitTime = SPLASH_TIME_IN_MILLIS - (endTime - startTime);
			if (waitTime > 0) {
				do {
					try {
						Thread.sleep(waitTime);
						waited = true;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (!waited);
			}
			return null;
		}

		public void onPostExecute(Void result) {
			startNextActivity();
		}
	}

	/**
	 * Perform any time consuming initialization here.
	 */
	protected abstract void doInBackGround();

	/**
	 * Start the main activity of the app here.
	 */
	protected abstract void startNextActivity();
}