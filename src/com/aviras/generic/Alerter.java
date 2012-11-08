package com.aviras.generic;

public interface Alerter {

	void showAlert(String sourceFragmentTag, int action, String message);

	void showAlert(String sourceFragmentTag, int action, int message);

	void showAlert(String sourceFragmentTag, int action, String title,
			String message);

	void showAlert(String sourceFragmentTag, int action, int title, int message);

	void showAlert(int action, String message);

	void showAlert(int action, int message);

	void showAlert(int action, String title, String message);

	void showAlert(int action, int title, int message);

	void showYesNoAlert(String sourceFragmentTag, int action, String title,
			String message);

	void showYesNoAlert(String sourceFragmentTag, int action, int title,
			int message);

	void showThreeButtonAlert(String sourceFragmentTag, int action, int title,
			int message, int positiveText, int neutralText, int negativeText);

	void showThreeButtonAlert(String sourceFragmentTag, int action,
			String title, String message, String positiveText,
			String neutralText, String negativeText);
}
