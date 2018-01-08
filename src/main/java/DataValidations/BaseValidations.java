package DataValidations;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ServiceAutomation.ServiceTests;
import TestScripts.commonTestClass;

public class BaseValidations {
	
	private static final Logger Log = Logger.getLogger(BaseValidations.class);
	
	public static List<LinkedHashMap<String, String>> getDataFromInputSheet(String inputFilePath) throws Exception {
		
		try{
			//reading the input excel as whole
			List<LinkedHashMap<String, String>> dataFromInputExcel = XlUtil.readExcel(inputFilePath);
			
			dataFromInputExcel = performWebserviceActions(dataFromInputExcel);
			return dataFromInputExcel;
			
		}catch(Exception e){
			Log.error("Exception occured while reading data from input Excel");
			throw e;
		}
	}
	
	//This method we are extracting Input parameters from the String with Comma-Separated values
	private static List<LinkedHashMap<String, String>> performWebserviceActions(
			List<LinkedHashMap<String, String>> dataFromInputExcel) throws Exception{
		try{

			for (int i=0; i<dataFromInputExcel.size();i++){
				LinkedHashMap<String, String> record = dataFromInputExcel.get(i);
				String comments = (record.get("COMMENTS")!=null ? record.get("COMMENTS"):"");
				StringBuffer updateComments = new StringBuffer(comments);
				updateComments = updateComments.append(System.lineSeparator());
				
				String inputParams = record.get("INPUT_PARAMETERS");
				List<String> sortedInputList = Arrays.asList(inputParams.split(","));
				HashMap<String, String> result = null;
				//checking if the request input is Soap or Rest
				switch(record.get("SOAP_SERVICE")){
				
				case "Y":
					result = new commonTestClass().commonTestForInitiatingSoapServices(sortedInputList, record.get("WEBSERVICE"), updateComments);
					break;
					
				case "N":
					result = new commonTestClass().commonTestForInitiatingRestfulServices(sortedInputList, record, updateComments);
					break;
				default:
					updateComments.append("SOAP_SERVICE paramter should be either Y or N. If you are using rest service mark it as N");
					updateComments.append(System.lineSeparator());
				}
				
				if(result == null){
					record.put("REQUEST", null);
					record.put("RESPONSE", null);
					record.put("RESULT_SET", null);
					record.put("COMMENTS", updateComments.toString());
				}else{
					record.put("REQUEST", result.get("REQUEST"));
					result.remove("REQUEST");
					record.put("RESPONSE", result.get("RESPONSE"));
					result.remove("RESPONSE");
					record.put("RESULT_SET", result.toString());
					record.put("COMMENTS", updateComments.toString());
				}				
			}
		}catch (Exception e){
			Log.info("Exception occured: " +e);
		}
		return dataFromInputExcel;
	}

}
