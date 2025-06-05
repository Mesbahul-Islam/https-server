package com.o3.tests;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bmp {
	private final int width;
	private final int height;
	private ArrayList<Pixel> pixels;
	private static final String SECRET = "Qk02AwAAAAAAADYAAAAoAAAAEAAAABAAAAABABgAAAAAAAADAADEDgAAxA4AAAAAAAAAAAAA////////////////////////////AAAA////////////////////////////////////////////////////////////AAAA////////////////////////////////////////Fmrm////////D2bl////AAAA////D2bl////D2blD2blD2blGGzm////////////D2bl////////D2bl////AAAA////D2bl////////////////////////////////D2bl////////D2bl////AAAA////D2bl////////////////////////////////D2bl////////D2bl////AAAA////D2bl////////////////////////////////////////////////////AAAA////////////////////////////////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA////////////////////////////AAAA////////////////////////////////////D2bl////////////////////AAAA////D2bl////////D2bl////////////////D2bl////////////////////AAAA////D2bl////////D2bl////////////////D2bl////////////////////AAAA////D2bl////////D2bl////////////////D2bl////////////////////AAAA////D2bl////////D2bl////////////////////////////////////////AAAA////////////////////////////////////////////////////////////AAAA////////////////////////////////////////////////////////////AAAA////////////////////////////////";
	private static final Random random = new Random();

	/**
	 * Create a BMP image with a given width, height, and pixels
	 *
	 * @param width
	 * @param height
	 * @param pixels
	 */
	Bmp(int width, int height, ArrayList<Pixel> pixels) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Width and height must be positive");
		}

		this.width = width;
		this.height = height;
		this.pixels = pixels != null ? pixels : new ArrayList<>();

		if (pixels.size() > width * height) {
			throw new IllegalArgumentException("Too many pixels");
		} else if (pixels.size() < width * height) {
			for (int i = pixels.size(); i < width * height; i++) {
				addPixel(new Pixel(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
			}
		}
	}

	/**
	 * Create a BMP image with a given width and height
	 *
	 * @param width
	 * @param height
	 */
	Bmp(int width, int height) {
		this(width, height, new ArrayList<>());
	}

	/**
	 * Create a BMP image from a base64 string
	 *
	 * @param base64
	 */
	Bmp(String base64) {
		byte[] bytes = java.util.Base64.getDecoder().decode(base64);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		buffer.position(18);
		width = buffer.getInt();
		height = buffer.getInt();
		buffer.position(0x1c);
		int bitsPerPixel = Short.toUnsignedInt(buffer.getShort());
		buffer.position(54);
		pixels = new ArrayList<>();
		for (int i = 0; i < width * height; i++) {
			byte b = buffer.get();
			byte g = buffer.get();
			byte r = buffer.get();
			byte a = bitsPerPixel == 32 ? buffer.get() : (byte) 255;
			Pixel pixel = new Pixel(r, g, b, a);
			pixels.add(pixel);
		}
	}

	/**
	 * Add a pixel to the image
	 *
	 * @param pixel - pixel data
	 */
	public void addPixel(Pixel pixel) {
		if (pixels.size() >= width * height) {
			throw new IllegalStateException("Too many pixels");
		}
		pixels.add(pixel);
	}

	/**
	 * Set the pixel at the given coordinates
	 *
	 * @param x     - x coordinate
	 * @param y     - y coordinate
	 * @param pixel - pixel data
	 */
	public void setPixel(int x, int y, Pixel pixel) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IllegalArgumentException("Invalid coordinates");
		}
		pixels.set(y * width + x, pixel);
	}

	/**
	 * Get the BMP file as an array of bytes
	 *
	 * @return
	 */
	public Byte[] getFileBytes() {
		ArrayList<Byte> bytes = new ArrayList<>();
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		int rawDataSize = width * height * 4;

		// ----------------------------------------------------------------------
		// BMP Header
		// ----------------------------------------------------------------------
		// BM magic number
		bytes.add((byte) 0x42);
		bytes.add((byte) 0x4D);
		// File size
		buffer.clear();
		buffer.putInt(54 + rawDataSize);
		bytes.add(buffer.get(0));
		bytes.add(buffer.get(1));
		bytes.add(buffer.get(2));
		bytes.add(buffer.get(3));
		// Reserved
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Offset
		bytes.add((byte) 0x36);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// DIB Header size
		bytes.add((byte) 0x28);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Width
		buffer.clear();
		buffer.putInt(width);
		bytes.add(buffer.get(0));
		bytes.add(buffer.get(1));
		bytes.add(buffer.get(2));
		bytes.add(buffer.get(3));
		// Height
		buffer.clear();
		buffer.putInt(height);
		bytes.add(buffer.get(0));
		bytes.add(buffer.get(1));
		bytes.add(buffer.get(2));
		bytes.add(buffer.get(3));
		// Planes
		bytes.add((byte) 0x01);
		bytes.add((byte) 0x00);
		// Bits per pixel
		bytes.add((byte) 0x20);
		bytes.add((byte) 0x00);
		// Compression
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Image size
		buffer.clear();
		buffer.putInt(rawDataSize);
		bytes.add(buffer.get(0));
		bytes.add(buffer.get(1));
		bytes.add(buffer.get(2));
		bytes.add(buffer.get(3));
		// Horizontal resolution
		bytes.add((byte) 0x13);
		bytes.add((byte) 0x0b);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Vertical resolution
		bytes.add((byte) 0x13);
		bytes.add((byte) 0x0b);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Number of colors
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		// Important colors
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);
		bytes.add((byte) 0x00);

		for (Pixel pixel : pixels) {
			byte[] pixelBytes = pixel.getBytes();
			for (byte b : pixelBytes) {
				bytes.add(b);
			}
		}

		return bytes.toArray(new Byte[bytes.size()]);
	}

	/**
	 * Get the base64 representation of the image
	 *
	 * @return
	 */
	public String getBase64() {
		Byte[] bytes = getFileBytes();
		byte[] byteArray = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			byteArray[i] = bytes[i];
		}
		return java.util.Base64.getEncoder().encodeToString(byteArray);
	}

	/**
	 * Get the MIME type of the image
	 *
	 * @return
	 */
	public String getMimeType() {
		return "image/bmp";
	}

	/**
	 * Get the data URL of the image
	 *
	 * @return
	 */
	public String getDataString() {
		return String.format("data:%s;base64,%s", getMimeType(), getBase64());
	}

	/**
	 * Get the Inline Image Protocol (IIP) representation of the image
	 *
	 * @return
	 */
	public String getIipImage() {
		return String.format(
				"%c]1337;File=width=%dpx;height=%dpx;inline=1:%s%c",
				(char) 27,
				width,
				height,
				getBase64(),
				(char) 7);
	}

	public static void main(String[] args) {
		int width = 200;
		int height = 200;
		Bmp bmp = new Bmp(width, height);
		// Draw the image on terminals that support it
		System.out.println(bmp.getIipImage());
		Bmp bmp2 = new Bmp(SECRET);
		System.out.println(bmp2.getIipImage());
		Bmp bmp3 = new Bmp(bmp.getBase64());
		System.out.println(bmp3.getIipImage());
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public List<Pixel> getPixels() {
		return pixels;
	}

	public void setPixels(List<Pixel> pixels) {
		if (pixels.size() != width * height) {
			throw new IllegalArgumentException("Wrong number of pixels");
		}
		this.pixels = new ArrayList<>(pixels);
	}

	@Override
	public String toString() {
		return getIipImage();
	}
}
