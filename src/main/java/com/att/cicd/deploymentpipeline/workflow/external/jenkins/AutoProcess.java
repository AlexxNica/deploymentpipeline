/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.att.cicd.deploymentpipeline.workflow.notification.ExceptionNotification;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;

public class AutoProcess implements JavaDelegate {

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}

	public void execute(DelegateExecution execution) throws Exception {
		java.util.Date dt = new java.util.Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(dt);
		execution.setVariable("startTime", String.valueOf(System.currentTimeMillis()));
		execution.setVariable("loop", true);
		String jenkinsURL = getVar(execution, "jenkinsURL");
		String jobName = getVar(execution, "jobName");
		String jenkinsUserName = getVar(execution, "jenkinsUserName");
		String jenkinsPassword = getVar(execution, "jenkinsPassword");
		String jenkinsParams = getVar(execution, "jenkinsParams");
		if (jenkinsParams.equals("%20")) {
			jenkinsParams = "";
		}
		String overrideNewOnboard = "";
		try {
			String params = jenkinsParams.substring(jenkinsParams.lastIndexOf("USE_ROOT_NS=") + 12,
					jenkinsParams.length());
			String pattern = "[A-Za-z]*_*[A-Za-z]*";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(params);
			if (m.find()) {
				overrideNewOnboard = m.group();
			}
		} catch (Exception e) {
		}
		String processName = getVar(execution, "processName");

		String processDescription = getVar(execution, "processDescription");
		String submoduleName = getVar(execution, "submodule_name");
		String pipelineName = getVar(execution, "pipeline_name");
		String recipient = getVar(execution, "assignTo");
		String gotsid = getVar(execution, "gotsid");
		String name = getVar(execution, "name");
		String acronym = getVar(execution, "acronym");
		String pipelineId = getVar(execution, "pipeline_id");
		String buildServerId = getVar(execution, "buildServerDropDown");
		String environmentId = getVar(execution, "environmentDropDown");
		String array_indexer = getVar(execution, "array_indexer");
		String deploymentName = Database.getDeploymentName(gotsid, pipelineName, submoduleName, name, array_indexer);
		if (!pipelineName.contains("jenkins")) {
			deploymentName = name;
		}
		boolean newOnboard = Database.isNewOnboardedApplication(gotsid);
		if (overrideNewOnboard.equalsIgnoreCase("false")) {
			newOnboard = true;
		}
		String reason = "";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"execute", "Beginning");

		// retrieve jenkins information from system connections
		Connection con = Database.getConnection();
		// New Jenkins Report - Method Call A - Save information in the
		// pipeline_jenkins_job db
		persistToJenkinsJobDB(gotsid, name, pipelineId, pipelineName, currentTime, recipient,deploymentName);
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT build_server_type, environment_url, username, password FROM camundabpmajsc6.build_server_environments where build_server_environment_id = ?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, buildServerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				if (rs.getString("build_server_type").equals("jenkins")) {
					jenkinsURL = rs.getString("environment_url");
					execution.setVariable("jenkinsURL", jenkinsURL);
					jenkinsUserName = rs.getString("username");
					jenkinsPassword = EncryptDecryptService.decrypt(rs.getString("password"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		con = Database.getConnection();
		ps = null;
		rs = null;
		sql = "Select environment_type, cluster_name, cluster_url, username, password from camundabpmajsc6.environments where environment_id=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, environmentId);
			rs = ps.executeQuery();
			if (rs.next()) {
				if (newOnboard) {
					jenkinsParams += "&TARGET_ENV="
							+ URLEncoder.encode(rs.getString("environment_type").toLowerCase(), "UTF-8")
							+ "&K8S_CLUSTER_URL=" + URLEncoder.encode(rs.getString("cluster_url"), "UTF-8")
							+ "&K8S_SERVICE_ACCOUNT=" + URLEncoder.encode(rs.getString("cluster_name"), "UTF-8")
							+ "&K8S_USERNAME=" + URLEncoder.encode(rs.getString("username"), "UTF-8") + "&K8S_PASSWORD="
							+ URLEncoder.encode(EncryptDecryptService.decrypt(rs.getString("password")), "UTF-8")
							+ "&ECO_PIPELINE_ID=" + URLEncoder.encode(pipelineId, "UTF-8");
				} else {
					jenkinsParams += "&USE_ROOT_NS=true&TARGET_ENV="
							+ URLEncoder.encode(rs.getString("environment_type").toLowerCase(), "UTF-8")
							+ "&K8S_CLUSTER_URL=" + URLEncoder.encode(rs.getString("cluster_url"), "UTF-8")
							+ "&K8S_SERVICE_ACCOUNT=" + URLEncoder.encode(rs.getString("cluster_name"), "UTF-8")
							+ "&K8S_USERNAME=" + URLEncoder.encode(rs.getString("username"), "UTF-8") + "&K8S_PASSWORD="
							+ URLEncoder.encode(EncryptDecryptService.decrypt(rs.getString("password")), "UTF-8")
							+ "&ECO_PIPELINE_ID=" + URLEncoder.encode(pipelineId, "UTF-8");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		CamundaConnector c = new CamundaConnector();

		try {
			String executeID = startJenkinsJob(jenkinsURL, jobName, jenkinsUserName, jenkinsPassword, jenkinsParams,
					gotsid, name, pipelineName, submoduleName, processName, pipelineId);
			execution.setVariable("jenkinsLoginFailed", "false");

			// boolean succeed = startJenkinsJob(jenkinsURL, jobName,
			// jenkinsUserName, jenkinsPassword, jenkinsParams,
			// gotsid, name, pipelineName, submoduleName, processName,
			// pipelineId);

			if (executeID == null || executeID.equals("")) {
				// New Jenkins Report______________
				Date dt1 = new Date();
				String endTime = sdf.format(dt1);
				// New Jenkins Report______________
				String info = "The Jenkins Build failed. Please double check that the Build Server System Connection, Jenkins Job Name, and Jenkins Parameters are all correct.";
				ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
						"AutoProcess", "execute", info, true);
				reason = "The Jenkins Build failed. Check to make sure that all of your configurations are correct.";
				ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
						processName, processDescription, submoduleName, pipelineName, acronym, pipelineId,
						deploymentName);
				// Set process description
				String description = Database.getDescription("descriptionFail", pipelineName, submoduleName,
						processName);
				String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deploymentName);
				execution.setVariable("processDescription", newDescription);
				execution.setVariable("isSuccess", false);
				// New Jenkins Report - Method Call C - Save information in the
				// pipeline_jenkins_job db
				persistFailure(gotsid, name, pipelineId, endTime, info);
			} else {
				execution.setVariable("executeID", executeID);
				execution.setVariable("isSuccess", true);
			}

			// if (!succeed) {
			// reason = "The Jenkins Build failed. Check to make sure that all
			// of your configurations are correct.";
			// ExceptionNotification.mail(recipient, jobName, gotsid, name,
			// reason, jenkinsURL, jenkinsUserName,
			// processName, processDescription, submoduleName, pipelineName,
			// acronym, pipelineId,
			// deploymentName);
			// }
		} catch (JenkinsBuildFailedException e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			String info = "The Jenkins build has failed. Please double check that the Build Server System Connection, Jenkins Job Name, and Jenkins Parameters are all correct.";
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", info, true);
			reason = "The Jenkins Build failed. Check to make sure that all of your configurations are correct.<br>"
					+ "Please click <a href=" + e.getMessage() + ">here</a> to check the logs.";
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, info);
		} catch (JenkinsLogInException e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			String info = "There was an issue logging into Jenkins. Please ensure that the username and password provided for your Build Server are correct in System Connections.";
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", info, true);
			reason = "There was an issue logging into Jenkins. Please ensure that the username and password provided for your Build Server are correct in System Connections.";
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, info);
			execution.setVariable("jenkinsLoginFailed", "true");
		} catch (JenkinsJobInitiationException e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			String info = "There was an issue finding the job with the job name and parameters provided. Please ensure that this job can be found here: "
					+ jenkinsURL + "/job/" + jobName;
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", info, true);
			reason = "There was an issue finding the job with the job name and parameters provided. Please ensure that this job can be found here: "
					+ jenkinsURL + "/job/" + jobName;
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db l
			persistFailure(gotsid, name, pipelineId, endTime, info);
		} catch (JenkinsJobStatusExceed1hrException e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			String info = "The Jenkins build took longer than 1 hour to complete, therefore the action has been cancelled. Please check your configurations for "
					+ name + " pipeline and try again.";
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", info, true);
			reason = "The Jenkins server could not find the job status for this build and the request timed out.";
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, info);
		} catch (JenkinsQueueExpireException e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			reason = "There was a timeout trying to build the Jenkins project. Finding the status of the queue took longer than the allowed time frame within the system, therefore the action was cancelled.";
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", reason, true);
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, reason);
		} catch (Exception e) {
			Date dt1 = new Date();
			String endTime = sdf.format(dt1);
			reason = "Unknown exception, please check with AT&T Eco support.";
			ApplicationLogger.callLog(gotsid, deploymentName, pipelineId, pipelineName, submoduleName, processName,
					"AutoProcess", "execute", reason, true);
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId, deploymentName);
			execution.setVariable("isSuccess", false);
			// New Jenkins Report - Method Call C - Save information in the
			// pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, reason);

			if ((boolean) execution.getVariable("isSuccess") == false) {
				// Set process description
				String description = Database.getDescription("descriptionFail", pipelineName, submoduleName,
						processName);
				String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deploymentName);
				execution.setVariable("processDescription", newDescription);
			}
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
					"execute", "End");
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "CheckStatus",
					"execute", "START");
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "CheckStatus",
					"execute", "Starting the looping process to find the status...");
		}
	}

	// New Jenkins Report - Method Call C - Save information in the
	// pipeline_jenkins_job db
	private void persistFailure(String gotsid, String name, String pipelineId, String endTime, String reason) {
		ResultSet rs = null;
		Boolean isSuccess = false;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "UPDATE pipeline_jenkins_job SET endTime=?, isSuccess=?, failReason=? "
				+ "WHERE gots_id=? and name=? and pipeline_id=? and endTime IS NULL";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, endTime);
			ps.setBoolean(2, isSuccess);
			ps.setString(3, reason);
			ps.setString(4, gotsid);
			ps.setString(5, name);
			ps.setString(6, pipelineId);
			rs = ps.executeQuery();
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
				ex.printStackTrace();
			}
		}
	}

	// New Jenkins Report - Method Call A - Save information in the
	// pipeline_jenkins_job db
	private void persistToJenkinsJobDB(String gotsid, String name, String pipelineId, String pipelineName,
			String currentTime, String recipient, String deploymentName) {
		List<String> recipients = new ArrayList<String>();
		if (recipient == null || recipient.trim().isEmpty()) {
			recipient = AAFConnector.getAdminList(gotsid);
		}
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "INSERT INTO `camundabpmajsc6`.`pipeline_jenkins_job`(`gots_id`,`name`,`pipeline_id`, `pipeline_name`,`startTime`, `assignTo`,deploy_config_name) "
				+ "values (?,?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipelineId);
			ps.setString(4, pipelineName);
			ps.setString(5, currentTime);
			ps.setString(6, recipient);
			ps.setString(7, deploymentName);
			ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {

				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static String startJenkinsJob(String jenkinsURL, String jobName, String jenkinsUserName,
			String jenkinsPassword, String jenkinsParams, String gotsid, String name, String pipelineName,
			String submoduleName, String processName, String pipelineId) throws IOException, JenkinsLogInException,
			JenkinsJobInitiationException, JenkinsJobStatusExceed1hrException, InterruptedException,
			JenkinsQueueExpireException, JenkinsBuildFailedException {
		String location = null;
		String executeID = null;
		String status = null;
		String refreshRate = Database.getSystemConfig("jenkinsRefreshRateSeconds");
		String queueExpireMins = Database.getSystemConfig("jenkinsQueueExpireMins");
		String statusExpireMins = Database.getSystemConfig("jenkinsJobStatusExpireMins");

		String plain = jenkinsUserName + ":" + jenkinsPassword;
		byte[] encodedBytes = Base64.encodeBase64(plain.getBytes());
		String auth = "Basic " + (new String(encodedBytes));

		// 1 start the jenkins job based on if the job requires parameters
		// or not.
		if (jenkinsParams == null || jenkinsParams.equals("")) {
			// 1a get job's queue id from the location header
			location = startJob(jenkinsURL + "/job/" + jobName + "/build", auth, gotsid, name, pipelineName,
					submoduleName, processName, pipelineId);
		} else {
			// 1b get job's queue id from the location header when user has
			// params
			location = startJobWithParams(jenkinsURL + "/job/" + jobName + "/buildWithParameters",
					jenkinsParams, auth, gotsid, name, pipelineName, submoduleName, processName, pipelineId);
		}

		// 2 get the status of the queue until a job execution id comes up,
		// max 10 minutes
		long startTime = System.currentTimeMillis();
		String info = "Looping every " + refreshRate + " Seconds and trying to get ExecutionID from Queue ID";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"startJenkinsJob", info);
		while (executeID == null || executeID.equals("")) {
			executeID = checkQueueStatus(location + "api/json", auth);
			// Thread.sleep(2000);
			Thread.sleep(Integer.parseInt(refreshRate) * 1000);
			// 2a only allow to run for 10 min
			if (System.currentTimeMillis() - startTime >= Integer.parseInt(queueExpireMins) * 60000) {
				info = "Finding the status of the queue in Jenkins took longer than 10 minutes. The Jenkins Build Server must be having issues, please try again at another time.";
				ApplicationLogger.callLog(gotsid, jenkinsUserName, pipelineId, pipelineName, submoduleName, processName,
						"AutoProcess", "startJenkinsJob", info, true);
				throw new JenkinsQueueExpireException("Finding the status of the Queue took longer than 10 minutes.");
			}
		}

		return executeID;

		// 3 get the status of the job, max 1 hour
		// startTime = System.currentTimeMillis();
		// while (status == null || status.equals("") || status.equals("null"))
		// {
		// status = getStatus(executeID + "api/json", auth);
		// String info = "Looping every "+ refreshRate + " seconds and status
		// is: " + status;
		// ApplicationLogger.callLog(gotsid, name,pipelineId, pipelineName,
		// submoduleName, processName, "AutoProcess", "startJenkinsJob", info);
		// // Thread.sleep(2000);
		// Thread.sleep(Integer.parseInt(refreshRate) * 1000);
		// // 3a only allow to run for 1 hour
		// if (System.currentTimeMillis() - startTime >=
		// Integer.parseInt(statusExpireMins) * 60000) {
		// throw new JenkinsJobStatusExceed1hrException("Finding the job status
		// took longer than 1 hour.");
		// }
		// }
		//
		// if (status.equals("SUCCESS")) {
		// String info = "SUCCESS";
		// ApplicationLogger.callLog(gotsid, name,pipelineId, pipelineName,
		// submoduleName, processName, "AutoProcess", "startJenkinsJob", info);
		// return true;
		// } else {
		// String info = "NOT SUCCESS";
		// ApplicationLogger.callLog(gotsid, name,pipelineId, pipelineName,
		// submoduleName, processName, "AutoProcess", "startJenkinsJob", info);
		// throw new JenkinsBuildFailedException(executeID + "consoleText");
		// }
	}

	private static String startJob(String url, String auth, String gotsid, String name, String pipelineName,
			String submoduleName, String processName, String pipelineId)
			throws IOException, JenkinsLogInException, JenkinsJobInitiationException {
		String location = null;
		String info = "Trying to find location header. URL: " + url;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"startJob", info);
		try {
			HttpURLConnection conn = null;
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestProperty("Authorization", auth);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			location = conn.getHeaderField("Location");
			info = Integer.toString(conn.getResponseCode());
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
					"startJob", info);
			if (conn.getResponseCode() == 401 || conn.getResponseCode() == 403) {
				throw new JenkinsLogInException("Log In issue with Jenkins.");
			} else if (conn.getResponseCode() == 400) {
				throw new JenkinsJobInitiationException(conn.getResponseMessage());
			} else if (conn.getResponseCode() == 404) {
				throw new JenkinsJobInitiationException(
						"The server cannot or will not process the request due to an apparent Jenkins error");
			}
			conn.disconnect();
		} catch (SocketTimeoutException e) {
			throw new JenkinsJobInitiationException("The url provided for the Build Server is not responding.");
		}
		info = "Location is at: " + location;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"startJob", info);
		return location;
	}

	private static String startJobWithParams(String url, String jenkinsParams, String auth, String gotsid, String name,
			String pipelineName, String submoduleName, String processName, String pipelineId)
			throws IOException, JenkinsLogInException, JenkinsJobInitiationException {
		String location = null;
		String info = "Trying to find location header with user's params, url:" + url + "?" + jenkinsParams;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"startJobWithParams", info);
		try {
			HttpURLConnection conn = null;
			URL u = new URL(url + "?" + jenkinsParams);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestProperty("Authorization", auth);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.connect();
			location = conn.getHeaderField("Location");
			conn.disconnect();
			if (conn.getResponseCode() == 401 || conn.getResponseCode() == 403) {
				throw new JenkinsLogInException("Log In issue with Jenkins.");
			} else if (conn.getResponseCode() == 400) {
				throw new JenkinsLogInException("Log In issue with Jenkins.");
			} else if (conn.getResponseCode() == 404) {
				throw new JenkinsJobInitiationException(
						"The server cannot or will not process the request due to an apparent Jenkins error");
			} else if (conn.getResponseCode() == 500) {
				throw new JenkinsJobInitiationException("This build is not parameterized in Jenkins. Please remove Jenkins Parameters.");
			}
		} catch (SocketTimeoutException e) {
			throw new JenkinsJobInitiationException("The url provided for the Build Server is not responding.");
		}

		info = "Location is at: " + location;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName, "AutoProcess",
				"startJobWithParams", info);
		return location;
	}

	private static String checkQueueStatus(String url, String auth) throws IOException, InterruptedException {
		String result = "";
		String output = "";
		String executable = "";
		try {
			HttpURLConnection conn = null;
			URL u = new URL(url);
			conn = (HttpURLConnection) u.openConnection();
			conn.setRequestProperty("Authorization", auth);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.connect();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			StringBuilder buffer = new StringBuilder();
			while ((output = br.readLine()) != null) {
				buffer.append(output);
			}
			output = buffer.toString();
			conn.disconnect();

			if (output.contains("executable")) {
				JsonElement jelement = new JsonParser().parse(output);
				JsonObject jobject = jelement.getAsJsonObject();
				executable = jobject.get("executable").toString();
				jelement = new JsonParser().parse(executable);
				jobject = jelement.getAsJsonObject();
				result = jobject.get("url").toString().replaceAll("\"", "");

				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executable;
	}

	// private static String getStatus(String url, String auth) throws
	// IOException {
	// String output = "";
	// HttpURLConnection conn = null;
	// URL u = new URL(url);
	// conn = (HttpURLConnection) u.openConnection();
	// conn.setRequestProperty("Authorization", auth);
	// conn.setRequestMethod("GET");
	// conn.setRequestProperty("Content-Type", "application/json");
	//
	// BufferedReader br = new BufferedReader(new
	// InputStreamReader((conn.getInputStream())));
	// StringBuilder buffer = new StringBuilder();
	// while ((output = br.readLine()) != null) {
	// buffer.append(output);
	// }
	// output = buffer.toString();
	//
	// JsonElement jelement = new JsonParser().parse(output);
	// JsonObject jobject = jelement.getAsJsonObject();
	// String building = jobject.get("building").toString();
	// if (building.equals("true"))
	// return "";
	// output = jobject.get("result").toString();
	// conn.disconnect();
	// return output.replaceAll("\"", "");
	// }
}
