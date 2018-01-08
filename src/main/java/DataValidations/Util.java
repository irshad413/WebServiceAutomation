package DataValidations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

public class Util {
	
	public static final Pattern pattern = Pattern.compile("\\{\\d*\\}");
	
	public String getValueFromConfigProperties(String key){
		String value = "";
		try {
			Properties prop = new Properties();
			InputStream in = getClass().getClassLoader().getResourceAsStream("Config.properties");
			prop.load(in);
			value = prop.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public String getRelativeValueFromConfigProperties(String key){
		String value = "";
		try {
			Properties prop = new Properties();
			InputStream in = getClass().getClassLoader().getResourceAsStream("Config.properties");
			prop.load(in);
			Collection<Object> keySet = prop.keySet();
			List<String> keyList = new ArrayList<String>();
			for(Object keys: keySet) {
				keyList.add(keys.toString());
			}
			for (int i=0; i<keyList.size();i++){
				if (StringUtils.contains(key,keyList.get(i))){
					key = keyList.get(i);
					break;
				}
			}
			value = prop.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public static long paramCount(String requestXml){
		
		return pattern.splitAsStream(requestXml).count();
	}
	
	//to convert a Json Object to Map
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	    Map<String, Object> retMap = new HashMap<String, Object>();

	    if(json != null) {
	        retMap = toMap(json);
	    }
	    return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();
	    Set keysItr = object.keySet();
	    for (Object key : keysItr) {
	    	 Object value = object.get(key);

		        if(value instanceof JSONArray) {
		            value = toList((JSONArray) value);
		        }

		        else if(value instanceof JSONObject) {
		            value = toMap((JSONObject) value);
		        }
		        map.put(key.toString(), value);
	    }
	    return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
}
