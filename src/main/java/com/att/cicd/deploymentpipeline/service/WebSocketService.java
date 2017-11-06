/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSocketService {
	
	public static void updateClients(String serverKeyCode, String user, String gotsid, String configName, String taskId) {
		WebSocketService w = new WebSocketService();
		HttpURLConnection conn = null;
		try {
			String baseUrl = (String) System.getProperty("uiurl");
			URL url = new URL(String.format(baseUrl+"notificationservices/pushItem/%s/%s/%s/%s/%s", serverKeyCode, user, gotsid, configName, taskId));
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			conn.getInputStream();
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
