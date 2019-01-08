package com.lti.reader;

public interface InvoiceBuilder {
	//InvoiceModel verifyInvoiceDetails(String[] lines, Map<String, Employee> empDetailMap, Path path);
	void verifyInvoiceDetails(DataCarrier carrier);
}
