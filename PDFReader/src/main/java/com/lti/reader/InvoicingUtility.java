package com.lti.reader;

import java.util.Arrays;
import java.util.function.Function;

public class InvoicingUtility {
	private static InvoiceModelBuilder builder = (s) -> {
		InvoiceModel im = InvoiceModel.builder().build();

		Function<String, String> extractBillingPeriod = (s1) -> s1.substring(0,
				s1.indexOf(Invoice.BILLING_PERIOD.getVal()));

		Function<String, String> extractFinalTotal = (s1) -> s1
				.substring((s1.indexOf(Invoice.FINAL_TOTAL.getVal()) + Invoice.FINAL_TOTAL.getVal().length())).trim();

		Arrays.stream(s).forEach(s1 -> {
			if (s1.contains(Invoice.BILLING_PERIOD.getVal()))
				im.setBillingPeriod(extractBillingPeriod.apply(s1));
			else if (s1.contains(Invoice.FINAL_TOTAL.getVal())) {
				im.setTotal(extractFinalTotal.apply(s1));
			}

		});
		return im;
	};

	public static InvoiceModel buildInvoiceModel(String[] lines) {
		return builder.build(lines);
	}
}
