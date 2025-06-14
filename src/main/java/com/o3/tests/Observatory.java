package com.o3.tests;

import org.json.JSONPropertyName;

public class Observatory {
	private String name;
	private Double latitude;
	private Double longitude;

	public Observatory(String name, Double latitude, Double longitude) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Observatory(String name) {
		this(name, null, null);
	}

	@JSONPropertyName("observatoryName")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
