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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.lti.reader.InvoiceModel.InvoiceModelBuilder;

public class InvoicingUtility {
	
	//Extract billing info from the invoice.
	static Function<String, String> extractBillingPeriod = (s1) -> s1.substring(0,
			s1.indexOf(BILLING_PERIOD.getVal()));

	//Extract the Final Total from invoice. 
	static Function<String, String> extractFinalTotal = (s1) -> s1
			.substring((s1.indexOf(FINAL_TOTAL.getVal()) + FINAL_TOTAL.getVal().length())).trim();

	//Extract info from Annexure part.
	static BiFunction<String, String, Employee> getEmployeeDetails = (name, detail) -> {
		System.out.println("Processing line: "+ detail);
		int startIndex = detail.startsWith(ONSITE_HRS.getVal()) ? ONSITE_HRS.getVal().length()
				: OFFSITE_HRS.getVal().length();
		startIndex++;
		
		String[] prices = detail.substring(detail.indexOf(PERSON_HRS.getVal())+ PERSON_HRS.getVal().length() + 1).trim().split("  ");
		
		Employee emp = Employee.builder()
				.qty(detail.substring(startIndex, startIndex + detail.substring(startIndex).indexOf(" ")))
				.rate(prices[0])
				.amt(prices[1])
				.name(name)
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
	
	private static InvoiceBuilder builder = (list) -> {
		
		InvoiceModelBuilder builder =  InvoiceModel.builder();
		for (int i = 0; i < list.length; i++) {
			String str = list[i];
			if (str.contains(BILLING_PERIOD.getVal())) {
				builder.billingPeriod(extractBillingPeriod.apply(str));
			}else if (str.startsWith(ANNEX_HEADER.getVal())) {
				List<Employee> empList = new ArrayList<>();
				i++;
				for (; i < list.length;) {
					if (list[i].contains(FINAL_TOTAL.getVal())) {
						builder.total(extractFinalTotal.apply(str));
						
					} else {
						empList.add(getEmployeeDetails.apply(list[i], list[i+1]));
					}
					i = (i + 2) < list.length ? i + 2 :
							i + 1;
				}
				builder.employee(empList);
			}else if(str.contains(INVOICE_NO.getVal())) {
				builder.invoiceNumber(getInvoiceNo.apply(str));
			}else if(str.contains(INVOICE_DATE.getVal())) {
				builder.invoiceDate(getInvoiceDate.apply(str));
			}else if(str.contains(ATTN.getVal())) {
				builder.attn1(getAttn.apply(str));
			}
		}
		return builder.build();
	};

	public static InvoiceModel buildInvoiceModel(String[] lines) {
		return builder.build(lines);
	}
}
