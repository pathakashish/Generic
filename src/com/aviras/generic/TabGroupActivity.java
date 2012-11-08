package com.aviras.generic;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

/**
 * The purpose of this Activity is to manage the activities in a tab. Note:
 * Child Activities can handle Key Presses before they are seen here.
 * 
 * @author Samuel Wejeus
 * @author (Some code based on ideas from: )
 */

@SuppressWarnings("deprecation")
public class TabGroupActivity extends ActivityGroup {

	protected ArrayList<String> mIdList;
	public static final String GOT_RETURN_VALUE = "GOT_RETURN_VALUE";
	private static final String TAG = "TabGroupActivity";
	public static final String EXTRA_ALREADY_STARTED = "extra_already_started";
	public static final String EXTRA_ID = "extra_id";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mIdList == null)
			mIdList = new ArrayList<String>();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "in TabGroupActivity.onNewIntent()");
		super.onNewIntent(intent);
		TabGroupActivity ac = (TabGroupActivity) getLocalActivityManager()
				.getCurrentActivity();
		if (ac != null)
			ac.onNewIntent(intent);
	}

	public void finishAll() {
		int i = 0;
		while (mIdList.size() > 1) {
			try {
				i++;
				Activity activity = getLocalActivityManager()
						.getCurrentActivity();
				// TabGroupActivity ac = (TabGroupActivity) activity;
				activity.finish();
				Log.v(TAG, "Finish Child Activity: " + i);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * This is called when a child activity of this one calls its finish method.
	 * This implementation calls {@link LocalActivityManager#destroyActivity} on
	 * the child activity and starts the previous activity. If the last child
	 * activity just called finish(),this activity (the parent), calls finish to
	 * finish the entire group.
	 */
	@Override
	public void finishFromChild(Activity child) {

		Bundle oldBundle = child.getIntent().getExtras();

		LocalActivityManager manager = getLocalActivityManager();

		int index = mIdList.size() - 1;

		if (index < 1) {
			// UIUtils.showYesNoAlert(this, "",
			// getString(R.string.are_you_sure_to_exit),
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// if (DialogInterface.BUTTON_POSITIVE == which) {
			finish();
			// }
			// }
			// });
			return;
		}

		if ((oldBundle != null) && (oldBundle.containsKey(GOT_RETURN_VALUE))) {
			if (oldBundle.getBoolean(GOT_RETURN_VALUE)) {

				String return_value = oldBundle
						.getString("MODIFY_SPACE_RETURN_VALUE");
				if (return_value.equals("DELETE")) {
					manager.destroyActivity(mIdList.get(index), true);
					mIdList.remove(index);
					index--;
				}

			}
			Log.v(TAG, "finishFromChild :" + mIdList.size());
		}

		manager.destroyActivity(mIdList.get(index), true);
		mIdList.remove(index);
		index--;
		String lastId = mIdList.get(index);
		if (manager.getActivity(lastId) == null) {
			finish();
		} else {
			Intent lastIntent = manager.getActivity(lastId).getIntent();
			Window newWindow = manager.startActivity(lastId, lastIntent);
			this.setContentView(newWindow.getDecorView());
		}
	}

	/**
	 * Starts an Activity as a child Activity to this.
	 * 
	 * @param Id
	 *            Unique identifier of the activity to be started.
	 * @param intent
	 *            The Intent describing the activity to be started.
	 * @throws com.facebook.android.content.ActivityNotFoundException.
	 */
	public void startChildActivity(String Id, Intent intent) {
		intent.putExtra(EXTRA_ID, Id);
		LocalActivityManager lm = getLocalActivityManager();
		Window window = lm.startActivity(Id,
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		if (window != null) {
			mIdList.add(Id);
			this.setContentView(window.getDecorView());
		}
		intent.putExtra(EXTRA_ALREADY_STARTED, true);
		Log.v(TAG, "Start Child No:" + mIdList.size());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		TabGroupActivity ac = (TabGroupActivity) getLocalActivityManager()
				.getCurrentActivity();
		if (ac != null)
			ac.onActivityResult(requestCode, resultCode, data);
	}

	public void setNewIntent(Intent intent) {
	}

	/**
	 * The primary purpose is to prevent systems before
	 * com.facebook.android.os.Build.VERSION_CODES.ECLAIR from calling their
	 * default KeyEvent.KEYCODE_BACK during onKeyDown.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Overrides the default implementation for KeyEvent.KEYCODE_BACK so that
	 * all systems call onBackPressed().
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * If a Child Activity handles KeyEvent.KEYCODE_BACK. Simply override and
	 * add this method.
	 */
	// @Override
	public void onBackPressed() {
		Log.v(TAG, "Back from Tab group");
		int length = mIdList.size();
		if (1 <= length) {
			Activity current = getLocalActivityManager().getActivity(
					mIdList.get(length - 1));
			if (current != null) {
				if (current instanceof IBackOverriders)
					((IBackOverriders) current).onBackKeyPressed();
				else
					current.finish();
			} else {
				finish();
			}
		} else
			finish();
	}

	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	protected void onDestroy() {
		// InternalActivityStack.removeActivity(this);
		super.onDestroy();
		Window w = getWindow();
		if (w != null) {
			// View v = findViewById(R.id.root_relativelayout);
			View v = w.getDecorView();
			if (v != null) {
				unbindDrawables(v);
			}
		}
	}

	private void unbindDrawables(View v) {
		try {
			if (v.getBackground() != null) {
				v.getBackground().setCallback(null);
			}
			if (v instanceof android.view.ViewGroup) {
				for (int i = 0; i < ((android.view.ViewGroup) v)
						.getChildCount(); i++) {
					unbindDrawables(((android.view.ViewGroup) v).getChildAt(i));
				}
				((android.view.ViewGroup) v).removeAllViews();
			}
		} catch (Exception e) {
		}
	}

	public final TabGroupActivity getActivityContext() {
		return getParent() == null ? this : (TabGroupActivity) getParent();
	}

	public void finishActivity(String id) {
		LocalActivityManager lm = getLocalActivityManager();
		finishFromChild(lm.getActivity(id));
		Log.v(TAG, "mIdList: " + mIdList);
	}
}
