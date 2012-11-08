package com.aviras.generic;

import android.content.DialogInterface;

public interface OnAlertEventsListener {

	void onDismiss(String sourceFragmentTag, int action, DialogInterface dialog);

	void onCancel(String sourceFragmentTag, int action, DialogInterface dialog);

	void onClick(String sourceFragmentTag, int action, DialogInterface dialog,
			int buttonId);
}
