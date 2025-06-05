package com.o3.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Helpers {
	private static Random random = new Random();
	private static final String[] words;

	static {
		try {
			words = readWords();
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private Helpers() {
	}

	/**
	 * Read a list of words from a file
	 *
	 * @return an array of words
	 * @throws IOException if an I/O error occurs
	 */
	public static String[] readWords() throws IOException {
		InputStream wordStream = Helpers.class.getClassLoader().getResourceAsStream("words_alpha.txt");
		if (wordStream == null) {
			return new String[] {};
		} else {
			String wordText = new String(wordStream.readAllBytes(), StandardCharsets.UTF_8);
			return wordText.split("\\s+");
		}
	}

	/**
	 * Generate a random string from a list of words
	 *
	 * @param words  the list of words to choose from
	 * @param length the number of words to include in the string
	 * @return a random string
	 */
	public static String generateRandomString(int length) {
		ArrayList<String> strings = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(words.length);
			strings.add(words[index]);
		}
		return strings.stream().collect(Collectors.joining(" "));
	}

	/**
	 * Create a new user with a random username and password
	 *
	 * @return User object with random username and password
	 */
	public static User createUser() {
		String username = Helpers.generateRandomString(1);
		String password = Helpers.generateRandomString(1);
		String nick = Helpers.generateRandomString(1);
		String email = String.format("%s.%s@%s.%s", username, nick,
				Helpers.generateRandomString(1),
				Helpers.generateRandomString(1));
		return new User(username, password, email, nick);
	}

	/**
	 * Check if an array contains a target string
	 *
	 * @param array  the array to search
	 * @param target the target string
	 * @return true if the target is in the array, false otherwise
	 */
	public static boolean contains(String[] array, String target) {
		if (array == null) {
			return false;
		}
		for (String s : array) {
			if (s.equals(target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Merge two sets
	 *
	 * @param set1 the first set
	 * @param set2 the second set
	 * @param <T>  the type of the set
	 * @return a new set containing all elements from set1 and set2
	 */
	public static <T> Set<T> mergeSets(Set<T> set1, Set<T> set2) {
		Set<T> mergedSet = new HashSet<>();
		mergedSet.addAll(set1);
		mergedSet.addAll(set2);
		return mergedSet;
	}

	/**
	 * Generate a random coordinate for rightAscension
	 *
	 * @return
	 */
	public static String createRightAscension() {
		return String.format("%dh %dm %ds", random.nextInt(24), random.nextInt(60), random.nextInt(60));
	}

	/**
	 * Generate a random coordinate for declination
	 *
	 * @return
	 */
	public static String createDeclination() {
		int declination = random.nextInt(-89, 90);
		return String.format("%c%d° %d' %d\"", declination >= 0 ? '+' : '-', Math.abs(declination), random.nextInt(60),
				random.nextInt(60));
	}
}
