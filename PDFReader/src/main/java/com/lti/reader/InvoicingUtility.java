package com.lti.reader;

import static com.lti.reader.Invoice.ANNEX_HEADER;
import static com.lti.reader.Invoice.ATTN;
import static com.lti.reader.Invoice.BILLING_PERIOD;
import static com.lti.reader.Invoice.FINAL_TOTAL;
import static com.lti.reader.Invoice.INVOICE_DATE;
import static com.lti.reader.Invoice.INVOICE_NO;
import static com.lti.reader.Invoice.OFFSITE_HRS;
import static com.lti.reader.Invoice.ONSITE_HRS;
import static com.lti.reader.Invoice.PERSON_HRS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lti.reader.Employee.EmployeeBuilder;
import com.lti.reader.InvoiceModel.InvoiceModelBuilder;


public class InvoicingUtility {
	
	static final String SPLITTER = ", ";
	static final String HIGHLIGHT = "\"";
	
	//Extract billing info from the invoice.
	static Function<String, String> extractBillingPeriod = (s1) -> s1.substring(0,
			s1.indexOf(BILLING_PERIOD.getVal()));

	//Extract the Final Total from invoice. 
	static Function<String, String> extractFinalTotal = (s1) -> s1
			.substring((s1.indexOf(FINAL_TOTAL.getVal()) + FINAL_TOTAL.getVal().length())).trim();

	//Extract info from Annexure part.
	static Function<List<String>, Employee> getEmployeeDetails = list -> {
		String detail = list.get(1);
		int startIndex = detail.startsWith(ONSITE_HRS.getVal()) ? ONSITE_HRS.getVal().length()
				: OFFSITE_HRS.getVal().length();
		startIndex++;
		
		String[] prices = detail.substring(detail.indexOf(PERSON_HRS.getVal())+ PERSON_HRS.getVal().length() + 1).trim().split("  ");
		
		Employee emp = Employee.builder()
				.qty(detail.substring(startIndex, startIndex + detail.substring(startIndex).indexOf(" ")))
				.rate(prices[0])
				.amt(prices[1])
				.name(list.get(0))
				.attn(list.get(2))
				.poNo(list.get(3))
				.build();
				
		return emp;
	};
	
	//Extract invoice number
	static Function<String, String> getInvoiceNo = str -> str.substring(0,
			str.indexOf(INVOICE_NO.getVal()));
	
	//Extract invoice date
	static Function<String, String> getInvoiceDate = str -> str.substring(0,
			str.indexOf(INVOICE_DATE.getVal()));
	
	//Extract Attn
	static Function<String, String> getAttn = str -> str.split(ATTN.getVal())[0].trim();
	
	//Extract Purchase order
	static Function<String, String> getPO = str -> str.split(" ")[2];
	
	//Return mismatched fields
	static BiFunction<Employee, Employee, String> highLightMismatchedFields = (empExcel,empPdf) -> {
		StringBuilder sb = new StringBuilder(empExcel.getName() + SPLITTER);
		
		sb.append(empExcel.getQty().equals(empPdf.getQty()) ? empPdf.getQty()
				: HIGHLIGHT + empExcel.getQty() + HIGHLIGHT).append(SPLITTER);
		sb.append(empExcel.getRate().equals(empPdf.getRate()) ? empPdf.getRate()
				: HIGHLIGHT + empExcel.getRate() + HIGHLIGHT).append(SPLITTER);
		sb.append(empExcel.getAmt().equals(empPdf.getAmt()) ? empPdf.getAmt()
				: HIGHLIGHT + empExcel.getAmt() + HIGHLIGHT).append(SPLITTER);
		sb.append(empExcel.getPoNo().equals(empPdf.getPoNo()) ? empPdf.getPoNo()
				: HIGHLIGHT + empExcel.getPoNo() + HIGHLIGHT).append(SPLITTER);
		sb.append(empExcel.getAttn().equals(empPdf.getAttn()) ? empPdf.getAttn()
				: HIGHLIGHT + empExcel.getAttn() + HIGHLIGHT).append(SPLITTER);
		sb.append(empPdf.getPdfPath());
		

		return sb.toString();
	};
	
	
	private static InvoiceBuilder builder = carrier -> {
		String attn = null, po = null;
		String[] list = carrier.getPdfData();
		Map<String, Employee> map = carrier.getExcelData();
		
		InvoiceModelBuilder builder =  InvoiceModel.builder();
		try (BufferedWriter writer = Files.newBufferedWriter(carrier.getUnmatchedRecordFilePath(),StandardOpenOption.APPEND)) {
			for (int i = 0; i < list.length; i++) {
				String str = list[i];
				if (str.contains(BILLING_PERIOD.getVal())) {
					builder.billingPeriod(extractBillingPeriod.apply(str));
				} else if (str.startsWith(ANNEX_HEADER.getVal())) {
					i++;
					for (; i < list.length;) {
						if (list[i].contains(FINAL_TOTAL.getVal())) {
							builder.total(extractFinalTotal.apply(str));
						} else {
							// empList.add(getEmployeeDetails.apply(Arrays.asList(list[i], list[i+1],
							// attn)));
							Employee emp = getEmployeeDetails.apply(Arrays.asList(list[i], list[i + 1], attn, po));
//						if(map.get(emp.getName()) != null)
//							System.out.println("\nExcel: "+ map.get(emp.getName()) + "\n PDF: "+ emp+ "\n isMatched: "+ map.get(emp.getName()).equals(emp));
//						else
//							System.out.println("\nExcel record is not exist: "+emp);

							// Logic to write the non-matching records into File.
							if(map.get(emp.getName()) != null && !map.get(emp.getName()).equals(emp)) {
								emp.setPdfPath(carrier.getDoneDirPath() + carrier.getPdfFilePath().substring(carrier.getPdfFilePath().lastIndexOf(File.separator)));
								writer.write(highLightMismatchedFields.apply(map.get(emp.getName()), emp));
								writer.newLine();
							}
						}
						i = (i + 2) < list.length ? i + 2 : i + 1;
					}

					// builder.employee(empList);
				} else if (str.contains(INVOICE_NO.getVal())) {
					builder.invoiceNumber(getInvoiceNo.apply(str));
				} else if (str.contains(INVOICE_DATE.getVal())) {
					builder.invoiceDate(getInvoiceDate.apply(str));
				} else if (str.contains(ATTN.getVal())) {
					attn = getAttn.apply(str);
				} else if (str.startsWith(Invoice.PO.getVal())) {
					po = getPO.apply(str);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		//return builder.build();
	};
	
	public static void validateData(DataCarrier carrier) {
		builder.verifyInvoiceDetails(carrier);
	}

//	public static InvoiceModel verifyInvoiceDetailsWithExcel(String[] lines, Map<String, Employee> empDetailMap, Path path) {
//		return builder.verifyInvoiceDetails(lines, empDetailMap, path);
//	}
	
	public static Map<String, Employee> readInvoiceTrackerExcel(String invTrackerFilePath, String manPowerFilePath) throws Exception {
		
		Map<String, String> psNoAndNameMap = readManPowerExcel(manPowerFilePath);
		//XSSFWorkbook workbook = null;
		try(XSSFWorkbook workbook= new XSSFWorkbook(new FileInputStream(new File(invTrackerFilePath)))) {
			XSSFSheet worksheet = null;
			Map<String, Employee> empMap = new HashMap<>();
			for (int k = 0; k < workbook.getNumberOfSheets(); k++) {
				System.out.println("Sheet No: "+ k + ", Name: " +workbook.getSheetName(k));
				worksheet = workbook.getSheetAt(k);
				if(worksheet.getRow(0) == null || worksheet.getRow(0).getCell(0) == null || !worksheet.getRow(0).getCell(0).getStringCellValue().equals(Invoice.EMP_NO.getVal()))
					continue;
				for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
					//System.out.println("Row "+i);
					XSSFRow row = worksheet.getRow(i);
					if(row.getCell(0) == null || row.getCell(15) == null || row.getCell(17) == null || row.getCell(29) == null)
						continue;
					row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
					row.getCell(15).setCellType(Cell.CELL_TYPE_STRING);
					row.getCell(17).setCellType(Cell.CELL_TYPE_STRING);
					row.getCell(29).setCellType(Cell.CELL_TYPE_STRING);
					//row.getCell(30).setCellType(Cell.CELL_TYPE_STRING);
					//row.getCell(31).setCellType(Cell.CELL_TYPE_STRING);
					String psNo = row.getCell(0).getStringCellValue();
					
					
					// System.out.println(row.getCell(0).getStringCellValue());
					if(psNoAndNameMap.get(psNo) == null)
						System.out.println("PS No: "+ psNo +" not exists in Man Power report");
					else {
						EmployeeBuilder e = Employee.builder().name(psNoAndNameMap.get(psNo))
								.poNo(row.getCell(15).getStringCellValue().trim())
								.attn(row.getCell(17).getStringCellValue().trim())
								.qty(row.getCell(29).getStringCellValue().trim())
								.rate(String.format("%,.2f", row.getCell(30).getNumericCellValue()).trim())
								.amt(String.format("%,.2f", row.getCell(31).getNumericCellValue()).trim());
						empMap.put(psNoAndNameMap.get(psNo), e.build());
					}
				}
				//System.out.println("Collected record count: "+empMap.size());
			}
			//
			//System.out.println(empMap);
			return empMap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return null;
	}

	public static Map<String, String> readManPowerExcel(String filePath) throws Exception {
		try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(filePath)))) {
			XSSFSheet worksheet = null;
			Map<String, String> psNoAndNameMap = new HashMap<>();
			worksheet = workbook.getSheetAt(0);
			if (worksheet.getRow(0) == null
					|| (!worksheet.getRow(0).getCell(9).getStringCellValue().equals(Invoice.PS_NO.getVal())
							&& !worksheet.getRow(0).getCell(10).getStringCellValue().equals(Invoice.NAME.getVal()))) {
				throw new Exception(
						"PS Number or Name column is not exist in Man Power excel sheet at position 10 & 11 respectively.");
			}
			for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = worksheet.getRow(i);
				// row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
				row.getCell(9).setCellType(Cell.CELL_TYPE_STRING);
				row.getCell(10).setCellType(Cell.CELL_TYPE_STRING);
				psNoAndNameMap.put(row.getCell(9).getStringCellValue().trim(), row.getCell(10).getStringCellValue().trim());
			}
			return psNoAndNameMap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return null;
	}
}
