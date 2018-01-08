package DataValidations;

import java.util.LinkedHashMap;

public class FindNearbyPostalCodesJson {
	
	LinkedHashMap<String, String> serviceParams = new LinkedHashMap<String, String>();
	
	public FindNearbyPostalCodesJson(){
		serviceParams.put("POSTALCODE", "");
		serviceParams.put("COUNTRY", "");
		serviceParams.put("RADIUS", "");
		serviceParams.put("USERNAME", "");
	}
	
	public LinkedHashMap<String, String> getServiceParams(){
		return serviceParams;
	}
}
