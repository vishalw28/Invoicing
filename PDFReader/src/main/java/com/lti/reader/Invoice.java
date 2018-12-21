package com.lti.reader;

public enum Invoice {
	BILLING_PERIOD("Billing Period:"), PO("Purchase Order:"), PAYMENT_TERM("Payment Terms:"), 
	PAYMENT_BEFORE("Payment Before:"), ANNEX_HEADER("Description Quantity UOM Rate (USD) Amount (USD)"),
	PERSON_HRS("Person Hours"), FINAL_TOTAL("Total :");

	private String val;

	private Invoice(String val) {
		this.val = val;
	}

	public String getVal() {
		return val;
	}
}
