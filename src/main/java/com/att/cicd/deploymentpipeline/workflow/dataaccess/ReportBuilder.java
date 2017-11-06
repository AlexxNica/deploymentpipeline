/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;

@Component
@Path("/reports")
public class ReportBuilder {

	private static final Logger LOGGER = Logger.getLogger(ReportBuilder.class);

	public static void main(String[] args) {
		ReportBuilder rb = new ReportBuilder();
		System.out.println(rb.getReportById("6"));
	}

	public static JSONObject getPipelineNameList() {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "select distinct (pipeline_name) from gots_app_stats;";
		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getPipelineNameListWithGotsid(String gotsid) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "select distinct (pipeline_name) from gots_app_stats where gots_id=?;";

		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getSummaryReport(String pipelineName) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "Call camundabpmajsc6.summary_report(?);";

		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipelineName);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getGotsReport(String pipelineName, String ltm) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "Call camundabpmajsc6.gots_report(?, ?);";

		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipelineName);
			ps.setString(2, ltm);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getGotsDetailSummaryReport(String pipelineName, String gotsid) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "CALL `camundabpmajsc6`.`gots_detail_summary_report`(?, ?);";

		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipelineName);
			ps.setString(2, gotsid);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getGotsDetailReport(String pipelineName, String gotsid) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		ResultSetMetaData rsmd = null;
		Connection con = Database.getConnection();
		String sql = "CALL `camundabpmajsc6`.`gots_detail_report`(?, ?);";

		JSONObject report = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pipelineName);
			ps.setString(2, gotsid);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				int numColumns = rsmd.getColumnCount();

				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					// switch (rsmd.getColumnType(i)) {
					// case java.sql.Types.ARRAY:
					// obj.append(column_name, rs.getArray(i));
					// break;
					// case java.sql.Types.BIGINT:
					// obj.append(column_name, rs.getInt(i));
					// break;
					// case java.sql.Types.BOOLEAN:
					// obj.append(column_name, rs.getBoolean(i));
					// break;
					// case java.sql.Types.BLOB:
					// obj.append(column_name, rs.getBlob(i));
					// break;
					// case java.sql.Types.DOUBLE:
					// obj.append(column_name, rs.getDouble(i));
					// break;
					// case java.sql.Types.FLOAT:
					// obj.append(column_name, rs.getFloat(i));
					// break;
					// case java.sql.Types.INTEGER:
					// obj.append(column_name, rs.getInt(i));
					// break;
					// case java.sql.Types.NVARCHAR:
					// obj.append(column_name, rs.getNString(i));
					// break;
					// case java.sql.Types.VARCHAR:
					// obj.append(column_name, rs.getString(i));
					// break;
					// case java.sql.Types.TINYINT:
					// obj.append(column_name, rs.getInt(i));
					// break;
					// case java.sql.Types.SMALLINT:
					// obj.append(column_name, rs.getInt(i));
					// break;
					// case java.sql.Types.DATE:
					// obj.append(column_name, rs.getDate(i));
					// break;
					// case java.sql.Types.TIMESTAMP:
					// obj.append(column_name, rs.getTimestamp(i));
					// break;
					// default:
					// obj.append(column_name, rs.getObject(i));
					// break;
					// }
					String column_value = rs.getString(i);
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(i + "|" + column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getServicesReport() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "select * from camundabpmajsc6.services_reports order by services_reports_id desc;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject getServicesReportsCountFeatures() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "SELECT count(*) count , feature_name FROM camundabpmajsc6.services_reports group by feature_name;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject servicesReportsCountUUID() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "select count( distinct uuid) as 'count' FROM camundabpmajsc6.services_reports;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject servicesReportsCountGOTS() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "select count( distinct gots_id) as 'count' FROM camundabpmajsc6.services_reports;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject servicesReportsShowGOTS() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "select distinct gots_id FROM camundabpmajsc6.services_reports;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}

	public static JSONObject servicesReportsCountByGotsAndFeature() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject report = new JSONObject();
		String sql = "SELECT count(*) count, gots_id, feature_name FROM camundabpmajsc6.services_reports group by gots_id, feature_name;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
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
					obj.put(column_name, column_value);
				}
				report.append("report", obj);
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
		return report;
	}
	
	public static JSONObject showAllReportOptions() {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject reports = new JSONObject();
		String sql = "Select report_queries_id, name, query_description from camundabpmajsc6.report_queries;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
				JSONObject obj= new JSONObject();
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
					obj.put(column_name, column_value);
				}
				reports.append("report", obj);
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
		return reports;
	}
	
	public static JSONArray getReportById(String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONArray result = new JSONArray();
		JSONObject reports = new JSONObject();
		String sql = "Select query from camundabpmajsc6.report_queries where report_queries_id=?;";
		String query = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				query = rs.getString("query");
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
		
		con = Database.getConnection();
		ps = null;
		rs = null;
		try {
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			JSONArray headers = new JSONArray();
			for (int i = 1; i < numColumns +1; i++) {
				String header = rsmd.getColumnLabel(i);
				headers.put(header);
			}
			reports.append("headers", headers);
			while (rs.next()) {
				JSONObject obj= new JSONObject();
				numColumns = rsmd.getColumnCount();
				
				for (int i = 1; i < numColumns + 1; i++) {
					String column_name = rsmd.getColumnLabel(i);
					String column_value = rs.getString(i);
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				reports.append("reports", obj);
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
		result.put(reports);
		return result;
	}

}
