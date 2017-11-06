/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/admin")
@Path("/adminServices")
public class AdminServices {
	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@ApiOperation(value = "Delete all permissions granted to the user role for a GOTS ID", 
			notes = "Delete all the permissions granted to the user role for a particular GOTS ID")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@GET
	@Path("/cleanPerm/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cleanPerm(@PathParam("gotsid") String gotsid) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".access|*|*")) {
			String adminjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
					+ "\",\n  \"action\":\"admin\"\n}";
			String userjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
					+ "\",\n  \"action\":\"user\"\n}";
			String adminRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin\"\n}";
			String userRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user\"\n}";
			String addAdminPermJson = "{\n    \"perm\":" + adminjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots."
					+ gotsid + ".admin\"\n}";
			String addUserPermJson = "{\n    \"perm\":" + userjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots."
					+ gotsid + ".user\"\n}";
			AAFConnector aafcon = new AAFConnector();

			aafcon.delete("/authz/role?force=true", adminRole);
			aafcon.delete("/authz/role?force=true", userRole);
			aafcon.delete("/authz/perm?force=true", adminjson);
			aafcon.delete("/authz/perm?force=true", userjson);
			return Response.status(200).build();
		} else {
			PiplelineException notSuperAdminException = new PiplelineException();
			notSuperAdminException.setCode(100);
			notSuperAdminException.setMessage("You do not have access to this api");
			return Response.status(400).entity(notSuperAdminException).build();
		}
	}


	@ApiOperation(value = "Extend expiration date for a user role", 
			notes = "Extend expiration date for a user role pertaining to a GOTS ID.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api")})
	@POST
	@Path("/extendAllPerGots/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response extendAll(@PathParam("gotsid") String gotsid) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".access|*|*")) {
			AAFConnector aafcon = new AAFConnector();
			CamundaConnector c = new CamundaConnector();
			String response = (aafcon.get("/authz/userRoles/role/" + ""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin"));
			Map top = c.jsonToMap(response);
			ArrayList<String> adminList = new ArrayList();
			ArrayList userRole = c.getJsonArray(top, "userRole");
			for (int i = 0; i < userRole.size(); i++) {
				String temp = c.getJsonString((Map) userRole.get(i), "user");
				adminList.add(temp);
			}
			response = (aafcon.get("/authz/userRoles/role/"+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user"));
			top = c.jsonToMap(response);
			ArrayList<String> userList = new ArrayList();
			userRole = c.getJsonArray(top, "userRole");
			for (int i = 0; i < userRole.size(); i++) {
				String temp = c.getJsonString((Map) userRole.get(i), "user");
				userList.add(temp);
			}
			for (String user : adminList) {
				aafcon.put("/authz/userRole/extend/" + user + "/"+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin", "");
			}
			for (String user : userList) {
				aafcon.put("/authz/userRole/extend/" + user + "/"+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user", "");
			}
			return Response.status(200).build();
		} else {
			PiplelineException notSuperAdminException = new PiplelineException();
			notSuperAdminException.setCode(101);
			notSuperAdminException.setMessage("You do not have access to this api");
			return Response.status(400).entity(notSuperAdminException).build();
		}
	}

	@ApiOperation(value = "Delete all permissions for a GOTS ID and remove GOTS ID", 
			notes = "Delete all permissions for a GOTS ID and completely delete GOTS ID from the ECO")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "You do not have access to this api")})
	@POST
	@Path("cleanGots/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cleanGots(@PathParam("gotsid") String gotsid) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".access|*|*")) {
			cleanPerm(gotsid);
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "call camundabpmajsc6.clean_gots(?);";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.executeQuery();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
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
			return Response.status(200).build();
		} else {
			PiplelineException notSuperAdminException = new PiplelineException();
			notSuperAdminException.setCode(100);
			notSuperAdminException.setMessage("You do not have access to this api");
			return Response.status(400).entity(notSuperAdminException).build();
		}
	}
	
}
