package com.lti.reader;

import java.util.function.Function;

public class InvoicingUtility {
	private static InvoiceModelBuilder builder = (list) -> {
		InvoiceModel im = InvoiceModel.builder().build();

		Function<String, String> extractBillingPeriod = (s1) -> s1.substring(0,
				s1.indexOf(Invoice.BILLING_PERIOD.getVal()));

		Function<String, String> extractFinalTotal = (s1) -> s1
				.substring((s1.indexOf(Invoice.FINAL_TOTAL.getVal()) + Invoice.FINAL_TOTAL.getVal().length())).trim();

		for (int i = 0; i < list.length; i++) {
			String str = list[i];
			if (str.contains(Invoice.BILLING_PERIOD.getVal()))
				im.setBillingPeriod(extractBillingPeriod.apply(str));
			else if (str.contains(Invoice.ANNEX_HEADER.getVal())) {
				for (; i < list.length;) {
					if (str.contains(Invoice.FINAL_TOTAL.getVal())) {
						im.setTotal(extractFinalTotal.apply(str));
					} else {
						
					}
					
					i = i+2 < list.length ? i+2 : i++;
				}
			}
		}
		return im;
	};

	public static InvoiceModel buildInvoiceModel(String[] lines) {
		return builder.build(lines);
	}
}
