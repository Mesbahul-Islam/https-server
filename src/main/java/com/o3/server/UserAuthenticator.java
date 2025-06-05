package com.o3.server;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator{
    private MessageDatabase db;
    public UserAuthenticator(String realm){
        super(realm);
        db = MessageDatabase.getInstance();
    }

    @Override
    public boolean checkCredentials(String user, String pwd){
        boolean userIsValid;
        try {
            userIsValid = db.checkUser(user, pwd);
        } catch (SQLException e) {
            return false;
        }
        return userIsValid;
    }
    public boolean addUser(String userName, String password, String email, String userNickname) throws JSONException, SQLException {
        boolean result = db.addUser(new JSONObject().put("username", userName).put("password", password).put("email", email).put("userNickname", userNickname));
        if (!result) {
            return false;
        }
        return result;
    }
}
