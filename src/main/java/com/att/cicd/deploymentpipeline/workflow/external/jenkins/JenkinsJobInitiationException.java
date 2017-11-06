/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

public class JenkinsJobInitiationException extends Exception{
	public JenkinsJobInitiationException (String message) {
		
		super(message);
	}
}
