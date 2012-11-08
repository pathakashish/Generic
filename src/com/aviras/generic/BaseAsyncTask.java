package com.aviras.generic;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public abstract class BaseAsyncTask extends AsyncTask<Void, Object, Integer> {

	public static final int RESULT_UNKNOWN = -1;
	public static final int SUCCESS = 0;
	public static final int FAILURE_INTERNER_NOT_AVAILABLE = -2;
	public static final int FAILURE_NETWORK_IO = -3;
	public static final int FAILURE_PARSING = -4;
	public static final int FAILURE_UNKNOWN = -5;
	public static final int FAILURE_CANCELED = -6;
	protected String mErrorString = null;
	protected Exception mException = null;
	protected WeakReference<Fragment> workerFragment = null;

	@Override
	protected void onPreExecute() {
		if (IoUtils.isOnline(MyApplication.getInstance()
				.getApplicationContext())) {
			super.onPreExecute();
		} else {
			cancel(true);
			internetNotConnected();
		}
	}

	public void onPostExecute(Integer result) {
		if (workerFragment != null && workerFragment.get() != null) {
			AsyncTaskResultCallback callback = (AsyncTaskResultCallback) workerFragment
					.get();
			callback.onPostExecute(this, result);
		}
	}

	public void internetNotConnected() {
		if (workerFragment != null && workerFragment.get() != null) {
			AsyncTaskResultCallback callback = (AsyncTaskResultCallback) workerFragment
					.get();
			callback.internetNotConnected(this);
		}
	}

	@Override
	protected void onCancelled() {
		if (workerFragment != null && workerFragment.get() != null) {
			AsyncTaskResultCallback callback = (AsyncTaskResultCallback) workerFragment
					.get();
			callback.onCancelled(this);
		}
	}

	public final String getErrorString() {
		return mErrorString;
	}

	protected final void setErrorString(String s) {
		mErrorString = s;
	}

	public final Exception getException() {
		return mException;
	}

	protected final void setException(Exception e) {
		this.mException = e;
	}

	public void setFragment(Fragment workerFragment) {
		if (this.workerFragment != null) {
			this.workerFragment.clear();
			this.workerFragment = null;
		}
		if (workerFragment != null) {
			this.workerFragment = new WeakReference<Fragment>(workerFragment);
		}
	}

	public static interface AsyncTaskResultCallback {
		void onPostExecute(BaseAsyncTask task, Integer result);

		void onCancelled(BaseAsyncTask task);

		void internetNotConnected(BaseAsyncTask task);
	}
}
