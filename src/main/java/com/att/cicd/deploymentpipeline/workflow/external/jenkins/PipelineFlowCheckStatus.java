/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.external.jenkins;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;
import com.att.cicd.deploymentpipeline.util.JenkinsConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.att.cicd.deploymentpipeline.workflow.notification.ExceptionNotification;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public class PipelineFlowCheckStatus implements JavaDelegate {

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}

	public void execute(DelegateExecution execution) throws Exception {
		String info = "";
		long startTime = Long.parseLong(getVar(execution, "startTime"));
		execution.setVariable("startTime", String.valueOf(System.currentTimeMillis()));
		execution.setVariable("loop", true);
		String gotsid = getVar(execution, "gotsid");
		String name = getVar(execution, "name");
		String processName = getVar(execution, "processName");
		String submoduleName = getVar(execution, "submodule_name");
		String pipelineId = getVar(execution, "pipeline_id");
		String pipelineName = getVar(execution, "pipeline_name");
		String acronym = getVar(execution, "acronym");
		String processDescription = getVar(execution, "processDescription");
		String emailDescription = Database.getDescription("emailDescription", pipelineName, submoduleName, processName);
		String deployment_indexer = getVar(execution, "deployment_indexer");
		String enabled = getVar(execution, "enable_notification");
		String recipient = getVar(execution, "notification_list");
		String deployConfigName = getVar(execution, "deployConfigName");
		String pipeline_flow_id = getVar(execution, "pipeline_flow_id");
		String build_server_id = getVar(execution, "build_server_id");
		String environment_id = getVar(execution, "environment_id");
		String automatic = getVar(execution, "automatic");
		Map environment = Database.getEnvironmentByID(environment_id);
		Map build_server = Database.getBuildServerByID(build_server_id);
		Map pipelineConfigInfo = Database.getPipelineFlowConfig(pipeline_flow_id, deployConfigName, name);
		String jenkinsURL = (String) build_server.get("environment_url");
		String jobName = (String) pipelineConfigInfo.get("jenkins_job");
		String jenkinsUserName = (String) build_server.get("username");
		String jenkinsPassword = EncryptDecryptService.decrypt(build_server.get("password").toString());
		String jenkinsParams = (String) pipelineConfigInfo.get("jenkins_params");
		String executeID = getVar(execution, "executeID");
		String phase = "";
		if (jenkinsParams.equals("%20")) {
			jenkinsParams = "";
		} else {
			try {
				String params = jenkinsParams.substring(jenkinsParams.lastIndexOf("PHASE=") + 6,
						jenkinsParams.length());
				String pattern = "[A-Z]*_*[A-Z]*";
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(params);
				if (m.find()) {
					phase = m.group();
				}
			} catch (Exception e) {

			}
		}
		String statusExpireMins = Database.getSystemConfig("jenkinsJobStatusExpireMins");
		String reason = "", k8s_environment_url = "", k8s_environment_type = "";
		if (!environment.isEmpty()) {
			k8s_environment_url = environment.get("cluster_url").toString();
			k8s_environment_type = environment.get("environment_type").toString();
			Database.update(gotsid, name, pipelineName, submoduleName, processName, recipient, false, automatic,
					pipelineId, "", true, k8s_environment_url, k8s_environment_type, phase);
			updateJenkinsJobDB(gotsid, name, pipelineId, executeID, phase, k8s_environment_type, k8s_environment_url,
					executeID);
		} else {
			Database.update(gotsid, name, pipelineName, submoduleName, processName, recipient, false, automatic,
					pipelineId, "");
			updateJenkinsJobDB(gotsid, name, pipelineId, executeID, phase, "", "", executeID);
		}
		if (System.currentTimeMillis() - startTime >= Integer.parseInt(statusExpireMins) * 60000) {
			// New Jenkins Report------------------
			java.util.Date dt1 = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endTime = sdf.format(dt1);
			// New Jenkins Report------------------
			reason = "End";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", reason);
			reason = "There was a timeout trying to build the Jenkins project. Finding the status of the queue took longer than the allowed time frame within the system. Please try again at another time.";
			ApplicationLogger.callLog(gotsid, deployConfigName, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", reason, true);
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId,
					deployConfigName);
			execution.setVariable("isSuccess", false);
			execution.setVariable("loop", false);
			String description = Database.getDescription("descriptionFail", pipelineName, submoduleName, processName);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployConfigName);
			execution.setVariable("processDescription", newDescription);
			// Save information in the pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, reason);
			return;
		}

		String plain = jenkinsUserName + ":" + jenkinsPassword;
		byte[] encodedBytes = Base64.encodeBase64(plain.getBytes());
		String auth = "Basic " + (new String(encodedBytes));

		String status = "";
		try {
			status = getStatus(executeID + "api/json", auth);
			execution.setVariable("taskStatus", getJenkinsPipelineStatus(gotsid, name, pipelineId, pipelineName,
					submoduleName, processName, pipelineId, jenkinsUserName, jenkinsPassword, executeID, jobName));
		} catch (Exception e) {
			// New Jenkins Report------------------
			java.util.Date dt1 = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endTime = sdf.format(dt1);
			// New Jenkins Report------------------
			info = "NOT SUCCESS";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info);
			info = "END";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info);
			execution.setVariable("loop", false);
			execution.setVariable("isSuccess", false);
			String description = Database.getDescription("descriptionFail", pipelineName, submoduleName, processName);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployConfigName);
			execution.setVariable("processDescription", newDescription);
			info = "The Jenkins Build failed. Please check the Jenkins Console Logs here: " + executeID;
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info, true);
			reason = "The Jenkins Build failed. Check to make sure that all of your configurations are correct.<br>"
					+ "Please click <a href=" + executeID + "consoleText>here</a> to check the logs.";
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId,
					deployConfigName);
			// Save information in the pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, reason);
			return;
		}

		if (status == null || status.equals("") || status.equals("null")) {
			execution.setVariable("loop", true);
			execution.setVariable("isSuccess", false);
		} else if (status.equals("SUCCESS")) {
			java.util.Date dt1 = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endTime = sdf.format(dt1);
			// Save information in the pipelien_jenkins_job_db
			persistSuccess(gotsid, name, pipelineId, endTime);
			execution.setVariable("isSuccess", true);
			execution.setVariable("loop", false);
			info = "SUCCESS";
			persistJenkinsPipelineStatus(gotsid, name, pipelineName, submoduleName, processName, pipelineId,
					jenkinsUserName, jenkinsPassword, executeID, jobName);
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info);
			info = "The Jenkins Job has successfully built.";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info, true);
			Database.update(gotsid, name, pipelineName, submoduleName, processName, recipient, false, "", pipelineId,
					"", true, k8s_environment_url, k8s_environment_type, phase);
		} else {
			java.util.Date dt1 = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endTime = sdf.format(dt1);
			info = "NOT SUCCESS";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info);
			info = "END";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info);
			execution.setVariable("loop", false);
			execution.setVariable("isSuccess", false);
			String description = Database.getDescription("descriptionFail", pipelineName, submoduleName, processName);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployConfigName);
			execution.setVariable("processDescription", newDescription);
			info = "The Jenkins Build failed. Please check the Jenkins Console Logs here: " + executeID;
			persistJenkinsPipelineStatus(gotsid, name, pipelineName, submoduleName, processName, pipelineId,
					jenkinsUserName, jenkinsPassword, executeID, jobName);
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipelineName, submoduleName, processName,
					"PipelineFlowCheckStatus", "execute", info, true);
			reason = "The Jenkins Build failed. Check to make sure that all of your configurations are correct.<br>"
					+ "Please click <a href=" + executeID + "consoleText>here</a> to check the logs.";
			ExceptionNotification.mail(recipient, jobName, gotsid, name, reason, jenkinsURL, jenkinsUserName,
					processName, processDescription, submoduleName, pipelineName, acronym, pipelineId,
					deployConfigName);
			// Save information in the pipeline_jenkins_job db
			persistFailure(gotsid, name, pipelineId, endTime, reason);
		}
	}

	private void updateJenkinsJobDB(String gotsid, String name, String pipelineId, String executeID, String phase,
			String envType, String k8s_environment_url, String jenkinsURL) {
		// Extract the Jenkins Build Id
		String urlString = executeID;
		String[] parts = urlString.split("/");
		String buildID = parts[parts.length - 1];
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		// "Update deployment_config set build_server_id=?, environment_id=?,
		// name=?, updated_date=NOW(), "
		// + "modifier=?, description=?, enable_notification=?,
		// notification_list=?, enable_approval=?, "
		// + "approval_list=?, configuration_repository_id=? where
		// deployment_config_id=?;";
		String sql = "UPDATE pipeline_jenkins_job SET execute_id=?, phase=?, env_type=?, k8_env_url=?, job_url=? "
				+ "WHERE gots_id=? and name=? and pipeline_id=? and endTime IS NULL";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, buildID);
			ps.setString(2, phase);
			ps.setString(3, envType);
			ps.setString(4, k8s_environment_url);
			ps.setString(5, jenkinsURL);
			ps.setString(6, gotsid);
			ps.setString(7, name);
			ps.setString(8, pipelineId);
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

	private void persistSuccess(String gotsid, String name, String pipelineId, String endTime) {
		ResultSet rs = null;
		Boolean isSuccess = true;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();

		// String sql = "UPDATE `camundabpmajsc6`.`pipeline_jenkins_job` SET
		// `endTime`=?,'isSuccess'=?"
		// + "WHERE gots_id=? and name=? and pipeline_id=? and endTime IS NULL";
		String sql = "UPDATE pipeline_jenkins_job SET endTime=?, isSuccess=? "
				+ "WHERE gots_id=? and name=? and pipeline_id=? and endTime IS NULL";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, endTime);
			ps.setBoolean(2, isSuccess);
			ps.setString(3, gotsid);
			ps.setString(4, name);
			ps.setString(5, pipelineId);
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

	private String getJenkinsPipelineStatus(String gotsid, String name, String pipelineId, String pipelineName,
			String submoduleName, String processName, String pipelineId2, String jenkinsUserName,
			String jenkinsPassword, String executeID, String jenkinsJobName) throws IOException {
		String jenkinsStatus = "";
		try {
			// Make the jenkins call to grab the status- one you make with the
			// persistJenkinsPipelineStatus
			int gots_app_stats_id = getGotsAppStatsId(gotsid, name, pipelineName, processName, submoduleName,
					pipelineId);
			String attributeName = "jobName";
			// Call Jenkins to get the other params
			HttpURLConnection connection = null;
			// Extract the Jenkins Build Id
			String urlString = executeID;
			String[] parts = urlString.split("/");
			String buildID = parts[parts.length - 1];
			// Connect to Jenkins
			String path = "/wfapi/runs?since=%23" + buildID;
			String method = "GET";
			String output = "";
			String links = "";
			JsonElement jelement1;
			JsonElement jelement2;
			connection = JenkinsConnector.getConnection(path, method, jenkinsJobName, jenkinsUserName, jenkinsPassword);
			// Read the Response stream
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			StringBuilder buffer = new StringBuilder();
			while ((output = br.readLine()) != null) {
				buffer.append(output);
			}
			output = buffer.toString();
			if (output.contains("_links")) {
				JsonElement jelement = new JsonParser().parse(output);
				JsonArray jarray = jelement.getAsJsonArray();
				for (int i = 0; i < jarray.size(); i++) {
					jelement2 = jarray.getAsJsonArray().get(i).getAsJsonObject().get("id");
					jelement1 = jarray.getAsJsonArray().get(i).getAsJsonObject().get("stages");
					for (int j = 0; j < jelement1.getAsJsonArray().size(); j++) {
						jenkinsStatus = jelement1.getAsJsonArray().getAsJsonArray().get(j).getAsJsonObject().get("name")
								.toString().replaceAll("[^a-z A-Z]", "");
					}
				}
			}
			connection.disconnect();
		} catch (Exception e) {
			return "Jenkins: In Progress";
		}
		return "Jenkins:" + jenkinsStatus;

	}

	private static void persistJenkinsPipelineStatus(String gotsid, String name, String pipelineName,
			String submoduleName, String processName, String pipelineId, String jenkinsUserName, String jenkinsPassword,
			String executeID, String jenkinsJobName) throws IOException {
		try {
			// Find the gots_app_stats_id
			int gots_app_stats_id = getGotsAppStatsId(gotsid, name, pipelineName, processName, submoduleName,
					pipelineId);
			// Find the jenkins job name from the DB
			String attributeName = "jobName";
			// Call pipeline_jenkins_job to get the id
			int pipeline_jenkins_job_id = getPipleineJenkinsJobId(gotsid, name, pipelineId);
			// Call Jenkins to get the other params
			getJenkinsParameter(jenkinsJobName, jenkinsUserName, jenkinsPassword, executeID, gots_app_stats_id,
					pipeline_jenkins_job_id);
		} catch (Exception e) {

		}
	}

	private static int getPipleineJenkinsJobId(String gotsid, String name, String pipelineId) {
		int pipeline_jenkins_job_id = 0;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "SELECT pipeline_jenkins_job_id from camundabpmajsc6.pipeline_jenkins_job where  gots_id=? and name=?  and pipeline_id=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipelineId);
			rs = ps.executeQuery();
			if (rs.next()) {
				pipeline_jenkins_job_id = rs.getInt("pipeline_jenkins_job_id");
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
			} catch (Exception ex) {

			}
		}
		return pipeline_jenkins_job_id;

	}

	private static void getJenkinsParameter(String jenkinsJobName, String jenkinsUserName, String jenkinsPassword,
			String executeID, int gots_app_stats_id, int pipeline_jenkins_job_id) throws IOException {
		String response = null;
		HttpURLConnection connection = null;
		// Extract the Jenkins Build Id
		String urlString = executeID;
		String[] parts = urlString.split("/");
		String buildID = parts[parts.length - 1];
		// Connect to Jenkins
		String path = "/wfapi/runs?since=%23" + buildID;
		String method = "GET";
		String output = "";
		String links = "";
		connection = JenkinsConnector.getConnection(path, method, jenkinsJobName, jenkinsUserName, jenkinsPassword);
		// Read the Response stream
		BufferedReader br;
		br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
		StringBuilder buffer = new StringBuilder();
		while ((output = br.readLine()) != null) {
			buffer.append(output);
		}
		output = buffer.toString();
		parseJsonArray(gots_app_stats_id, output, pipeline_jenkins_job_id);
		connection.disconnect();
	}

	private static void parseJsonArray(int gots_app_stats_id, String output, int pipeline_jenkins_job_id) {
		String status = "";
		String startTimeMillis;
		String durationMillis;
		JsonElement jelement1;
		JsonElement jelement2;
		String jenkinsName = "";
		if (output.contains("_links")) {
			JsonElement jelement = new JsonParser().parse(output);
			JsonArray jarray = jelement.getAsJsonArray();
			for (int i = 0; i < jarray.size(); i++) {
				jelement2 = jarray.getAsJsonArray().get(i).getAsJsonObject().get("id");
				jelement1 = jarray.getAsJsonArray().get(i).getAsJsonObject().get("stages");
				for (int j = 0; j < jelement1.getAsJsonArray().size(); j++) {
					jenkinsName = jelement1.getAsJsonArray().getAsJsonArray().get(j).getAsJsonObject().get("name")
							.toString().replaceAll("[^a-z A-Z]", "");
					status = jelement1.getAsJsonArray().getAsJsonArray().get(j).getAsJsonObject().get("status")
							.toString().replaceAll("[^a-z A-Z]", "");
					startTimeMillis = jelement1.getAsJsonArray().getAsJsonArray().get(j).getAsJsonObject()
							.get("startTimeMillis").toString();
					durationMillis = jelement1.getAsJsonArray().getAsJsonArray().get(j).getAsJsonObject()
							.get("durationMillis").toString();
					ResultSet rs = null;
					PreparedStatement ps = null;
					Connection con = Database.getConnection();
					String sql = "INSERT INTO `camundabpmajsc6`.`pipeline_jenkins_status`(`gots_app_stats_id`, `name`,`status`, `startTimeMillis`,`durationMillis`,`pipeline_jenkins_job_id`) "
							+ "values (?,?,?,?,?,?)";
					try {

						ps = con.prepareStatement(sql);
						ps.setInt(1, gots_app_stats_id);
						ps.setString(2, jenkinsName);
						ps.setString(3, status);
						ps.setString(4, startTimeMillis);
						ps.setString(5, durationMillis);
						ps.setInt(6, pipeline_jenkins_job_id);
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
			}
		}

	}

	private static int getGotsAppStatsId(String gotsid, String name, String pipelineName, String processName,
			String subModuleName, String pipelineId) {
		// TODO Auto-generated method stub
		int gots_app_stats_id = 0;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "Select gots_app_stats_id from camundabpmajsc6.gots_app_stats where  gots_id=? and name=?  and pipeline_name=? and process_name=? and submodule_name=? and process_id=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipelineName);
			ps.setString(4, processName);
			ps.setString(5, subModuleName);
			ps.setString(6, pipelineId);
			rs = ps.executeQuery();
			if (rs.next()) {
				gots_app_stats_id = rs.getInt("gots_app_stats_id");
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
			} catch (Exception ex) {

			}
		}
		return gots_app_stats_id;
	}

	private static String getStatus(String url, String auth) throws IOException {
		String output = "";
		HttpURLConnection conn = null;
		URL u = new URL(url);
		conn = (HttpURLConnection) u.openConnection();
		conn.setRequestProperty("Authorization", auth);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		StringBuilder buffer = new StringBuilder();
		while ((output = br.readLine()) != null) {
			buffer.append(output);
		}
		output = buffer.toString();

		JsonElement jelement = new JsonParser().parse(output);
		JsonObject jobject = jelement.getAsJsonObject();
		String building = jobject.get("building").toString();
		if (building.equals("true"))
			return "";
		output = jobject.get("result").toString();
		conn.disconnect();
		return output.replaceAll("\"", "");
	}
}
