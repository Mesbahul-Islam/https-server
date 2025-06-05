package com.o3.tests;

public class CipherMessage {
	private int key;
	private String message;

	public CipherMessage(int key, String message) {
		this.key = key;
		this.message = message;
	}

	public int getKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}
}
