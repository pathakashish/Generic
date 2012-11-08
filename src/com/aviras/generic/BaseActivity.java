package com.aviras.generic;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class BaseActivity extends FragmentActivity implements Alerter,
		OnAlertEventsListener {

	private static final String TAG = BaseActivity.class.getSimpleName();
	protected MyApplication app;

	public BaseActivity() {
		super();
		Log.v(TAG, "BaseActivity()");
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		app = MyApplication.getInstance();
		InternalActivityStack.pushActivity(this);
	}

	protected void onDestroy() {
		super.onDestroy();
		InternalActivityStack.removeActivity(this);
	}

	@Override
	public void showAlert(String sourceFragmentTag, int action, String message) {
		AlertDialogUtils.showAlert(this, sourceFragmentTag, action, message);
	}

	@Override
	public void showAlert(String sourceFragmentTag, int action, int message) {
		AlertDialogUtils.showAlert(this, sourceFragmentTag, action, message);
	}

	@Override
	public void showAlert(String sourceFragmentTag, int action, String title,
			String message) {
		AlertDialogUtils.showAlert(this, sourceFragmentTag, action, title,
				message);
	}

	@Override
	public void showAlert(String sourceFragmentTag, int action, int title,
			int message) {
		AlertDialogUtils.showAlert(this, sourceFragmentTag, action, title,
				message);
	}

	@Override
	public void showAlert(int action, String message) {
		AlertDialogUtils.showAlert(this, "", action, message);
	}

	@Override
	public void showAlert(int action, int message) {
		AlertDialogUtils.showAlert(this, "", action, message);
	}

	@Override
	public void showAlert(int action, String title, String message) {
		AlertDialogUtils.showAlert(this, "", action, title, message);
	}

	@Override
	public void showAlert(int action, int title, int message) {
		AlertDialogUtils.showAlert(this, "", action, title, message);
	}

	@Override
	public void showYesNoAlert(String sourceFragmentTag, int action,
			String title, String message) {
		AlertDialogUtils.showThreeButtonAlert(this, sourceFragmentTag, action,
				title, message, this.getString(android.R.string.yes), null,
				this.getString(android.R.string.no), this);
	}

	@Override
	public void showYesNoAlert(String sourceFragmentTag, int action, int title,
			int message) {
		AlertDialogUtils.showThreeButtonAlert(this, sourceFragmentTag, action,
				this.getString(title), this.getString(message),
				this.getString(android.R.string.yes), null,
				this.getString(android.R.string.no), this);
	}

	@Override
	public void showThreeButtonAlert(String sourceFragmentTag, int action,
			int title, int message, int positiveText, int neutralText,
			int negativeText) {
		AlertDialogUtils.showThreeButtonAlert(
				this,
				sourceFragmentTag,
				action,
				title == 0 ? null : this.getString(title),
				this.getString(message),
				positiveText == 0 ? this.getString(android.R.string.yes) : this
						.getString(positiveText),
				neutralText == 0 ? null : this.getString(neutralText),
				negativeText == 0 ? this.getString(android.R.string.no) : this
						.getString(negativeText), this);
	}

	@Override
	public void showThreeButtonAlert(String sourceFragmentTag, int action,
			String title, String message, String positiveText,
			String neutralText, String negativeText) {
		AlertDialogUtils.showThreeButtonAlert(this, sourceFragmentTag, action,
				title, message, positiveText, neutralText, negativeText, this);
	}

	@Override
	public void onDismiss(String sourceFragmentTag, int action,
			DialogInterface dialog) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag(sourceFragmentTag);
		if (f != null) {
			try {
				OnAlertEventsListener listener = (OnAlertEventsListener) f;
				listener.onDismiss(sourceFragmentTag, action, dialog);
			} catch (ClassCastException e) {
				throw new RuntimeException(
						"Must implement OnAlertEventsListener inteface.");
			}
		}
	}

	@Override
	public void onCancel(String sourceFragmentTag, int action,
			DialogInterface dialog) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag(sourceFragmentTag);
		if (f != null) {
			try {
				OnAlertEventsListener listener = (OnAlertEventsListener) f;
				listener.onCancel(sourceFragmentTag, action, dialog);
			} catch (ClassCastException e) {
				throw new RuntimeException(
						"Must implement OnAlertEventsListener inteface.");
			}
		}
	}

	@Override
	public void onClick(String sourceFragmentTag, int action,
			DialogInterface dialog, int buttonId) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag(sourceFragmentTag);
		if (f != null) {
			try {
				OnAlertEventsListener listener = (OnAlertEventsListener) f;
				listener.onClick(sourceFragmentTag, action, dialog, buttonId);
			} catch (ClassCastException e) {
				throw new RuntimeException(
						"Must implement OnAlertEventsListener inteface.");
			}
		}
	}
}
