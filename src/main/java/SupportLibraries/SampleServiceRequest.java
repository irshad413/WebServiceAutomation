package SupportLibraries;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SampleServiceRequest extends WebServicesHelper implements GenericRequest {
	
	private StringBuffer requestXml = new StringBuffer();
	
	public SampleServiceRequest(String sampleXml){
		requestXml = new StringBuffer(sampleXml);
	}
	
	@Override
	public String updateXmlWithInputData(List<String> testData, String sampleXml){
		overrideSampleXmlWithInputData(testData);
		return requestXml.toString();
	}
	

	private void overrideSampleXmlWithInputData(List<String> testData) {
		int i=0;
		while (requestXml.toString().contains("?")){
			requestXml = new StringBuffer(requestXml.toString().replaceFirst("\\?", testData.get(i)));
			i++;
		}
		//MessageFormat.format(requestXml, testData);
	}

	public String getCompletedRequest(){
		return requestXml.toString();
	}
	
	


}
