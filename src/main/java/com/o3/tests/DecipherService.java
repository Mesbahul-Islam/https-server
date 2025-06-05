package com.o3.tests;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

public class DecipherService {
	private String serverAddress;
	private String context;
	private HttpClient client;

	public DecipherService(String serverAddress, String context) {
		this.serverAddress = serverAddress;
		this.context = context;
		this.client = HttpClient.newBuilder()
			.version(Version.HTTP_1_1)
			.followRedirects(Redirect.NORMAL)
			.connectTimeout(java.time.Duration.ofSeconds(10))
			.build();
	}

	public DecipherService() {
		this("http://localhost:4002/", "decipher");
	}

	public String decipher(int shift, String ciphertext) throws IOException, InterruptedException {
		HttpRequest req = HttpRequest.newBuilder()
			.uri(java.net.URI.create(serverAddress + context + "?shift=" + shift))
			.POST(BodyPublishers.ofString(ciphertext, StandardCharsets.UTF_8))
			.build();

		HttpResponse<String> res = client.send(req, BodyHandlers.ofString());

		if (res.statusCode() >= 200 && res.statusCode() < 300) {
			return res.body();
		}

		return ciphertext;
	}
}
