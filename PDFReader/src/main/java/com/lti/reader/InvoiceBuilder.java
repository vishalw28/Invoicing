package com.lti.reader;

import java.util.Map;

public interface InvoiceBuilder {
	InvoiceModel verifyInvoiceDetails(String[] lines, Map<String, Employee> empDetailMap);
}
