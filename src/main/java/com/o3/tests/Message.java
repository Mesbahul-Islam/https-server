package com.o3.tests;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONPropertyIgnore;
import org.json.JSONPropertyName;

public class Message {
	private String identifier;
	private String description;
	private String payload;
	private String rightAscension;
	private String declination;
	private String owner;
	private ZonedDateTime timeReceived;
	private ArrayList<Observatory> observatories;
	private ArrayList<Weather> weather;
	private static final Random random = new Random();

	public Message(String identifier, String description, String payload, String rightAscension, String declination) {
		this.identifier = identifier;
		this.description = description;
		this.payload = payload;
		this.rightAscension = rightAscension;
		this.declination = declination;
		this.observatories = new ArrayList<>();
		this.weather = new ArrayList<>();
	}

	public Message(String identifier, String description, String payload, String rightAscension, String declination,
			ZonedDateTime timeReceived) {
		this(identifier, description, payload, rightAscension, declination);
		this.timeReceived = timeReceived;
	}

	public Message(String identifier, String description, String payload) {
		this(identifier, description, payload, Helpers.createRightAscension(), Helpers.createDeclination());
	}

	public Message(String identifier, String description, String payload, ZonedDateTime timeReceived) {
		this(identifier, description, payload);
		this.timeReceived = timeReceived;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Identifier: ").append(identifier).append(", ");
		sb.append("Description: ").append(description).append(", ");
		sb.append("Payload: ").append(payload).append(", ");
		sb.append("Right Ascension: ").append(rightAscension).append(", ");
		sb.append("Declination: ").append(declination).append(", ");
		if (owner != null) {
			sb.append("Owner: ").append(owner).append(", ");
		}
		if (timeReceived != null) {
			sb.append("Time Received: ").append(timeReceived).append(", ");
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Message)) {
			return false;
		}

		Message msg = (Message) obj;
		return identifier.equals(msg.getIdentifier()) && description.equals(msg.getDescription())
				&& payload.equals(msg.getPayload()) && rightAscension.equals(msg.getRightAscension())
				&& declination.equals(msg.getDeclination()) && owner.equals(msg.getOwner());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + identifier.hashCode();
		result = 31 * result + description.hashCode();
		result = 31 * result + payload.hashCode();
		result = 31 * result + rightAscension.hashCode();
		result = 31 * result + declination.hashCode();
		result = 31 * result + owner.hashCode();
		return result;
	}

	public JSONObject toJson() {
		JSONObject msg = new JSONObject(this);
		if (observatories == null || observatories.isEmpty())
			msg.remove("observatory");

		return msg;
	}

	public String toJsonString() {
		return toJson().toString();
	}

	public String toJsonString(int indentFactor) {
		return toJson().toString(indentFactor);
	}

	public String toJsonString(boolean includeWeather) {
		JSONObject msg = toJson();
		if (includeWeather)
			msg.put("observatoryWeather", weather);
		return msg.toString();
	}

	public String toJsonString(int indentFactor, boolean includeWeather) {
		JSONObject msg = toJson();
		if (includeWeather)
			msg.put("observatoryWeather", weather);
		return msg.toString(indentFactor);
	}

	@JSONPropertyIgnore
	public ZonedDateTime getTimeReceived() {
		return timeReceived;
	}

	// @JSONPropertyName("timestamp")
	@JSONPropertyIgnore
	public String getTimeReceivedString() {
		if (timeReceived == null) {
			return null;
		}
		return timeReceived
				.withZoneSameInstant(ZoneOffset.UTC)
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
	}

	public void setTimeReceived(ZonedDateTime timeReceived) {
		this.timeReceived = timeReceived;
	}

	@JSONPropertyIgnore
	public static Random getRandom() {
		return random;
	}

	@JSONPropertyName("recordDescription")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JSONPropertyName("recordIdentifier")
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@JSONPropertyName("recordPayload")
	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@JSONPropertyName("recordRightAscension")
	public String getRightAscension() {
		return rightAscension;
	}

	public void setRightAscension(String rightAscension) {
		this.rightAscension = rightAscension;
	}

	@JSONPropertyName("recordDeclination")
	public String getDeclination() {
		return declination;
	}

	public void setDeclination(String declination) {
		this.declination = declination;
	}

	@JSONPropertyName("recordOwner")
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@JSONPropertyName("observatory")
	public List<Observatory> getObservatories() {
		return observatories;
	}

	public void setObservatories(List<Observatory> observatories) {
		this.observatories = new ArrayList<>(observatories);
	}

	@JSONPropertyIgnore
	public List<Weather> getWeather() {
		return weather;
	}

	public void setWeather(List<Weather> weather) {
		this.weather = new ArrayList<>(weather);
	}
}
