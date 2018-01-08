package TestScripts;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;

import DataValidations.Util;
import SupportLibraries.GenericServiceFactory;
import SupportLibraries.SampleServiceRequest;
import SupportLibraries.SampleServiceResponse;
import SupportLibraries.WebServicesHelper;

public class commonTestClass extends WebServicesHelper{
	
	public HashMap<String,String> commonTestForInitiatingSoapServices(List<String> inputList, String requestName, StringBuffer comments) throws Exception{
		HashMap<String, String> responseMap = null;
		
		try{
			
			final GenericServiceFactory<SampleServiceRequest, SampleServiceResponse> factory = new GenericServiceFactory();
			
			String reqTemplatePath = "sampleXmls/"+requestName+".xml";
			File inputSample = new File(getClass().getClassLoader().getResource(reqTemplatePath).getFile());
			String inputFile = FileUtils.readFileToString(inputSample);
			//String inputFile = new String(Files.readAllBytes(Paths.get(reqTemplatePath)));
			if(StringUtils.countMatches(inputFile, "?")!= inputList.size()){
				comments.append("Mismatch in number of input parameters");
				comments.append(System.lineSeparator());
				comments.append("Expected input params "+StringUtils.countMatches(inputFile, "?")+
						", actual params provided "+inputList.size());
				throw new Exception("Mismatch in number of input paramaters");
			}else{
				SampleServiceRequest soapRequest = factory.buildRequestFromXmlString(SampleServiceRequest.class, reqTemplatePath);
				
				String newRequest = soapRequest.updateXmlWithInputData(inputList, inputFile);
				comments.append("Number of input params matched, executing test");
				comments.append(System.lineSeparator());
				String soapResponse = callServiceWithUrl(newRequest, new Util().getRelativeValueFromConfigProperties(requestName));
				
				//SampleServiceResponse soapResp = factory.buildResponse(SampleServiceResponse.class);
				responseMap = new SampleServiceResponse().getServiceStatus(soapResponse);
				responseMap.put("REQUEST", newRequest);
				responseMap.put("RESPONSE", soapResponse);
			}
			
		}catch (Exception e){
			comments.append("Received an Exception");
			comments.append(System.lineSeparator());
			comments.append(e.getMessage());
			e.getMessage();
		}
		
		return responseMap;
	}
	
	public HashMap<String,String> commonTestForInitiatingRestfulServices(List<String> inputList, LinkedHashMap<String, String> record, StringBuffer comments) throws Exception{
		Map<String, Object> responseMap = null;
		HashMap<String, String> responseData = new HashMap<String, String>();
		
		try{
			comments.append("Executing rest service for resource "+record.get("RESOURCE"));
			comments.append(System.lineSeparator());
			
			String getMockUrl = new Util().getRelativeValueFromConfigProperties(record.get("WEBSERVICE"));
			getMockUrl = getMockUrl.replace("resource", record.get("RESOURCE"));
			
			//first we are splitting string to 2 parts, so we can update the parameters for GET method
			//for POST or PUT request we can exclude it
			String url = getMockUrl.substring(0, getMockUrl.indexOf('?'));
			String params = getMockUrl.substring(getMockUrl.indexOf('?'));
			/*Pattern pattern = Pattern.compile("?*");
			Matcher matcher = pattern.matcher(getMockUrl);
			String url = getMockUrl.substring(0, matcher.start());
			String params = getMockUrl.substring(matcher.end());*/
			
			//we need to check request method and call appropriate service
			switch(record.get("METHOD")){
			
			case "GET":
				responseMap = new sendRequestToRestfulService().sendGetRequestToService(getMockUrl, inputList, record.get("METHOD"), comments);
				break;
			case "POST":
				responseMap = new sendRequestToRestfulService().sendPostRequestToService(record.get("WEBSERVICE"), url, inputList, record.get("METHOD"), comments);
				break;
			default: 
				comments.append(record.get("METHOD")+" is not recognized as valid opertaion, please provide a valid operation");
				comments.append(System.lineSeparator());	
			}
			//sending only Request and Response in HashMap
			//Response is present as JSON object in 'Response Map', we can use it for further validations
			responseData.put("REQUEST", responseMap.get("REQUEST").toString());
			responseData.put("RESPONSE", responseMap.get("RESPONSE").toString());
		}catch (Exception e){
			comments.append("Received an Exception");
			comments.append(System.lineSeparator());
			comments.append(e.getMessage());
			e.getMessage();
		}
		
		return responseData;
	}
}
