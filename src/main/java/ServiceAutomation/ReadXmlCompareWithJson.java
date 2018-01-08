package ServiceAutomation;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.istack.logging.Logger;

public class ReadXmlCompareWithJson {
	//reading xml file and comparing with JSON
	private static final Logger Log = Logger.getLogger(ServiceTests.class);
	
	public static void main( String[] args ) throws Exception
    {
		boolean result = false;
		File inputFileXml = new File("./inputData_Sample\\inputFields.xml");
		
		String inputFileInString = FileUtils.readFileToString(inputFileXml);
		HashMap<String, String> inputValuesFromXml = new HashMap<String, String>();
		HashMap<String, String> valuesFromJson = new HashMap<String, String>();
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			Document soapResp;
			dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(inputFileInString));
			soapResp = dBuilder.parse(is);
			
			soapResp.getDocumentElement().normalize();
			
			//reading all the input fields that should be read from the XML
			List<String> inputParams = Arrays.asList("RegFeeAmount","AdminFeeAmount","TACFeeAmount","STRServiceFeeAmount","DemandID");
			
			for(int i=0;i<inputParams.size();i++){
				Node type = null;
				
				NodeList xmlNodeList = soapResp.getElementsByTagName("b:"+inputParams.get(i));
				if(xmlNodeList.getLength()!= 0){
					type = xmlNodeList.item(0).getFirstChild();
					inputValuesFromXml.put(inputParams.get(i),type.getTextContent());
				}else{
					NodeList nodeList = soapResp.getElementsByTagName("b:Amount");
					for(int j=0;j<nodeList.getLength();j++){
						if(nodeList.item(j).getTextContent().contains(inputParams.get(i))){
							String value = nodeList.item(j).getTextContent();
							int index = value.indexOf("\n", 1);
							inputValuesFromXml.put(inputParams.get(i), value.substring(1, index).trim());
							break;
						}
					}		
				}
			}
			
			//Now all data from XML is read and is available in HashMap
			File inputFileJson = new File("./inputData_Sample\\jsonResponse.json");
			
			String inputStringJson = FileUtils.readFileToString(inputFileJson);
			JSONObject jsonObject = new JSONObject(inputStringJson);
			
			String demandSection = jsonObject.getJSONArray("records").getJSONObject(0).get("demand").toString();
			JSONObject jsonObject1 = new JSONObject(demandSection);
			
			for(int x=0; x < inputParams.size();x++){
				String value = "";
				if(jsonObject1.keySet().contains(inputParams.get(x))){
				 value = jsonObject1.get(inputParams.get(x)).toString();
					valuesFromJson.put(inputParams.get(x), value);
				}else{
					for(int y=0;y<jsonObject1.getJSONArray("amounts").length();y++){
						if(jsonObject1.getJSONArray("amounts").getJSONObject(y).get("type").equals(inputParams.get(x))){
							value = jsonObject1.getJSONArray("amounts").getJSONObject(y).get("amount").toString();
							valuesFromJson.put(inputParams.get(x), value);
							break;
						}
					}
				}
			}
			
			/*String demandId = jsonObject1.get("demandId").toString();
			
			@SuppressWarnings("resource")
            String content = new Scanner(new File("./inputData_Sample\\jsonResponse.json")).useDelimiter("\\Z").next();

			JSONObject jsonObject = new JSONObject(inputStringJson);
			
			JSONArray cells = (JSONArray) jsonObject.get("records");
	        Iterator<Object> iterator = cells.iterator();
	        
			String demandId2 = JsonPath.read(new JSONObject(content).toString(),"$.records[0].demand.demandId").toString();
			valuesFromJson.put("demandId", demandId2);
			
	        String value = JsonPath.read(new JSONObject(content).toString(),"$.records.demand.amounts.amount").toString();*/
			
			//comparing values of both HashMaps are same
			boolean matchResult = false;
			for(Entry<String, String> entry: valuesFromJson.entrySet()){
				String key = entry.getKey();
				matchResult = inputValuesFromXml.get(key).equals(entry.getValue()) ? true : false ;
				if(!matchResult){
					Log.info("Input and OutPut do not match");
					Log.info("value from input XML is "+inputValuesFromXml.get(key)+" value in JSON is "+entry.getValue());
					break;
				}
				
			}
			result = matchResult;
			System.out.println("final result is " +result);
			
		}catch(Exception e){
			Log.info("Exception thrown" +e);
		}
		Log.info("Execution is complete with out any issues");
			
    }

}
