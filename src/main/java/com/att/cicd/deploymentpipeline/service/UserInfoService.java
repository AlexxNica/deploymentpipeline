/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/userInfo")
@Path("/userInfoService")
public class UserInfoService {

	public static final String NO_NAME = "noname";
	public static final String NO_EMAIL = "noemail";
	private static final String NO_PROFILE = "noprofile";
	private static final Logger LOGGER = Logger.getLogger(UserInfoService.class);
	
	
	@ApiOperation(value = "Get the username associated with an emaill address", 
			notes = "Get the username associated with an emaill address")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@GET
	@Path("/getUser/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("email") String email) {
		String user = getUsername(email);
		if(user.equals(NO_NAME)) {
			PiplelineException badInputException = new PiplelineException();
			badInputException.setCode(403);
			badInputException.setMessage("No username associated with that email address");
			return Response.status(400).entity(badInputException).build();
		}
		
		return Response.status(200).entity(JSONObject.quote(user)).build();
	}
	
	@ApiOperation(value = "Get the email associated with a username", 
			notes = "Get the email associated with a username")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@GET
	@Path("/getEmail/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEmail(@PathParam("username") String username) {
		String email = getEmailAddress(username);
		if(email.equals(NO_EMAIL)) {
			PiplelineException badInputException = new PiplelineException();
			badInputException.setCode(403);
			badInputException.setMessage("No email address associated with that username");
			return Response.status(400).entity(badInputException).build();
		}
		return Response.status(200).entity(JSONObject.quote(email)).build();
	}
	
	public static String getUsername(String email) {
		String user = NO_NAME;
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM login_info "
				+ "WHERE email=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, email);
			rs = ps.executeQuery();
			if(rs.next()) {
				user = rs.getString("username");
			} else {
				user = NO_NAME;
			}
		} catch (Exception e) {
			LOGGER.warn("username not retrieved for email "+email);
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
		
		return user;
	}
	
	public static String getEmailAddress(String user) {
		String email = NO_EMAIL;
		Connection con = Database.getConnection();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM login_info "
				+ "WHERE username=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, user);
			rs = ps.executeQuery();
			if(rs.next()) {
				email = rs.getString("email");
			} else {
				email = NO_EMAIL;
			}
		} catch (Exception e) {
			LOGGER.warn("email not retrieved for username "+user);
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
		
		return email;
	}
	
	public static String fullyQualify(String user) {
		String profile = NO_PROFILE;
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM login_info "
				+ "WHERE username=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, user);
			rs = ps.executeQuery();
			if(rs.next()) {
				profile = rs.getString("aafprofile");
			} else {
				profile = NO_PROFILE;
			}
		} catch (Exception e) {
			LOGGER.warn("aaf profile not retrieved for user "+user);
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
		
		return profile;
	}
	
	public static boolean isFullyQualified(String user) {
		int atIndex;
		if((atIndex = user.indexOf("@"))<0) {
			return false;
		} else {
			String domain = user.substring(atIndex);
			if(domain.split("\\.").length == 3) {
				return true;
			}
		}
		return false;
	}
}
