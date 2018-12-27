package com.lti.reader;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InvoiceModel {
//	private String billerName;
//	private String billerAddress;
//	private String billerTelNo;
//	private String billerFax;
//	private String billerCinNo;
//	private String billerTaxId;
//	private String billerEmail;
//	private String billerWebsite;
	private String invoiceNumber;
	private String invoiceDate;
	
	//Billed to info
//	private String billeeDetails;
//	private String shipToDetails;
	private String attn1;
	private String attn2;
	
	private String contractNo;
	private String contractDate;
	private String purchaseOrder;
//	private String paymentTerm;
//	private String paymentBefore;
	private String billingPeriod;
	
	private List<Employee> employee;
	
	private String total;
}
