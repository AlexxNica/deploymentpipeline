/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline.service;

import com.att.cicdpipeline.deploymentpipeline.model.HelloWorld;

public interface SpringService {
	public HelloWorld getQuickHello(String name);
}
