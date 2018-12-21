package com.lti.reader;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfReaderApplication.class, args);
		readPDF("/Users/10648331/Downloads/Invoice_23111039_20180902_3.PDF");
	}

	public static void readPDF(String filePath){
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
}

