package com.o3.tests;

public class Pixel {
	private int r;
	private int g;
	private int b;
	private int a;

	public Pixel(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public Pixel(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public byte[] getBytes() {
		return new byte[] { (byte) b, (byte) g, (byte) r, (byte) a };
	}

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
	}

	public int getA() {
		return a;
	}

	public void setR(int r) {
		this.r = r;
	}

	public void setG(int g) {
		this.g = g;
	}

	public void setB(int b) {
		this.b = b;
	}

	public void setA(int a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return String.format("Pixel(%d, %d, %d, %d)", r, g, b, a);
	}
}
