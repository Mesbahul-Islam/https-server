package com.o3.tests;

public class Weather {
	private double temperatureInKelvins;
	private double cloudinessPercentage;
	private double backgroundLightVolume;

	public Weather(double temperatureInKelvins, double cloudinessPercentage, double backgroundLightVolume) {
		this.temperatureInKelvins = temperatureInKelvins;
		this.cloudinessPercentage = cloudinessPercentage;
		this.backgroundLightVolume = backgroundLightVolume;
	}

	public double getRemperatureInKelvins() {
		return temperatureInKelvins;
	}

	public void setRemperatureInKelvins(double temperatureInKelvins) {
		this.temperatureInKelvins = temperatureInKelvins;
	}

	public double getCloudinessPercentage() {
		return cloudinessPercentage;
	}

	public void setCloudinessPercentage(double cloudinessPercentage) {
		this.cloudinessPercentage = cloudinessPercentage;
	}

	public double getBackgroundLightVolume() {
		return backgroundLightVolume;
	}

	public void setBackgroundLightVolume(double backgroundLightVolume) {
		this.backgroundLightVolume = backgroundLightVolume;
	}
}
