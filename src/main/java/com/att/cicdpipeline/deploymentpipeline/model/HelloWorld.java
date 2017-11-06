/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline.model;

public class HelloWorld {

	private String message;

	public HelloWorld() {
		// needed for deserializer
	}

	public HelloWorld(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "message = " + getMessage();
	}
}
