package com.lti.reader;

import java.nio.file.Path;
import java.util.Map;

public interface InvoiceBuilder {
	//InvoiceModel verifyInvoiceDetails(String[] lines, Map<String, Employee> empDetailMap, Path path);
	void verifyInvoiceDetails(DataCarrier carrier);
}
