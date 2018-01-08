package TestScripts;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import DataValidations.Util;
import SupportLibraries.WebServicesHelper;

public class sendRequestToRestfulService {
	//this where actual request call is happening
	
	//service call for POST method
	public Map<String, Object> sendPostRequestToService(String webservice, String url, List<String> inputList, 
			String method, StringBuffer comments)throws JSONException, ParseException, Exception{
		String input = "", output="";
		String serviceClass = "DataValidations."+webservice;
		try{
			Class<?> testClass = Class.forName(serviceClass);
        	Object testService = testClass.newInstance();
        	Method testMethod = testService.getClass().getMethod("getServiceParams");
        	HashMap<String, String>serviceParams = (HashMap<String, String>) testMethod.invoke(testService);
        	
        	for(int i=0; i<inputList.size();i++){
        		if(i<serviceParams.size()){
        			serviceParams.put((String) serviceParams.keySet().toArray()[i], inputList.get(i));
        		}else{
        			serviceParams.put("ELEMENT"+String.valueOf(i) , inputList.get(i));
        		}
        	}
        	
        	input = WebServicesHelper.mapToJsonConversion(serviceParams);
        	output = new WebServicesHelper().callRestServiceWithJson(url,input, method);
        	
		}catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
		JSONParser parser = new JSONParser();
        
        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(output);
        
        Map<String, Object>finalOutput = Util.jsonToMap(jsonObject);
        finalOutput.put("REQUEST", input);
        finalOutput.put("RESPONSE",output);
		return finalOutput;
	}
	
	//Service call for GET method
	public Map<String, Object> sendGetRequestToService(String url, List<String> inputList, 
			String method, StringBuffer comments)throws JSONException, ParseException, Exception{
		String input = "", output="";
		
		try{
			//replace input parameters
			if(Util.paramCount(url) != inputList.size()){
				comments.append("Mismatch in number of input parameters");
				comments.append(System.lineSeparator());
				comments.append("Expected input params "+Util.paramCount(url)+", actual params provided "+inputList.size());
				throw new Exception("Mismatch in number of input paramaters");
			}else{
				String newUrl = MessageFormat.format(url, inputList.toArray());
				output = new WebServicesHelper().callRestFulServiceWithUrl(newUrl, method);
			}
        	
		}catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
		JSONParser parser = new JSONParser();
        
        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(output);
        
        Map<String, Object>finalOutput = Util.jsonToMap(jsonObject);
        finalOutput.put("REQUEST", input);
        finalOutput.put("RESPONSE",output);
		return finalOutput;
	}
}
