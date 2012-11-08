package com.aviras.generic;

import java.util.ArrayList;

import android.app.Activity;

public class InternalActivityStack {

	private static ArrayList<Activity> sActivityStack = new ArrayList<Activity>();

	public InternalActivityStack() {
	}

	public static void finishAllOtherActivities(Activity activity) {
		synchronized (sActivityStack) {

			int i = sActivityStack.size() - 1;
			do {
				if (i < 0) {
					break;
				}
				Activity a = sActivityStack.get(i);
				if (a != null && !a.isFinishing() && a != activity) {
					a.finish();
					sActivityStack.remove(a);
				}
				i--;
			} while (true);
		}
	}

	public static void pushActivity(Activity activity) {
		sActivityStack.add(activity);
	}

	public static void removeActivity(Activity activity) {
		sActivityStack.remove(activity);
		if (sActivityStack.size() <= 0) {
			new ImageLoader(activity).clearCache();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void finishAllOtherActivities(Class[] cl) {
		synchronized (sActivityStack) {

			int i = sActivityStack.size() - 1;
			do {
				if (i < 0) {
					break;
				}
				Activity a = sActivityStack.get(i);
				if (a != null && !a.isFinishing()) {
					boolean remove = true;
					for (Class c : cl) {
						if (a.getClass().getName().equals(c.getName())) {
							remove = false;
							break;
						}
					}
					if (remove) {
						a.finish();
						sActivityStack.remove(a);
					}
				}
				i--;
			} while (true);
		}
	}

	public static void finishTopActivityIfExists() {
		synchronized (sActivityStack) {
			int i = sActivityStack.size() - 1;
			if (i >= 0) {
				Activity a = sActivityStack.get(i);
				if (a != null && !a.isFinishing()) {
					a.finish();
					sActivityStack.remove(a);
				}
			}
		}
	}
}
