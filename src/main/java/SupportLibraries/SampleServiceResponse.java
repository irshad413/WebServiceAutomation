package SupportLibraries;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.cxf.api.CurrencyConvertor.ConversionRateResponse;

public class SampleServiceResponse extends WebServicesHelper implements GenericResponse{
	
	@Override
	public HashMap<String, String> getServiceStatus(String resp){
		HashMap<String, String> target = new HashMap<String, String>();
		if(StringUtils.containsIgnoreCase(resp, "<soap:Fault>")){
			target.put("FAULTY_ID", resp.substring(resp.indexOf("<soap:Fault>")+"<soap:Fault>".length(), resp.indexOf("</soap:Fault>")));
		}
		else{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			Document soapResp;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(resp));
				soapResp = dBuilder.parse(is);
				
				soapResp.getDocumentElement().normalize();
				
				NodeList nodeList = soapResp.getDocumentElement().getChildNodes();
		        if (nodeList!=null && nodeList.getLength()>0 ){
		            for(int i=0 ; i < nodeList.getLength() ; i++){
		                Node node = nodeList.item(i); //Take the node from the list
		                NodeList valueNode = node.getChildNodes(); // get the children of the node
		                if(node.hasChildNodes()){
		                	NodeList childNodeList = node.getChildNodes();
		                	for(i=0; i<childNodeList.getLength();i++){
		                		String value = (childNodeList.item(0)!=null) ? childNodeList.item(0).getTextContent() : "";
				                target.put(childNodeList.item(0).getNodeName(), value);
		                	}
		                }else{
		                	String value = (valueNode.item(0)!=null) ? valueNode.item(0).getTextContent() : "";
			                target.put(valueNode.item(0).getNodeName(), value);
		                }
		            }
		        }else{
		        	throw new Exception("Error extracting node information from response");
		        }
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
		return target;
	}
	

}
