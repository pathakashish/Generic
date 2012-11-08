package com.aviras.generic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.aviras.generic.BaseAsyncTask.AsyncTaskResultCallback;

public class WorkerFragment extends BaseDialogFragment implements
		AsyncTaskResultCallback {

	public static final String TAG = WorkerFragment.class.getSimpleName();

	public static final int WORKER_TASK = 1;

	// The task we are running.
	private BaseAsyncTask mAsyncTask;
	private int result = BaseAsyncTask.RESULT_UNKNOWN;

	public void setTask(BaseAsyncTask task) {
		mAsyncTask = task;
		// Tell the AsyncTask to call updateProgress() and taskFinished() on
		// this fragment.
		mAsyncTask.setFragment(this);
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle("");
		dialog.setMessage(getString(R.string.please_wait));
		dialog.setIndeterminate(true);
		dialog.setCancelable(isCancelable());
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this instance so it isn't destroyed when MainActivity and
		// MainFragment change configuration.
		setRetainInstance(true);

		// Start the task! You could move this outside this activity if you
		// want.
		if (mAsyncTask != null)
			mAsyncTask.execute();
	}

	// This is to work around what is apparently a bug. If you don't have it
	// here the dialog will be dismissed on rotation, so tell it not to dismiss.
	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		Log.i(TAG, "onCancel(DialogInterface dialog) called.");
		cancelAsyncTask();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG,
				result == BaseAsyncTask.RESULT_UNKNOWN ? "Resuming normally..."
						: "Using result.");
		if (result != BaseAsyncTask.RESULT_UNKNOWN) {
			dismiss();
			if (result == BaseAsyncTask.FAILURE_CANCELED) {
				onCancelled(mAsyncTask);
			} else if (result == BaseAsyncTask.FAILURE_INTERNER_NOT_AVAILABLE) {
				internetNotConnected(mAsyncTask);
			} else {
				onPostExecute(mAsyncTask, result);
			}
		}
	}

	@Override
	public void onPostExecute(BaseAsyncTask task, Integer result) {
		Log.i(TAG, "onPostExecute");
		// If we aren't resumed, setting the task to null will allow us to
		// dimiss ourselves in
		// onResume().
		this.result = result;
		if (isResumed()) {
			Log.i(TAG, "Delivering result.");
			// Make sure we check if it is resumed because we will crash if
			// trying
			// to dismiss the dialog
			// after the user has switched to another app.

			// Tell the fragment that we are done.
			if (getTargetFragment() != null) {
				try {
					((AsyncTaskResultCallback) getTargetFragment())
							.onPostExecute(task, result);
				} catch (ClassCastException e) {
					throw new RuntimeException(
							"Must implement BaseAsyncTask.AsyncTaskResultCallback interface.");
				}
			}
			if (isResumed())
				dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		Log.i(TAG, TAG + ".onDismiss(DialogInterface dialog)");
	}

	@Override
	public void internetNotConnected(BaseAsyncTask task) {
		Log.i(TAG, "internetNotConnected");
		this.result = BaseAsyncTask.FAILURE_INTERNER_NOT_AVAILABLE;
		if (isResumed()) {
			Log.i(TAG, "Internet not available.");
			if (isResumed())
				dismiss();
			if (getTargetFragment() != null) {
				try {
					((AsyncTaskResultCallback) getTargetFragment())
							.internetNotConnected(task);
				} catch (ClassCastException e) {
					throw new RuntimeException(
							"Must implement BaseAsyncTask.AsyncTaskResultCallback interface.");
				}
			}
		}
	}

	@Override
	public void onCancelled(BaseAsyncTask baseAsyncTask) {
		Log.i(TAG, "onCancelled");
		this.result = BaseAsyncTask.FAILURE_CANCELED;
		if (isResumed()) {
			Log.i(TAG, "Result cancelled");
			if (getTargetFragment() != null) {
				try {
					((AsyncTaskResultCallback) getTargetFragment())
							.onCancelled(baseAsyncTask);
				} catch (ClassCastException e) {
					throw new RuntimeException(
							"Must implement BaseAsyncTask.AsyncTaskResultCallback interface.");
				}
			}
			dismiss();
		}
	}

	public void cancelAsyncTask() {
		// If true, the thread is interrupted immediately, which may do bad
		// things.
		// If false, it guarantees a result is never returned (onPostExecute()
		// isn't called)
		// but you have to repeatedly call isCancelled() in your
		// doInBackground()
		// function to check if it should exit. For some tasks that might not be
		// feasible.
		if (mAsyncTask != null)
			mAsyncTask.cancel(false);
	}

	@Override
	public void setDialogWidthAndHeight() {
		// Need not do anything for default ProgressDialog. However, if custom
		// implementation is used, we need to set size for dialog.

	}
}
