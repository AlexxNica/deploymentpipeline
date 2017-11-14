/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAFConnector {
	private static String secret = "pX5R7Ct6EAiZWOknlPeAHlrjAXSjLFCl";
	private static String credPassword = "aaf_credential";
	private static final Logger LOGGER = Logger.getLogger(AAFConnector.class);

	public AAFConnector() {
	}

	public static HttpURLConnection getConnection(String path, String method) {
		HttpURLConnection connection = null;
		try {
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(secret);
			String password = "";
			try {
				password = textEncryptor.decrypt((String) System.getProperty("aafpassword"));
			} catch (EncryptionOperationNotPossibleException e) {
				LOGGER.warn("password decryption failed. Using unprocessed password");
				password = System.getProperty("aafpassword");
			}

			String fullPath = System.getProperty("aafurl").trim();
			if(!(fullPath.endsWith("/") || path.startsWith("/"))) {
				fullPath+="/";
			}
			fullPath+=path;
			
			URL url = new URL(fullPath);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			String mechid = (String) System.getProperty("aafusername");
			String authString = mechid + ":" + password;
			String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
			connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			connection.setRequestProperty("Content-Type", "application/json");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return connection;
	}

	public static String get(String path) {

		LOGGER.info("--------------------------------------------------------");
		LOGGER.info("Calling :" + path);
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		HttpURLConnection connection = getConnection(path, "GET");
		InputStream content = null;
		String response = "no response";

		try {
			LOGGER.info("ResponseCode :" + connection.getResponseCode());
			if(connection.getResponseCode() == 200) {
				content = (InputStream) connection.getInputStream();
			} else {
				content = (InputStream) connection.getErrorStream();
			}
			if(content != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				response="";
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


	public static String post(String path, String body) {

		LOGGER.info("--------------------------------------------------------");
		LOGGER.info("Calling :" + path);
		LOGGER.info("Body: " + body);	
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		HttpURLConnection connection = getConnection(path, "POST");
		InputStream content = null;
		String response = "no response";

		try {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(body.getBytes());
			LOGGER.info("ResponseCode :" + connection.getResponseCode());
			if(connection.getResponseCode() == 200) {
				content = (InputStream) connection.getInputStream();
			} else {
				content = (InputStream) connection.getErrorStream();
			}
			if(content != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				response="";
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

	public static String put(String path, String body) {

		LOGGER.info("--------------------------------------------------------");
		LOGGER.info("Calling :" + path);
		LOGGER.info("Body: " + body);
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		HttpURLConnection connection = getConnection(path, "PUT");
		InputStream content = null;
		String response = "no response";

		try {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(body.getBytes());
			LOGGER.info("ResponseCode :" + connection.getResponseCode());
			if(connection.getResponseCode() == 200) {
				content = (InputStream) connection.getInputStream();
			} else {
				content = (InputStream) connection.getErrorStream();
			}
			if(content != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				response="";
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

	public static String delete(String path, String body) {

		LOGGER.info("--------------------------------------------------------");
		LOGGER.info("Calling :" + path);
		LOGGER.info("Body: " + body);	
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		HttpURLConnection connection = getConnection(path, "DELETE");
		InputStream content = null;
		String response = "no response";

		try {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(body.getBytes());
			LOGGER.info("ResponseCode :" + connection.getResponseCode());
			if(connection.getResponseCode() == 200) {
				content = (InputStream) connection.getInputStream();
			} else {
				content = (InputStream) connection.getErrorStream();
			}
			if(content != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				response="";
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

	public static void main(String args[]) throws Exception {
		AAFConnector con = new AAFConnector();
		LOGGER.info(con.getAdminList("25316"));
	}

	public static String getAdminList(String gotsid) {
		LOGGER.info("Getting list of admin for gotsid " + gotsid);
		String recipients = "";
		AAFConnector aafcon = new AAFConnector();
		String response = (aafcon.get("/authz/userRoles/role/" + ""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin"));
		ObjectMapper mapper = new ObjectMapper();
		Map object = null;
		if(response.equals("")){

		}else{
			try {
				object = mapper.readValue(response, Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		// StringJoiner joiner = new StringJoiner(", ");
		String users = "";
		if (object != null && object.get("userRole") != null) {
			ArrayList objArr = (ArrayList) object.get("userRole");
			for (int i = 0; i < objArr.size(); i++) {
				Map obj = (Map) objArr.get(i);
				String user = obj.get("user").toString();
				user = user.substring(0, user.indexOf("@"));
				users = users + user;
				if (i != objArr.size()-1) {
					users = users + ", ";
				}
			}
		}
		recipients = users;
		LOGGER.info("Recipients: " + recipients);
		return recipients;
	}

	private static String getUser(String path) {
		String user = "";

		String[] exploadedPath = path.split("/");
		for(String s : exploadedPath) {
			if(s.contains("@")) {
				user = s;
				break;
			}
		}

		return user;
	}

	private static boolean hasAAFCred(String path) {
		String user = getUser(path);
		if(user==null || user.length()==0) {
			return false;
		}
		String validationPath = "authn/validate";
		//Note: this body must match the post body in ensureAAFCred(path)
		String validationBody = "{"
				+ "\"id\":\""+UserInfoService.fullyQualify(user)+"\","
				+ "\"password\":\""+credPassword+"\""
				+ "}";

		LOGGER.info("--------------------------------------------------------");
		LOGGER.info("Checking user credential for user "+user);
		LOGGER.info("Calling :" + validationPath);
		LOGGER.info("Body: " + validationBody);	

		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		HttpURLConnection connection = getConnection(validationPath, "POST");
		InputStream content = null;
		String response = "";

		try {
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(validationBody.getBytes());
			if(connection.getResponseCode()==200) {
				LOGGER.info("Credential found for user "+user);
				LOGGER.info("--------------------------------------------------------");
				return true;
			} else {
				LOGGER.info("Credential not found for user "+user);
				LOGGER.info("--------------------------------------------------------");
				return false;
			}
		} catch (IOException e) {
			LOGGER.warn("Credential not found for user "+user+". Unexpected error occured.");
			LOGGER.info("--------------------------------------------------------");
			e.printStackTrace();
			return false;
		}

	}
}
