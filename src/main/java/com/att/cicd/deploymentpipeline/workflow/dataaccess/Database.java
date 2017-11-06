/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;

public class Database {
	@Autowired
	JdbcTemplate jdbcTemplate;

	private final static Logger LOGGER = Logger.getLogger("Database");

	public static void main(String[] args) throws Exception {

		Map r =  Database.getEnvironmentByID("25");
		System.out.println(isAssociated("TMP10018"));
	}

	public static Connection getConnection() {

		Connection connection = null;
		String driver;
		String dburl;
		String userName;
		String password;

		

		// Get Camunda Datasource Information from the environment else get it
		// from the Properties
		if (System.getenv("spring_datasource_driver_class") != null) {			
			driver = System.getenv("spring_datasource_driver_class");
			dburl = System.getenv("spring_datasource_url");
			userName = System.getenv("spring_datasource_username");
			password = System.getenv("spring_datasource_password");
		} else {			
			driver = System.getProperty("spring.datasource.driver-class-name");
			dburl = System.getProperty("spring.datasource.url");
			userName = System.getProperty("spring.datasource.username");
			password = System.getProperty("spring.datasource.password");
		}

		try {
			
			Class.forName(driver).newInstance();
			connection = DriverManager.getConnection(dburl, userName, password);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return connection;
	}

	public static String getVariable(String gots_id, String name, String pipeline_name, String submodule_name,
			String process_name, String attribute_name) {
//		String info = "Getting attribute for gots_id: " + gots_id + ", name: " + name + ", process_name: "
//				+ process_name + ", attribute_name: " + attribute_name + ", submodule_name: " + submodule_name
//				+ ", pipeline_name: " + pipeline_name;
//		ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database",
//				"getVariable", info);		
		Connection con = Database.getConnection();		
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "SELECT camundabpmajsc6.pipeline_gots_config.attribute_value FROM camundabpmajsc6.pipeline_gots_config "
				+ "INNER JOIN camundabpmajsc6.pipeline_config ON camundabpmajsc6.pipeline_gots_config.pipeline_config_id = pipeline_config.pipeline_config_id "
				+ "WHERE camundabpmajsc6.pipeline_gots_config.gots_id=? and camundabpmajsc6.pipeline_gots_config.name=? and pipeline_config.pipeline_name =? and "
				+ "pipeline_config.submodule_name=? and pipeline_config.process_name=? and pipeline_config.attribute_name=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gots_id);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, submodule_name);
			ps.setString(5, process_name);
			ps.setString(6, attribute_name);
			rs = ps.executeQuery();
			rs.next();
			String value = rs.getString("attribute_value");
			if (value == null) {
				value = "";
			}
//			info = "Value of attribute for gots_id: " + gots_id + ", name: " + name + ", process_name: " + process_name
//					+ ", attribute_name: " + attribute_name + ", submodule_name: " + submodule_name
//					+ ", pipeline_name: " + pipeline_name + ", value: " + value;
//			ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database",
//					"getVariable", info);
			return value;

		} catch (SQLException e) {
			e.printStackTrace();
			return "";
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

	public static String getVariableWithIndex(String gots_id, String name, String pipeline_name, String submodule_name,
			String process_name, String attribute_name, String array_indexer) {
//		String info = "Getting attribute for gots_id: " + gots_id + ", name: " + name + ", process_name: "
//				+ process_name + ", attribute_name: " + attribute_name + ", submodule_name: " + submodule_name
//				+ ", pipeline_name: " + pipeline_name;
//		ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database",
//				"getVariableWithIndex", info);
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "SELECT camundabpmajsc6.pipeline_gots_config.attribute_value FROM camundabpmajsc6.pipeline_gots_config "
				+ "INNER JOIN camundabpmajsc6.pipeline_config ON camundabpmajsc6.pipeline_gots_config.pipeline_config_id = pipeline_config.pipeline_config_id "
				+ "WHERE camundabpmajsc6.pipeline_gots_config.gots_id=? and camundabpmajsc6.pipeline_gots_config.name=? and pipeline_config.pipeline_name =? and "
				+ "pipeline_config.submodule_name=? and pipeline_config.process_name=? and pipeline_config.attribute_name=? and pipeline_gots_config.array_indexer=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gots_id);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, submodule_name);
			ps.setString(5, process_name);
			ps.setString(6, attribute_name);
			ps.setString(7, array_indexer);
			rs = ps.executeQuery();
			String value = "";
			if (rs.next()) {
				value = rs.getString("attribute_value");
			}
			if (value == null) value = "";
//			info = "Value of attribute for gots_id: " + gots_id + ", name: " + name + ", process_name: " + process_name
//					+ ", attribute_name: " + attribute_name + ", submodule_name: " + submodule_name
//					+ ", pipeline_name: " + pipeline_name + ", value: " + value;
//			ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database",
//					"getVariableWithIndex", info);
			return value;

		} catch (SQLException e) {
			e.printStackTrace();
			return "";
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
	
	public static Map getDeploymentConfigAttributesByIndex(String pipeline_flow_id, String gotsid, String deployment_order) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		String sql = "select build_server_id, environment_id, configuration_repository_id, enable_notification, notification_list, enable_approval, approval_list "
				+ "from deployment_config "
				+ "where pipeline_flow_id=? and gots_id=? and deployment_order=?;";
		Map obj = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_flow_id);
			ps.setString(2, gotsid);
			ps.setString(3, deployment_order);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			if (rs.next()) {
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("system_password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
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
				ex.printStackTrace();
			}
		}
		return obj;
	}

	public static Map getConfigAttributesByIndex(String gotsid, String name, String pipeline_name,
			String submodule_name, String process_name, String index) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "SELECT camundabpmajsc6.pipeline_config.attribute_name, camundabpmajsc6.pipeline_gots_config.attribute_value FROM camundabpmajsc6.pipeline_gots_config "
				+ "INNER JOIN camundabpmajsc6.pipeline_config ON camundabpmajsc6.pipeline_gots_config.pipeline_config_id = pipeline_config.pipeline_config_id "
				+ "WHERE camundabpmajsc6.pipeline_gots_config.gots_id=? and camundabpmajsc6.pipeline_gots_config.name=? and pipeline_config.pipeline_name =? and "
				+ "pipeline_config.submodule_name=? and pipeline_config.process_name=? and pipeline_gots_config.array_indexer=? order by config_order;";
		Map obj = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, submodule_name);
			ps.setString(5, process_name);
			ps.setString(6, index);
			rs = ps.executeQuery();
			while (rs.next()) {
				String attribute_name = rs.getString("attribute_name");
				String attribute_value = rs.getString("attribute_value");
				if (attribute_value == null || attribute_value.equals("")) {
					attribute_value = "";
				}
				obj.put(attribute_name, attribute_value);
			}
			obj.put("processDescription",
					Database.getDescription("description", pipeline_name, submodule_name, process_name));
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
		return obj;
	}
	
	public static Map getDeploymentConfigInfo(String pipeline_flow_id) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "Select min(deployment_order) as 'Initial Deployment', max(deployment_order) as 'Number of Configurations' from deployment_config where pipeline_flow_id=?";
		Map obj = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_flow_id);
			rs = ps.executeQuery();
			int initialDeployment = 0;
			int numConfigs = 0;
			if (rs.next()) {
				initialDeployment = rs.getInt("Initial Deployment");
				numConfigs = rs.getInt("Number of Configurations");
			} else {
				numConfigs = -1;
			}
			if (initialDeployment == 0 && numConfigs == 0) {
				obj.put("initialDeployment", initialDeployment);
				obj.put("numConfigs", -1);
			} else {
				obj.put("initialDeployment", initialDeployment);
				obj.put("numConfigs", numConfigs);
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
				ex.printStackTrace();
			}
		}
		return obj;
	}

	public static Map getIndexInformation(String gotsid, String name, String pipeline_name, String submodule_name,
			String process_name) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select min(array_indexer) as 'Initial Index', (select attribute_value from camundabpmajsc6.pipeline_gots_config pmc "
				+ "join camundabpmajsc6.pipeline_config pc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where pmc.gots_id=? and pmc.name=? and pc.pipeline_name=? "
				+ "and pc.submodule_name=? and pc.process_name='global' and pc.process_name_label='Number of Deployments') as 'Number of Deployments' "
				+ "from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pmc.pipeline_config_id = pc.pipeline_config_id "
				+ "where pmc.gots_id=? and pmc.name=? and pc.pipeline_name=? "
				+ "and pc.submodule_name=? and pc.process_name='global';";
		Map obj = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, submodule_name);
			ps.setString(5, gotsid);
			ps.setString(6, name);
			ps.setString(7, pipeline_name);
			ps.setString(8, submodule_name);
			rs = ps.executeQuery();
			int initialIndex = 0;
			int numDeployments = 0;
			if (rs.next()) {
				initialIndex = rs.getInt("Initial Index");
				numDeployments = rs.getInt("Number of Deployments");
			} else {
				initialIndex = 0;
				numDeployments = -1;
			}
			if (initialIndex == 0 && numDeployments == 0) {
				obj.put("initialIndex", initialIndex);
				obj.put("numDeployments", -1);
			} else {
				obj.put("initialIndex", initialIndex);
				obj.put("numDeployments", numDeployments);
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
				ex.printStackTrace();
			}
		}
		return obj;
	}

	public static String getDeploymentName(String gotsid, String pipeline_name, String submodule_name, String name,
			String array_indexer) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and pipeline_name=? and submodule_name=? and process_name='global' "
				+ "and attribute_name='deploymentName' and name=? and pmc.array_indexer=?;";
		String deploymentName = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, pipeline_name);
			ps.setString(3, submodule_name);
			ps.setString(4, name);
			ps.setString(5, array_indexer);
			rs = ps.executeQuery();
			if (rs.next()) {
				deploymentName = rs.getString("attribute_value");
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
				ex.printStackTrace();
			}
		}
		return deploymentName;
	}

	public static void addToServicesReports(String uuid, String gotsid, String feature_name) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "Insert into camundabpmajsc6.services_reports (uuid, gots_id, start_time, feature_name) "
				+ "values (?,?,NOW(),?);";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, uuid);
			ps.setString(2, gotsid);
			ps.setString(3, feature_name);
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

	public static void addToReport(String gots_id, String name, String pipeline_name, String submodule_name,
			String process_name, String assigned_to, boolean is_approved, String is_automatic, String process_id,
			boolean is_success, String process_type) {
//		String info = "Inserting into report: " + gots_id + ", name: " + name + ", process_name: " + process_name
//				+ ", submodule_name: " + submodule_name + ", pipeline_name: " + pipeline_name + ", is approved: "
//				+ is_approved + ", process_id: " + process_id;
//		ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database",
//				"addToReport", info);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "insert into camundabpmajsc6.gots_app_stats (gots_id, name, pipeline_name, submodule_name, process_name, "
				+ "start_time, assigned_to, is_approved, is_automatic, process_id, is_success, process_type) values (?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gots_id);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, submodule_name);
			ps.setString(5, process_name);
			ps.setString(6, assigned_to);
			ps.setBoolean(7, is_approved);
			ps.setString(8, is_automatic);
			ps.setString(9, process_id);
			ps.setBoolean(10, is_success);
			ps.setString(11, process_type);
			ps.executeUpdate();

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
			}
		}
	}

	public static void update(String gots_id, String name, String pipeline_name, String submodule_name,
			String process_name, String assigned_to, boolean is_approved, String is_automatic, String process_id,
			String is_success) {
		update(gots_id, name, pipeline_name, submodule_name, process_name, assigned_to, is_approved, is_automatic, process_id, is_success, false, "", "", "");
	}
	
	public static void update(String gots_id, String name, String pipeline_name, String submodule_name,
			String process_name, String assigned_to, boolean is_approved, String is_automatic, String process_id,
			String is_success, boolean kubeInfo, String k8s_environment_url, String k8s_environment_type, String phase) {
		if (is_success == "null") {
			is_success = "false";
		}
//		String info = "Updating with: " + gots_id + ", name: " + name + ", process_name: " + process_name
//				+ ", submodule_name: " + submodule_name + ", pipeline_name: " + pipeline_name + ", assigned to: "
//				+ assigned_to + ", is approved: " + is_approved + ", is automatic?: " + is_automatic + ", process_id: "
//				+ process_id + ", k8s url: " + k8s_environment_url + ", k8s env type: " + k8s_environment_type + ", phase: " + phase;
//		ApplicationLogger.callLog(gots_id, name, "", pipeline_name, submodule_name, process_name, "Database", "update",
//				info);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		if (kubeInfo) {
			String sql = "update camundabpmajsc6.gots_app_stats set assigned_to=?, is_approved=?, is_automatic=?, "
					+ "is_success=?, k8s_environment_url=?, k8s_environment_type=?, phase=? where process_id=? and process_name=? and end_time IS NULL";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, assigned_to);
				ps.setBoolean(2, is_approved);
				ps.setString(3, is_automatic);
				ps.setString(4, is_success);
				ps.setString(5, k8s_environment_url);
				ps.setString(6, k8s_environment_type);
				ps.setString(7, phase);
				ps.setString(8, process_id);
				ps.setString(9, process_name);
				ps.executeUpdate();

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
				}
			}
		} else {
			String sql = "update camundabpmajsc6.gots_app_stats set end_time=NOW(), assigned_to=?, is_approved=?, is_automatic=?, "
					+ "is_success=? where process_id=? and process_name=? and end_time IS NULL";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, assigned_to);
				ps.setBoolean(2, is_approved);
				ps.setString(3, is_automatic);
				ps.setString(4, is_success);
				ps.setString(5, process_id);
				ps.setString(6, process_name);
				ps.executeUpdate();

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
				}
			}
		}
		
	}

	public static String getSystemConfig(String attributeName) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String value = "";
		String sql = "select (Select attribute_value from camundabpmajsc6.system_config where attribute_name=?) as value;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, attributeName);
			rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getString("value");
			}

		} catch (Exception e) {

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
		return value;
	}

	public static String getDescription(String attributeName, String pipelineName, String submoduleName,
			String processName) {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String value = "";
//		String info = "Getting the description for the step " + pipelineName + " -> " + submoduleName + " -> "
//				+ processName;
//		ApplicationLogger.callLog("", "", "", pipelineName, submoduleName, processName, "Database", "getDescription",
//				info);
		String sql = "Select (select attribute_value from camundabpmajsc6.pipeline_description where attribute_name=? and pipeline_name=? and submodule_name=? and process_name=?) as value;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, attributeName);
			ps.setString(2, pipelineName);
			ps.setString(3, submoduleName);
			ps.setString(4, processName);
			rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getString("value");
			}
		} catch (Exception e) {

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
		if (value == null) {
			value = "";
		}
		return value;
	}

	public static void insertSystemConnection(String name, String gotsid, String url, String username, String password,
			String systemType) throws Exception {
		String encryptedPassword = EncryptDecryptService.encrypt(password);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "Insert into camundabpmajsc6.system_connections(connection_name, system_url, gots_id, system_username, system_password, system_type) values (?, ?, ?, ?, ?, ?);";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, url);
			ps.setString(3, gotsid);
			ps.setString(4, username);
			ps.setString(5, encryptedPassword);
			ps.setString(6, systemType);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
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

	public static JSONObject getSystemConnection(String gotsid, String systemType) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "select connection_name, system_connections_id, system_url, system_username, system_password, system_type from camundabpmajsc6.system_connections where gots_id = ? and system_type =? ";
		JSONObject properties = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, systemType);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("system_password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				properties.append("system_properties", obj);
			}
		} catch (Exception e) {

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
		return properties;
	}

	public static JSONObject getSystemConnectionById(String systemConnectionId) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "select gots_id, connection_name, system_connections_id, system_url, system_username, system_password, system_type from camundabpmajsc6.system_connections where system_connections_id = ? ";
		JSONObject properties = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, systemConnectionId);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("system_password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				properties.append("system_properties", obj);
			}
		} catch (Exception e) {

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
		return properties;
	}

	public static void deleteSystemConnection(String systemConnectionId) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.system_connections where system_connections_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, systemConnectionId);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
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

	public static void updateSystemConnection(String name, String systemConnectionId, String url, String username,
			String password) throws Exception {
		String encryptedPassword = EncryptDecryptService.encrypt(password);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "update camundabpmajsc6.system_connections set connection_name = ?, system_url = ?, system_username = ?, system_password = ? where system_connections_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, url);
			ps.setString(3, username);
			ps.setString(4, encryptedPassword);
			ps.setString(5, systemConnectionId);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
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

	public static void saveEnvironment(String gotsid, String name, String cluster_name, String cluster_url,
			String username, String password, String environment_type) throws Exception {
		String encryptedPassword = EncryptDecryptService.encrypt(password);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "Insert into camundabpmajsc6.environments(environment_name, cluster_name, cluster_url, username, password, environment_type, gots_id) values (?,?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, cluster_name);
			ps.setString(3, cluster_url);
			ps.setString(4, username);
			ps.setString(5, encryptedPassword);
			ps.setString(6, environment_type);
			ps.setString(7, gotsid);
			ps.executeQuery();
		} catch (Exception e) {
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

	public static void deleteEnvironment(String environment_id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.environments where environment_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, environment_id);
			ps.executeQuery();
		} catch (Exception e) {
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

	public static void saveBuildServer(String gotsid, String name, String url, String username, String password,
			String build_server_type) throws Exception {
		String encryptedPassword = EncryptDecryptService.encrypt(password);
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "Insert into camundabpmajsc6.build_server_environments(environment_name, environment_url, username, password, build_server_type, gots_id) values (?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, url);
			ps.setString(3, username);
			ps.setString(4, encryptedPassword);
			ps.setString(5, build_server_type);
			ps.setString(6, gotsid);
			ps.executeQuery();
		} catch (Exception e) {
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

	public static void deleteBuildServer(String build_server_environment_id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.build_server_environments where build_server_environment_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, build_server_environment_id);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
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
	
	public static String getSubmoduleNameLabel(String submodule_name) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select submodule_label from camundabpmajsc6.pipeline_config where submodule_name = ?";
		String label = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, submodule_name);
			rs = ps.executeQuery();
			if (rs.next()) {
				label = rs.getString("submodule_label");
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
		return label;
	}
	
	public static String getProcessNameLabel(String process_name) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select process_name_label from camundabpmajsc6.pipeline_config where process_name = ?";
		String label = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, process_name);
			rs = ps.executeQuery();
			if (rs.next()) {
				label = rs.getString("process_name_label");
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
		return label;
	}
	
	public static ArrayList<String> getPipelineInstanceLogs(String pipeline_id) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "SELECT log_data FROM camundabpmajsc6.application_log where user_friendly=1 and pipeline_id=? order by application_log_id asc;";
		ArrayList<String> data = new ArrayList<String>();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				data.add(rs.getString("log_data"));
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
				ex.printStackTrace();
			}
		}
		return data;
	}
	
	public static Map getBuildServerByID(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "Select * from camundabpmajsc6.build_server_environments where build_server_environment_id = ?";
		Map properties = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
//				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("system_password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					properties.put(column_name, column_value);
//					obj.put(column_name, column_value);
				}
//				properties.append("system_properties", obj);
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
		return properties;
	}
	
	public static Map getEnvironmentByID(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "Select * from camundabpmajsc6.environments where environment_id = ?";
		Map obj = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			if (rs.next()) {
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("system_password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
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
		return obj;
	}
	
	public static boolean isNewOnboardedApplication(String gotsid) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select onboarded from asf_gots where gots_id=? and onboarded=true";
		boolean newOnboard = false;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			rs = ps.executeQuery();
			if (rs.next()) {
				newOnboard = true;
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
		return newOnboard;
	}
	
	public static void deletePipelineFlow(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.pipeline_flow where pipeline_flow_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
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
	
	public static void deleteDeploymentConfiguration(String id, int deployment_order, String pipeline_flow_id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.deployment_config where deployment_config_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.executeQuery();
		} catch (Exception e) {

		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {

			}
		}
		con = Database.getConnection();
		ps = null;
		sql = "Update camundabpmajsc6.deployment_config Set deployment_order = deployment_order - 1 where deployment_order > ? and pipeline_flow_id = ?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, deployment_order);
			ps.setString(2, pipeline_flow_id);
			ps.executeUpdate();
		} catch (Exception e) {
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

			}
		}
	}

	public static void deleteConfigurationRepository(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		String sql = "delete from camundabpmajsc6.configuration_repositories where configuration_repository_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.executeQuery();
		} catch (Exception e) {
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

			}
		}
	}

	public static String getDeploymentConfigName(String pipeline_flow_id, String deployment_indexer) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select name from deployment_config where pipeline_flow_id=? and deployment_order=?;";
		String name = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_flow_id);
			ps.setString(2, deployment_indexer);
			rs = ps.executeQuery();
			if (rs.next()) {
				name = rs.getString("name");
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

			}
		}
		return name;
	}
	
	public static Map getPipelineFlowConfig(String pipeline_flow_id, String name, String pipelineName) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select dc.deployment_config_id, pdc.pipeline_deployment_config_id, pipeline_flow_id, dc.name, pdc.jenkins_job, pdc.jenkins_params from deployment_config dc "
				+ "join pipeline_deployment_config pdc on pdc.deployment_config_id = dc.deployment_config_id "
				+ "where dc.pipeline_flow_id=? and dc.name=? and pdc.name=?;";
		Map pipelineFlowConfig = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_flow_id);
			ps.setString(2, name);
			ps.setString(3, pipelineName);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			if (rs.next()) {
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					pipelineFlowConfig.put(column_name, column_value);
				}
			}
		} catch (Exception e) {
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
		return pipelineFlowConfig;
	}
	
	public static int getMaxDeploymentNum(String pipeline_flow_id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select count(distinct(deployment_order)) as max from deployment_config where pipeline_flow_id = ?";
		int max = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_flow_id);
			rs = ps.executeQuery();
			if (rs.next()) {
				max = rs.getInt("max");
			}
		} catch (Exception e) {

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
		return max;
	}
	
	public static Map getConfigRepoByID(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select * from configuration_repositories where configuration_repository_id=?";
		Map configRepo = new HashMap();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			if (rs.next()) {
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					configRepo.put(column_name, column_value);
				}
			}
		}catch (Exception e) {
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
		return configRepo;
	}
	
	/**
	 * Returns true always.
	 * 
	 * Code used to validate a gotsid had been approved.
	 * Code is left here commented out in case someone wants to add that functionality back.
	 * @param gotsid
	 * @return
	 */
	public static boolean isAssociated(String gotsid) {
		boolean associated = true;
/*		
 		boolean associate = false;
  		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select associated_gots_id from asf_gots where gots_id=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			rs = ps.executeQuery();
			if (rs.next()) {
				if (rs.getString("associated_gots_id") != null) {
					associated = true;
				}
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
		}*/
		return associated;
	}
}