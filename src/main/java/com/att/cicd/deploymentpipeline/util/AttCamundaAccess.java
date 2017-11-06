/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.io.IOException;
import java.io.InputStream;

import com.att.cadi.Access;

public class AttCamundaAccess implements Access
{

	public void log(Level paramLevel, Object... paramVarArgs) {
		// TODO Auto-generated method stub
		
	}

	public void log(Exception paramException, Object... paramVarArgs) {
		// TODO Auto-generated method stub
		
	}

	public void setLogLevel(Level paramLevel) {
		// TODO Auto-generated method stub
		
	}

	public ClassLoader classLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProperty(String paramString1, String paramString2) {
		if(paramString1 != null && paramString1.equalsIgnoreCase("aaf_url"))
		{
			String value = System.getProperty(paramString1);
			return value;
		}
		return paramString2;
	}

	public void load(InputStream paramInputStream) throws IOException {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public String decrypt(String arg0, boolean arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
