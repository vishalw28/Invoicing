package com.lti.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfReaderApplication {
	
	private static final String DONE_DIR = "done"; 
	private static final String LOG_DIR = "log"; 
	
	public static void main(String[] args) {
		SpringApplication.run(PdfReaderApplication.class, args);
		
		if(args.length != 0) {
			String basePath = args[0]; 
			createDirctories(basePath);
			Map<String, Employee> empDetailMap = InvoicingUtility.readExcel("/Users/10648331/Downloads/Invoice Tracker Dec_2.0.xlsx");
			readPDFAndVerify("/Users/10648331/Downloads/Invoice_23111039_20180902_3.PDF", empDetailMap);
//			try {
//				Path baseDirPath = Paths.get(basePath);
//				//Files.move(, Paths.get(basePath + File.separator + DONE_DIR));
//				//Files.walk(baseDirPath).filter(s -> s.getFileName().toString().toUpperCase().endsWith(".PDF") || s.getFileName().toString().toUpperCase().endsWith(".XLSX")).forEach(Files.move(Function.identity(),Paths.get(basePath + File.separator + DONE_DIR)));;
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}else {
			System.out.println("Please provide the base path as argument.");
		}
		
		
		
	}

	static void createDirctories(String basePath) {
		try {
			Files.createDirectories(Paths.get(basePath + File.separator + DONE_DIR));
			Files.createDirectories(Paths.get(basePath + File.separator + LOG_DIR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void readPDFAndVerify(String filePath, Map<String, Employee> empDetailMap) {
		try (PDDocument document = PDDocument.load(new File(filePath))) {
			document.getClass();
			if (!document.isEncrypted()) {
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);
				String lines[] = pdfFileInText.split("\\r?\\n");
				System.out.println(InvoicingUtility.verifyInvoiceDetailsWithExcel(lines, empDetailMap));
			}
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
