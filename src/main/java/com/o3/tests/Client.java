package com.o3.tests;

import java.io.IOException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.github.mizosoft.methanol.Methanol;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class Client {
	// Constants
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String GET = "GET";
	public static final String DELETE = "DELETE";

	private int requestTimeout = 20;
	private String serverAddress;
	private String registerContext = "registration";
	private String messageContext = "datarecord";
	private String searchContext = "search";
	private String contentType = "application/json";
	private HttpClient httpClient;
	private String auth;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Create a new HTTP(S) client
	 *
	 * @param address Server address
	 */
	public Client(String address) throws NoSuchAlgorithmException, KeyManagementException {
		this(address, null);
	}

	/**
	 * Create a new HTTP(S) client
	 *
	 * @param address Server address
	 * @param auth    Authenticator for connections
	 */
	@SuppressWarnings({ "java:S4830", "java:S1168" })
	public Client(String address, Authenticator auth) throws NoSuchAlgorithmException, KeyManagementException {
		if (!address.endsWith("/"))
			this.serverAddress = address + "/";
		else
			this.serverAddress = address;

		// Trust all HTTPS certificates
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] {
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
						// Trust all clients
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
						// Trust all servers
					}
				}
		}, new SecureRandom());

		HttpClient.Builder httpClientBuilder = Methanol.newBuilder()
				.version(Version.HTTP_1_1)
				.baseUri(this.serverAddress)
				.requestTimeout(Duration.ofSeconds(requestTimeout))
				.headersTimeout(Duration.ofSeconds(requestTimeout))
				.readTimeout(Duration.ofSeconds(requestTimeout))
				.connectTimeout(Duration.ofSeconds(requestTimeout))
				.followRedirects(Redirect.NORMAL)
				.sslContext(sslContext);

		if (auth != null) {
			httpClientBuilder.authenticator(auth);
		}
		this.httpClient = httpClientBuilder.build();
	}

	/**
	 * Test server connection
	 *
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to test client
	 */
	public synchronized StatusCode testConnection() throws InterruptedException {
		URI uri = URI.create(serverAddress);
		HttpResponse<String> response = sendRequest(uri, null, Client.GET);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Send message to server
	 *
	 * @param message Message to be sent
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode sendMessage(String message) throws InterruptedException {
		return sendMessage(message, null);
	}

	/**
	 * Send message to server
	 *
	 * @param message Message to be sent
	 * @param params  Map of parameters to be sent with the message
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode sendMessage(String message, Map<String, String> params) throws InterruptedException {
		String paramsString = "";
		if (params != null) {
			StringBuilder urlParams = new StringBuilder("?");
			for (Map.Entry<String, String> entry : params.entrySet()) {
				urlParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			paramsString = urlParams.toString();
		}
		URI uri = URI.create(serverAddress + messageContext + paramsString);
		HttpResponse<String> response = sendRequest(uri, message, Client.POST);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Send delete request to server
	 *
	 * @param id ID of the message to be deleted
	 * @return HTTP response status code or -1 on failure
	 * @throws InterruptedException Faulty URI set to the test client
	 */
	public StatusCode deleteMessage(int id) throws InterruptedException {
		URI uri = URI.create(serverAddress + messageContext + "?id=" + id);
		HttpResponse<String> response = sendRequest(uri, null, Client.DELETE);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Update message stored in the server
	 *
	 * @param message  Message to be sent
	 * @param urlPrams String containing Url parameters for update
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode updateMessage(String message, String urlParams) throws InterruptedException {
		URI uri = URI.create(serverAddress + messageContext + "?" + urlParams);
		HttpResponse<String> response = sendRequest(uri, message, Client.PUT);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Get messages from server
	 *
	 * @return String representation of messages
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode getMessages() throws InterruptedException {
		URI uri = URI.create(serverAddress + messageContext);
		HttpResponse<String> response = sendRequest(uri, null, Client.GET);

		if (response == null)
			return null;

		return new StatusCode(response.statusCode(), response.body());
	}

	public StatusCode searchMessages(SearchQuery query) throws InterruptedException {
		URI uri = URI.create(serverAddress + searchContext + query.toQueryString());
		HttpResponse<String> response = sendRequest(uri, null, Client.GET);

		if (response == null)
			return null;

		return new StatusCode(response.statusCode(), response.body());
	}

	/**
	 * Register user to server
	 *
	 * @param userInfo User information to be registered
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode register(String userInfo) throws InterruptedException {
		URI uri = URI.create(serverAddress + registerContext);
		HttpResponse<String> response = sendRequest(uri, userInfo, Client.POST);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Get user information from server
	 *
	 * @return HTTP response status code or -1 on failure
	 * @throws URISyntaxException Faulty URI set to the test client
	 */
	public StatusCode getRegister() throws InterruptedException {
		URI uri = URI.create(serverAddress + registerContext);
		HttpResponse<String> response = sendRequest(uri, null, Client.GET);

		if (response == null)
			return new StatusCode(-1);

		return new StatusCode(response.statusCode());
	}

	/**
	 * Send HTTP request to specified URI
	 * <br>
	 * If no content is provided, sends a GET request,
	 * otherwise sends a POST request
	 *
	 * @param uri     URI for request
	 * @param content Request content
	 * @return HttpResponse object
	 */
	public HttpResponse<String> sendRequest(URI uri, String content, String verb) throws InterruptedException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
				.timeout(Duration.ofSeconds(requestTimeout))
				.setHeader("Content-Type", contentType);

		if (auth != null)
			requestBuilder.setHeader("Authorization", auth);

		if (verb.equals(Client.PUT))
			requestBuilder.PUT(BodyPublishers.ofString(content, StandardCharsets.UTF_8));
		else if (verb.equals(Client.POST))
			requestBuilder.POST(BodyPublishers.ofString(content, StandardCharsets.UTF_8));
		else if (verb.equals(Client.DELETE))
			requestBuilder.DELETE();
		else
			requestBuilder.GET();

		HttpRequest request = requestBuilder.build();

		try {
			return httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (ConnectException ex) {
			logger.severe(
					String.format("Error connecting to the server at `%s`! Is the server running on the correct port?",
							request.uri().toString()));
			return null;
		} catch (HttpTimeoutException ex) {
			logger.severe(ex.getMessage());
			logger.severe(
					String.format("""
							Request to `%s` timed out after %d seconds!
							Did you remember to send the response body?
							""",
							request.uri().toString(),
							requestTimeout));
			return null;
		} catch (RuntimeException | IOException ex) {
			logger.severe(String.format("%s", ex.getMessage()));
			return null;
		}
	}

	/**
	 * Get server address
	 *
	 * @return Current server address
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * Set server address
	 *
	 * @param address New server address
	 */
	public void setServerAddress(String address) {
		if (!address.endsWith("/"))
			this.serverAddress = address + "/";
		else
			this.serverAddress = address;
	}

	/**
	 * Get registration context
	 *
	 * @return Current registration context
	 */
	public String getRegisterContext() {
		return registerContext;
	}

	/**
	 * Set registration context
	 *
	 * @param registerContext New registration context
	 */
	public void setRegisterContext(String registerContext) {
		this.registerContext = registerContext;
	}

	/**
	 * Get message context
	 *
	 * @return Current message context
	 */
	public String getMessageContext() {
		return messageContext;
	}

	/**
	 * Set message context
	 *
	 * @param messageContext New message context
	 */
	public void setMessageContext(String messageContext) {
		this.messageContext = messageContext;
	}

	/**
	 * Get request timeout
	 *
	 * @return Current request timeout
	 */
	public int getRequestTimeout() {
		return this.requestTimeout;
	}

	/**
	 * Set request timeout
	 *
	 * @param timeout New request timeout
	 */
	public void setRequestTimeout(int timeout) {
		if (timeout > 0)
			this.requestTimeout = timeout;
	}

	/**
	 * Get content type
	 *
	 * @return Current content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set content type
	 *
	 * @param contentType New content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set authentication for the client
	 *
	 * @param username Username for authentication
	 * @param password Password for authentication
	 */
	public void setAuth(String username, String password) {
		this.auth = "Basic "
				+ Base64.getEncoder().encodeToString(
						(username + ":" + password).getBytes(StandardCharsets.UTF_8));
	}
}
