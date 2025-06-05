package com.o3.tests;

import java.util.Collections;
import java.util.Map;
import java.util.List;

public class Constants {
	public static final String SERVER_DEFAULT_ADDRESS_HTTP = "http://localhost:8001";
	public static final String SERVER_DEFAULT_ADDRESS_HTTPS = "https://localhost:8001";
	public static final String SERVER_DEFAULT_ADDRESS_KEY = "SERVER_ADDRESS";
	public static final Map<String, String> ENVIRONMENT = Collections.unmodifiableMap(System.getenv());
	public static final String IMAGE_DATA_REGEX = "data:image\\/\\w+;base64,([a-zA-Z0-9\\/\\+=])+";
	public static final String IMAGE_DATA_PLACEHOLDER = "data:image/...";
	public static final Map<Integer, String> JSON_KEYMAP = Collections.unmodifiableMap(Map.of(
			0b00001, "recordIdentifier",
			0b00010, "recordDescription",
			0b00100, "recordPayload",
			0b01000, "recordDeclination",
			0b10000, "recordRightAscension"));

	public static final List<CipherMessage> CIPHERTEXT = Collections.unmodifiableList(List.of(
			new CipherMessage(13, "Pbnji? V gnc pbnji äåpr. Duri yäpxrq zr vå n bääz. N beoorb bääz! N beoorb bääz gvdu bndc,nåq bndc znxr zr pbnji."),
			new CipherMessage(3, "Wklv lv d whvw phvvdjh."),
			new CipherMessage(7, "P ohöl h zljylå yljpwl mvy jopjrlu uännlåz åohå P't uvå zäwwvzlk åv ålss hucivkc iljhäzl på'z tc zljylå yljpwl iäå p'ss ålss cvä hucahc på'z uvå åohå ohyk cvä qäzå ullk zvtl jopjrlu uännlåz huk h mycpun whu mpyzå cvä åhrl vul uännlå huk åolu cvä jhåjo pu åol whst vm cväy ohuk åolu cvä vwlu cväy ohuk åol uännlå pz nvul zv aohå cvä kv åolu pz cvä wäå huvåoly uännlå pu åol mycpun whu huk åolu cvä jsvzl cväy lclz huk cvä pthnpul åohå åol mpyzå uännlå pz pu cväy ohuk huk cvä vwlu cväy ohuk huk åol uännlå pz nvul zv åol mycpun whu pz uva ltwåc vm uännlåz zv åolu cvä vwlu cväy ohuk hnhpu huk h ula uännlå hwwlhyz zv åolu cvä jsvzl cväy ohuk hnhpu huk åol uännlå pz nvul zv cvä vwlu cväy ohuk huk h ula uännlå hwwlhyz huk cvä jsvzl cväy ohuk hnhpu huk åol uännlå pz nvul zv cvä vwlu cväy ohuk hnhpu huk hnhpu huk hnhpu huk h ula uännlå hwwlhyz huk cvä jsvzl cväy ohuk hnhpu huk hnhpu huk åol uännlåz rllw hwwlhypun huk cvä qäzå rllw vwlupun huk jsvzpun cväy ohukz huk åol mycpun whu rllwz nlååpun ltwåply huk ltwåply huk löluåähssc åol uännlåz hyl nvul."),
			new CipherMessage(1, "bcd efgh ijk lmn opq rst uvw xyz."),
			new CipherMessage(26, "zyx wvu tsr qpo nml kji hgf edc ba."),
			new CipherMessage(845, "Soeö kyöw xleow jsv xli piekyi xmtw: uymx, yrmrwxepp, so, åmpp hs."),
			new CipherMessage(161, "Jxua jyää lbh äuqea? Jxua jyää lbh äuqea gxqg lbhe qsgybaf xqiu sbafudhuasuf‽")
	));

	private Constants() {
	}
}
