/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

public class JenkinsQueueExpireException extends Exception {
	public JenkinsQueueExpireException (String message) {
		
		super(message);
	}
}
