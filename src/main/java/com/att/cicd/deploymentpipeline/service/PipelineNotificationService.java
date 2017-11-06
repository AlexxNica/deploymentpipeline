/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/notifications")
@Path("/notifications")
public class PipelineNotificationService {
	@Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = Logger.getLogger(DeploymentPipelineReportingService.class);

	public static void main(String[] args) throws JsonProcessingException {
	}

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@ApiOperation(value = "Get Release Notification Count", notes = "Get Release Notification Count")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getReleaseNotificationCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReleaseNotificationCount() {
		int count = 0;
		String user = request.getUserPrincipal().getName();
		String sql = "SELECT COUNT(*) AS COUNT FROM eco_notification en WHERE en.eco_notification_id NOT IN (SELECT eco_notification_id FROM eco_user_notification "
				+ "WHERE do_not_show=TRUE AND uuid=?) and type='RELEASE' AND is_active=1 ORDER BY created_date";
		Map m = jdbcTemplate.queryForMap(sql, user);
		return Response.status(200).entity(m).build();
	}

	@ApiOperation(value = "Display Release Notifications", notes = "Display all Release Notifications")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getAllReleaseNotification")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllReleaseNotification() {
		String user = request.getUserPrincipal().getName();
		/*String sql = "SELECT en.eco_notification_id, en.title, en.type, en.description, en.link, en.created_date FROM eco_notification en WHERE en.eco_notification_id NOT IN (SELECT eco_notification_id FROM eco_user_notification "
				+ "WHERE do_not_show=TRUE AND uuid=?) and type='RELEASE' AND is_active=1 ORDER BY created_date";*/
		String sql = "SELECT en.eco_notification_id, en.link_name, en.link,en.title, en.type, CAST(en.description as char character set utf8) as description,DATE_FORMAT(en.created_date,'%M %d, %Y') as created_date, CASE WHEN (SELECT do_not_show from eco_user_notification "+ 
				"where uuid=? and eco_notification_id=en.eco_notification_id) is null THEN 1 ELSE 0 END as newitem  FROM eco_notification en where type='RELEASE' and is_active=1 "
				+ "order by en.created_date desc";
		List listNotification = jdbcTemplate.queryForList(sql, user);				
		return Response.status(200).entity(listNotification).build();
	}

	@ApiOperation(value = "Get Alert Notification Count", notes = "Get Alert Notification Count")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getAlertNotificationCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlertNotificationCount() {
		int count = 0;
		String user = request.getUserPrincipal().getName();
		String sql = "Select count(*) as COUNT from (SELECT * FROM (SELECT en.eco_notification_id, en.title, en.type, en.description, en.created_date FROM eco_notification en WHERE en.eco_notification_id  NOT IN (SELECT eco_notification_id FROM "
				+ "eco_user_notification WHERE do_not_show=TRUE AND uuid=?) AND type='TIPS' AND is_active=1 ORDER BY Rand() Limit 1) AS TIPS UNION SELECT * FROM (SELECT eco_notification_id, title, type, description, "
				+ "created_date FROM eco_notification WHERE eco_notification_id  NOT IN (SELECT eco_notification_id FROM eco_user_notification WHERE do_not_show=TRUE AND uuid=?) AND type IN('WARNING', 'ERROR') AND "
				+ "start_notification_date <=now() AND end_notification_date >= now() AND is_active=1 ORDER BY created_date) AS ALERT ) AS COUNT";
		Map m = jdbcTemplate.queryForMap(sql, user, user);
		return Response.status(200).entity(m).build();
	}

	@ApiOperation(value = "Display Alert Notifications", notes = "Display all Alert Notifications")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })	
	@GET
	@Path("/getAllAlertNotification")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAlertNotification() {
		String user = request.getUserPrincipal().getName();
		String sql = "SELECT * FROM (SELECT en.eco_notification_id, en.title, en.type, CAST(en.description as char character set utf8) as description,DATE_FORMAT(en.created_date,'%M %d, %Y') as created_date FROM eco_notification en "
				+ "where en.eco_notification_id NOT IN (select eco_notification_id from eco_user_notification where do_not_show=TRUE and uuid=?) "
				+ "and type='TIPS' and is_active=1 and NOW() > IFNULL(((SELECT un1.created_date FROM eco_user_notification un1 "
				+ "join eco_notification n1 on un1.eco_notification_id=n1.eco_notification_id and n1.type='TIPS' where un1.uuid=? order by un1.created_date "
				+ "desc limit 1)),NOW()-INTERVAL 2 DAY)+ INTERVAL 1 DAY  Order by Rand() Limit 1) as TIPS UNION SELECT * FROM (SELECT eco_notification_id, title, type, CAST(description as char character set utf8) as description,"
				+ "DATE_FORMAT(created_date,'%M %d, %Y') as created_date FROM eco_notification where eco_notification_id  not in (select eco_notification_id from eco_user_notification where "
				+ "do_not_show=TRUE and uuid=?) and type in ('WARNING', 'ERROR') and start_notification_date <=now() and end_notification_date >= now() "
				+ "and is_active=1 order by created_date) as ALERT";
		List listNotification = jdbcTemplate.queryForList(sql, user, user,user);
		return Response.status(200).entity(listNotification).build();
	}

	@ApiOperation(value = "Add User Notifications", notes = "Add User Notification details")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/insertUserNotification/{eco_notification_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertUserNotification(
			@ApiParam(value = "ECO Notification ID", required = true) @PathParam("eco_notification_id") String id) {
		String user = request.getUserPrincipal().getName();
		String selectSql = "SELECT * FROM eco_user_notification where eco_notification_id = ? and uuid = ?";
		List userNotification = jdbcTemplate.queryForList(selectSql, id, user);
		if (userNotification.size() == 0) {
			String read = "0";
			String do_not_show = "1";
			String sql = "INSERT INTO eco_user_notification(`eco_notification_id`,`uuid`,`do_not_show`,`read`) values (?,?,?,?)";
			jdbcTemplate.update(sql, id, user, do_not_show,read);
		}
		return Response.status(200).build();
	}		
}
