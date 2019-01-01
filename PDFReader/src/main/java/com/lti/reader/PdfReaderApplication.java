package com.lti.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfReaderApplication {

	private static final String DONE_DIR = "done";
	private static final String LOG_DIR = "log";
	private static String BASE_PATH = "";
	private static final String LOG_EXT = ".csv";

	static Consumer<DataCarrier> processor = carrier -> {
		
		try (PDDocument document = PDDocument.load(new File(carrier.getPdfFilePath()))) {
			document.getClass();
			if (!document.isEncrypted()) {
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);
				String lines[] = pdfFileInText.split("\\r?\\n");
				carrier.setPdfData(lines);
				//System.out.println(InvoicingUtility.verifyInvoiceDetailsWithExcel(lines, carrier.getExcelData(), carrier.getUnmatchedRecordFilePath()));
				InvoicingUtility.validateData(carrier);
			}
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//readPDFAndVerify(list.get(0).toString(), (Map<String, Employee>) list.get(1), (Path)list.get(2));

	};

	public static void main(String[] args) {
		SpringApplication.run(PdfReaderApplication.class, args);

		if (args.length != 0) {
			BASE_PATH = args[0];
			createDirctories(BASE_PATH);

			try {
				Optional<Path> path = Files.list(Paths.get(BASE_PATH)).filter(s -> {
					return s.toString().endsWith(".xlsx");
				}).findFirst();

				Map<String, Employee> empDetailMap = InvoicingUtility.readExcel(path.get().toFile().getAbsolutePath());
				
				Path p = Paths.get(BASE_PATH + File.separator + LOG_DIR + File.separator + LocalDateTime.now().toString().replaceAll(":", "_")+LOG_EXT);
				
				Files.list(Paths.get(BASE_PATH)).filter(s -> {
					return s.toString().endsWith(".PDF");
				}).forEach(o -> processor.accept(DataCarrier.builder()
						.pdfFilePath(o.toFile().toString())
						.excelData(empDetailMap)
						.unmatchedRecordFilePath(p)
						.build()));
				;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			try {
//				Path baseDirPath = Paths.get(basePath);
//				//Files.move(, Paths.get(basePath + File.separator + DONE_DIR));
//				//Files.walk(baseDirPath).filter(s -> s.getFileName().toString().toUpperCase().endsWith(".PDF") || s.getFileName().toString().toUpperCase().endsWith(".XLSX")).forEach(Files.move(Function.identity(),Paths.get(basePath + File.separator + DONE_DIR)));;
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else {
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

//	public static void readPDFAndVerify(String filePath, Map<String, Employee> empDetailMap, Path path) {
//		//System.out.println("Processing file >> " + filePath);
//		try (PDDocument document = PDDocument.load(new File(filePath))) {
//			document.getClass();
//			if (!document.isEncrypted()) {
//				PDFTextStripper tStripper = new PDFTextStripper();
//				String pdfFileInText = tStripper.getText(document);
//				String lines[] = pdfFileInText.split("\\r?\\n");
//				System.out.println(InvoicingUtility.verifyInvoiceDetailsWithExcel(lines, empDetailMap, path));
//			}
//		} catch (InvalidPasswordException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

}
