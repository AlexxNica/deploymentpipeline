/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import com.att.cicd.deploymentpipeline.filemonitor.ServicePropertiesMap;

public class ServicePropertiesMapBean {

	public static String getProperty(String propFileName, String propertyKey) {
		return ServicePropertiesMap.getProperty(propFileName, propertyKey);
	}
}
