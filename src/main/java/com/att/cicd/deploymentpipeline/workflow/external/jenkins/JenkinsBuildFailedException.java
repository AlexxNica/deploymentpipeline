/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

public class JenkinsBuildFailedException extends Exception {
public JenkinsBuildFailedException (String message) {
		
		super(message);
	}
}
