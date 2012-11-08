package com.aviras.generic;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlertDialogUtils extends BaseDialogFragment implements
		android.view.View.OnClickListener {

	private static final String TAG = AlertDialogUtils.class.getSimpleName();

	private static final String EXTRA_TITLE = "EXTRA_TITLE";
	private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
	private static final String EXTRA_POSITIVE_BUTTON_TEXT = "EXTRA_POSITIVE_BUTTON_TEXT";
	private static final String EXTRA_NEUTRAL_BUTTON_TEXT = "EXTRA_NEUTRAL_BUTTON_TEXT";
	private static final String EXTRA_NEGATIVE_BUTTON_TEXT = "EXTRA_NEGATIVE_BUTTON_TEXT";
	private static final String EXTRA_CANCELABLE = "EXTRA_CANCELABLE";
	private static final String EXTRA_ALERT_LISTEENER_SET = "EXTRA_SET_ON_CLICK_LISTENER";

	private static final String EXTRA_ACTION = "EXTRA_ACTION";
	private static final String EXTRA_SOURCE_FRAGMENT_TAG = "EXTRA_SOURCE_FRAGMENT_TAG";

	private Button positiveButton;
	private Button neutralButton;
	private Button negativeButton;

	private TextView titleTextView;
	private TextView messageTextView;

	private View v;

	private boolean alertListenerSet = false;

	private String title;
	private String message;
	private String positiveText;
	private String neutralText;
	private String negativeText;

	private int action;
	private String sourceFragmentTag;

	private OnAlertEventsListener listener;

	public static AlertDialogUtils newInstance() {
		return new AlertDialogUtils();
	}

	public AlertDialogUtils() {

	}

	@Override
	public void onStart() {
		super.onStart();
		setTitleImpl(title);
		setMessageImpl(message);
		if (positiveText != null || negativeText != null) {
			setButtonsImpl(positiveText, neutralText, negativeText);
		} else {
			if (neutralText == null) {
				neutralButton.setVisibility(View.GONE);
			} else {
				neutralButton.setVisibility(View.VISIBLE);
				neutralButton.setText(neutralText);
			}
		}

		Activity activity = getActivity();
		if (alertListenerSet) {
			try {
				listener = (OnAlertEventsListener) activity;
			} catch (ClassCastException e) {
				throw new RuntimeException(
						"Must implement AlertEventsListener interface.");
			}
		}
	}

	private void setButtonsImpl(String positiveText, String neutralText,
			String negativeText) {
		alertListenerSet = true;
		if (positiveText == null) {
			positiveButton.setVisibility(View.GONE);
		} else {
			positiveButton.setVisibility(View.VISIBLE);
			positiveButton.setText(positiveText);
		}

		if (neutralText == null) {
			neutralButton.setVisibility(View.GONE);
		} else {
			neutralButton.setVisibility(View.VISIBLE);
			neutralButton.setText(neutralText);
		}

		if (negativeText == null) {
			negativeButton.setVisibility(View.GONE);
		} else {
			negativeButton.setVisibility(View.VISIBLE);
			negativeButton.setText(negativeText);
		}
	}

	private void setMessageImpl(String message) {
		if (message != null) {
			if (message.contains("<") && message.contains(">")) {
				final SpannableString s = new SpannableString(
						Html.fromHtml(message));
				Linkify.addLinks(s, Linkify.WEB_URLS);
				messageTextView.setMovementMethod(LinkMovementMethod
						.getInstance());
				messageTextView.setText(s);
			} else {
				messageTextView.setText(message);
			}
		} else {
			messageTextView.setText("");
		}
	}

	private void setTitleImpl(String title) {
		if (title == null) {
			titleTextView.setText(null);
			titleTextView.setVisibility(View.GONE);
		} else {
			titleTextView.setVisibility(View.VISIBLE);
			if (title.toString().contains("<")
					&& title.toString().contains(">")) {
				final SpannableString s = new SpannableString(
						Html.fromHtml(title.toString()));
				Linkify.addLinks(s, Linkify.WEB_URLS);
				titleTextView.setMovementMethod(LinkMovementMethod
						.getInstance());
				titleTextView.setText(s);
			} else {
				titleTextView.setText(title);
			}
		}
	}

	private void setNeutralButtonText(String text) {
		neutralText = text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.alert_dialog, container, false);
		inflateViewsFromXml(v);
		setListenersOnViews();
		setValuesOnViews();
		restoreFromSavedInstanceState(savedInstanceState);
		return v;
	}

	private void restoreFromSavedInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			setCancelable(savedInstanceState.getBoolean(EXTRA_CANCELABLE));
			alertListenerSet = savedInstanceState
					.getBoolean(EXTRA_ALERT_LISTEENER_SET);
			sourceFragmentTag = savedInstanceState
					.getString(EXTRA_SOURCE_FRAGMENT_TAG);
			setFragmentSource(sourceFragmentTag);
			action = savedInstanceState.getInt(EXTRA_ACTION);
			setAction(action);
			title = savedInstanceState.getString(EXTRA_TITLE);
			setTitle(title);
			message = savedInstanceState.getString(EXTRA_MESSAGE);
			setMessage(message);
			positiveText = savedInstanceState
					.getString(EXTRA_POSITIVE_BUTTON_TEXT);
			neutralText = savedInstanceState
					.getString(EXTRA_NEUTRAL_BUTTON_TEXT);
			negativeText = savedInstanceState
					.getString(EXTRA_NEGATIVE_BUTTON_TEXT);
			if (positiveText != null || negativeText != null) {
				setButtons(positiveText, neutralText, negativeText);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(EXTRA_CANCELABLE, isCancelable());
		outState.putBoolean(EXTRA_ALERT_LISTEENER_SET, alertListenerSet);
		outState.putString(EXTRA_SOURCE_FRAGMENT_TAG, sourceFragmentTag);
		outState.putInt(EXTRA_ACTION, action);
		if (titleTextView.getVisibility() == View.VISIBLE) {
			outState.putString(EXTRA_TITLE, titleTextView.getText().toString());
		}
		outState.putString(EXTRA_MESSAGE, messageTextView.getText().toString());
		if (positiveButton.getVisibility() == View.VISIBLE) {
			outState.putString(EXTRA_POSITIVE_BUTTON_TEXT, positiveButton
					.getText().toString());
		}
		if (neutralButton.getVisibility() == View.VISIBLE) {
			outState.putString(EXTRA_NEUTRAL_BUTTON_TEXT, neutralButton
					.getText().toString());
		}
		if (negativeButton.getVisibility() == View.VISIBLE) {
			outState.putString(EXTRA_NEGATIVE_BUTTON_TEXT, negativeButton
					.getText().toString());
		}
	}

	@Override
	public void setDialogWidthAndHeight() {
		if (v != null) {
			v.post(new Runnable() {

				@Override
				public void run() {
					Dialog d = getDialog();
					if (d != null) {
						WindowManager.LayoutParams params = d.getWindow()
								.getAttributes();
						params.width = Math.max(v.getWidth(),
								v.getMeasuredWidth());
						params.height = Math.max(v.getHeight(),
								v.getMeasuredHeight());
						d.getWindow().setAttributes(
								(WindowManager.LayoutParams) params);
					}
				}
			});
		}
	}

	private void setValuesOnViews() {
		titleTextView.setVisibility(View.GONE);

		positiveButton.setVisibility(View.GONE);
		negativeButton.setVisibility(View.GONE);

		neutralButton.setVisibility(View.VISIBLE);
		neutralButton.setText(android.R.string.ok);
		positiveButton.setText(null);
		negativeButton.setText(null);
	}

	private void setListenersOnViews() {
		positiveButton.setOnClickListener(this);
		neutralButton.setOnClickListener(this);
		negativeButton.setOnClickListener(this);
	}

	private void inflateViewsFromXml(View contentView) {
		positiveButton = (Button) contentView
				.findViewById(R.id.positive_button);
		neutralButton = (Button) contentView.findViewById(R.id.neutral_button);
		negativeButton = (Button) contentView
				.findViewById(R.id.negative_button);
		titleTextView = (TextView) contentView
				.findViewById(R.id.title_textview);
		messageTextView = (TextView) contentView
				.findViewById(R.id.message_textview);
	}

	public void setOnAlertEventsListener(OnAlertEventsListener listener) {
		alertListenerSet = true;
		this.listener = listener;
	}

	private void setAction(int action) {
		this.action = action;
	}

	private void setFragmentSource(String sourceFragmentTag) {
		this.sourceFragmentTag = sourceFragmentTag;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setButtons(String positiveText, String neutralText,
			String negativeText) {
		this.positiveText = positiveText;
		this.neutralText = neutralText;
		this.negativeText = negativeText;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (listener != null) {
			listener.onCancel(sourceFragmentTag, action, dialog);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (listener != null) {
			listener.onDismiss(sourceFragmentTag, action, dialog);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		int buttonId = DialogInterface.BUTTON_NEUTRAL;
		switch (id) {
		case R.id.positive_button:
			buttonId = DialogInterface.BUTTON_POSITIVE;
			break;
		case R.id.neutral_button:
			buttonId = DialogInterface.BUTTON_NEUTRAL;
			break;
		case R.id.negative_button:
			buttonId = DialogInterface.BUTTON_NEGATIVE;
			break;
		}

		if (listener == null) {
			if (negativeText == null && positiveText == null) {
				dismiss();
			}
		} else {
			listener.onClick(sourceFragmentTag, action, getDialog(), buttonId);
		}
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String message) {
		showAlert(activityContext, sourceFragmentTag, action, null, message,
				null);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int message) {
		showAlert(activityContext, sourceFragmentTag, action, null,
				activityContext.getString(message), null);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String message,
			OnAlertEventsListener listener) {
		showAlert(activityContext, sourceFragmentTag, action, null, message,
				listener);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int message,
			OnAlertEventsListener listener) {
		showAlert(activityContext, sourceFragmentTag, action, null,
				activityContext.getString(message), listener);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String title, String message) {
		showAlert(activityContext, sourceFragmentTag, action, title, message,
				null);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int title, int message) {
		showAlert(activityContext, sourceFragmentTag, action,
				activityContext.getString(title),
				activityContext.getString(message), null);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int title, int message,
			OnAlertEventsListener listener) {
		showAlert(activityContext, sourceFragmentTag, action,
				activityContext.getString(title),
				activityContext.getString(message), listener);
	}

	public static void showAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String title, String message,
			OnAlertEventsListener listener) {
		AlertDialogUtils alert = AlertDialogUtils.newInstance();
		alert.setFragmentSource(sourceFragmentTag);
		alert.setAction(action);
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setNeutralButtonText(activityContext
				.getString(android.R.string.ok));
		alert.setCancelable(true);
		// listner will be null if user wants to just dismiss the dialog when
		// clicked on neutralButton
		if (listener != null) {
			alert.setOnAlertEventsListener(listener);
		}
		alert.show(activityContext.getSupportFragmentManager(),
				AlertDialogUtils.TAG);
	}

	public static void showYesNoAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String title, String message,
			OnAlertEventsListener listener) {
		showThreeButtonAlert(activityContext, sourceFragmentTag, action, title,
				message, activityContext.getString(android.R.string.yes), null,
				activityContext.getString(android.R.string.no), listener);
	}

	public static void showYesNoAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int title, int message,
			OnAlertEventsListener listener) {
		showThreeButtonAlert(activityContext, sourceFragmentTag, action,
				activityContext.getString(title),
				activityContext.getString(message),
				activityContext.getString(android.R.string.yes), null,
				activityContext.getString(android.R.string.no), listener);
	}

	public static void showThreeButtonAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, int title, int message,
			int positiveText, int neutralText, int negativeText,
			OnAlertEventsListener listener) {
		showThreeButtonAlert(
				activityContext,
				sourceFragmentTag,
				action,
				activityContext.getString(title),
				activityContext.getString(message),
				positiveText == 0 ? activityContext
						.getString(android.R.string.yes) : activityContext
						.getString(positiveText),
				neutralText == 0 ? null : activityContext
						.getString(neutralText),
				negativeText == 0 ? activityContext
						.getString(android.R.string.no) : activityContext
						.getString(negativeText), listener);
	}

	public static void showThreeButtonAlert(FragmentActivity activityContext,
			String sourceFragmentTag, int action, String title, String message,
			String positiveText, String neutralText, String negativeText,
			OnAlertEventsListener listener) {
		AlertDialogUtils alert = AlertDialogUtils.newInstance();
		alert.setFragmentSource(sourceFragmentTag);
		alert.setAction(action);
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setCancelable(false);
		alert.setOnAlertEventsListener(listener);
		alert.setButtons(positiveText, neutralText, negativeText);
		alert.show(activityContext.getSupportFragmentManager(),
				AlertDialogUtils.TAG);
	}
}
