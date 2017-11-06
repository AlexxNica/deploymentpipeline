/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/login")
@Path("/loginService")
public class LoginService {
	
	@ApiOperation(value = "Check the login credintials of a user", 
			notes = "Check the login credintials of a user")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@GET
	@Path("/login/{username}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@PathParam("username") String username, @PathParam("password") String password) {
		if((username == null || password == null) ||
				(username.isEmpty() || password.isEmpty())) {
			PiplelineException badInputException = new PiplelineException();
			badInputException.setCode(403);
			badInputException.setMessage("Must enter a username and password");
			return Response.status(400).entity(badInputException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM login_info "
				+ "WHERE username=? "
				+ "AND pass=?;";
		boolean success = false;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if(rs.next()) {
				success = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(success) {
			return Response.status(200).build();
		} else {
			PiplelineException notFoundException = new PiplelineException();
			notFoundException.setCode(403);
			notFoundException.setMessage("Incorrect username or password");
			return Response.status(400).entity(notFoundException).build();
		}
	}
	
	
	
	@ApiOperation(value = "Register a new user", 
			notes = "Register a new")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@GET
	@Path("/register/{username}/{email}/{aafprofile}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@PathParam("username") String username, @PathParam("email") String email, @PathParam("aafprofile") String aafprofile, @PathParam("password") String password) {
		if((username == null || email== null || aafprofile==null || password == null) ||
				(username.isEmpty() || email.isEmpty() || password.isEmpty())) {
			PiplelineException badInputException = new PiplelineException();
			badInputException.setCode(403);
			badInputException.setMessage("Must enter a username, email, aafprofile, and password");
			return Response.status(400).entity(badInputException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String checkExistanceSql = "SELECT * FROM login_info "
				+ "WHERE username=? "
				+ "OR email=?;";
		boolean success = false;
		PiplelineException existingInputException = null;
		try {
			ps = con.prepareStatement(checkExistanceSql);
			ps.setString(1, username);
			ps.setString(2, email);
			rs = ps.executeQuery();
			if(rs.next()) {
				existingInputException = new PiplelineException();
				existingInputException.setCode(401);
				existingInputException.setMessage("Username or email not available");
			} else {
				String createUserSql = "INSERT INTO login_info "
						+ "(`username`, `email`, `aafprofile`, `pass`) "
						+ "VALUES (?,?,?,?);";
				try {
					ps.close();
					ps = con.prepareStatement(createUserSql);
					ps.setString(1, username);
					ps.setString(2, email);
					ps.setString(3, aafprofile);
					ps.setString(4, password);
					ps.executeQuery();
					success = true;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(success) {
			return Response.status(200).build(); 
		} else {
			if(existingInputException != null)
				return Response.status(400).entity(existingInputException).build();
			else
				return Response.status(400).build();
		}
	}
	
}
