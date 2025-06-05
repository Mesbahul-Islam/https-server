package com.o3.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.sql.ResultSet;

import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;


public class Server implements HttpHandler {
	private static final MessageDatabase db = MessageDatabase.getInstance();
	private Server() {
	}

	@Override
	public void handle(HttpExchange t) throws IOException{
		if(t.getRequestMethod().equalsIgnoreCase("POST")){
			handlePOSTrequest(t);
		}
		else if(t.getRequestMethod().equalsIgnoreCase("GET")){
			handleGETrequest(t);
		}
		else if(t.getRequestMethod().equalsIgnoreCase("PUT")){
			handlePUTrequest(t);
		}
		else{
			handleOtherRequest(t);
		}

	}

	private static SSLContext myServerSSLContext(String path, String pass) throws Exception{
		char[] passphrase = pass.toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(path), passphrase);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, passphrase);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return ssl;
	}

	private void handlePOSTrequest(HttpExchange exchange) throws IOException {
		try {
			String authenticatedUser = exchange.getPrincipal().getUsername();
			
			JSONTokener tokener = new JSONTokener(exchange.getRequestBody());
			JSONObject message = new JSONObject(tokener);
			String shift = "N/A";
			String query = exchange.getRequestURI().getQuery();
			if (query != null && query.contains("shift=")) {
				shift = query.split("shift=")[1].split("&")[0];
			}
			message.put("shift", shift);
			
			// Store the message in the database
			db.setMessage(message, authenticatedUser);
			
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.getResponseHeaders().add("Content-Length", "0");
			exchange.sendResponseHeaders(200, -1);
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(400, -1);
		}
	}
	private void handleGETrequest(HttpExchange exchange) throws IOException {
		try {
			ResultSet rs = db.getMessages();
			JSONArray messages = new JSONArray();
			String username = exchange.getPrincipal().getUsername();

			while (rs.next()) {
				JSONObject message = new JSONObject();

				//mandatory fields
				message.put("recordIdentifier", rs.getString("recordIdentifier"));
				message.put("recordDescription", rs.getString("recordDescription"));
				
				message.put("recordPayload", rs.getString("recordPayload"));
				//check for shift and username
				//if shift is not N/A and username matches, decipher the payload
				if(!rs.getString("shift").equals("N/A") && rs.getString("username").equals(username)){
					String recordPayload = rs.getString("recordPayload");
					int shift = Integer.parseInt(rs.getString("shift"));
					String decipheredPayload = DecipherService.decipher(recordPayload, shift);
					message.put("recordPayload", decipheredPayload);
				}
				
				message.put("recordRightAscension", rs.getString("recordRightAscension"));
				message.put("recordDeclination", rs.getString("recordDeclination"));
				message.put("recordOwner", rs.getString("recordOwner"));
				message.put("recordTimeReceived", rs.getString("recordTimeReceived"));

                String modified = rs.getString("modified");
				System.out.println(modified);

				message.put("id", rs.getInt("id"));

				if(modified != null){
					message.put("updateReason", rs.getString("updateReason"));
					message.put("modified", modified);
				}
            

				if (rs.getString("observatoryName") == null) {
					messages.put(message);
					continue;
				}

				JSONArray observatory = new JSONArray();
				JSONObject observatoryObject = new JSONObject();
				observatoryObject.put("observatoryName", rs.getString("observatoryName"));
				observatoryObject.put("latitude", rs.getDouble("latitude"));
				observatoryObject.put("longitude", rs.getDouble("longitude"));
				observatory.put(observatoryObject);
				message.put("observatory", observatory);


				if(rs.getDouble("temperatureInKelvins") != 0) {
					if(message.has("updateReason") && message.has("id")){
						message.remove("id");
						message.remove("updateReason");
						message.remove("modified");
					}
					JSONObject weather = new JSONObject();
					weather.put("temperatureInKelvins", rs.getDouble("temperatureInKelvins"));
					weather.put("cloudinessPercentage", rs.getDouble("cloudinessPercentage"));
					weather.put("backgroundLightVolume", rs.getDouble("backgroundLightVolume"));
					JSONArray weatherArray = new JSONArray();
					weatherArray.put(weather);
					message.put("observatoryWeather", weatherArray);
				}
				messages.put(message);

			}
			if (messages.length() == 0) {
				String responseString = messages.toString();
				byte[] responseBytes = responseString.getBytes("UTF-8");
				exchange.sendResponseHeaders(200, responseBytes.length);
				OutputStream os = exchange.getResponseBody();
				os.write(responseBytes);
				os.flush();
				os.close();
			} 
			else {
				String responseString = messages.toString();
				byte[] responseBytes = responseString.getBytes("UTF-8");
				exchange.sendResponseHeaders(200, responseBytes.length);
				OutputStream os = exchange.getResponseBody();
				os.write(responseBytes);
				os.flush();
				os.close();
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(400, -1);
		}
	}

	private void handleOtherRequest(HttpExchange exchange) throws IOException{
		OutputStream os = exchange.getResponseBody();
		String responseString = "Invalid Request";
		byte[] responseBytes = responseString.getBytes("UTF-8");
		exchange.sendResponseHeaders(400, responseBytes.length);
		os.write(responseString.getBytes());
		os.flush();
		os.close();
	}

	private void handlePUTrequest(HttpExchange exchange) throws IOException {
		try {
			String authenticatedUser = exchange.getPrincipal().getUsername();

			String query = exchange.getRequestURI().getQuery();
			if (query == null || !query.startsWith("id=")) {
				sendResponse(exchange, 400, "Missing or invalid query parameter: id");
				return;
			}
			int id = Integer.parseInt(query.split("=")[1]);
	
			JSONTokener tokener = new JSONTokener(exchange.getRequestBody());
			JSONObject updatedMessage = new JSONObject(tokener);


			if (!db.isMessageOwner(id, authenticatedUser)) {
				sendResponse(exchange, 403, "You are not authorized to update this message");
				return;
			}
	
			if (!updatedMessage.has("updateReason") || updatedMessage.getString("updateReason").isEmpty()) {
				updatedMessage.put("updateReason", (Object) null);
			}
	
			// Set the `modified` timestamp
			updatedMessage.put("modified", (Object) null);
	
			// Update the message in the database
			boolean success = db.updateMessage(id, updatedMessage, authenticatedUser);
			if (!success) {
				sendResponse(exchange, 404, "Message not found");
				return;
			}
	
			JSONObject updatedRecord = db.getMessage(id);
        	sendJsonResponse(exchange, 200, updatedRecord.toString());
		} catch (Exception e) {
			e.printStackTrace();
			sendResponse(exchange, 400, "Invalid request");
		}
	}
	
	private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, message.isEmpty() ? -1 : responseBytes.length);

        if (!message.isEmpty()) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
                os.flush();
            }
        }
    }

	private void sendJsonResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(statusCode, message.isEmpty() ? -1 : responseBytes.length);
		exchange.getResponseHeaders().set("Content-Length", String.valueOf(responseBytes.length));
	
		if (!message.isEmpty()) {
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(responseBytes);
				os.flush();
			}
		}
	}


	public static void main(String[] args) throws Exception {
		try{
			db.open("myDatabase.db");
			//create the http server to port 8001 with default logger
			HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
			UserAuthenticator ua = new UserAuthenticator("datarecord");
			SSLContext ssl = myServerSSLContext(args[0], args[1]);	
			server.setHttpsConfigurator (new HttpsConfigurator(ssl) {
				public void configure (HttpsParameters params) {
				SSLContext c = getSSLContext();
				SSLParameters sslparams = c.getDefaultSSLParameters();
				params.setSSLParameters(sslparams);
				}
			});
			final HttpContext httpContextFinal = server.createContext("/datarecord", new Server());
			httpContextFinal.setAuthenticator(ua);
			server.createContext("/registration", new RegistrationHandler(ua));
			server.createContext("/search", new SearchFunction());
			// creates a default executor
			server.setExecutor((Executors.newCachedThreadPool()));
			System.out.println("Request handled in thread " + Thread.currentThread().getName()); 
			server.start();
		}
		catch(FileNotFoundException e){
			System.out.println("File not found");
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
