package com.o3.tests;

import org.json.JSONObject;
import org.json.JSONPropertyName;

public class User {
	private String username;
	private String password;
	private String email;
	private String nickname;

	public User(String username, String password, String email, String nickname) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.nickname = nickname;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return String.format("User: %s, Password: %s", username, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof User)) {
			return false;
		}

		User user = (User) obj;
		return username.equals(user.getUsername()) && password.equals(user.getPassword());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + username.hashCode();
		result = 31 * result + password.hashCode();
		return result;
	}

	@JSONPropertyName("userNickname")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nick) {
		this.nickname = nick;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public JSONObject toJson() {
		return new JSONObject(this);
	}

	public String toJsonString() {
		return toJson().toString();
	}
}
