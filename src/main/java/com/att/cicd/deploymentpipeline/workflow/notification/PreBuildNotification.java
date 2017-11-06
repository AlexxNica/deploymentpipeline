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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

public class PreBuildNotification implements JavaDelegate {


	@Override
	public void execute(DelegateExecution execution) throws Exception {

		String gotsid = getVar(execution, "gotsid");
		String name = getVar(execution, "name");
		String processName = getVar(execution, "processName");
		String submoduleName = getVar(execution, "submodule_name");
		String pipelineId = getVar(execution, "pipeline_id");
		String pipelineName = getVar(execution, "pipeline_name");
		String acronym = getVar(execution, "acronym");
		String processDescription = getVar(execution, "processDescription");
		String emailDescription = Database.getDescription("emailDescription", pipelineName, submoduleName, processName);
		String array_indexer = "", enabled = "", recipient = "", deploymentName = "";
		if (pipelineName.contains("jenkins")) {
			array_indexer = getVar(execution, "array_indexer");
			enabled = Database.getVariableWithIndex(gotsid, name, pipelineName, submoduleName, "global",
					"enableApproval", array_indexer);
			recipient = Database.getVariableWithIndex(gotsid, name, pipelineName, submoduleName, "global",
					"deploymentNotificationEmail", array_indexer);
			deploymentName = Database.getDeploymentName(gotsid, pipelineName, submoduleName, name, array_indexer);
		} else {
			enabled = Database.getVariable(gotsid, name, pipelineName, submoduleName, "global",
					"enableEnvironmentNotificationEmail");
			recipient = Database.getVariable(gotsid, name, pipelineName, submoduleName, "global",
					"environmentNotificationEmail");
			deploymentName = name;
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
				"PreBuildNotification", "execute", "Beginning");
		if (enabled.equals("true")) {
			mail(recipient, gotsid, name, processName, submoduleName, pipelineId, pipelineName, acronym,
					processDescription, emailDescription, deploymentName);
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
				"PreBuildNotification", "execute", "End");
	}

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}

	private void mail(String recipient, String gotsid, String name, String processName, String submoduleName,
			String pipelineId, String pipelineName, String acronym, String processDescription, String emailDescription,
			String deploymentName) {
		String emailBody = "";
		String emailFromAddress = System.getProperty("emailfromaddress");
		String emailHost = System.getProperty("emailhostname");
		String emailUsername = System.getProperty("emailusername");
		String emailPassword = System.getProperty("emailpassword");
		String emailSenderName = System.getProperty("emailsendername");
		String clientURL = System.getProperty("clienturl");
		int emailPort = Integer.valueOf(System.getProperty("emailsmtpport"));
		String emailSubject = "";
		List<String> recipients = new ArrayList<String>();
		boolean multipleRecipients = false;
		if (recipient == null || recipient.trim().isEmpty()) {
			String info = "There was no email address added, getting admin from aaf";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PreBuildNotification", "mail", info);
			recipient = AAFConnector.getAdminList(gotsid);
		}
		if (recipient.contains(",")) {
			recipients = Arrays.asList(recipient.split("\\s*,\\s*"));
			multipleRecipients = true;
		} else if (recipient.contains(";")) {
			recipients = Arrays.asList(recipient.split("\\s*;\\s*"));
			multipleRecipients = true;
		}
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='environmentPreBuildEmailBody') as emailBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='environmentPreBuildEmailSubject') as emailSubject;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				emailBody = rs.getString("emailBody");
				emailSubject = rs.getString("emailSubject");
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
						if (!personAddress.equals(UserInfoService.NO_EMAIL)) {
							recipient += person + ", ";
							email.addTo(personAddress);
							String info = "Added " + personAddress + " to send the notification to.";
							ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
									processName, "PreBuildNotification", "mail", info);
						} else {
							String info = "No email detected. Not sending email to " + person;
							ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
									processName, "PreBuildNotification", "mail", info);
						}
					}
				} else {
					String person = UserInfoService.getEmailAddress(recipient);
					if (!person.equals(UserInfoService.NO_EMAIL)) {
						String info = "Added " + person + " to send the notification to.";
						ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
								"PreBuildNotification", "mail", info);
						email.addTo(person);
					} else {
						String info = "No email detected. Not sending email to " + recipient;
						ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
								processName, "PreBuildNotification", "mail", info);
					}
				}

				emailSubject = emailSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((name))"), name);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((processName))"), processName);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((pipelineId))"), pipelineId);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((acronym))"), acronym);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((processDescription))"), processDescription);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((emailDescription))"), emailDescription);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				emailBody = emailBody.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailBody = emailBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailBody = emailBody.replaceAll(Pattern.quote("((name))"), name);
				emailBody = emailBody.replaceAll(Pattern.quote("((processName))"), processName);
				emailBody = emailBody.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				emailBody = emailBody.replaceAll(Pattern.quote("((pipelineId))"), pipelineId);
				emailBody = emailBody.replaceAll(Pattern.quote("((acronym))"), acronym);
				emailBody = emailBody.replaceAll(Pattern.quote("((processDescription))"), processDescription);
				emailBody = emailBody.replaceAll(Pattern.quote("((emailDescription))"), emailDescription);
				emailBody = emailBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailBody = emailBody.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				email.setSubject(emailSubject);
				email.setContent(emailBody, "text/html; charset=utf-8");

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
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
						"PreBuildNotification", "mail", info);
			} catch (Exception e) {
				String info = "Invalid Email Address.";
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
						"PreBuildNotification", "mail", info);
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
	}

}
