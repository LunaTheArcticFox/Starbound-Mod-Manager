package net.krazyweb.helpers;

import com.eclipsesource.json.JsonObject;

public class JSONHelper {
	
	public static String getString(final JsonObject o, final String key, final String defaultValue) {
		
		if (o.get(key) != null) {
			return o.get(key).asString();
		}
		
		return defaultValue;
		
	}
	
}