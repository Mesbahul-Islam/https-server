package com.o3.tests;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SearchQuery {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private String nickname;
	private ZonedDateTime before;
	private ZonedDateTime after;
	private String identification;

	public SearchQuery(String nickname, ZonedDateTime before, ZonedDateTime after, String identification) {
		this.nickname = nickname;
		this.before = before;
		this.after = after;
		this.identification = identification;
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		if (nickname != null) {
			map.put("nickname", nickname);
		}
		if (before != null) {
			map.put("before", before.withZoneSameInstant(ZoneOffset.UTC).format(FORMATTER));
		}
		if (after != null) {
			map.put("after", after.withZoneSameInstant(ZoneOffset.UTC).format(FORMATTER));
		}
		if (identification != null) {
			map.put("identification", identification);
		}
		return map;
	}

	public String toQueryString() {
		StringBuilder sb = new StringBuilder("?");
		Map<String, String> map = toMap();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		return sb.toString();
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public ZonedDateTime getBefore() {
		return before;
	}

	public void setBefore(ZonedDateTime before) {
		this.before = before;
	}

	public ZonedDateTime getAfter() {
		return after;
	}

	public void setAfter(ZonedDateTime after) {
		this.after = after;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}
}
