package com.o3.server;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SearchFunction implements HttpHandler{
    private final MessageDatabase db = MessageDatabase.getInstance();
    
    @Override
    public void handle(HttpExchange t) throws IOException {
        if(t.getRequestMethod().equalsIgnoreCase("POST")){
            OutputStream os = t.getResponseBody();
            String response = "Not supported";
            byte[] responseBytes = response.getBytes("UTF-8");
            t.sendResponseHeaders(405, responseBytes.length);   
            os.write(responseBytes);  
            os.flush();
            os.close();
        }
        else if(t.getRequestMethod().equalsIgnoreCase("GET")){
            handleGETrequest(t);
        }
        else{
            OutputStream os = t.getResponseBody();
            String response = "Not supported";
            byte[] responseBytes = response.getBytes("UTF-8");
            t.sendResponseHeaders(405, responseBytes.length);   
            os.write(responseBytes);  
            os.flush();
            os.close();
        }
    }

    public void handleGETrequest(HttpExchange exchange) throws IOException{
        String query = exchange.getRequestURI().getQuery();
        if(query == null || query.isEmpty()){
            sendResponse(exchange, 400, "Query parameters missing");
        }
        else{
            try {
            JSONObject searchCriteria = parseQueryParameters(query);
            JSONArray results = searchDatabase(searchCriteria);
            sendResponse(exchange, 200, results.toString());
            } 
            catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Something wrong with database");
            }
        }
    }
    private JSONObject parseQueryParameters(String query) {
        JSONObject criteria = new JSONObject();
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
    
                switch (key) {
                    case "nickname":
                        criteria.put("nickname", value);
                        break;
                    case "before":
                        criteria.put("before", value);
                        break;
                    case "after":
                        criteria.put("after", value);
                        break;
                    case "identification":
                        criteria.put("identification", value);
                        break;
                    default:
                        break;
                }
            }
        }
        return criteria;
    }
    private JSONArray searchDatabase(JSONObject criteria) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM messages WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (criteria.has("nickname")) {
            sql.append(" AND recordOwner = ?");
            parameters.add(criteria.getString("nickname"));
        }
        if (criteria.has("before") && criteria.has("after")) {
            sql.append(" AND recordTimeReceived BETWEEN ? AND ?");
            parameters.add(criteria.getString("after"));
            parameters.add(criteria.getString("before"));
        }
        if (criteria.has("identification")) {
            sql.append(" AND recordIdentifier = ?");
            parameters.add(criteria.getString("identification"));
        }
        System.out.println("SQL: " + sql.toString());
        ResultSet rs = db.executeQuery(sql.toString(), parameters);
        JSONArray results = new JSONArray();

        while (rs.next()) {
            JSONObject message = new JSONObject();
            message.put("recordIdentifier", rs.getString("recordIdentifier"));
            message.put("recordDescription", rs.getString("recordDescription"));
            message.put("recordPayload", rs.getString("recordPayload"));
            message.put("recordOwner", rs.getString("recordOwner"));
            message.put("recordTimeReceived", rs.getString("recordTimeReceived"));
            message.put("recordRightAscension", rs.getString("recordRightAscension"));
            message.put("recordDeclination", rs.getString("recordDeclination"));
            results.put(message);
        }

        return results;
    }
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        OutputStream os = exchange.getResponseBody();
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);   
        os.write(responseBytes);  
        os.flush();
        os.close();
    }
}
