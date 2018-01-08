package SupportLibraries;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import DataValidations.Util;

/** WebServicesHelper to be inherited by each TestNg test. 
 * Setup end point URL, build request/receive response
 * Can be used for Soap/Rest service testing
 * 
 * @author Irshad Ali
 */
public class WebServicesHelper {
	//protected static final String redColor = "#FE2E2E";
	public static String soapTemplate = "SoapTemplate.txt";
	//private static HashMap<String, String> config_properties = new HashMap<String, String>();
	
	public final static ObjectMapper MAPPER = new ObjectMapper();
	
	/** should be used when Soap body and Soap template are stored differently
	 * This approach is helpful only when service structure is not yet finalized
	 * @param reqBody
	 * @return
	 * @throws IOException
	 */
	protected static String createFullRequest(String reqBody) throws IOException{
		String soapBody = readFile(getSoapTemplatePath(soapTemplate), StandardCharsets.UTF_8);
		int offset = soapBody.indexOf("<SOAP: Body>")+"<SOAP: Body>".length();
		
		String str = new StringBuilder(soapBody).insert(offset, reqBody).toString();
		
		return str;
	}
	
	/**
	 * 
	 * @param path -- Full path of the file
	 * @param encoding -- encoding format i.e. StandardCharsets.UTF_8 in general
	 * @return
	 * @throws IOException
	 */
	private static String readFile(String path , Charset encoding) throws IOException{
		byte [] encoded = Files.readAllBytes(Paths.get(path));
		return new String (encoded, encoding);
	}

	private static String getSoapTemplatePath(String soapTemplate2) {
		//need to update script for fetching soap template and requests separately
		return null;
	}
	
	//converting document to String
	
	public String writeToString(Document _doc) throws TransformerException{
		StringWriter stw = new StringWriter();
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		serializer.transform(new DOMSource(_doc), new StreamResult(stw));
		return stw.toString();
	}
	
	public String callServiceWithUrl(String soapReq, String service_url) throws IOException, ParserConfigurationException, SAXException
	{
		return callServiceToString(service_url, soapReq);
	}
	
	public String callRestServiceWithUrl(String rest_url, String input, String method) throws Exception{
		return callRestServiceWithJson(rest_url, input, method);
	}
	
	/**
	 * 
	 * @param service_url
	 * @param soapReq  - completely formed Soap request for the service
	 * @return response  - service response
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String callServiceToString(String service_url, String soapReq) throws IOException, ParserConfigurationException, SAXException
	{
		try{
			disableSSLVerification();
			URL url = new URL (service_url);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setConnectTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("connectTimeout")));
			httpConn.setReadTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("readTimeout")));
			/*String credentials = "admin:password";
			String basicAuth = "Basic "+ new String(new Base64().encode(credentials.getBytes()));
			httpConn.setRequestProperty("Authorization", basicAuth);*/
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[soapReq.length()];
			buffer = soapReq.getBytes();
			bout.write(buffer);
			
			byte[] b = bout.toByteArray();
			
			httpConn.setRequestProperty("Content-length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-type", "text/xml; charset=utf-8");
			/*httpConn.setRequestProperty("SOAPAction", null);*/
			httpConn.setRequestMethod("POST");
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			
			long startTime = System.nanoTime();
			OutputStream out = httpConn.getOutputStream();
			//write the content of the request to Output stream of HTTP connection
			out.write(b);
			out.close();
			
			//Read the response
			InputStreamReader isr = (httpConn.getResponseCode() == 200 || httpConn.getResponseCode() == 404)
					? new InputStreamReader (httpConn.getInputStream()) 
					: new InputStreamReader (httpConn.getErrorStream());
			double serviceExeTime = TimeUnit.MILLISECONDS.convert(System.nanoTime()- startTime, TimeUnit.NANOSECONDS);
			
			DecimalFormat df = new DecimalFormat("#.##");
			//should be able to log the service execution time in the logs or also can return variable if needed
			
			BufferedReader resp = new BufferedReader(isr);
			String responseContent = resp.lines().collect(Collectors.joining(System.lineSeparator()));
			resp.close();
			return responseContent;
			
		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 
	 * @param serviceUrl  - URL that is being hit
	 * @param input  - JSON request for the service URL
	 * @return response  - JSON response
	 * @throws Exception
	 */
	public String callRestServiceWithJson(String serviceUrl, String input, String method) throws Exception{
		try{
			disableSSLVerification();
			URL url = new URL (serviceUrl);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setConnectTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("connectTimeout")));
			httpConn.setReadTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("readTimeout")));
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[input.length()];
			buffer = input.getBytes();
			bout.write(buffer);
			byte[] b = bout.toByteArray();
			
			//This should be the token for authorizing rest service
			//When token is generated dynamically, new method should be added to create token based on input
			/*String basicAuth = "";
			httpConn.setRequestProperty("Authorization", basicAuth);*/
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setRequestProperty("Content-length", String.valueOf(b.length));
			httpConn.setRequestProperty("Content-type", "application/json");
			httpConn.setRequestMethod(method);
			
			OutputStream out = httpConn.getOutputStream();
			//write the content of the request to Output stream of HTTP connection
			out.write(b);
			out.flush();
			long startTime = System.nanoTime();
			//Read the response
			InputStreamReader isr = httpConn.getResponseCode() == 200 
					? new InputStreamReader (httpConn.getInputStream()) 
					: new InputStreamReader (httpConn.getErrorStream());
			double serviceExeTime = TimeUnit.MILLISECONDS.convert(System.nanoTime()- startTime, TimeUnit.NANOSECONDS);
					
			DecimalFormat df = new DecimalFormat("#.##");
			
			//should be able to log the service execution time in the logs or also can return variable if needed
			BufferedReader resp = new BufferedReader(isr);
			String responseContent = resp.lines().collect(Collectors.joining(System.lineSeparator()));
			resp.close();
			return responseContent;
			
		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 
	 * @param serviceUrl
	 * @param requestMethod
	 * @return
	 * @throws Exception
	 */
	public String callRestFulServiceWithUrl(String serviceUrl, String requestMethod) throws Exception{
		String response  = null;
		try{
			disableSSLVerification();
			URL url = new URL (serviceUrl);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setConnectTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("connectTimeout")));
			httpConn.setReadTimeout(Integer.parseInt(new Util().getValueFromConfigProperties("readTimeout")));
			
			httpConn.setRequestMethod(requestMethod);
			httpConn.setRequestProperty("Accept", "application/json");
			
			if (httpConn.getResponseCode() != httpConn.HTTP_OK) {

                throw new RuntimeException("Failed : HTTP error code : "
                        + httpConn.getResponseCode());
            }
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
		            (httpConn.getInputStream())));
			response = br.lines().collect(Collectors.joining(System.lineSeparator()));
			httpConn.disconnect();
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		return response;
	}
	
	/**
	 * Disable Certificate Validations in Java SSL connections
	 */
	private void disableSSLVerification() {
		try{
			//create a trust master that does not validate certificates
			TrustManager[] trustAllCerts = new TrustManager[]{
					new X509ExtendedTrustManager(){
						@Override
						public void checkClientTrusted(X509Certificate[] X509Certificates, String s)
						throws CertificateException{
							
						}
						
						@Override
						public void checkServerTrusted(X509Certificate[] X509Certificates, String s)
						throws CertificateException{
							
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						@Override
						public void checkClientTrusted(X509Certificate[] X509Certificates,
								String s, Socket socket)
								throws CertificateException {
							
						}

						@Override
						public void checkClientTrusted(X509Certificate[] X509Certificates,
								String s, SSLEngine sslEngine)
								throws CertificateException {
							
						}

						@Override
						public void checkServerTrusted(X509Certificate[] X509Certificates,
								String s, Socket socket)
								throws CertificateException {
							
						}

						@Override
						public void checkServerTrusted(X509Certificate[] X509Certificates,
								String s, SSLEngine sslEngine)
								throws CertificateException {
							
						}
						
					}
			};
			//Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
			//create all-trusting host verifier 
			HostnameVerifier allHostsValid = new HostnameVerifier(){
				@Override
				public boolean verify(String hostName, SSLSession session) {
					return false;
				}
			};
			
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
						
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	//Unmarshaller for SoapMessage
	public static SOAPMessage getSoapMessageFromStringSoap1_2(String xml) throws SOAPException, IOException{
		MessageFactory factory = MessageFactory.newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
		return message;
	}
	
	public static SOAPMessage getSoapMessageFromStringSoap1_1(String xml) throws SOAPException, IOException{
		MessageFactory factory = MessageFactory.newInstance(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
		return message;
	}
	
	/**
	 * Map Object ---> Json string
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String mapToJsonConversion(Map<String, String> input) throws Exception{
		return MAPPER.writeValueAsString(input);
	}
	
}
