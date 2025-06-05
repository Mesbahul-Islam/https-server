package com.o3.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;

public class MessageDatabase {
    private Connection databaseConnection = null;
    private static MessageDatabase instance = null;
    SecureRandom random = new SecureRandom();
    public synchronized static MessageDatabase getInstance() {
        if (instance == null) {
            instance = new MessageDatabase();
        }
        return instance;
    }
    private MessageDatabase() {
    }

    public synchronized void setMessage(JSONObject message, String authenticatedUser) throws SQLException {
        String recordIdentifier = message.getString("recordIdentifier");
        String recordDescription = message.getString("recordDescription");
        String recordPayload = message.getString("recordPayload");
        String shift = message.getString("shift");
        String recordRightAscension = message.getString("recordRightAscension");
        String recordDeclination = message.getString("recordDeclination");
        String recordOwner = message.optString("recordOwner", "");
        String observatoryName = null;
        double observatoryLatitude = 0;
        double observatoryLongitude = 0;
        if (message.has("observatory") && message.getJSONArray("observatory").length() > 0) {
            JSONArray observatoryArray = message.getJSONArray("observatory");
            JSONObject observatory = observatoryArray.getJSONObject(0);
            observatoryName = observatory.getString("observatoryName");
            observatoryLatitude = observatory.getDouble("latitude");
            observatoryLongitude = observatory.getDouble("longitude");

        }
        if (recordOwner.isEmpty()) {
            String selectUserSQL = "SELECT userNickname FROM users WHERE username = ?";
            try (PreparedStatement pstmt = databaseConnection.prepareStatement(selectUserSQL)) {
                pstmt.setString(1, authenticatedUser);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    recordOwner = rs.getString("userNickname");
                }
            }
        }
        double temperatureInKelvins = 0;
        double cloudinessPercentage = 0;
        double backgroundLightVolume = 0; 
        if(observatoryLatitude != 0 && observatoryLongitude != 0 && message.has("observatoryWeather")) {
            JSONObject weather = WeatherService.getWeatherData(observatoryLatitude, observatoryLongitude);
            temperatureInKelvins = weather.getDouble("temperatureInKelvins");
            cloudinessPercentage = weather.getDouble("cloudinessPercentage");
            backgroundLightVolume = weather.getDouble("backgroundLightVolume");
        }
        String updateReason = "N/A";
        String modified = null;
        String insertMessageSQL =
            """
            INSERT INTO messages (recordIdentifier, recordDescription, recordPayload, shift, recordRightAscension,
            recordDeclination, recordOwner, observatoryName, latitude, longitude, recordTimeReceived,
            temperatureInKelvins, cloudinessPercentage, backgroundLightVolume, updateReason, modified, username)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)        
            """;
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(insertMessageSQL)) {
            pstmt.setString(1, recordIdentifier);
            pstmt.setString(2, recordDescription);
            pstmt.setString(3, recordPayload);
            pstmt.setString(4, shift);
            pstmt.setString(5, recordRightAscension);
            pstmt.setString(6, recordDeclination);
            pstmt.setString(7, recordOwner);
            pstmt.setString(8, observatoryName);
            pstmt.setDouble(9, observatoryLatitude);
            pstmt.setDouble(10, observatoryLongitude);
            pstmt.setString(11, getCurrentUtcTimestamp());
            pstmt.setDouble(12, temperatureInKelvins);
            pstmt.setDouble(13, cloudinessPercentage);
            pstmt.setDouble(14, backgroundLightVolume);
            pstmt.setString(15, updateReason);
            pstmt.setString(16, modified);
            pstmt.setString(17, authenticatedUser);
    
            pstmt.executeUpdate();
        }

    }
    public static String getCurrentUtcTimestamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        return now.format(formatter);
    }
    public ResultSet getMessages() throws SQLException {
        String selectMessagesSQL = 
            """
        SELECT * FROM messages
        """;
        Statement stmt = databaseConnection.createStatement();
        return stmt.executeQuery(selectMessagesSQL);
    }
    public void open(String dbName) throws SQLException {
        String database = "jdbc:sqlite:" + dbName;
        File dbFile = new File(database);
        boolean fileExists = dbFile.exists() && !dbFile.isDirectory();
        databaseConnection = DriverManager.getConnection(database);

        System.out.println("Opened database successfully");
        if(databaseConnection != null && !fileExists) {
            initializeDatabase();
        }
    }
    private void initializeDatabase() throws SQLException{

        if(databaseConnection != null){
            Statement statement = databaseConnection.createStatement();
            String createUser = "create table if not exists users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(50) NOT NULL," +
                "email VARCHAR(50) NOT NULL," +
                "userNickname VARCHAR(50) NOT NULL)";
            statement.executeUpdate(createUser);
            String createMessage = "create table if not exists messages (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "recordIdentifier TEXT NOT NULL," +
            "recordDescription TEXT," +
            "recordPayload TEXT," +
            "shift TEXT," +
            "recordRightAscension TEXT," +
            "recordDeclination TEXT," +
            "recordOwner TEXT," +
            "observatoryName TEXT," +
            "latitude REAL," +
            "longitude REAL," +
            "recordTimeReceived TEXT NOT NULL," +
            "temperatureInKelvins REAL," +
            "cloudinessPercentage REAL," +
            "backgroundLightVolume REAL," +
            "updateReason TEXT DEFAULT 'N/A'," +
            "modified TEXT," + 
            "username TEXT NOT NULL," +
            "FOREIGN KEY (username) REFERENCES users(username))";
            statement.executeUpdate(createMessage);
            statement.close();
            System.out.println("Log - Database initialized");
        }
        else{
            System.out.println("Log - Connection is null");
        }
    }
    public synchronized boolean addUser(JSONObject user) throws SQLException {
        byte bytes[] = new byte[13];
        random.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;
        String username = user.getString("username");
        String password = user.getString("password");
        String hashedPassword = Crypt.crypt(password, salt);
        String email = user.getString("email");
        String userNickname = user.getString("userNickname");
        String insertUserSQL = "INSERT INTO users (username, password, email, userNickname) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(insertUserSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, email);
            pstmt.setString(4, userNickname);
            pstmt.executeUpdate();
            return true;
        }
    }
    public boolean checkUser(String username, String password) throws SQLException {
        String selectUserSQL = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(selectUserSQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (hashedPassword.equals(Crypt.crypt(password, hashedPassword))) {
                    return true;
                }
            }
        }
        return false;
    }

    public ResultSet executeQuery(String sql, List<Object> parameters) throws SQLException {
        PreparedStatement pstmt = databaseConnection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            pstmt.setObject(i + 1, parameters.get(i));
        }
        return pstmt.executeQuery();
    }
    
    public boolean isMessageOwner(int id, String username) throws SQLException {
        String selectMessageOwnerSQL = "SELECT username FROM messages WHERE id = ?";
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(selectMessageOwnerSQL)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username").equals(username);
            }
        }
        return false;
    }

    public JSONObject getMessage(int id) throws SQLException {
        String selectMessageSQL = "SELECT * FROM messages";
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(selectMessageSQL)) {
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                JSONObject message = new JSONObject();
                message.put("id", rs.getInt("id"));
                System.out.println("I am getting" + rs.getInt("id"));
                message.put("recordIdentifier", rs.getString("recordIdentifier"));
                message.put("recordDescription", rs.getString("recordDescription"));
                message.put("recordPayload", rs.getString("recordPayload"));
                message.put("recordRightAscension", rs.getString("recordRightAscension"));
                message.put("recordDeclination", rs.getString("recordDeclination"));
                message.put("recordOwner", rs.getString("recordOwner"));
                message.put("recordTimeReceived", rs.getString("recordTimeReceived"));

                String updateReason = rs.getString("updateReason");
                message.put("updateReason", updateReason != null ? updateReason : "N/A");
                String modified = rs.getString("modified");
                message.put("modified", modified != null ? modified : null);


                if(rs.getString("observatoryName") != null){
                    JSONArray observatory = new JSONArray();
                    JSONObject observatoryObject = new JSONObject();
                    observatoryObject.put("observatoryName", rs.getString("observatoryName"));
                    observatoryObject.put("latitude", rs.getDouble("latitude"));
                    observatoryObject.put("longitude", rs.getDouble("longitude"));
                    observatory.put(observatoryObject);
                    message.put("observatory", observatory);
                }
                return message;
            }
            return null;
        }
    }

    public boolean updateMessage(int id, JSONObject updatedMessage, String username) throws SQLException{
        String getOwnerSQL = "SELECT COUNT(*) FROM messages WHERE id = ? AND username = ?";
        boolean Owner = false;
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(getOwnerSQL)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Owner = rs.getInt(1) > 0;
            }
        }

        if(!Owner){
            return false;
        }
        String updateReason = "N/A";
        if(updatedMessage.has("updateReason")) {
            String providedReason = updatedMessage.getString("updateReason");
            if(providedReason != null && !providedReason.isEmpty()) {
                updateReason = providedReason;
            }
        }
        String update = """
                UPDATE messages
                SET recordIdentifier = ?,
                recordDescription = ?,
                recordPayload = ?,
                recordRightAscension = ?,
                recordDeclination = ?,
                updateReason = ?,
                modified = ?
                WHERE id = ?
                """;
        try (PreparedStatement pstmt = databaseConnection.prepareStatement(update)) {
            pstmt.setString(1, updatedMessage.getString("recordIdentifier"));
            pstmt.setString(2, updatedMessage.getString("recordDescription"));
            pstmt.setString(3, updatedMessage.getString("recordPayload"));
            pstmt.setString(4, updatedMessage.getString("recordRightAscension"));
            pstmt.setString(5, updatedMessage.getString("recordDeclination"));
            pstmt.setString(6, updateReason);
            pstmt.setString(7, getCurrentUtcTimestamp());
            pstmt.setInt(8, id);
            int updated = pstmt.executeUpdate();
            System.out.println("Updated: " + updated);
            if(updated == 0){
                return false;
            }
        }
        if(updatedMessage.has("observatory")){
            String updateObservatory = """
                UPDATE messages
                SET observatoryName = ?,
                latitude = ?,
                longitude = ?
                WHERE id = ?
                """;
            JSONArray observatoryArray = updatedMessage.getJSONArray("observatory");
            JSONObject observatory = observatoryArray.getJSONObject(0);
            try(PreparedStatement pstmt = databaseConnection.prepareStatement(updateObservatory)){
                pstmt.setString(1, observatory.getString("observatoryName"));
                pstmt.setDouble(2, observatory.getDouble("latitude"));
                pstmt.setDouble(3, observatory.getDouble("longitude"));
                pstmt.setInt(4, id);
                pstmt.executeUpdate();
            }
                
        }
        return true;
    }


}
