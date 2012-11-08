package com.aviras.generic;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public class BaseLoaderCallbacks<D> implements LoaderCallbacks<D> {

	@Override
	public Loader<D> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<D> arg0, D arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoaderReset(Loader<D> arg0) {
		// TODO Auto-generated method stub

	}

	public void useLoader(FragmentActivity a, int id, Bundle args,
			boolean checkInternet) {
		Loader<D> loader = a.getSupportLoaderManager().initLoader(id, args,
				this);
		loader.forceLoad();
	}
}
