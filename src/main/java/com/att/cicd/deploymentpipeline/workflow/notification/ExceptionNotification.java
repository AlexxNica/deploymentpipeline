/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;

public class ExceptionNotification {


	public static void mail(String recipient, String jobName, String gotsid, String name, String reason,
			String jenkinsURL, String jenkinsUserName, String processName, String processDescription,
			String submoduleName, String pipelineName, String acronym, String pipelineId, String deploymentName) {
		ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", "Beginning");
		String exceptionBody = "";
		String emailFromAddress = System.getProperty("emailfromaddress");
		String emailHost = System.getProperty("emailhostname");
		String emailUsername = System.getProperty("emailusername");
		String emailPassword = System.getProperty("emailpassword");
		String emailSenderName = System.getProperty("emailsendername");
		String clientURL = System.getProperty("clienturl");
		int emailPort = Integer.valueOf(System.getProperty("emailsmtpport"));
		String exceptionSubject = "";
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='exceptionBody') as exceptionBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='exceptionSubject') as exceptionSubject;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				;
				exceptionBody = rs.getString("exceptionBody");
				exceptionSubject = rs.getString("exceptionSubject");
			}

			List<String> recipients = new ArrayList<String>();
			boolean multipleRecipients = false;
			if (recipient == null || recipient.equals("")) {
				String info = "There was no email address added, getting admin from aaf";
				ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
				recipient = AAFConnector.getAdminList(gotsid);
			}
			if (recipient.contains(",")) {
				recipients = Arrays.asList(recipient.split("\\s*,\\s*"));
				multipleRecipients = true;
			} else if (recipient.contains(";")) {
				recipients = Arrays.asList(recipient.split("\\s*;\\s*"));
				multipleRecipients = true;
			}
			Email email = new SimpleEmail();
			email.setHostName(emailHost);
			// email.setAuthentication(emailUsername, emailPassword);
			try {
				email.setFrom(emailFromAddress, emailSenderName);
				email.setSmtpPort(emailPort);

				if (multipleRecipients) {
					recipient = "";
					for (String person : recipients) {
						String personAddress = UserInfoService.getEmailAddress(person);
						if(!personAddress.equals(UserInfoService.NO_EMAIL)) {
							recipient += person + ", ";
							email.addTo(personAddress);
							String info = "Added " + personAddress + " to send the notification to.";
							ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
						} else {
							String info = "No email detected. Not sending email to " + person;
							ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
						}
					}
					String info = "Sending Exception Email notification to: " + recipient.substring(0, recipient.length()-2);
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
							"ExceptionNotification", "execute", info, true);
				} else {
					String person = UserInfoService.getEmailAddress(recipient);
					if(!person.equals(UserInfoService.NO_EMAIL)) {
						String info = "Added " + person + " to send the notification to.";
						ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
						email.addTo(person);
						recipient = recipient + ", ";
					} else {
						String info = "No email detected. Not sending email to " + recipient;
						ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
					}

					String info = "Sending Exception Email notification to: " + recipient.substring(0, recipient.length()-2);
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
							"ExceptionNotification", "execute", info, true);
				}

				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((emailTo))"), recipient);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((jobName))"), jobName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((name))"), name);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((reason))"), reason);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((jenkinsURL))"), jenkinsURL);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((jenkinsUserName))"), jenkinsUserName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((processName))"), processName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((processDescription))"),
						processDescription);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((pipelineName))"), pipelineName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((acronym))"), acronym);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((pipelineId))"), pipelineId);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((emailTo))"), recipient);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((jobName))"), jobName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((name))"), name);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((reason))"), reason);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((jenkinsURL))"), jenkinsURL);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((jenkinsUserName))"), jenkinsUserName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((processName))"), processName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((processDescription))"), processDescription);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((pipelineName))"), pipelineName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((acronym))"), acronym);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((pipelineId))"), pipelineId);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				email.setSubject(exceptionSubject);
				email.setContent(exceptionBody, "text/html; charset=utf-8");

				String protocol = System.getProperty("emailprotcol");
				protocol = protocol.toUpperCase();
				switch(protocol) {
				case "SSL":
					email.setSSLOnConnect(true);
					break;
				case "TLS":
					email.setStartTLSEnabled(true);
					email.setStartTLSRequired(true);
					break;
				case "SSLTLS":
					email.setSSLOnConnect(true);
					email.setStartTLSEnabled(true);
					email.setStartTLSRequired(true);
					break;
				case "NONE":
					//do nothing
					break;
				default:
					//default to do nothing. If this changes, update system.properties comment
				}

				email.setAuthenticator(new DefaultAuthenticator(emailUsername, emailPassword));
				email.send();
				String info = "Email sent successfully.....";
				ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
			} catch (Exception e) {
				String info = "Invalid Email Address.";
				ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", info);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (Exception ex) {
			}
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId,  pipelineName, submoduleName, processName, "ExceptionNotification", "mail", "End");
	}

}
