package me.prisonranksx.bukkitutils;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

public class MCProgressBar {

	private Map<Integer, String> progressBarValues;
	private int charactersAmount;

	public MCProgressBar(int charactersAmount, String character, String completionPrefix, String leftPrefix) {
		this.charactersAmount = charactersAmount;
		String defaultProgressBar = Strings.repeat(character, charactersAmount);
		progressBarValues = new HashMap<>();
		for (int i = 1; i < charactersAmount + 1; i++) {
			String leftProgressBar = defaultProgressBar.substring(i);
			String completedProgressBar = defaultProgressBar.substring(charactersAmount - i);
			String finalProgressBar = completionPrefix + completedProgressBar + leftPrefix + leftProgressBar;
			progressBarValues.put(i, finalProgressBar);
		}
	}

	public String getByValue(int value) {
		return progressBarValues.get(value);
	}

	public String getByPercentage(int percent) {
		float floatPercent = percent / 100.0f;
		return progressBarValues.get((int) (floatPercent * charactersAmount));
	}

	public String getByPercentage(double percent) {
		float floatPercent = (float) percent / 100.0f;
		return progressBarValues.get((int) (floatPercent * charactersAmount));
	}

}
