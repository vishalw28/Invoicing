package com.lti.reader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Employee {
	private String name;
	private String desc;
	private String qty;
	private String rate;
	private String amt;
}
