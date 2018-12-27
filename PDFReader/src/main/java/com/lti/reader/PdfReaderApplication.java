package com.lti.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lti.reader.Employee.EmployeeBuilder;

@SpringBootApplication
public class PdfReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfReaderApplication.class, args);
		//readPDF("/Users/10648331/Downloads/Invoice_23111039_20180902_3.PDF");
		readExcel("/Users/10648331/Downloads/Invoice Tracker Dec_2.0.xlsx");
	}

	public static void readPDF(String filePath) {
		try (PDDocument document = PDDocument.load(new File(filePath))) {
			document.getClass();
			if (!document.isEncrypted()) {
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);
				String lines[] = pdfFileInText.split("\\r?\\n");
				System.out.println(InvoicingUtility.buildInvoiceModel(lines));
			}
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readExcel(String filePath) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(filePath)));
			XSSFSheet worksheet = workbook.getSheetAt(0);
			Map<String, Employee> empMap = new HashMap<>();
//			CellStyle textStyle = workbook.createCellStyle();
//			//sets the default style for the first column of worksheet to TEXT.
//			textStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
//			worksheet.setDefaultColumnStyle(0, textStyle);
			for (int i = 0; i < worksheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = worksheet.getRow(i);
				//row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
				row.getCell(20).setCellType(Cell.CELL_TYPE_STRING);
				row.getCell(16).setCellType(Cell.CELL_TYPE_STRING);
				String empName = row.getCell(5).getStringCellValue();
				//System.out.println(row.getCell(0).getStringCellValue());
				EmployeeBuilder e = Employee.builder()
						.name(empName)
						//rate = 
						.rate(row.getCell(20).getStringCellValue())
						//Amt = Working Days
						.amt(row.getCell(16).getStringCellValue());
					
				
				
				empMap.put(empName, e.build());
			}
			
			System.out.println(empMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
