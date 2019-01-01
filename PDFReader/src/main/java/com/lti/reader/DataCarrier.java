package com.lti.reader;

import java.nio.file.Path;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataCarrier {
	private String pdfFilePath;
	private String[] pdfData;
	private Map<String, Employee> excelData;
	private Path unmatchedRecordFilePath;
}
