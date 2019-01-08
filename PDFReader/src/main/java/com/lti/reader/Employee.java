package com.lti.reader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(includeFieldNames=false)
@EqualsAndHashCode
public class Employee {
	private String name;
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private String desc;
	private String qty;
	@EqualsAndHashCode.Exclude
	@Builder.Default
	private String uom = Invoice.PERSON_HRS.getVal();
	private String rate;
	private String amt;
	private String poNo;
	private String attn;
	@EqualsAndHashCode.Exclude
	private String pdfPath;
	
	//Change the sequence as per the toString() method.
	static String getColumnHeader() {
		return "Name, Qty, Rate, Amt,PO No, Attn, PDF Path";
	}
	
	@Override
	public String toString() {
		return name + ", "+ qty + ", "+ rate +", "+ amt +", "+ poNo +", "+ attn;
	}
}
