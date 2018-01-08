package SupportLibraries;

import javax.xml.parsers.FactoryConfigurationError;

public class GenericFactoryRestServices <T>{
	
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
}
