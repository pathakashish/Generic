package com.aviras.generic;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public abstract class BaseDialogFragment extends DialogFragment {

	protected static final String WORKER_FRAGMENT_TAG = "WORKER_FRAGMENT_TAG";
	protected static final String EXTRA_STARTED_TASK = "EXTRA_STARTED_TASK";
	protected static final String EXTRA_SCROLL_POS = "EXTRA_SCROLL_POS";

	public BaseDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public abstract void setDialogWidthAndHeight();

	@Override
	public void onStart() {
		super.onStart();
		setDialogWidthAndHeight();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		Log.v(getTag(), getTag() + ".onDismiss(DialogInterface dialog)");
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		Log.v(getTag(), getTag() + ".onCancel(DialogInterface dialog)");
	}

	// @Override
	// public void dismiss() {
	// Dialog d = getDialog();
	// if (d == null) {
	// Log.e(BaseAsyncTask.class.getSimpleName(),
	// "This is not expected at all.");
	// } else {
	// d.dismiss();
	// }
	// }

	// @Override
	// public void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// outState.putBundle("hack", outState);
	// }

	// @Override
	// public void show(FragmentManager manager, String tag) {
	// // DialogFragment.show() will take care of adding the fragment
	// // in a transaction. We also want to remove any currently showing
	// // dialog, so make our own transaction and take care of that here.
	// FragmentTransaction ft = manager.beginTransaction();
	// Fragment prev = manager.findFragmentByTag(tag);
	// if (prev != null) {
	// ft.remove(prev);
	// }
	// ft.addToBackStack(null);
	// super.show(ft, tag);
	// }
}
