/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

public class EmailDetail {
	String gotsid;
	String reason;
	
	public String getGotsid() {
		return gotsid;
	}
	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
