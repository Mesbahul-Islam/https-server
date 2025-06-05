package com.o3.server;
import java.io.IOException;
import java.io.OutputStream;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler{
    UserAuthenticator userAuthenticator = null;
    public RegistrationHandler(UserAuthenticator userAuthenticator){
        this.userAuthenticator = userAuthenticator;
    }
    @Override
    public void handle(HttpExchange t) throws IOException {
        if(t.getRequestMethod().equalsIgnoreCase("POST")){
            handlePOSTrequest(t);
        }
        else if(t.getRequestMethod().equalsIgnoreCase("GET")){
            handleGETrequest(t);
        }
        else{
            handleOtherRequest(t);
        }
    }
    private void handlePOSTrequest(HttpExchange t) throws IOException {
        t.getResponseHeaders().set("Content-Type", "application/json");
        try {
            JSONTokener tokener = new JSONTokener(t.getRequestBody());
            JSONObject user = new JSONObject(tokener);
            if (user.length() != 4 || !user.has("username") || !user.has("password") || !user.has("email") || !user.has("userNickname")) {
                OutputStream os = t.getResponseBody();
                String responseString = "Fields are missing";
                byte[] responseBytes = responseString.getBytes("UTF-8");
                t.sendResponseHeaders(400, responseBytes.length);
                os.write(responseBytes);
                os.flush();
                os.close();
            } else {
                String username = user.getString("username");
                String password = user.getString("password");
                String email = user.getString("email");
                String userNickname = user.getString("userNickname");
    
                if (userAuthenticator.addUser(username, password, email, userNickname)) {
                    t.sendResponseHeaders(200, -1);
                } else {
                    OutputStream os = t.getResponseBody();
                    String responseString = "User already exists";
                    byte[] responseBytes = responseString.getBytes("UTF-8");
                    t.sendResponseHeaders(400, responseBytes.length);
                    os.write(responseBytes);
                    os.flush();
                    os.close();
                }
            }
        } catch (Exception e) {
            OutputStream os = t.getResponseBody();
            String responseString = "Bad JSON format";
            byte[] responseBytes = responseString.getBytes("UTF-8");
            t.sendResponseHeaders(400, responseBytes.length);
            os.write(responseBytes);
            os.flush();
            os.close();
        }
    }
    private void handleGETrequest(HttpExchange t) throws IOException {
        OutputStream os = t.getResponseBody();
        String response = "Not supported";
        byte[] responseBytes = response.getBytes("UTF-8");
        t.sendResponseHeaders(400, responseBytes.length);   
        os.write(responseBytes);  
        os.flush();
        os.close();
        
    }
    private void handleOtherRequest(HttpExchange t) throws IOException {
        OutputStream os = t.getResponseBody();
        String response = "Invalid request";
        byte[] responseBytes = response.getBytes("UTF-8");
        t.sendResponseHeaders(401, responseBytes.length);   
        os.write(responseBytes);  
        os.flush();
        os.close();
    }
}
