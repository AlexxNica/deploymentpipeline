/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CamundaConnector {
	
	private static final Logger LOGGER = Logger.getLogger(CamundaConnector.class);
	
	public static void main(String[] args) {
		String cookie = CamundaConnector.getAuth();

	}
	
	public static HttpURLConnection getConnection(String path, String method, String cookie, boolean acceptJson) {
		HttpURLConnection connection = null;
		try {
			String baseUrl = (String) System.getProperty("baseurl").trim();
			URL url = new URL(baseUrl + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			if (acceptJson) {
				connection.setRequestProperty("Accept", "application/json");
			}
			connection.setRequestProperty("Cookie", cookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return connection;
	}
			
	public static String getAuth() {
		HttpURLConnection connection = null;
		try {
			String baseUrl = (String) System.getProperty("baseurl").trim();
			URL url = new URL(baseUrl + "camunda/api/admin/auth/user/default/login/tasklist");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/json");
			InputStream content = null;
			String response = "";
			String body = "username=demo&password=password";
			try {
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.write(body.getBytes());
				LOGGER.info("ResponseCode :" + connection.getResponseCode());
				if (connection.getResponseCode() == 200) {
					return connection.getHeaderField("Set-Cookie");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			LOGGER.info("Response :" + response);
			LOGGER.info("--------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String get(String path, String cookie, boolean acceptJson) {
		LOGGER.info("Calling: " + path);
		HttpURLConnection connection = getConnection(path, "GET", cookie, acceptJson);
		InputStream content = null;
		String response = "";
		try {
			content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String line;

			while ((line = in.readLine()) != null) {
				response = response + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Response :" + response);
		LOGGER.info("--------------------------------------------------------");
		return response;
	}
	
	public static String delete (String path, String cookie, boolean acceptJson) {
		LOGGER.info("Calling: " + path);
		HttpURLConnection connection = getConnection(path, "DELETE", cookie, acceptJson);
		InputStream content = null;
		String response = "";
		try {
			content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String line;

			while ((line = in.readLine()) != null) {
				response = response + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Response :" + response);
		LOGGER.info("--------------------------------------------------------");
		return response;
	}

	public static String post(String path, String body, String cookie, boolean acceptJson) {
		LOGGER.info("Calling :" + path);
		LOGGER.info("Body: " + body);
		HttpURLConnection connection = getConnection(path, "POST", cookie, acceptJson);
		InputStream content = null;
		String response = "";

		try {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(body.getBytes());
			LOGGER.info("ResponseCode :" + connection.getResponseCode());
			if (connection.getResponseCode() == 200) {
				content = (InputStream) connection.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;

				while ((line = in.readLine()) != null) {
					response = response + line;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Response :" + response);
		LOGGER.info("--------------------------------------------------------");
		return response;
	}
	
	public static ArrayList getJsonArray(Map jsonObject, String objectName) {
		ArrayList instances = (ArrayList) jsonObject.get(objectName);
		if (instances == null) {
			instances = new ArrayList(); 
		} 
		return instances; 
	}
	
	public static String getJsonString(Map jsonObject, String variableName) { 
		String variableValue = (String) jsonObject.get(variableName);
		if (variableValue == null) {
			variableValue = "";
		}
		return variableValue.trim();
	}
	
	public static String getJsonBoolean(Map jsonObject, String variableName) {
		String variableValue = jsonObject.get(variableName).toString();
		if (variableValue == null) {
			variableValue = "";
		}
		return variableValue;
	}
	
	public static String getJsonInteger(Map jsonObject, String variableName) {
		String variableValue = Integer.toString((int) jsonObject.get(variableName));
		if (variableValue == null) {
			variableValue = "";
		}
		return variableValue;
	}
	
	public static Map getJsonMap (Map jsonObject, String variableName) {
		Map instances = (Map) jsonObject.get(variableName);
		if (instances == null) {
			instances = new HashMap();
		} 
		return instances; 
	}
	
	public static Map jsonToMap(String response) {
		ObjectMapper mapper = new ObjectMapper();
		Map object = null;
		try {
			object = mapper.readValue(response, Map.class);
		} catch (Exception e) {
			object = new HashMap();
		}
		return object;	
	}
	
	public static ArrayList jsonToArrayList(String response) {
		ObjectMapper mapper = new ObjectMapper();
		ArrayList object = null;
		try {
			object = mapper.readValue(response, ArrayList.class);
		} catch (Exception e) {
			e.printStackTrace();
			object = new ArrayList();
		}
		return object;		
	}
}
