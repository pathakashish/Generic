package com.aviras.generic;

import android.app.TabActivity;
import android.os.Bundle;

@SuppressWarnings("deprecation")
public abstract class TabsBaseActivity extends TabActivity {

	private static final String TAG = "BaseActivity";
	protected MyApplication app;

	public TabsBaseActivity() {
		super();
		Log.v(TAG, "BaseActivity()");
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		InternalActivityStack.pushActivity(this);
		app = MyApplication.getInstance();
	}

	protected void onDestroy() {
		super.onDestroy();
		InternalActivityStack.removeActivity(this);
	}
}
