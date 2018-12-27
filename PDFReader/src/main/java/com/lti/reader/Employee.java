package com.lti.reader;

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
public class Employee {
	private String name;
	private String desc;
	private String qty;
	@Builder.Default
	private String uom = Invoice.PERSON_HRS.getVal();
	private String rate;
	private String amt;
	//private String attn;
}
