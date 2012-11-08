package com.aviras.generic;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class BaseAsyncTaskLoader extends AsyncTaskLoader<Integer> {

	public static final int SUCCESS = 0;
	public static final int FAILURE_INTERNER_NOT_AVAILABLE = 1;
	public static final int FAILURE_NETWORK_IO = 2;
	public static final int FAILURE_PARSING = 3;
	public static final int FAILURE_UNKNOWN = 4;

	protected String mErrorString = null;
	protected Exception mException = null;

	public BaseAsyncTaskLoader(Context context) {
		super(context);
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
}
