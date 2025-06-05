package com.o3.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class DecipherService {
    private static final String DECIPHER_URL = "http://localhost:4002/decipher";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    
    public static String decipher(String ciphertext, int shift) throws IOException, InterruptedException {
        String url = DECIPHER_URL + "?shift=" + shift;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "text/plain")
            .POST(BodyPublishers.ofString(ciphertext))
            .build();
            
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("Deciphered response: " + response.body());
            return response.body();
        }
        
        // Return original ciphertext if decipher service fails
        return ciphertext;
    }
}