package ServiceAutomation;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import com.sun.istack.logging.Logger;

import DataValidations.BaseValidations;
import DataValidations.XlUtil;

public class ServiceTests {
	
	private static final Logger Log = Logger.getLogger(ServiceTests.class);
	
	public static void main( String[] args ) throws Exception
    {
		File inputFileWithServices = new File("./inputData_Sample\\InputSheetWithServices.xlsx");
		String inputFile_path = inputFileWithServices.getCanonicalPath();
		
		String outputFile = inputFileWithServices.getParentFile().getCanonicalPath()+"\\OutputSheetAfterValidations.xlsx";
		try{
			List<LinkedHashMap<String, String>> inputData= BaseValidations.getDataFromInputSheet(inputFile_path);
			XlUtil.prepareOutputFile(outputFile,"Output",inputData);
		
		}catch(Exception e){
			Log.info("Exception thrown" +e);
		}
		Log.info("Execution is complete with out any issues");
			
    }


}
