/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.att.cicd.deploymentpipeline.workflow.notification.EmailNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/onboarding")
@Path("/onboarding")
public class DeploymentPipelineOnboardingService {
	@Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = Logger.getLogger(DeploymentPipelineOnboardingService.class);

	public static void main(String[] args) throws JsonProcessingException {

		// ApplicationContext context = new FileSystemXmlApplicationContext(
		// "C:/dev/att/ajsccamunda/deploymentpipeline/src/main/ajsc/deploymentpipeline_v1/deploymentpipeline/v1/conf/serviceBeans.xml");
		DeploymentPipelineOnboardingService rest = new DeploymentPipelineOnboardingService();
		// Response r = (rest.onboardPipeline("15"));
		// Response r = (rest.getlist());
		// ObjectMapper mapper = new ObjectMapper();
		// System.out.println("TEST RESPONSECODE IS:" + (r.getStatus()));
		// System.out.println("TEST RESPONSE IS:" +
		// mapper.writeValueAsString(r.getEntity()));

		rest.cleanPerm("26");
	}

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@ApiOperation(value = "Get Admin List for a particular GOTS ID", notes = "Returns the list of user with the admin role for a particular GOTSID  for an onboarded application.")
	@ApiResponses(value = {
			// @ApiResponse(code = 200, message= "Successful Operation",
			// response= Response.class),
			@ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api") })
	@GET
	@Path("/onboardedApplicationAdminList/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAdminList(@PathParam("gotsid") String gotsid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots." + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		List<GotsDetail> objectlist = new ArrayList<GotsDetail>();
		GenericEntity<List<GotsDetail>> list = new GenericEntity<List<GotsDetail>>(objectlist) {
		};
		String response = (aafcon.get("/authz/userRoles/role/" +((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin"));

		ObjectMapper mapper = new ObjectMapper();
		Map object = null;
		if (response.equals("")) {

		} else {
			try {
				object = mapper.readValue(response, Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(200).entity(object).build();
	}

	@ApiOperation(value = "Checks if User is an Admin", notes = "An API to check if the role of the user logged in is an admin")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/isSystemAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isSystemAdmin() {
		return Response.status(200).entity(request.isUserInRole(((String) System.getProperty("aafnamespace"))+".access|*|*")).build();
	}

	@ApiOperation(value = "Checks the session", notes = "An API to check if the session of the user logged in ")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
	@GET
	@Path("/checkSession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkSession() {
		return Response.status(200).entity("true").build();
	}

	@ApiOperation(value = "Get User List for a particular GOTS ID", notes = "Returns the list of user with the user role for a particular GOTSID  for an onboarded application.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 102, message = "You do not have access to this api") })
	@POST
	@Path("/copyConfig/{gotsid}/{pipelineName}/{originalName}/{newName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response copyConfig(@PathParam("gotsid") String gotsid, @PathParam("pipelineName") String pipelineName,
			@PathParam("originalName") String originalName, @PathParam("newName") String newName) throws SQLException {
		List<String> values = new ArrayList<String>();
		List<Integer> id = new ArrayList<Integer>();
		List<Integer> array_indexer = new ArrayList<Integer>();
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "Select seed_id, seed_owner, build_server_url, source_code_url from pipeline_seed_info where name=? and pipeline_name=? and gots_id=?";
		String seedId = "", seedOwner = "", url = "", source_code_url = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, originalName);
			ps.setString(2, pipelineName);
			ps.setString(3, gotsid);
			rs = ps.executeQuery();
			if (rs.next()) {
				seedId = rs.getString("seed_id");
				seedOwner = rs.getString("seed_owner");
				url = rs.getString("build_server_url");
				source_code_url = rs.getString("source_code_url");
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
		con = Database.getConnection();
		rs = null;
		ps = null;
		sql = "Select * from camundabpmajsc6.pipeline_gots_config where name=? and gots_id=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, newName);
			ps.setString(2, gotsid);
			rs = ps.executeQuery();
			if (rs.next()) {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(300);
				configAlreadyExistsException.setMessage("A different name must be used when copying a configuration.");
				return Response.status(400).entity(configAlreadyExistsException).build();
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
		String pipeline_flow_id = "";
		try {
			sql = "select pc.attribute_name, pmc.pipeline_config_id, pmc.attribute_value, pmc.array_indexer from camundabpmajsc6.pipeline_gots_config pmc "
					+ "join camundabpmajsc6.pipeline_config pc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and name=? and pipeline_name=? order by pc.pipeline_config_id;";
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, originalName);
			ps.setString(3, pipelineName);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("attribute_name").equals("pipelineFlowDropDown")) {
					pipeline_flow_id = rs.getString("attribute_value");
				}
				values.add(rs.getString("attribute_value"));
				id.add(rs.getInt("pipeline_config_id"));
				array_indexer.add(rs.getInt("array_indexer"));
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
		try {
			sql = "Insert into camundabpmajsc6.pipeline_gots_config (pipeline_config_id, gots_id, name, attribute_value, array_indexer) values (?,?,?,?,?);";
			for (int i = 0; i < values.size(); i++) {
				ps = con.prepareStatement(sql);
				ps.setInt(1, id.get(i));
				ps.setString(2, gotsid);
				ps.setString(3, newName);
				ps.setString(4, values.get(i));
				if (array_indexer.get(i) == 0) {
					ps.setNull(5, java.sql.Types.VARCHAR);
				} else {
					ps.setInt(5, array_indexer.get(i));
				}
				ps.executeQuery();
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
		DeploymentPipelineRestService dprs = new DeploymentPipelineRestService();
		if (pipelineName.equals("cdp-workflow-v1")) {
			try {
				List l = jdbcTemplate.queryForList(
						"select deployment_config_id from deployment_config where pipeline_flow_id=?;",
						pipeline_flow_id);
				for (int i = 0; i < l.size(); i++) {
					Map<String, Integer> ids = (Map<String, Integer>) l.get(i);
					jdbcTemplate.update(
							"insert into pipeline_deployment_config (deployment_config_id, gots_id, name, jenkins_job, jenkins_params) "
									+ "values (?, ?, ?, (select * from (select jenkins_job from pipeline_deployment_config where deployment_config_id=? and gots_id=? and name=?) as t), "
									+ "(select * from (select jenkins_params from pipeline_deployment_config where deployment_config_id=? and gots_id=? and name=?) as ta))",
									ids.get("deployment_config_id"), gotsid, newName, ids.get("deployment_config_id"), gotsid,
									originalName, ids.get("deployment_config_id"), gotsid, originalName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dprs.persistSeedInfo(seedId, newName, pipelineName, gotsid, seedOwner, url, source_code_url);
		return Response.status(200).build();
	}

	@ApiOperation(value = "Get User List for an Application", notes = "Display the User List for an onboarded application")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 200, message = "This Application already exists in GOTS. If you are the GOTS Application Contact, register using the GOTS Application ID.") })
	@GET
	@Path("/onboardedApplicationUserList/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserList(@PathParam("gotsid") String gotsid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(102);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		List<GotsDetail> objectlist = new ArrayList<GotsDetail>();
		GenericEntity<List<GotsDetail>> list = new GenericEntity<List<GotsDetail>>(objectlist) {
		};
		String response = (aafcon.get("/authz/userRoles/role/" + ""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user"));

		ObjectMapper mapper = new ObjectMapper();
		Map object = null;
		if (response.equals("")) {

		} else {
			try {
				object = mapper.readValue(response, Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Response.status(200).entity(object).build();
	}

	@ApiOperation(value = "Onboard an Application", notes = "Onboard an Application")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 200, message = "This Application already exists in GOTS. If you are the GOTS Application Contact, register using the GOTS Application ID."),
			@ApiResponse(code = 201, message = "An Application with this Acronym already exists in AT&T Eco.<br>Would you like to submit a request to be added as an Administrator for this Application?"),
			@ApiResponse(code = 202, message = "You do not have permission to onboard this GOTS ID, please contact the GOTS Application contact"),
			@ApiResponse(code = 203, message = "GOTS application entered is not found in the system. Please check the GOTS ID"),
			@ApiResponse(code = 204, message = "No GOTS Application found, please check GOTS connection"),
			@ApiResponse(code = 205, message = "Please be sure to provide an acronym and application name"),
			@ApiResponse(code = 206, message = "Please be sure to provide a namespace name") })
	@POST
	@Path("/onboardApplication")
	@Produces(MediaType.APPLICATION_JSON)
	public Response onboardApplication(GotsDetail payload) {
		String gotsid = payload.getGotsid();
		String namespace = payload.getNamespace();
		boolean gots = true;
		if (gotsid == null || gotsid.trim().equals("")) {
			gots = false;
		}
		String info = "START";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		if (namespace == null || namespace.trim().equals("")) {
			info = "User did not provide a namespace";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"onboardApplication", info);
			PiplelineException GOTSalreadyExistsException = new PiplelineException();
			GOTSalreadyExistsException.setCode(206);
			GOTSalreadyExistsException.setMessage("Please be sure to provide a namespace.");
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"onboardApplication", info);
			return Response.status(400).entity(GOTSalreadyExistsException).build();
		}
		String applicationName = "", acronym = "";
		// If no GOTS ID, set flag
		applicationName = payload.getApplicationName();
		acronym = payload.getAcronym().toUpperCase();
		if ((applicationName == null || applicationName.trim().equals(""))
				|| (acronym == null || acronym.trim().equals(""))) {
			info = "User did not provide either an application name or acronym";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"onboardApplication", info);
			PiplelineException GOTSalreadyExistsException = new PiplelineException();
			GOTSalreadyExistsException.setCode(205);
			GOTSalreadyExistsException.setMessage("Please be sure to provide an acronym and application name.");
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"onboardApplication", info);
			return Response.status(400).entity(GOTSalreadyExistsException).build();
		}

		// search for acronym in asf_gots
		info = "No GOTS ID received. Checking if Acronym exists in GOTS.";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "SELECT * FROM camundabpmajsc6.asf_gots WHERE Acronym = ?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, acronym);
			rs = ps.executeQuery();
			if (rs.next()) {
				// If Acronym exists, throw error.
				info = "Acronym exists in GOTS. Throwing Error.";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"onboardApplication", info);
				PiplelineException GOTSalreadyExistsException = new PiplelineException();
				GOTSalreadyExistsException.setCode(200);
				GOTSalreadyExistsException.setMessage(
						"This Application already exists in GOTS. If you are the GOTS Application Contact, register using the GOTS Application ID.");
				info = "END";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"onboardApplication", info);
				return Response.status(400).entity(GOTSalreadyExistsException).build();
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
		info = "Acronym does not exist in GOTS.";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		// If acronym exists within Eco, error
		info = "Checking if Acronym exists in Eco.";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		rs = null;
		ps = null;
		con = Database.getConnection();
		sql = "Select * from asf_gots where acronym=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, acronym);
			rs = ps.executeQuery();
			if (rs.next()) {
				info = "Acronym exists in Eco. Throwing Error.";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"onboardApplication", info);
				PiplelineException GOTSalreadyExistsException = new PiplelineException();
				GOTSalreadyExistsException.setCode(201);
				GOTSalreadyExistsException.setMessage(
						"An Application with this Acronym already exists in AT&T Eco.<br>Would you like to submit a request to be added as an Administrator for this Application?");
				// TODO: If yes, do something!
				info = "END";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"onboardApplication", info);
				return Response.status(400).entity(GOTSalreadyExistsException).build();
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
		info = "Acronym does not exist in Eco. Attempting to create GOTS ID";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		// Give the GOTS ID a temporary ID
		ps = null;
		con = Database.getConnection();
		sql = "Update sequence SET ID=LAST_INSERT_ID(id+1);";
		try {
			ps = con.prepareStatement(sql);
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
		rs = null;
		ps = null;
		con = Database.getConnection();
		sql = "Select id from sequence;";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) {
				gotsid = rs.getString("id");
				info = "GOTS ID assigned for " + applicationName + " (" + acronym + "): " + gotsid;
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"onboardApplication", info);
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

		String user = request.getUserPrincipal().getName();

		createPerm(gotsid, user, true);
		ps = null;
		con = Database.getConnection();
		sql = "Insert into camundabpmajsc6.asf_gots (gots_id, Acronym, Application_Name, Description, Appl_Contact, IT_LTM, created_date, namespace, onboarded, associated_gots_id) "
				+ "values (?, ?, ?, ?, ?, ?, NOW(), ?, true, ?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, acronym);
			ps.setString(3, applicationName);
			ps.setString(4, "This Application was created by " + user + "."
					+ " A GOTS ID is required for any deployments to a production environment. "
					+ "To deploy to a production environment, this Application must be associated with a GOTS ID.");
			ps.setString(5, user);
			ps.setString(6, user);
			ps.setString(7, namespace);
			ps.setString(8, gotsid);
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
		info = "END";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"onboardApplication", info);
		return Response.status(200).build();
	}

	@ApiOperation(value = "Add Admin for a GOTSID", notes = "Grant Admin Role permission to a user for a particular GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 107, message = "You do not have access to this api") })
	@POST
	@Path("/onboardApplicationAdmin/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAdmin(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(107);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		String addUserRoleJson = "{\"user\":\"" + UserInfoService.fullyQualify(userid) + "\",\"role\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid
				+ ".admin\"}";
		AAFConnector aafcon = new AAFConnector();
		aafcon.post("/authz/userRole", addUserRoleJson);
		return Response.status(200).build();
	}

	@ApiOperation(value = "Add User for a GOTSID", notes = "Grant User Role permission to a user for a particular GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 108, message = "You do not have access to this api") })
	@POST
	@Path("/onboardApplicationUser/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUser(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(108);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		String addUserRoleJson = "{\"user\":\"" + UserInfoService.fullyQualify(userid) + "\",\"role\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid
				+ ".user\"}";
		AAFConnector aafcon = new AAFConnector();
		aafcon.post("/authz/userRole", addUserRoleJson);
		return Response.status(200).build();
	}

	@ApiOperation(value = "Extension of Admin Expiry date for a GOTSID", notes = "Extending the Expiry date for an admin role for a particular GOTSID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 111, message = "You do not have access to this api") })
	@POST
	@Path("/extendApplicationAdmin/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response extendAdmin(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(111);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		aafcon.put("/authz/userRole/extend/" + UserInfoService.fullyQualify(userid) + "/"+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin", "");
		return Response.status(200).build();
	}

	@ApiOperation(value = "Extension of User Expiry date for a GOTSID", notes = "Extending the Expiry date for an admin role for a particular GOTSID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 112, message = "You do not have access to this api") })
	@POST
	@Path("/extendApplicationUser/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response extendUser(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(112);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		aafcon.put("/authz/userRole/extend/" + UserInfoService.fullyQualify(userid) + "/"+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user", "");
		return Response.status(200).build();
	}

	@ApiOperation(value = "Delete Admin role for a GOTSID", notes = "Revoke the admin role permission for a particular GOTSID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 109, message = "You do not have access to this api") })
	@POST
	@Path("/removeApplicationAdmin/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeAdmin(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(109);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		aafcon.delete("/authz/userRole/" + UserInfoService.fullyQualify(userid) + "/" + ""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin", "");
		return Response.status(200).build();
	}

	@ApiOperation(value = "Delete User role for a GOTSID", notes = "Revoke the User role permission for a particular GOTSID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 110, message = "You do not have access to this api") })
	@POST
	@Path("/removeApplicationUser/{gotsid}/{userid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUser(@PathParam("gotsid") String gotsid, @PathParam("userid") String userid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(110);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		AAFConnector aafcon = new AAFConnector();
		aafcon.delete("/authz/userRole/" + UserInfoService.fullyQualify(userid) + "/" + ""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user", "");
		return Response.status(200).build();
	}

	private void createPerm(String gotsid, String userid, Boolean addAdmin) {
		String adminjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
				+ "\",\n  \"action\":\"admin\"\n}";
		String userjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
				+ "\",\n  \"action\":\"user\"\n}";
		String adminRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin\"\n}";
		String userRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user\"\n}";
		String addAdminPermJson = "{\n    \"perm\":" + adminjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots."
				+ gotsid + ".admin\"\n}";
		String addUserPermJson = "{\n    \"perm\":" + userjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid
				+ ".user\"\n}";
		String addUserRoleJson = "{\"user\":\"" + UserInfoService.fullyQualify(userid) + "\",\"role\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid
				+ ".admin\"}";
		AAFConnector aafcon = new AAFConnector();
		aafcon.post("/authz/perm", adminjson);
		aafcon.post("/authz/perm", userjson);
		aafcon.post("/authz/role", adminRole);
		aafcon.post("/authz/role", userRole);
		aafcon.post("/authz/role/perm", addAdminPermJson);
		aafcon.post("/authz/role/perm", addUserPermJson);
		if (addAdmin)
			aafcon.post("/authz/userRole", addUserRoleJson);

	}

	private void cleanPerm(String gotsid) {
		String adminjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
				+ "\",\n  \"action\":\"admin\"\n}";
		String userjson = "{ \"type\":\""+((String) System.getProperty("aafnamespace"))+".gots\",\n  \"instance\":\"" + gotsid
				+ "\",\n  \"action\":\"user\"\n}";
		String adminRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".admin\"\n}";
		String userRole = "{ \"name\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid + ".user\"\n}";
		String addAdminPermJson = "{\n    \"perm\":" + adminjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots."
				+ gotsid + ".admin\"\n}";
		String addUserPermJson = "{\n    \"perm\":" + userjson + ",\n    \"role\":\""+((String) System.getProperty("aafnamespace"))+".gots." + gotsid
				+ ".user\"\n}";
		AAFConnector aafcon = new AAFConnector();

		aafcon.delete("/authz/role?force=true", adminRole);
		aafcon.delete("/authz/role?force=true", userRole);
		aafcon.delete("/authz/perm?force=true", adminjson);
		aafcon.delete("/authz/perm?force=true", userjson);
	}

	@ApiOperation(value = "List onboarded application", notes = "Display the list of all onboarded application")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getOnboardedApplication")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getlist() {
		AAFConnector aafcon = new AAFConnector();
		List<GotsDetail> objectlist = new ArrayList<GotsDetail>();
		GenericEntity<List<GotsDetail>> list = new GenericEntity<List<GotsDetail>>(objectlist) {
		};
		String response = (aafcon.get("/authz/perms/user/" + UserInfoService.fullyQualify(request.getUserPrincipal().getName())));
		ObjectMapper mapper = new ObjectMapper();
		Map object = null;
		if (response.equals("")) {

		} else {
			try {
				object = mapper.readValue(response, Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		HashMap gotsmap = new HashMap<String, String>();
		if (object != null && object.get("perm") != null) {
			ArrayList permarray = (ArrayList) object.get("perm");
			for (int i = 0; i < permarray.size(); i++) {
				Map perm = (Map) permarray.get(i);
				if (((String) perm.get("type")).equalsIgnoreCase(((String) System.getProperty("aafnamespace"))+".gots")) {
					if (((String) perm.get("action")).equalsIgnoreCase("admin")) {
						gotsmap.put(((String) perm.get("instance")), "admin");
					} else if (((String) perm.get("action")).equalsIgnoreCase("user")) {
						if (((String) gotsmap.get((String) perm.get("instance"))) == null) {
							gotsmap.put(((String) perm.get("instance")), "user");
						}
					}
				}
			}
			
			Iterator it = gotsmap.entrySet().iterator();

			while (it.hasNext()) {
				GotsDetail detail = new GotsDetail();
				Map.Entry pair = (Map.Entry) it.next();
				String gotsid = (String) pair.getKey();
				detail.setGotsid((String) pair.getKey());
				detail.setRole((String) pair.getValue());

				ResultSet rs = null;
				PreparedStatement ps = null;
				Connection con = Database.getConnection();
				String sql = "SELECT * FROM camundabpmajsc6.asf_gots where gots_id=?";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					rs = ps.executeQuery();
					if (rs.next()) {
						detail.setAcronym(rs.getString("Acronym"));
						detail.setApplicationName(rs.getString("Application_Name"));
						detail.setDescription(rs.getString("Description"));
						detail.setAppContact(rs.getString("Appl_Contact"));
						detail.setIT_LTM(rs.getString("IT_LTM"));
						detail.setAssociated_gots_id(rs.getString("associated_gots_id"));
						// associated gots
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

				objectlist.add(detail);
			}
			Collections.sort(objectlist, new Comparator<GotsDetail>() {
				public int compare(GotsDetail gotsid1, GotsDetail gotsid2) {
					if (gotsid1.getAcronym() != null && gotsid2.getAcronym() != null) {
						if (gotsid1.getAcronym().compareTo(gotsid2.getAcronym()) < 0) {
							return -1;
						} else {
							return 1;
						}
					}
					return 1;
				}
			});
		}

		return Response.status(200).entity(list).build();
	}


	@ApiOperation(value = "Request to contact the application admin", notes = "Request to contact the application admin")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 100, message = "There was an error searching for the GOTS ID provided. Please ensure your GOTS ID exists in GOTS."),
			@ApiResponse(code = 101, message = "Please provide a reason for your GOTS Application registration request."),
			@ApiResponse(code = 102, message = "The GOTS ID provided does not exist in GOTS.") })
	@POST
	@Path("/applicationContactRequest")
	@Produces(MediaType.APPLICATION_JSON)
	public Response applicationContactRequest(EmailDetail ed) {
		String gotsid = ed.getGotsid();
		String reason = ed.getReason().trim();
		String info = "START";
		ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"applicationContactRequest", info);
		String requesterId = request.getUserPrincipal().getName();
		boolean exists = false;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "SELECT * FROM `camundabpmajsc6`.`asf_gots` WHERE gots_id = ?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			rs = ps.executeQuery();
			if (rs.next()) {
				exists = true;
			}
		} catch (Exception e) {
			info = "There was an error searching for the GOTS ID provided. Please ensure your GOTS ID exists in GOTS.";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"applicationContactRequest", info);
			PiplelineException databaseUpdateException = new PiplelineException();
			databaseUpdateException.setCode(100);
			databaseUpdateException.setMessage(
					"There was an error searching for the GOTS ID provided. Please ensure your GOTS ID exists in GOTS.");
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"associateGots", info);
			return Response.status(400).entity(databaseUpdateException).build();
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
		if (exists) {
			info = requesterId + " is contacting the application contact to associate to GOTS: " + gotsid;
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"applicationContactRequest", info);
			if (reason == null || reason.equals("")) {
				info = requesterId = " did not provide a reason. Throwing error.";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"applicationContactRequest", info);
				PiplelineException noReasonException = new PiplelineException();
				noReasonException.setCode(101);
				noReasonException.setMessage("Please provide a reason for your GOTS Application registration request.");
				info = "END";
				ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
						"applicationContactRequest", info);
				return Response.status(400).entity(noReasonException).build();
			} else {
				EmailNotification.applicationContactRequest(gotsid, reason, requesterId);
				return Response.status(200).build();
			}
		} else {
			info = requesterId = "The GOTS ID provided does not exist in GOTS.";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"applicationContactRequest", info);
			PiplelineException noReasonException = new PiplelineException();
			noReasonException.setCode(102);
			noReasonException.setMessage("The GOTS ID provided does not exist in GOTS.");
			info = "END";
			ApplicationLogger.callLog(gotsid, "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"applicationContactRequest", info);
			return Response.status(400).entity(noReasonException).build();
		}
	}

	@ApiOperation(value = "Request Admin Access", notes = "Request Admin Access for a GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "Please provide a reason for your GOTS Application registration request."),
			@ApiResponse(code = 102, message = "The GOTS ID provided does not exist in GOTS.") })
	@POST
	@Path("/administratorAccessRequest")
	@Produces(MediaType.APPLICATION_JSON)
	public Response administratorAccessRequest(EmailDetail ed) {
		String gotsid = ed.getGotsid();
		String reason = ed.getReason().trim();
		String info = "START";
		ApplicationLogger.callLog("", "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"administratorAccessRequest", info);
		String requester = request.getUserPrincipal().getName();
		info = requester + " is requesting access to Acronym: " + gotsid;
		ApplicationLogger.callLog("", "", "", "", "", "", "DeploymentPipelineOnboardingService",
				"administratorAccessRequest", info);
		if (reason == null || reason.equals("")) {
			info = requester + " did not provide a reason. Throwing error.";
			ApplicationLogger.callLog("", "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"administratorAccessRequest", info);
			PiplelineException noReasonException = new PiplelineException();
			noReasonException.setCode(101);
			noReasonException.setMessage("Please provide a reason for your Administrator Access request.");
			info = "END";
			ApplicationLogger.callLog("", "", "", "", "", "", "DeploymentPipelineOnboardingService",
					"administratorAccessRequest", info);
			return Response.status(400).entity(noReasonException).build();
		} else {
			EmailNotification.administratorAccessRequest(gotsid, reason, requester);
			return Response.status(200).build();
		}
	}

	@ApiOperation(value = "Assign Cluster", notes = "Onboard to cluster")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Stack Trace Error.") })
	@POST
	@Path("/assignCluster")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignCluster() {
		String jsonString = "";
		try {
			jsonString = IOUtils.toString(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		int statusCode = 0;
		String responseString = "";
		try {
			Cookie[] cookies = request.getCookies();
			String authCookie = "";
			for (Cookie cookie : cookies) {
				authCookie += cookie.getName() + "=" + cookie.getValue() + ";";
			}
			String url = Database.getSystemConfig("OnboardAPI_url");
			String postBody = jsonString;
			HttpPut request = new HttpPut(url);
			StringEntity params = new StringEntity(postBody);
			request.addHeader("content-type", "application/json");
			request.addHeader("Cookie", authCookie);
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
			StatusLine status = response.getStatusLine();
			statusCode = status.getStatusCode();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(400).build();
		}
		return Response.status(statusCode).entity(responseString).build();
	}
}
