package SupportLibraries;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

public class GenericServiceFactory <T, R> {

	
	/*DocumentBuilderFactory dbFactory;
	DocumentBuilder dBuilder;
	Document doc;*/
	
	/*public T buildRequest (Class<T> serviceClass, String path){
		
		if(serviceClass == null){
			System.out.println("Service class is null");
			return null;
		}
		
		try{
			File inputFile = new File(getClass().getClassLoader().getResource(path).getFile());
			InputStream is = FileUtils.openInputStream(inputFile);
			//InputStream is = getClass().getClassLoader().getResourceAsStream(path);
			
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(is);
			 T request = buildRequestFromDoc(serviceClass, doc);
			 return request;
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}*/
	
	
	/*public T buildRequestFromXmlStream (Class<T> serviceClass, InputStream instr){
		if(serviceClass == null){
			return null;
		}
		try{
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(instr);
			return (buildRequestFromDoc(serviceClass, doc));
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}*/
	
	public T buildRequestFromXmlString (Class<T> serviceClass, String str){
		if(serviceClass == null){
			return null;
		}
		try{
			File inputSample = new File(getClass().getClassLoader().getResource(str).getFile());
			String inputFile = FileUtils.readFileToString(inputSample);
			return (buildRequestFromString(serviceClass, inputFile));
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private T buildRequestFromString(Class<T> serviceClass, String req){
		try{
			Class<?> c = Class.forName(serviceClass.getName());
			Constructor <?> ctor = c.getConstructor(String.class);
			return (T) ctor.newInstance(new Object[] {req});
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private T returnInstanceOfClass(String className){
		try{
			return (T) Class.forName(className).getConstructor().newInstance();
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private T buildRequestFromDoc(Class<T> serviceClass, Document doc){
		try{
			Class<?> c = Class.forName(serviceClass.getName());
			Constructor <?> ctor = c.getConstructor(Document.class);
			return (T) ctor.newInstance(new Object[] {doc});
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public R buildResponse(Class<R> serviceClass){
		if(serviceClass == null){
			return null;
		}
		try{
			Class<?> c = Class.forName(serviceClass.getName());
			Constructor <?> ctor = c.getConstructor();
			return (R) ctor.newInstance();
		}catch(FactoryConfigurationError e){
			e.printStackTrace();
			return null;
		}catch (Exception e){
			return null;
		}
	}


}
