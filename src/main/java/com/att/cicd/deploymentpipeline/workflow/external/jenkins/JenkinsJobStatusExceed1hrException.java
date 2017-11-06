/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

public class JenkinsJobStatusExceed1hrException extends Exception {
	public JenkinsJobStatusExceed1hrException (String message) {
		
		super(message);
	}

}
