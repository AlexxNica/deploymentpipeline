/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jasypt.util.text.BasicTextEncryptor;

public class JenkinsConnector {

	public JenkinsConnector() {

	}

	public static HttpURLConnection getConnection(String path, String method, String jenkinsJobName, String jenkinsUserName, String jenkinsPassword) {
		HttpURLConnection connection = null;
		try {		
				String baseUrl = (String) System.getProperty("jenkinsbase");
			    URL url = new URL(baseUrl+"/job/"+jenkinsJobName+path);					
				connection = (HttpURLConnection) url.openConnection();						
				connection.setRequestMethod(method);
				connection.setDoOutput(true);								
				connection.setRequestProperty("Content-Type", "application/json");				
				String plain = jenkinsUserName + ":" + jenkinsPassword;
				byte[] encodedBytes = Base64.encodeBase64(plain.getBytes());
				String auth = "Basic " + (new String(encodedBytes));	
				connection.setRequestProperty("Authorization", auth);
				connection.setConnectTimeout(30000);
				connection.setReadTimeout(30000);
				if (connection.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ connection.getResponseCode());
				}
				
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return connection;
	
	}
}
