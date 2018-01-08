package DataValidations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlUtil {
	
	private static final Logger Log = Logger.getLogger(XlUtil.class);
	
	//reading data from input spreadsheet
	public static List<LinkedHashMap<String, String>> readExcel(String inputFilePath) throws Exception{
		
		List<LinkedHashMap<String, String>> records = new ArrayList<LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> rowDataMap = null;
		
		List<String> header = new ArrayList<String>();
		
		try{
			FileInputStream ExcelFile = new FileInputStream(inputFilePath);
			XSSFWorkbook ExcelWbook = new XSSFWorkbook(ExcelFile);
			XSSFSheet ExcelWsheet = ExcelWbook.getSheetAt(0);
			XSSFRow row;
			XSSFCell cell;
			
			Iterator<Row> rows = ExcelWsheet.rowIterator();
			int dataRowIndex = 1;
			
			while (rows.hasNext()){
				row = (XSSFRow) rows.next();
				if(dataRowIndex>1){
					rowDataMap = new LinkedHashMap<String, String>();
				}
				Iterator cells = row.iterator();
				int dataColumnIndex = 0, i=0;
				
				while(cells.hasNext()){
					if(dataRowIndex == 1){
						cell = (XSSFCell) cells.next();
						header.add(cell.getStringCellValue());
					}else if(i<header.size()){
						cell = row.getCell(i, row.CREATE_NULL_AS_BLANK);
						String column = cell.getStringCellValue();
						rowDataMap.put(header.get(dataColumnIndex), column);
					}else{
						break;
					}
					dataColumnIndex++;
					i++;					
				}
				dataRowIndex++;
				if(rowDataMap !=null){
					records.add(rowDataMap);
				}
			}
			
		}catch (Exception e){
			Log.error("failed to get records from Excel "+e.getMessage()+ "from sheet"+inputFilePath);
		}
		return records;
	}
	
	//writing new output spreadsheet
	public static void prepareOutputFile(String outputFilePath, String sheetName,
			List<LinkedHashMap<String, String>> resultRecord) {
		try{
			File ExcelFile = new File(outputFilePath);
			XSSFWorkbook ExcelWbook = new XSSFWorkbook();
			XSSFSheet ExcelWsheet;
			XSSFRow row;
			
			if(!ExcelFile.exists()){
				ExcelWsheet = ExcelWbook.createSheet(sheetName);
			}else{
				ExcelFile.delete();
				ExcelWsheet = ExcelWbook.createSheet(sheetName);
			}
			CellStyle cellStyleOfOutput = setCellStyleForOutput(ExcelWbook, ExcelWsheet); 
			
			int rownum = 0;
			for (int i=0; i < resultRecord.size(); i++){
				LinkedHashMap<String, String> rowData = resultRecord.get(i);
				Set<String> keySetRowDataHeader = rowData.keySet();
				if(i==0){
					Row rowHeader = ExcelWsheet.createRow(rownum);
					int cellnum = 0;
					for(String key : keySetRowDataHeader){
						Cell cellHeader = rowHeader.createCell(cellnum++);
						cellHeader.setCellValue(key);
						ExcelWsheet.autoSizeColumn((short) cellnum);
						cellHeader.setCellStyle(cellStyleOfOutput);
					}
					rownum++;
				}
				row = ExcelWsheet.createRow(rownum);
				int cellnum = 0;
				for(String key : keySetRowDataHeader){
					Cell cellRow = row.createCell(cellnum++);
					String dataValue = rowData.get(key);
					cellRow.setCellValue(dataValue);
					ExcelWsheet.autoSizeColumn((short) cellnum);
					cellRow.setCellStyle(cellStyleOfOutput);
				}
				rownum++;
			}
			FileOutputStream outputFile = new FileOutputStream(outputFilePath);
			ExcelWbook.write(outputFile);
			outputFile.close();
		}catch (Exception e){
			Log.error("Exception occured while preparting Output file: " +e.getMessage());
		}
		
	}

	private static CellStyle setCellStyleForOutput(XSSFWorkbook excelWbook, XSSFSheet excelWsheet) {
		CellStyle cellStyle = excelWbook.createCellStyle();
		cellStyle.setBorderTop((short) 6);
		cellStyle.setBorderLeft((short) 6);
		cellStyle.setBorderRight((short) 6);
		cellStyle.setBorderBottom((short) 6);
		cellStyle.setWrapText(true);
		
		return cellStyle;
	}

	
	
}
