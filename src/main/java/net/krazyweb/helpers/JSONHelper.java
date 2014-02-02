package net.krazyweb.helpers;

import javax.json.JsonObject;

public class JSONHelper {
	
	public static String getString(final JsonObject o, final String key, final String defaultValue) {
		
		if (o.containsKey(key)) {
			return o.getString(key);
		}
		
		return defaultValue;
		
	}
	
}