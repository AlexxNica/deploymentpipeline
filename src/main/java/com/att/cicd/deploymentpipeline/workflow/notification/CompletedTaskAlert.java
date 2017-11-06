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

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

public class CompletedTaskAlert implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String gotsid = getVar(execution, "gotsid");
		String name = getVar(execution, "name");
		String processName = getVar(execution, "processName");
		String submoduleName = getVar(execution, "submodule_name");
		String pipelineName = getVar(execution, "pipeline_name");
		String pipelineId = getVar(execution, "pipeline_id");
		String array_indexer = getVar(execution, "array_indexer");
		boolean isApproved = (boolean) execution.getVariable("isApproved");
		String recipient = "";
		if (getVar(execution, "reviewSubmodule").equals("false")) {
			recipient = Database.getVariableWithIndex(gotsid, name, pipelineName, submoduleName, processName,
					"assignTo", array_indexer);
		} else {
			recipient = Database.getVariable(gotsid, name, pipelineName, submoduleName, processName, "assignTo");
		}

		String acronym = getVar(execution, "acronym");
		String processDescription = getVar(execution, "processDescription");
		String approver = getVar(execution, "approver");
		String deploymentName = Database.getDeploymentName(gotsid, pipelineName, submoduleName, name, array_indexer);
		if (!pipelineName.contains("jenkins")) {
			deploymentName = name;
		} else if (getVar(execution, "reviewSubmodule").equals("true")) {
			deploymentName = name;
		} else {
			deploymentName = Database.getDeploymentName(gotsid, pipelineName, submoduleName, name, array_indexer);
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
				"CompletedTaskAlert", "execute", "Beginning");
		if (isApproved == true) {
			String approverId = approver.substring(0, approver.indexOf('@'));
			approver = approverId;
			String info = "The pipeline (" + name + ") has been continued to the next step of the pipeline by: "
					+ approver;
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"CompletedTaskAlert", "execute", info, true);
			mail(recipient, gotsid, name, processName, processDescription, submoduleName, pipelineName, acronym,
					approver, deploymentName, pipelineId);
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
				"CompletedTaskAlert", "execute", "End");

	}

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}


	public static void mail(String recipient, String gotsid, String name, String processName, String processDescription,
			String submoduleName, String pipelineName, String acronym, String approver, String deploymentName,
			String pipelineId) {
		recipient = recipient.trim();
		String info = "recipient: " + recipient + " | approver: " + approver;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
				"CompletedTaskAlert", "mail", info);
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
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='taskCompleteBody') as taskCompleteBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='taskCompleteSubject') as taskCompleteSubject;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				exceptionBody = rs.getString("taskCompleteBody");
				exceptionSubject = rs.getString("taskCompleteSubject");
			}

			List<String> recipients = new ArrayList<String>();
			boolean multipleRecipients = false;
			if (recipient == null || recipient.equals("")) {
				info = "There was no email address added, getting admin from aaf";
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
						"CompletedTaskAlert", "mail", info);
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
						email.addTo(personAddress);
						info = "Added " + personAddress + " to send the notification to.";
						ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
								processName, "CompletedTaskAlert", "mail", info);
						recipient += person + ", ";
						} else {
							info = "No email detected. Not sending email to " + person;
							ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
									processName, "CompletedTaskAlert", "mail", info);
						}
					}
				} else {
					String person = UserInfoService.getEmailAddress(recipient);
					if(!person.equals(UserInfoService.NO_EMAIL)) {
						recipient = recipient + ", ";
						info = "Added " + person + " to send the notification to.";
						ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
								"CompletedTaskAlert", "mail", info);
						email.addTo(person);
					} else {
						info = "No email detected. Not sending email to " + recipient;
						ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName,
								processName, "CompletedTaskAlert", "mail", info);
					}
				}

				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((emailTo))"), recipient);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((approver))"), approver);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((name))"), name);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((processName))"), processName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((processDescription))"),
						processDescription);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((pipelineName))"), pipelineName);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((acronym))"), acronym);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				exceptionSubject = exceptionSubject.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((emailTo))"), recipient);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((approver))"), approver);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((name))"), name);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((processName))"), processName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((processDescription))"), processDescription);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((submoduleName))"), submoduleName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((pipelineName))"), pipelineName);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((acronym))"), acronym);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				exceptionBody = exceptionBody.replaceAll(Pattern.quote("((deploymentName))"), deploymentName);

				email.setSubject(exceptionSubject);
				email.setContent(exceptionBody, "text/html; charset=utf-8");

				if (multipleRecipients) {
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
					info = "Email sent successfully.....";
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
							"CompletedTaskAlert", "mail", info);
				} else {
					info = "One one person assigned to this task, no email sent.";
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
							"CompletedTaskAlert", "mail", info);
				}
			} catch (Exception e) {
				info = "Invalid Email Address.";
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
						"CompletedTaskAlert", "mail", info);
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
