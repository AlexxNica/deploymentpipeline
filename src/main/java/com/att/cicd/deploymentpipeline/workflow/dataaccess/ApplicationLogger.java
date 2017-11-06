/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mysql.jdbc.Statement;

public class ApplicationLogger {

	public static void main(String[] args) {
		ApplicationLogger el = new ApplicationLogger();
		el.callLog("15", "test","test", "test", "test", "test", "test", "test", "test");

	}
	
	public static void callLog(String gotsid, String name, String pipeline_id, String pipelineName, String submoduleName, String processName, String class_name, String method_name, String info) {
		callLog(gotsid, name, pipeline_id, pipelineName, submoduleName, processName, class_name, method_name, info, false);
	}
	
	public static int callLog(String gotsid, String name, String pipeline_id, String pipelineName, String submoduleName, String processName, String class_name, String method_name, String info, boolean userFriendly) {
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		ResultSet rs = null;
		int id = 0;
		String sql = "Insert into camundabpmajsc6.application_log (gots_id, name,pipeline_id, pipeline_name, submodule_name, process_name, time, class_name, method_name, log_data, user_friendly) values (?,?,?,?,?,?,NOW(),?,?,?,?);";
		try {
			ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_id);
			ps.setString(4, pipelineName);
			ps.setString(5, submoduleName);
			ps.setString(6, processName);
			ps.setString(7, class_name);
			ps.setString(8, method_name);
			ps.setString(9, info);
			ps.setBoolean(10, userFriendly);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
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
		return id;
	}
	
	public static void updatePipelineId(int application_log_id, String pipeline_id) {
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "Update camundabpmajsc6.application_log set pipeline_id = ? where application_log_id = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipeline_id);
			ps.setInt(2, application_log_id);
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

}
