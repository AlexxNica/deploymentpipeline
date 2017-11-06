/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.notification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

public class EmailNotification {

	public static void main(String[] args) {
		
	}


	public static void applicationContactRequest(String gotsid, String reason, String requesterId) {
		String info = "";
		String requesterName = requesterId;
		String emailBody = "";
		String emailFromAddress = System.getProperty("emailfromaddress");
		String emailHost = System.getProperty("emailhostname");
		String emailUsername = System.getProperty("emailusername");
		String emailPassword = System.getProperty("emailpassword");
		String emailSenderName = System.getProperty("emailsendername");
		String clientURL = System.getProperty("clienturl");
		int emailPort = Integer.valueOf(System.getProperty("emailsmtpport"));
		String emailSubject = "";
		String acronym = "";

		String appContact = "";
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='gotsApplicationContactRequest') as emailBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='gotsApplicationContactRequestSubject') as emailSubject, "
				+ "(SELECT Appl_Contact from asf_gots where gots_id=?) as appContact, "
				+ "(SELECT Acronym from asf_gots where gots_id=?) as acronym;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, gotsid);
			rs = ps.executeQuery();
			while (rs.next()) {
				emailBody = rs.getString("emailBody");
				emailSubject = rs.getString("emailSubject");
				appContact = rs.getString("appContact");
				acronym = rs.getString("acronym");
			}
			info = "Sending email to application contact: " + appContact;
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "applicationContactRequest", info);
			Email email = new SimpleEmail();
			email.setHostName(emailHost);
			// email.setAuthentication(emailUsername, emailPassword);
			try {
				email.setFrom(emailFromAddress, emailSenderName);
				email.setSmtpPort(emailPort);

				emailSubject = emailSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((reason))"), reason);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((requesterName))"), requesterName);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((requesterId))"), requesterId);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((appContact))"), appContact);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((acronym))"), acronym);

				emailBody = emailBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailBody = emailBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailBody = emailBody.replaceAll(Pattern.quote("((reason))"), reason);
				emailBody = emailBody.replaceAll(Pattern.quote("((requesterName))"), requesterName);
				emailBody = emailBody.replaceAll(Pattern.quote("((requesterId))"), requesterId);
				emailBody = emailBody.replaceAll(Pattern.quote("((appContact))"), appContact);
				emailBody = emailBody.replaceAll(Pattern.quote("((acronym))"), acronym);

				email.setSubject(emailSubject);
				email.addTo(UserInfoService.getEmailAddress(appContact));
				email.addCc(UserInfoService.getEmailAddress(requesterId));
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
				info = "Email Sent";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "applicationContactRequest", info);
				info = "End";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "applicationContactRequest", info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = "Error sending email.";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "applicationContactRequest", info);
			info = "End";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "applicationContactRequest", info);
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


	public static void administratorAccessRequest(String acronym, String reason, String requesterId) {
		String info = "";
		String requesterName = requesterId;
		String emailBody = "";
		String emailFromAddress = System.getProperty("emailfromaddress");
		String emailHost = System.getProperty("emailhostname");
		String emailUsername = System.getProperty("emailusername");
		String emailPassword = System.getProperty("emailpassword");
		String emailSenderName = System.getProperty("emailsendername");
		String clientURL = System.getProperty("clienturl");
		int emailPort = Integer.valueOf(System.getProperty("emailsmtpport"));
		String emailSubject = "";
		String gotsid = "";

		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='administratorAccessRequest') as emailBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='administratorAccessRequestSubject') as emailSubject, "
				+ "(SELECT Appl_Contact from asf_gots where Acronym=?) as appContact, "
				+ "(SELECT Acronym from asf_gots where Acronym=?) as acronym,"
				+ "(SELECT gots_id from asf_gots where acronym=?) as gotsid;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, acronym);
			ps.setString(2, acronym);
			ps.setString(3, acronym);
			rs = ps.executeQuery();
			while (rs.next()) {
				emailBody = rs.getString("emailBody");
				emailSubject = rs.getString("emailSubject");
				gotsid = rs.getString("gotsid");
			}
			Email email = new SimpleEmail();
			email.setHostName(emailHost);
			// email.setAuthentication(emailUsername, emailPassword);
			try {
				email.setFrom(emailFromAddress, emailSenderName);
				email.setSmtpPort(emailPort);

				List<String> recipients = new ArrayList<String>();
				boolean multipleRecipients = false;
				String recipient = AAFConnector.getAdminList(gotsid);
				info = "Sending admin requst email(s) to: " + recipient;
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "administratorAccessRequest", info);
				if (recipient.contains(",")) {
					recipients = Arrays.asList(recipient.split("\\s*,\\s*"));
					multipleRecipients = true;
				} else if (recipient.contains(";")) {
					recipients = Arrays.asList(recipient.split("\\s*;\\s*"));
					multipleRecipients = true;
				}

				if (multipleRecipients) {
					recipient = "";
					for (String person : recipients) {

						String personAddress = UserInfoService.getEmailAddress(person);
						if(!personAddress.equals(UserInfoService.NO_EMAIL)) {
							recipient += person + ", ";
							email.addTo(personAddress);
						} else {
						}
					}
				} else {
					String person = UserInfoService.getEmailAddress(recipient);
					if(!person.equals(UserInfoService.NO_EMAIL)) {
						email.addTo(person);
						recipient = recipient + ", ";
					} else {
					}
				}

				emailSubject = emailSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((reason))"), reason);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((requesterName))"), requesterName);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((requesterId))"), requesterId);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((acronym))"), acronym);

				emailBody = emailBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailBody = emailBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailBody = emailBody.replaceAll(Pattern.quote("((reason))"), reason);
				emailBody = emailBody.replaceAll(Pattern.quote("((requesterName))"), requesterName);
				emailBody = emailBody.replaceAll(Pattern.quote("((requesterId))"), requesterId);
				emailBody = emailBody.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailBody = emailBody.replaceAll(Pattern.quote("((acronym))"), acronym);

				email.setSubject(emailSubject);
				email.addCc(UserInfoService.getEmailAddress(requesterId));
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
				info = "Email Sent";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "administratorAccessRequest", info);
				info = "End";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "administratorAccessRequest", info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = "Error sending email.";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "administratorAccessRequest", info);
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "administratorAccessRequest", info);
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

	public static void productionRestricted(String acronym, String name, String gotsid) {
		String info = "START";
		ApplicationLogger.callLog(gotsid, name, "", "", "", "", "EmailNotification", "productionRestricted", info);
		String emailBody = "";
		String emailFromAddress = System.getProperty("emailfromaddress");
		String emailHost = System.getProperty("emailhostname");
		String emailUsername = System.getProperty("emailusername");
		String emailPassword = System.getProperty("emailpassword");
		String emailSenderName = System.getProperty("emailsendername");
		String clientURL = System.getProperty("clienturl");
		int emailPort = Integer.valueOf(System.getProperty("emailsmtpport"));
		String emailSubject = "";

		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select (SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='productionRestricted') as emailBody, "
				+ "(SELECT attribute_value FROM camundabpmajsc6.system_config where attribute_name='productionRestrictedSubject') as emailSubject;";
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

				List<String> recipients = new ArrayList<String>();
				boolean multipleRecipients = false;
				String recipient = AAFConnector.getAdminList(gotsid);
				info = "Sending admin requst email(s) to: " + recipient;
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "productionRestricted", info);
				if (recipient.contains(",")) {
					recipients = Arrays.asList(recipient.split("\\s*,\\s*"));
					multipleRecipients = true;
				} else if (recipient.contains(";")) {
					recipients = Arrays.asList(recipient.split("\\s*;\\s*"));
					multipleRecipients = true;
				}

				if (multipleRecipients) {
					recipient = "";
					for (String person : recipients) {
						String personAddress = UserInfoService.getEmailAddress(person);
						if(!personAddress.equals(UserInfoService.NO_EMAIL)) {
							email.addTo(personAddress);
							recipient += person + ", ";
						} else {
						}
					}
				} else {
					String person = UserInfoService.getEmailAddress(recipient);
					if(!person.equals(UserInfoService.NO_EMAIL)) {
						email.addTo(person);
						recipient = recipient + ", ";
					} else {
					}
				}

				emailSubject = emailSubject.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailSubject = emailSubject.replaceAll(Pattern.quote("((acronym))"), acronym);

				emailBody = emailBody.replaceAll(Pattern.quote("((gotsid))"), gotsid);
				emailBody = emailBody.replaceAll(Pattern.quote("((clientURL))"), clientURL);
				emailBody = emailBody.replaceAll(Pattern.quote("((emailTo))"), recipient);
				emailBody = emailBody.replaceAll(Pattern.quote("((acronym))"), acronym);
				emailBody = emailBody.replaceAll(Pattern.quote("((name))"), name);

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
				info = "Email Sent";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "productionRestricted", info);
				info = "End";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "productionRestricted", info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = "Error sending email.";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "productionRestricted", info);
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "EmailNotification", "productionRestricted", info);
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
