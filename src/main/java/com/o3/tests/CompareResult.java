package com.o3.tests;

import java.util.ArrayList;
import java.util.List;

public class CompareResult {
	private List<String> differences;
	private boolean areEqual;

	CompareResult() {
		differences = new ArrayList<>();
		areEqual = true;
	}

	public void addDifference(String difference) {
		differences.add(difference);
		areEqual = false;
	}

	public List<String> getDifferences() {
		return differences;
	}

	public boolean areEqual() {
		return areEqual;
	}

	@Override
	public String toString() {
		if (areEqual) {
			return "No differences found";
		} else {
			return String.join("\n", differences);
		}
	}
}
