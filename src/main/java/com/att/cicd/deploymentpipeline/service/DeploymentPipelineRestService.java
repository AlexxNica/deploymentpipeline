/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.mysql.jdbc.Statement;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/pipeline")
@Path("/pipelineconfig")
public class DeploymentPipelineRestService {
	@Autowired
	JdbcTemplate jdbcTemplate;

	public static void main(String[] args) throws SQLException {
		// ApplicationContext context = new FileSystemXmlApplicationContext(
		// "C:/dev/att/ajsccamunda/deploymentpipeline/src/main/ajsc/deploymentpipeline_v1/deploymentpipeline/v1/conf/serviceBeans.xml");
		DeploymentPipelineRestService rest = new DeploymentPipelineRestService();
		// rest.getlist("15", "xcxzcxzxc");
		// Response r = (rest.getconfig("20978", "jackietest",
		// "application-delivery-workflow"));
		// System.out.println(r);
		// ArrayList originalArray = new ArrayList();
		// originalArray.add("name1");
		// originalArray.add("name2");
		// originalArray.add("name3");
		// originalArray.add("name4");
		// originalArray.add("name5");
		// System.out.println(originalArray);
		// System.out.println(rest.insertBefore(originalArray, 3, 0));
		System.out.println(rest.checkGotsIntegrity("TMP10032", "17", "11", "39"));
	}

	private static final Logger LOGGER = Logger.getLogger(DeploymentPipelineRestService.class);

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	public ArrayList<String> insertAfter(ArrayList<String> originalArray, int originalPosition, int targetPosition) {
		String originalInfo = originalArray.get(originalPosition);
		if (originalPosition < targetPosition) {
			originalArray.remove(originalPosition);
			originalArray.add(targetPosition, originalInfo);
		} else if (originalPosition > targetPosition) {
			originalArray.remove(originalPosition);
			originalArray.add(targetPosition + 1, originalInfo);
		}
		return originalArray;
	}

	@ApiOperation(value = "List Pipeline details", notes = "Displays all the details of a pipeline for a GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getlist/{gotsid}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getlist(@PathParam("gotsid") String gotsid, @PathParam("name") String name) {

		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		try {
			System.out.println("Connection Obj : " + con.getMetaData().getURL());
		} catch (SQLException e) {
			System.out.println("DB connection null");
			e.printStackTrace();
			return Response.status(400).build();
		}
		String sql = "SELECT DISTINCT(camundabpmajsc6.pipeline_gots_config.name),  "
				+ "camundabpmajsc6.pipeline_config.pipeline_name From camundabpmajsc6.pipeline_config "
				+ "INNER JOIN camundabpmajsc6.pipeline_gots_config ON camundabpmajsc6.pipeline_gots_config.pipeline_config_id = pipeline_config.pipeline_config_id "
				+ "where camundabpmajsc6.pipeline_gots_config.gots_id=? and camundabpmajsc6.pipeline_gots_config.name <> ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			rs = ps.executeQuery();
			List<PipelineConfig> configlist = new ArrayList<PipelineConfig>();
			GenericEntity<List<PipelineConfig>> list = new GenericEntity<List<PipelineConfig>>(configlist) {
			};

			while (rs.next()) {
				PipelineConfig config = new PipelineConfig();
				config.setName(rs.getString("name"));
				config.setPipeline_name(rs.getString("pipeline_name"));
				ResultSet rs2 = null;
				PreparedStatement ps2 = null;
				Connection con2 = Database.getConnection();
				String sql2 = "select DISPLAY_NAME, ICON from seedrepository.seed where id in "
						+ "(select seed_id from pipeline_seed_info where name=? and pipeline_name=? and gots_id=?);";
				try {
					ps2 = con2.prepareStatement(sql2);
					ps2.setString(1, rs.getString("name"));
					ps2.setString(2, rs.getString("pipeline_name"));
					ps2.setString(3, gotsid);
					rs2 = ps2.executeQuery();
					if (rs2.next()) {
						byte[] icon = rs2.getBytes("ICON");
						String image = Base64.getEncoder().encodeToString(icon);
						config.setSeed_display_name(rs2.getString("DISPLAY_NAME"));
						config.setSeed_icon(image);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return Response.status(400).build();
				} catch (Exception e) {
				} finally {
					try {
						if (rs2 != null) {
							rs2.close();
						}
						if (ps2 != null) {
							ps2.close();
						}
						if (con2 != null) {
							con2.close();
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				configlist.add(config);
			}
			return Response.status(200).entity(list).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(400).build();
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

	@ApiOperation(value = "Check if the Pipeline already exists for the GOTSID", notes = "Checks if the Pipeline already exists for a particular application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "The Pipeline Name already exists"),
			@ApiResponse(code = 401, message = "You do not have access to the api"),
			@ApiResponse(code = 402, message = "The Pipeline Type is not Jenkins Automated Workflow") })
	@POST
	@Path("/checkPipelineExists/{gotsid}/{name}/{pipeline_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkPipelineExists(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name) throws SQLException {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(401).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pmc.gots_id, pmc.name, pc.pipeline_name from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name=? and pipeline_name=? order by pc.pipeline_config_id";
		ps = con.prepareStatement(sql);
		ps.setString(1, gotsid);
		ps.setString(2, name);
		ps.setString(3, pipeline_name);
		rs = ps.executeQuery();

		if (!rs.next()) {
			return Response.status(200).build();
		} else {

			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(400);
			configAlreadyExistsException.setMessage(
					"The Pipeline Name " + name + " already exists for " + pipeline_name + " pipeline type.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
	}

	@ApiOperation(value = "Create new Pipeline Configuration", notes = "Create new Pipeline configuration for an application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "The Pipeline Name already exists.") })
	@POST
	@Path("/createconfig/{gotsid}/{name}/{pipeline_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createconfig(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name) throws SQLException {
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pmc.gots_id, pmc.name, pc.pipeline_name, pc.submodule_name, pc.process_name_label,"
				+ "pc.process_name, pc.attribute_name, pmc.attribute_value, pc.attribute_type, pc.description, pc.label, "
				+ "pc.submodule_label, pc.validation from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name = ? and pipeline_name = ? order by pc.pipeline_config_id";

		ps = con.prepareStatement(sql);
		ps.setString(1, gotsid);
		ps.setString(2, name);
		ps.setString(3, pipeline_name);
		rs = ps.executeQuery();

		if (!rs.next()) {
			return getconfig(gotsid, name, pipeline_name);
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(400);
			configAlreadyExistsException.setMessage("The Pipeline Name " + name + " already exists.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
	}

	@ApiOperation(value = "Create CDP workflow Configuration", notes = "Create new CDP workflow configuration for an application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not permitted to generate a seed.") })
	@POST
	@Path("/createCDPWorkflowConfig")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createCDPWorkflowConfig(@ApiParam CDPWorkflowConfig cdp) throws SQLException {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + cdp.getGotsid() + "|admin")) {
			String pipeline_name = "cdp-workflow-v1";
			/****************************************************************
			 * Create a pipeline flow with DEV deployment config, if it does not
			 * already exist.
			 ****************************************************************/
			Map l = null;
			List<Map<String, Object>> buildServer = new ArrayList<Map<String, Object>>();
			String buildServerID = "";
			try {
				buildServer = jdbcTemplate.queryForList(
						"select * "
								+ "from camundabpmajsc6.build_server_environments where environment_url=? and username=? "
								+ "and build_server_type='jenkins' and gots_id=?",
						cdp.getUrl(), cdp.getUsername(), cdp.getGotsid());
				for (int i = 0; i < buildServer.size(); i++) {
					Map<String, Object> result = buildServer.get(i);
					if (EncryptDecryptService.decrypt(result.get("password").toString()).equals(cdp.getPassword())) {
						buildServerID = result.get("build_server_environment_id").toString();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String pipeline_flow_id = "";
			try {
				l = jdbcTemplate.queryForMap(
						"SELECT pipeline_flow_id FROM pipeline_flow where gots_id=? and name = 'DefaultPipelineFlow'",
						cdp.getGotsid());
				pipeline_flow_id = String.valueOf(l.get("pipeline_flow_id"));
			} catch (Exception ex) {
				/****
				 * Pipeline flow name 'DefaultPipelineFlow' does not already
				 * exist, creating..
				 ****/
				PipelineFlowDetail body = new PipelineFlowDetail();
				body.setGotsid(cdp.getGotsid());
				body.setName("DefaultPipelineFlow");
				body.setDescription("This is the default pipeline flow from generating a seed");
				Response r = savePipelineFlow(body);
				pipeline_flow_id = r.getEntity().toString();

				DeploymentConfigurationDetail dcd = new DeploymentConfigurationDetail();
				dcd.setPipeline_flow_id(pipeline_flow_id);
				dcd.setPipeline_name("DefaultPipelineFlow");
				dcd.setName("DEV");
				dcd.setGotsid(cdp.getGotsid());
				dcd.setBuild_server_id(buildServerID);
				dcd.setEnable_notification(true);
				dcd.setNotification_list(request.getUserPrincipal().getName());
				dcd.setEnable_approval(false);
				dcd.setDescription("This is the default deployment configuration from generating a seed");
				saveDeploymentConfiguration(dcd);
			}

			/****************************************************************
			 * Create a cdp-workflow-v1 pipeline with that pipeline flow
			 ****************************************************************/
			Response r = createconfig(cdp.getGotsid(), cdp.getName(), pipeline_name);
			if (r.getStatus() == 400) {
				return r;
			}
			try {
				jdbcTemplate.update(
						"Update pipeline_gots_config pmc set attribute_value=? where pmc.gots_id=? "
								+ "and pmc.name=? and pipeline_config_id="
								+ " (select pipeline_config_id from pipeline_config pc where pc.pipeline_name='cdp-workflow-v1'"
								+ " and pc.process_name='global' and pc.attribute_name='pipelineFlowDropDown' and "
								+ "pc.submodule_name='CDP Pipeline Workflow')",
						pipeline_flow_id, cdp.getGotsid(), cdp.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			/****************************************************************
			 * Persist seed information from catalogAPI to local database
			 ****************************************************************/
			persistSeedInfo(cdp.getSeed_id(), cdp.getName(), pipeline_name, cdp.getGotsid(), cdp.getSeed_owner(),
					cdp.getUrl(), cdp.getSource_code_url());

			/****************************************************************
			 * Set jenkins job and jenkins params for DEV deployment
			 * configuration, if a deployment configuration for DEV exists.
			 ****************************************************************/
			try {
				l = jdbcTemplate.queryForMap(
						"select deployment_config_id from deployment_config where gots_id=? "
								+ "and pipeline_flow_id=? and name='DEV' and pipeline_name='DefaultPipelineFlow'",
						cdp.getGotsid(), pipeline_flow_id);
				String deploymentConfigId = String.valueOf(l.get("deployment_config_id"));
				getPipelineFlowConfig(pipeline_flow_id, cdp.getName(), cdp.getGotsid());
				jdbcTemplate.update(
						"UPDATE `camundabpmajsc6`.`pipeline_deployment_config` SET `jenkins_job` = ?, `jenkins_params` = ? WHERE gots_id=? and name=?;",
						cdp.getDevJobName(), cdp.getDevJenkinsParams(), cdp.getGotsid(), cdp.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not permitted to generate a seed.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();
	}

	@ApiOperation(value = "Create Jenkins Pipeline Configuration", notes = "Create new Jenkins Pipeline configuration for an application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not permitted to generate a seed.") })
	@POST
	@Path("/createJenkinsPipelineConfig")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createJenkinsPipelineConfig(JenkinsPipelineConfig jpc) throws SQLException {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + jpc.getGotsid() + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not permitted to generate a seed.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}

		String pipeline_name = "jenkins-automated-deployment-workflow-v1";
		Response r = createconfig(jpc.getGotsid(), jpc.getName(), pipeline_name);

		// --------------
		// Persist the Seed info from CatalogAPI in to our local Database
		// pipeline_seed_info

		persistSeedInfo(jpc.getSeed_id(), jpc.getName(), pipeline_name, jpc.getGotsid(), jpc.getSeed_owner(),
				jpc.getUrl(), jpc.getSource_code_url());

		// ---------------
		if (r.getStatus() == 400) {
			return r;
		}
		addDeploymentConfig(jpc.getGotsid(), pipeline_name, "developmentLoop", jpc.getName(), "SampleDeployment");
		// addDeploymentConfig(jpc.getGotsid(), pipeline_name, "qcLoop",
		// jpc.getName(), "FirstDeployment");
		// addDeploymentConfig(jpc.getGotsid(), pipeline_name, "clientLoop",
		// jpc.getName(), "FirstDeployment");
		// addDeploymentConfig(jpc.getGotsid(), pipeline_name, "productionLoop",
		// jpc.getName(), "FirstDeployment");

		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select build_server_environment_id, password from camundabpmajsc6.build_server_environments where environment_url=? and username=? and build_server_type='jenkins' and gots_id=?";
		String buildServerId = "";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, jpc.getUrl());
			ps.setString(2, jpc.getUsername());
			ps.setString(3, jpc.getGotsid());
			rs = ps.executeQuery();
			while (rs.next()) {
				if (jpc.getPassword().equals(EncryptDecryptService.decrypt(rs.getString("password")))) {
					buildServerId = rs.getString("build_server_environment_id");
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
		saveDeploymentAttribute(jpc.getGotsid(), pipeline_name, jpc.getName(), "triggerJenkinsDevelopment",
				"dev-pipeline-jenkins", "developmentLoop", "1", "buildServerDropDown", buildServerId);
		saveDeploymentAttribute(jpc.getGotsid(), pipeline_name, jpc.getName(), "triggerJenkinsDevelopment",
				"dev-pipeline-jenkins", "developmentLoop", "1", "jobName", jpc.getDevJobName());
		saveDeploymentAttribute(jpc.getGotsid(), pipeline_name, jpc.getName(), "triggerJenkinsDevelopment",
				"dev-pipeline-jenkins", "developmentLoop", "1", "jenkinsParams", jpc.getDevJenkinsParams());
		saveDeploymentAttribute(jpc.getGotsid(), pipeline_name, jpc.getName(), "triggerJenkinsDevelopment",
				"dev-pipeline-jenkins", "developmentLoop", "1", "automatic", "true");
		saveConfig(jpc.getGotsid(), jpc.getName(), pipeline_name, "RequestProductionTestingApproval",
				"client-testing-pipeline-jenkins", "enableApproval", "true");

		return Response.status(200).build();
	}

	public void persistSeedInfo(String seed_id, String name, String pipeline_name, String gotsid, String seed_owner,
			String url, String source_code_url) {
		// TODO Auto-generated method stub

		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "INSERT INTO `camundabpmajsc6`.`pipeline_seed_info`(`seed_id`, `name`,`pipeline_name`, `gots_id`,`seed_owner`,`build_server_url`,`source_code_url`,created_time) "
				+ "values (?,?,?,?,?,?,?,NOW())";
		try {

			ps = con.prepareStatement(sql);
			ps.setString(1, seed_id);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, gotsid);
			ps.setString(5, seed_owner);
			ps.setString(6, url);
			ps.setString(7, source_code_url);
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

	@ApiOperation(value = "Delete Pipeline Configuration", notes = "Delete Pipeline configuration for an application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 401, message = "The Pipeline Name cannot be deleted because it has processes currently running. Please end all current running processes.") })
	@POST
	@Path("removeconfig/{gotsid}/{pipelineName}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeconfig(@PathParam("gotsid") String gotsid, @PathParam("pipelineName") String pipelineName,
			@PathParam("name") String name) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		ArrayList currentProcesses = c
				.jsonToArrayList(c.get("camunda/api/engine/engine/default/process-instance?processDefinitionKey="
						+ pipelineName + "&businessKey=" + gotsid + "&variables=name_eq_" + name, cookie, true));
		if (currentProcesses == null || currentProcesses.size() == 0) {
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "delete pmc from camundabpmajsc6.pipeline_gots_config pmc "
					+ "inner join camundabpmajsc6.pipeline_config pc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and name=? and pipeline_name=?";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, name);
				ps.setString(3, pipelineName);
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
			/**
			 * Delete from pipeline_seed_info
			 */
			try {
				jdbcTemplate.update("delete from pipeline_seed_info where pipeline_name=? and gots_id=? and name=?",
						pipelineName, gotsid, name);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/**
			 * Delete from pipeline_deployment_config
			 */
			if (pipelineName.equals("cdp-workflow-v1")) {
				try {
					jdbcTemplate.update(
							"delete from camundabpmajsc6.pipeline_deployment_config where gots_id=? and name=?", gotsid,
							name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return Response.status(200).build();
		} else {
			PiplelineException processesRunning = new PiplelineException();
			processesRunning.setCode(401);
			processesRunning.setMessage("The Pipeline Name " + name
					+ " cannot be deleted because it has processes currently running. Please end all current running processes.");
			return Response.status(400).entity(processesRunning).build();
		}
	}

	@ApiOperation(value = "List Pipeline Configuration", notes = "List Pipeline configuration for an application GOTSID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 401, message = "The Pipeline Name cannot be deleted because") })
	@GET
	@Path("/getconfig/{gotsid}/{name}/{pipeline_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getconfig(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name) {

		generateConfig(gotsid, name, pipeline_name);

		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pmc.gots_id, pmc.name, pc.pipeline_name, pc.submodule_name, pc.process_name_label,"
				+ "pc.process_name, pc.attribute_name, pmc.attribute_value, pc.attribute_type, pc.description, pc.label, "
				+ "pc.submodule_label, pc.validation, pc.config_identifier from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name = ? and pipeline_name = ? and (config_type<>'array' or config_type is null) order by pc.config_order";
		try {

			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			rs = ps.executeQuery();
			List<PipelineConfigDetail> configlist = new ArrayList<PipelineConfigDetail>();
			GenericEntity<List<PipelineConfigDetail>> list = new GenericEntity<List<PipelineConfigDetail>>(configlist) {
			};

			while (rs.next()) {
				PipelineConfigDetail config = new PipelineConfigDetail();
				config.setGots_id(rs.getString("gots_id"));
				config.setName(rs.getString("name"));
				config.setPipeline_name(rs.getString("pipeline_name"));
				config.setSubmodule_name(rs.getString("submodule_name"));
				config.setProcess_name_label(rs.getString("process_name_label"));
				config.setProcess_name(rs.getString("process_name"));
				config.setAttribute_name(rs.getString("attribute_name"));
				config.setAttribute_value(rs.getString("attribute_value"));
				config.setAttribute_type(rs.getString("attribute_type"));
				config.setDescription(rs.getString("description"));
				config.setLabel(rs.getString("label"));
				config.setSubmodule_label(rs.getString("submodule_label"));
				config.setValidation(rs.getString("validation"));
				config.setConfig_identifier(rs.getString("config_identifier"));

				configlist.add(config);
			}
			return Response.status(200).entity(list).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(400).build();
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

	public static int getMaxArrayIndex(String config_identifier, String name, String gotsid) {
		// select max index from previous configs
		PreparedStatement ps3 = null;
		ResultSet rs3 = null;
		Connection con3 = Database.getConnection();
		String sql = "select count(distinct(pmc.array_indexer)) as 'max' from camundabpmajsc6.pipeline_gots_config pmc "
				+ "join camundabpmajsc6.pipeline_config pc where pc.pipeline_config_id = pmc.pipeline_config_id and pc.config_identifier=? and pmc.name=? and pmc.gots_id=? "
				+ "and (pmc.array_indexer is not null or pmc.array_indexer<>'');";
		int maxArrayIndex = 0;
		try {
			ps3 = con3.prepareStatement(sql);
			ps3.setString(1, config_identifier);
			ps3.setString(2, name);
			ps3.setString(3, gotsid);
			rs3 = ps3.executeQuery();
			if (rs3.next()) {
				maxArrayIndex = rs3.getInt("max");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs3 != null) {
					rs3.close();
				}
				if (ps3 != null) {
					ps3.close();
				}
				if (con3 != null) {
					con3.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return maxArrayIndex;
	}

	public ArrayList<String> insertBefore(ArrayList<String> originalArray, int originalPosition, int targetPosition) {
		String originalInfo = originalArray.get(originalPosition);
		if (originalPosition < targetPosition) {
			originalArray.remove(originalPosition);
			originalArray.add(targetPosition - 1, originalInfo);
		} else if (originalPosition > targetPosition) {
			originalArray.remove(originalPosition);
			originalArray.add(targetPosition, originalInfo);
		}
		return originalArray;
	}

	@ApiOperation(value = "Edit Deployment Sequence for Jenkins Workflow", notes = "Edit Deployment Sequence for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 401, message = "The Deployment Sequence cannot be edited because this configuration currently has processes running. Please end all current running processes to edit the sequence."),
			@ApiResponse(code = 402, message = "Cannot have two deployments with the same index.") })
	@POST
	@Path("editDeploymentSequence/{gotsid}/{name}/{pipeline_name}/{config_identifier}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response editDeploymentSequence(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("config_identifier") String config_identifier,
			String body) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		ArrayList currentProcesses = c
				.jsonToArrayList(c.get("camunda/api/engine/engine/default/process-instance?processDefinitionKey="
						+ pipeline_name + "&businessKey=" + gotsid + "&variables=name_eq_" + name, cookie, true));
		// if (currentProcesses == null || currentProcesses.size() == 0) {
		ArrayList bodyDeploymentNames = c.jsonToArrayList(body);
		Map indexChecker = new HashMap();
		ArrayList deploymentNames = new ArrayList();
		for (int i = 0; i < bodyDeploymentNames.size(); i++) {
			if (indexChecker.containsKey(c.getJsonString((Map) bodyDeploymentNames.get(i), "index"))) {
				PiplelineException processesRunning = new PiplelineException();
				processesRunning.setCode(402);
				processesRunning.setMessage("Cannot have two deployments with the same index.");
				return Response.status(400).entity(processesRunning).build();
			} else {
				indexChecker.put(c.getJsonString((Map) bodyDeploymentNames.get(i), "index"), "");
			}
			Map info = new HashMap();
			info.put("name", c.getJsonString((Map) bodyDeploymentNames.get(i), "name"));
			info.put("index", c.getJsonString((Map) bodyDeploymentNames.get(i), "index"));
			deploymentNames.add(info);
		}
		HashMap envName = new HashMap();
		for (int i = 0; i < deploymentNames.size(); i++) {
			ArrayList configIds = new ArrayList();
			HashMap info = (HashMap) deploymentNames.get(i);
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String sql = "select pmc.pipeline_gots_config_id "
					+ "from camundabpmajsc6.pipeline_config pc join camundabpmajsc6.pipeline_gots_config pmc "
					+ "on pc.pipeline_config_id = pmc.pipeline_config_id where gots_id=? "
					+ "and name=? and pipeline_name=? "
					+ "and config_identifier=? and array_indexer = (select pmc.array_indexer from camundabpmajsc6.pipeline_config pc "
					+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where attribute_value=? and pmc.name=? and config_identifier=? and (array_indexer is not null or array_indexer<>'')) "
					+ "order by pc.config_order;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, name);
				ps.setString(3, pipeline_name);
				ps.setString(4, config_identifier);
				ps.setString(5, info.get("name").toString());
				ps.setString(6, name);
				ps.setString(7, config_identifier);
				rs = ps.executeQuery();
				while (rs.next()) {
					configIds.add(rs.getString("pipeline_gots_config_id"));
				}
				envName.put(info.get("index").toString(), configIds);
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
		Iterator it = envName.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			int index = Integer.parseInt(pair.getKey().toString());
			ArrayList configIds = new ArrayList();
			configIds = (ArrayList) pair.getValue();
			for (int i = 0; i < configIds.size(); i++) {
				Connection con = Database.getConnection();
				PreparedStatement ps = null;
				String sql = "Update camundabpmajsc6.pipeline_gots_config set array_indexer=? where pipeline_gots_config_id=?;";
				try {
					ps = con.prepareStatement(sql);
					ps.setInt(1, (index + 1000));
					ps.setString(2, configIds.get(i).toString());
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
		it = envName.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			int index = Integer.parseInt(pair.getKey().toString());
			ArrayList configIds = new ArrayList();
			configIds = (ArrayList) pair.getValue();
			for (int i = 0; i < configIds.size(); i++) {
				Connection con = Database.getConnection();
				PreparedStatement ps = null;
				String sql = "Update camundabpmajsc6.pipeline_gots_config set array_indexer= array_indexer - 1000 where pipeline_gots_config_id=?;";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, configIds.get(i).toString());
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
		return Response.status(200).build();
		// } else {
		// PiplelineException processesRunning = new PiplelineException();
		// processesRunning.setCode(401);
		// processesRunning.setMessage(
		// "The Deployment Sequence cannot be edited because this configuration
		// currently has processes running. Please end all current running
		// processes to edit the sequence.");
		// return Response.status(400).entity(processesRunning).build();
		// }
	}

	@ApiOperation(value = "List Deployment Configuration Names", notes = "List all Deployment Configuration Names for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("getAllDeploymentConfigNames/{gotsid}/{pipeline_name}/{config_identifier}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllDeploymentConfigNames(@PathParam("gotsid") String gotsid,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("config_identifier") String config_identifier,
			@PathParam("name") String name) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = Database.getConnection();
		String sql = "select pmc.attribute_value, array_indexer from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name = ? and pipeline_name = ? "
				+ "and attribute_name='deploymentName' and config_identifier=? order by pmc.array_indexer;";
		ArrayList<String> results = new ArrayList<String>();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, config_identifier);
			rs = ps.executeQuery();
			while (rs.next()) {
				JSONObject configNames = new JSONObject();
				configNames.put("gotsid", gotsid);
				configNames.put("pipeline_name", pipeline_name);
				configNames.put("config_identifier", config_identifier);
				configNames.put("pipeline_config_name", name);
				configNames.put("name", rs.getString("attribute_value"));
				configNames.put("index", rs.getString("array_indexer"));
				results.add(configNames.toString());
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
		return Response.status(200).entity(results.toString()).build();
	}

	@ApiOperation(value = "Save Deployment Attribute", notes = "Persist Deployment Attribute for Jenkins Automated Workflow Pipeline Type in the database")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@POST
	@Path("saveDeploymentAttribute/{gotsid}/{pipeline_name}/{name}/{process_name}/{submodule_name}/{config_identifier}/{array_indexer}/{attribute_name}/{attribute_value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveDeploymentAttribute(@PathParam("gotsid") String gotsid,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("name") String name,
			@PathParam("process_name") String process_name, @PathParam("submodule_name") String submodule_name,
			@PathParam("config_identifier") String config_identifier, @PathParam("array_indexer") String array_indexer,
			@PathParam("attribute_name") String attribute_name, @PathParam("attribute_value") String attribute_value) {
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		if (attribute_value == null)
			attribute_value = "";
		String sql = "Update camundabpmajsc6.pipeline_gots_config mc set attribute_value=? "
				+ "where mc.gots_id=? and mc.name=? and mc.array_indexer=? "
				+ "and pipeline_config_id = (select pipeline_config_id from camundabpmajsc6.pipeline_config pc "
				+ "where pipeline_name=? and pc.process_name=? and pc.attribute_name=? "
				+ "and pc.submodule_name=? and pc.config_identifier=?);";
		try {

			ps = con.prepareStatement(sql);
			ps.setString(1, attribute_value);
			ps.setString(2, gotsid);
			ps.setString(3, name);
			ps.setString(4, array_indexer);
			ps.setString(5, pipeline_name);
			ps.setString(6, process_name);
			ps.setString(7, attribute_name);
			ps.setString(8, submodule_name);
			ps.setString(9, config_identifier);
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
		return Response.status(200).build();
	}

	@ApiOperation(value = "List Deployment Attribute", notes = "List Deployment Attributes for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("getDeploymentAttributes/{gotsid}/{pipeline_name}/{name}/{config_identifier}/{array_indexer}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeploymentAttributes(@PathParam("gotsid") String gotsid,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("name") String name,
			@PathParam("config_identifier") String config_identifier,
			@PathParam("array_indexer") String array_indexer) {
		generateConfig(gotsid, name, pipeline_name);
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pmc.gots_id, pmc.name, pc.pipeline_name, pc.submodule_name, pc.process_name_label, "
				+ "pc.process_name, pc.attribute_name, pmc.attribute_value, pc.attribute_type, "
				+ "pc.description, pc.label, pc.submodule_label, pc.validation, pc.config_identifier from camundabpmajsc6.pipeline_config pc "
				+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name=? and pipeline_name=? "
				+ "and config_identifier=? and array_indexer=? order by pc.config_order;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, config_identifier);
			ps.setString(5, array_indexer);
			rs = ps.executeQuery();
			List<PipelineConfigDetail> configlist = new ArrayList<PipelineConfigDetail>();
			GenericEntity<List<PipelineConfigDetail>> list = new GenericEntity<List<PipelineConfigDetail>>(configlist) {
			};

			while (rs.next()) {
				PipelineConfigDetail config = new PipelineConfigDetail();
				config.setGots_id(rs.getString("gots_id"));
				config.setName(rs.getString("name"));
				config.setPipeline_name(rs.getString("pipeline_name"));
				config.setSubmodule_name(rs.getString("submodule_name"));
				config.setProcess_name_label(rs.getString("process_name_label"));
				config.setProcess_name(rs.getString("process_name"));
				config.setAttribute_name(rs.getString("attribute_name"));
				config.setAttribute_value(rs.getString("attribute_value"));
				config.setAttribute_type(rs.getString("attribute_type"));
				config.setDescription(rs.getString("description"));
				config.setLabel(rs.getString("label"));
				config.setSubmodule_label(rs.getString("submodule_label"));
				config.setValidation(rs.getString("validation"));
				config.setConfig_identifier(rs.getString("config_identifier"));

				configlist.add(config);
			}
			return Response.status(200).entity(list).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(400).build();
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

	@ApiOperation(value = "Add New Deployment Configuration", notes = "Add a new Deployment Configuration for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 409, message = "The Deployment Name cannot be added because that name already Exists. Please use a unique name.") })
	@POST
	@Path("addDeploymentConfig/{gotsid}/{pipeline_name}/{config_identifier}/{name}/{deployment_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDeploymentConfig(@PathParam("gotsid") String gotsid,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("config_identifier") String config_identifier,
			@PathParam("name") String name, @PathParam("deployment_name") String deployment_name) {
		// Check to see if the name already exists in that config identifier
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "Select pmc.attribute_value from camundabpmajsc6.pipeline_config pc join camundabpmajsc6.pipeline_gots_config pmc "
				+ "on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where gots_id=? and name=? and pipeline_name=? "
				+ "and attribute_name='deploymentName' and config_identifier=? and pmc.attribute_value=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, config_identifier);
			ps.setString(5, deployment_name);
			rs = ps.executeQuery();
			if (rs.next()) {
				PiplelineException deploymentNameExists = new PiplelineException();
				deploymentNameExists.setCode(409);
				deploymentNameExists.setMessage("The Deployment Name " + deployment_name
						+ " cannot be added because that name already Exists. Please use a unique name.");
				return Response.status(400).entity(deploymentNameExists).build();
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

		// Get the config id's information from pipeline_config
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Connection con2 = Database.getConnection();
		sql = "select pipeline_config_id from camundabpmajsc6.pipeline_config where config_identifier=? and config_type='array';";
		ArrayList results = new ArrayList();
		try {
			ps2 = con2.prepareStatement(sql);
			ps2.setString(1, config_identifier);
			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				results.add(rs2.getString("pipeline_config_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs2 != null) {
					rs2.close();
				}
				if (ps2 != null) {
					ps2.close();
				}
				if (con2 != null) {
					con2.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		int maxArrayIndex = getMaxArrayIndex(config_identifier, name, gotsid);
		maxArrayIndex++;
		for (int j = 0; j < results.size(); j++) {
			PreparedStatement ps4 = null;
			Connection con4 = Database.getConnection();
			sql = "insert into camundabpmajsc6.pipeline_gots_config (pipeline_config_id, gots_id, name, array_indexer) values (?,?,?,?)";
			try {

				ps4 = con4.prepareStatement(sql);
				ps4.setString(1, results.get(j).toString());
				ps4.setString(2, gotsid);
				ps4.setString(3, name);
				ps4.setInt(4, maxArrayIndex);
				ps4.executeQuery();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (ps4 != null) {
						ps4.close();
					}
					if (con4 != null) {
						con4.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		ps = null;
		con = Database.getConnection();
		sql = "Update camundabpmajsc6.pipeline_gots_config mc set attribute_value = ? where mc.gots_id=? and mc.name=? "
				+ "and pipeline_config_id = (select pipeline_config_id from camundabpmajsc6.pipeline_config pc "
				+ "where pipeline_name = ? and pc.config_type='loop' and config_identifier=?)";
		try {

			ps = con.prepareStatement(sql);
			ps.setInt(1, maxArrayIndex);
			ps.setString(2, gotsid);
			ps.setString(3, name);
			ps.setString(4, pipeline_name);
			ps.setString(5, config_identifier);
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
		ps = null;
		con = Database.getConnection();
		sql = "Update camundabpmajsc6.pipeline_gots_config mc set attribute_value = ? where mc.gots_id=? and mc.name=? and mc.array_indexer=? "
				+ "and pipeline_config_id = (select pipeline_config_id from camundabpmajsc6.pipeline_config pc where pipeline_name = ? "
				+ "and pc.attribute_name='deploymentName' and config_identifier=?);";
		try {

			ps = con.prepareStatement(sql);
			ps.setString(1, deployment_name);
			ps.setString(2, gotsid);
			ps.setString(3, name);
			ps.setInt(4, maxArrayIndex);
			ps.setString(5, pipeline_name);
			ps.setString(6, config_identifier);
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
		if (!config_identifier.equals("developmentLoop")) {
			ps = null;
			con = Database.getConnection();
			sql = "Update camundabpmajsc6.pipeline_gots_config set attribute_value='true' where pipeline_config_id in (select pipeline_config_id from camundabpmajsc6.pipeline_config "
					+ "where config_identifier=? and attribute_name='enableDeploymentNotificationEmail') and array_indexer = ?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, config_identifier);
				ps.setInt(2, maxArrayIndex);
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

			ps = null;
			con = Database.getConnection();
			sql = "Update camundabpmajsc6.pipeline_gots_config set attribute_value='true' where pipeline_config_id in (select pipeline_config_id "
					+ "from camundabpmajsc6.pipeline_config where config_identifier=? and label='Require Approval') and array_indexer=?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, config_identifier);
				ps.setInt(2, maxArrayIndex);
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
		return Response.status(200).build();
	}

	@ApiOperation(value = "Delete Deployment Configuration", notes = "Delete Deployment Configuration for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 401, message = "This deployment configuration cannot be deleted because there are processes currently running. Please end all current running processes to delete this deployment configuration.") })
	@DELETE
	@Path("removeDeploymentConfig/{gotsid}/{pipeline_name}/{config_identifier}/{name}/{index}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeDeploymentConfig(@PathParam("gotsid") String gotsid,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("config_identifier") String config_identifier,
			@PathParam("name") String name, @PathParam("index") String index) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		ArrayList currentProcesses = c
				.jsonToArrayList(c.get("camunda/api/engine/engine/default/process-instance?processDefinitionKey="
						+ pipeline_name + "&businessKey=" + gotsid + "&variables=name_eq_" + name, cookie, true));
		// if (currentProcesses == null || currentProcesses.size() == 0) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = Database.getConnection();
		String sql = "delete from camundabpmajsc6.pipeline_gots_config where gots_id=? and name=? and pipeline_config_id in "
				+ "(select pipeline_config_id from camundabpmajsc6.pipeline_config where config_identifier=? and pipeline_name=?) and array_indexer=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, config_identifier);
			ps.setString(4, pipeline_name);
			ps.setString(5, index);
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

		PreparedStatement ps2 = null;
		ResultSet rs2 = null;
		Connection con2 = Database.getConnection();
		sql = "select pipeline_gots_config_id from camundabpmajsc6.pipeline_gots_config pmc "
				+ "join camundabpmajsc6.pipeline_config pc on pc.pipeline_config_id = pmc.pipeline_config_id "
				+ "where pmc.gots_id=? and pc.pipeline_name=? and pmc.name=? and array_indexer > ? and config_identifier=?;";
		ArrayList configIds = new ArrayList();
		try {
			ps2 = con2.prepareStatement(sql);
			ps2.setString(1, gotsid);
			ps2.setString(2, pipeline_name);
			ps2.setString(3, name);
			ps2.setString(4, index);
			ps2.setString(5, config_identifier);
			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				configIds.add(rs2.getString("pipeline_gots_config_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps2 != null) {
					ps2.close();
				}
				if (rs2 != null) {
					rs2.close();
				}
				if (con2 != null) {
					con2.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (int i = 0; i < configIds.size(); i++) {
			ps = null;
			con = Database.getConnection();
			sql = "Update camundabpmajsc6.pipeline_gots_config set array_indexer = array_indexer - 1 where pipeline_gots_config_id = ?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, configIds.get(i).toString());
				ps.executeQuery();
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
		}

		int maxArrayIndex = getMaxArrayIndex(config_identifier, name, gotsid);
		ps = null;
		con = Database.getConnection();
		sql = "Update camundabpmajsc6.pipeline_gots_config mc set attribute_value = ? "
				+ "where mc.gots_id=? and mc.name=? "
				+ "and pipeline_config_id = (select pipeline_config_id from camundabpmajsc6.pipeline_config pc "
				+ "where pipeline_name = ? and pc.config_type='loop' and config_identifier=?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, maxArrayIndex);
			ps.setString(2, gotsid);
			ps.setString(3, name);
			ps.setString(4, pipeline_name);
			ps.setString(5, config_identifier);
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

		return Response.status(200).build();
		// } else {
		// PiplelineException processesRunning = new PiplelineException();
		// processesRunning.setCode(401);
		// processesRunning.setMessage(
		// "This deployment configuration cannot be deleted because there are
		// processes currently running. Please end all current running processes
		// to delete this deployment configuration.");
		// return Response.status(400).entity(processesRunning).build();
		// }
	}

	@ApiOperation(value = "Save Deployment Configuration Parameters", notes = "Save Deployment Configuration Parameters for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 409, message = "The Deployment Name cannot be added because that name already Exists. Please use a unique name.") })
	@POST
	@Path("/saveconfig/{gotsid}/{name}/{pipeline_name}/{process_name}/{submodule_name}/{attribute_name}/{attribute_value}")
	@Produces(MediaType.APPLICATION_JSON)
	public void saveConfig(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("process_name") String process_name,
			@PathParam("submodule_name") String submodule_name, @PathParam("attribute_name") String attribute_name,
			@PathParam("attribute_value") String attribute_value) {
		/**
		 * If the gots id for the pipeline flow does not align with the gots ID
		 * being used, don't save
		 */
		boolean matchingGots = false;
		if (attribute_name.equals("pipelineFlowDropDown")) {
			try {
				Map<String, Object> m = null;
				m = jdbcTemplate.queryForMap("select gots_id from pipeline_flow where pipeline_flow_id=?;",
						attribute_value);
				matchingGots = m.get("gots_id").toString().equals(gotsid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			matchingGots=true;
		}
		if (matchingGots) {
			/**
			 * Save if the gots ids match.
			 **/
			PreparedStatement ps = null;
			Connection con = Database.getConnection();
			if (attribute_value == null)
				attribute_value = "";
			String sql = "Update camundabpmajsc6.pipeline_gots_config mc set attribute_value = ? "
					+ "where mc.gots_id=? and mc.name=? "
					+ "and pipeline_config_id = (select pipeline_config_id from camundabpmajsc6.pipeline_config pc "
					+ "where pipeline_name = ? and pc.process_name=? and pc.attribute_name=? and pc.submodule_name=?)";
			try {

				ps = con.prepareStatement(sql);
				ps.setString(1, attribute_value.trim());
				ps.setString(2, gotsid);
				ps.setString(3, name);
				ps.setString(4, pipeline_name);
				ps.setString(5, process_name);
				ps.setString(6, attribute_name);
				ps.setString(7, submodule_name);
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

	@ApiOperation(value = "Add new Build Server Configuration", notes = "Add new Build Server Configuration for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Build Server name already exists"),
			@ApiResponse(code = 405, message = "There was an error processing the request.") })
	@POST
	@Path("/saveBuildServer/{gotsid}/{environment_name}/{environment_url}/{username}/{password}/{build_server_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveBuildServer(@PathParam("gotsid") String gotsid,
			@PathParam("environment_name") String environment_name,
			@PathParam("environment_url") String environment_url, @PathParam("username") String username,
			@PathParam("password") String password, @PathParam("build_server_type") String build_server_type)
			throws Exception {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			Cookie[] cookies = request.getCookies();
			String authCookie = "";
			for (Cookie cookie : cookies) {
				authCookie += cookie.getName() + "=" + cookie.getValue() + ";";
			}
			String url = System.getProperty("seedurl");
			url = url + "/store/service/validateBuildserver";
			String postBody = "{\"buildServer\":\"" + environment_url + "\",\"username\":\"" + username
					+ "\",\"password\":\"" + password + "\"}";
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(postBody);
			request.addHeader("content-type", "application/json");
			request.addHeader("Cookie", authCookie);
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			CamundaConnector c = new CamundaConnector();
			Map responseBody = c.jsonToMap(responseString);
			String status = (String) responseBody.get("status");
			String error = (String) responseBody.get("description");
			if (status.toUpperCase().equals("SUCCESS")) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "select environment_name from camundabpmajsc6.build_server_environments where gots_id=? and environment_name=?;";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					ps.setString(2, environment_name);
					rs = ps.executeQuery();
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
				if (!rs.next()) {
					Database.saveBuildServer(gotsid, environment_name, environment_url, username, password,
							build_server_type);
					return Response.status(200).build();
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException
							.setMessage("The Build Server name '" + environment_name + "' already exists.");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				PiplelineException invalidCredentialsException = new PiplelineException();
				invalidCredentialsException.setCode(405);
				invalidCredentialsException.setMessage(error);
				return Response.status(400).entity(invalidCredentialsException).build();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			PiplelineException invalidCredentialsException = new PiplelineException();
			invalidCredentialsException.setCode(405);
			invalidCredentialsException.setMessage("There was an error processing the request.");
			return Response.status(400).entity(invalidCredentialsException).build();
		}
	}

	@ApiOperation(value = "List Build Server Configuration", notes = "Display Build Server Configuration for Jenkins Automated Workflow Pipeline Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getBuildServers/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildServers(@PathParam("gotsid") String gotsid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select build_server_environment_id, environment_name, environment_url, username, password, build_server_type from camundabpmajsc6.build_server_environments where gots_id = ?;";
		JSONObject environments = new JSONObject();
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
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				environments.append("environments", obj);
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
		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "List Build Server by build server type", notes = "Display Build Server Configuration by build server type for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getBuildServerByType/{gotsid}/{build_server_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildServerByType(@PathParam("gotsid") String gotsid,
			@PathParam("build_server_type") String build_server_type) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select build_server_environment_id, environment_name, environment_url, username, password, build_server_type "
				+ "from camundabpmajsc6.build_server_environments where gots_id = ? and build_server_type = ?;";
		JSONObject environments = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, build_server_type);
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
				environments.append("environments", obj);
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
		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "List Build Server by build server environment type", notes = "Display Build Server Configuration by build server environment type for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getBuildServerById/{build_server_environment_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBuildServerById(@PathParam("build_server_environment_id") String build_server_environment_id)
			throws JSONException {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select gots_id, build_server_environment_id, environment_name, environment_url, username, password, build_server_type from camundabpmajsc6.build_server_environments where build_server_environment_id = ?;";
		JSONObject environments = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, build_server_environment_id);
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
				environments.append("environments", obj);
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
		String gotsid = "";
		JSONArray system_properties = environments.getJSONArray("environments");
		for (int i = 0; i < system_properties.length(); i++) {
			JSONObject item = system_properties.getJSONObject(i);
			gotsid = item.getString("gots_id");
		}
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "Update Build Server Configuration", notes = "Update Build Server Configuration for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Build Server name already exists"),
			@ApiResponse(code = 400, message = "There was an error processing the request.") })
	@POST
	@Path("/updateBuildServer/{gotsid}/{build_server_environment_id}/{environment_name}/{environment_url}/{username}/{password}/{build_server_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateBuildServer(@PathParam("gotsid") String gotsid,
			@PathParam("build_server_environment_id") String build_server_environment_id,
			@PathParam("environment_name") String environment_name,
			@PathParam("environment_url") String environment_url, @PathParam("username") String username,
			@PathParam("password") String password, @PathParam("build_server_type") String build_server_type)
			throws Exception {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			Cookie[] cookies = request.getCookies();
			String authCookie = "";
			for (Cookie cookie : cookies) {
				authCookie += cookie.getName() + "=" + cookie.getValue() + ";";
			}
			String url = System.getProperty("seedurl");
			url = url + "/store/service/validateBuildserver";
			String postBody = "{\"buildServer\":\"" + environment_url + "\",\"username\":\"" + username
					+ "\",\"password\":\"" + password + "\"}";
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(postBody);
			request.addHeader("content-type", "application/json");
			request.addHeader("Cookie", authCookie);
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			CamundaConnector c = new CamundaConnector();
			Map responseBody = c.jsonToMap(responseString);
			String status = (String) responseBody.get("status");
			String error = (String) responseBody.get("description");
			if (status.toUpperCase().equals("SUCCESS")) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "select environment_name from camundabpmajsc6.build_server_environments where gots_id=? and environment_name=? and build_server_environment_id <> ?;";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					ps.setString(2, environment_name);
					ps.setString(3, build_server_environment_id);
					rs = ps.executeQuery();
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

				if (!rs.next()) {
					String encryptedPassword = EncryptDecryptService.encrypt(password);
					con = Database.getConnection();
					ps = null;
					sql = "update camundabpmajsc6.build_server_environments set environment_name = ?, environment_url = ?, username = ?, password = ?, build_server_type = ? where build_server_environment_id = ?";
					try {
						ps = con.prepareStatement(sql);
						ps.setString(1, environment_name);
						ps.setString(2, environment_url);
						ps.setString(3, username);
						ps.setString(4, encryptedPassword);
						ps.setString(5, build_server_type);
						ps.setString(6, build_server_environment_id);
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
					return Response.status(200).build();
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException
							.setMessage("The Build Server name '" + environment_name + "' already exists.");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				PiplelineException invalidCredentialsException = new PiplelineException();
				invalidCredentialsException.setCode(405);
				invalidCredentialsException.setMessage(error);
				return Response.status(400).entity(invalidCredentialsException).build();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			PiplelineException invalidCredentialsException = new PiplelineException();
			invalidCredentialsException.setCode(405);
			invalidCredentialsException.setMessage("There was an error processing the request.");
			return Response.status(400).entity(invalidCredentialsException).build();
		}
	}

	@ApiOperation(value = "Delete Build Server Configuration", notes = "Delete Build Server Configuration for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "This Build Server cannot be deleted because it is being used by pipeline") })
	@DELETE
	@Path("/deleteBuildServer/{gotsid}/{build_server_environment_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteBuildServer(@PathParam("build_server_environment_id") String build_server_environment_id,
			@PathParam("gotsid") String gotsid) throws SQLException {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pf.name as flowName, dc.name as configName from deployment_config dc "
				+ "join pipeline_flow pf on pf.pipeline_flow_id=dc.pipeline_flow_id"
				+ " where dc.gots_id=? and dc.build_server_id = ? limit 1;";
		String flowName = "", flowConfigName = "";
		boolean flowFound = false;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, build_server_environment_id);
			rs = ps.executeQuery();
			if (rs.next()) {
				flowName = rs.getString("flowName");
				flowConfigName = rs.getString("configName");
				flowFound = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!flowFound) {
			sql = "select pmc.name, pc.attribute_name, pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
					+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and attribute_name='buildServerDropDown' order by pc.config_order;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				rs = ps.executeQuery();
				boolean found = false;
				String value = "", name = "";
				while (rs.next() && found == false) {
					if (!(rs.getString("attribute_value") == null)) {
						value = rs.getString("attribute_value");
						name = rs.getString("name");
						if (value.equals(build_server_environment_id)) {
							found = true;
						}
					}
				}
				if (!found) {
					Database.deleteBuildServer(build_server_environment_id);
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage(
							"This Build Server cannot be deleted because it is being used by pipeline '" + name + "'");
					return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(401);
			configAlreadyExistsException
					.setMessage("This Build Server cannot be deleted because it is being used by the pipeline flow '"
							+ flowName + "', configuration '" + flowConfigName + "'");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();
	}

	@ApiOperation(value = "Add Source Code Connection", notes = "Create new Source Code Connection for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Source Code name already exists."),
			@ApiResponse(code = 405, message = "There was an error processing the request.") })
	@POST
	@Path("/saveSystemConnection/{name}/{gotsid}/{url}/{username}/{password}/{systemType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveSystemConnection(@PathParam("name") String name, @PathParam("gotsid") String gotsid,
			@PathParam("url") String url, @PathParam("username") String username,
			@PathParam("password") String password, @PathParam("systemType") String systemType) throws Exception {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			Cookie[] cookies = request.getCookies();
			String authCookie = "";
			for (Cookie cookie : cookies) {
				authCookie += cookie.getName() + "=" + cookie.getValue() + ";";
			}
			String kurl = System.getProperty("seedurl");
			kurl = kurl + "/store/service/validateReposerver";
			String postBody = "{\"repopath\":\"" + url + "\",\"username\":\"" + username + "\",\"password\":\""
					+ password + "\"}";
			HttpPost request = new HttpPost(kurl);
			StringEntity params = new StringEntity(postBody);
			request.addHeader("content-type", "application/json");
			request.addHeader("Cookie", authCookie);
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			CamundaConnector c = new CamundaConnector();
			Map responseBody = c.jsonToMap(responseString);
			String status = (String) responseBody.get("status");
			String error = (String) responseBody.get("description");
			if (status.toUpperCase().equals("SUCCESS")) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "select connection_name from camundabpmajsc6.system_connections where gots_id=? and connection_name=?;";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					ps.setString(2, name);
					rs = ps.executeQuery();
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

				if (!rs.next()) {
					Database.insertSystemConnection(name, gotsid, url, username, password, systemType);
					return Response.status(200).build();
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage("The Source Code name '" + name + "' already exists.");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				PiplelineException invalidCredentialsException = new PiplelineException();
				invalidCredentialsException.setCode(405);
				invalidCredentialsException.setMessage(error);
				return Response.status(400).entity(invalidCredentialsException).build();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			PiplelineException invalidCredentialsException = new PiplelineException();
			invalidCredentialsException.setCode(405);
			invalidCredentialsException.setMessage("There was an error processing the request.");
			return Response.status(400).entity(invalidCredentialsException).build();
		}
	}

	@ApiOperation(value = "List Source Code Connections by System Type", notes = "Display Source Code Connections for a GOTS ID by System Type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getSystemConnection/{gotsid}/{systemType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSystemConnection(@PathParam("gotsid") String gotsid,
			@PathParam("systemType") String systemType) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		JSONObject response = new JSONObject();
		try {
			response = Database.getSystemConnection(gotsid, systemType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiOperation(value = "List Source Code Connections by System Connection ID", notes = "Display Source Code Connections for a GOTS ID by System Connection ID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getSystemConnectionById/{systemConnectionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSystemConnectionById(@PathParam("systemConnectionId") String id) throws JSONException {
		JSONObject response = new JSONObject();
		try {
			response = Database.getSystemConnectionById(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String gotsid = "";
		JSONArray system_properties = response.getJSONArray("system_properties");
		for (int i = 0; i < system_properties.length(); i++) {
			JSONObject item = system_properties.getJSONObject(i);
			gotsid = item.getString("gots_id");
		}
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiOperation(value = "Update Source Code Connections by System Connection ID", notes = "Update Source Code Connection parameters.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Source Code name already exists."),
			@ApiResponse(code = 405, message = "There was an error processing the request.") })
	@POST
	@Path("/updateSystemConnection/{gotsid}/{name}/{systemConnectionId}/{url}/{username}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateSystemConnection(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("systemConnectionId") String systemConnectionId, @PathParam("url") String url,
			@PathParam("username") String username, @PathParam("password") String password) throws SQLException {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			Cookie[] cookies = request.getCookies();
			String authCookie = "";
			for (Cookie cookie : cookies) {
				authCookie += cookie.getName() + "=" + cookie.getValue() + ";";
			}
			String kurl = System.getProperty("seedurl");
			kurl = kurl + "/store/service/validateReposerver";
			String postBody = "{\"repopath\":\"" + url + "\",\"username\":\"" + username + "\",\"password\":\""
					+ password + "\"}";
			HttpPost request = new HttpPost(kurl);
			StringEntity params = new StringEntity(postBody);
			request.addHeader("content-type", "application/json");
			request.addHeader("Cookie", authCookie);
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			CamundaConnector c = new CamundaConnector();
			Map responseBody = c.jsonToMap(responseString);
			String status = (String) responseBody.get("status");
			String error = (String) responseBody.get("description");
			if (status.toUpperCase().equals("SUCCESS")) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "select connection_name from camundabpmajsc6.system_connections where gots_id=? and connection_name=? and system_connections_id <> ?";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					ps.setString(2, name);
					ps.setString(3, systemConnectionId);
					rs = ps.executeQuery();
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

				if (!rs.next()) {
					try {
						Database.updateSystemConnection(name, systemConnectionId, url, username, password);
					} catch (Exception e) {
						e.printStackTrace();
						PiplelineException invalidCredentialsException = new PiplelineException();
						invalidCredentialsException.setCode(405);
						invalidCredentialsException.setMessage("There was an error processing the request.");
						return Response.status(400).entity(invalidCredentialsException).build();
					}
					return Response.status(200).build();
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage("The Source Code name '" + name + "' already exists.");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				PiplelineException invalidCredentialsException = new PiplelineException();
				invalidCredentialsException.setCode(405);
				invalidCredentialsException.setMessage(error);
				return Response.status(400).entity(invalidCredentialsException).build();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			PiplelineException invalidCredentialsException = new PiplelineException();
			invalidCredentialsException.setCode(405);
			invalidCredentialsException.setMessage("There was an error processing the request.");
			return Response.status(400).entity(invalidCredentialsException).build();
		}

	}

	@ApiOperation(value = "Delete Source Code Connection by System Connection ID", notes = "Delete Source Code Connection by System Connection ID.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@DELETE
	@Path("/deleteSystemConnection/{systemConnectionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteSystemConnection(@PathParam("systemConnectionId") String systemConnectionId)
			throws JSONException {
		JSONObject response = new JSONObject();
		try {
			response = Database.getSystemConnectionById(systemConnectionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String gotsid = "";
		JSONArray system_properties = response.getJSONArray("system_properties");
		for (int i = 0; i < system_properties.length(); i++) {
			JSONObject item = system_properties.getJSONObject(i);
			gotsid = item.getString("gots_id");
		}
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots." + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}

		try {
			Database.deleteSystemConnection(systemConnectionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).build();
	}

	@ApiOperation(value = "Add new Environment for a GOTS ID", notes = "Add new Environment for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Environment name already exists") })
	@POST
	@Path("/saveEnvironment/{gotsid}/{name}/{cluster_name}/{cluster_url}/{username}/{password}/{environment_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEnvironment(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("cluster_name") String cluster_name, @PathParam("cluster_url") String cluster_url,
			@PathParam("username") String username, @PathParam("password") String password,
			@PathParam("environment_type") String environment_type) throws Exception {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots." + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select environment_name from camundabpmajsc6.environments where gots_id=? and environment_name=?;";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			rs = ps.executeQuery();
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

		if (!rs.next()) {
			Database.saveEnvironment(gotsid, name, cluster_name, cluster_url, username, password, environment_type);
			return Response.status(200).build();
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(400);
			configAlreadyExistsException.setMessage("The Environment name '" + name + "' already exists.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
	}

	@ApiOperation(value = "List Environment for a GOTS ID", notes = "Display all Environments for a particular GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getEnvironments/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironments(@PathParam("gotsid") String gotsid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots." + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select gots_id, environment_id, environment_name, cluster_name, cluster_url, username, password, environment_type from camundabpmajsc6.environments where gots_id = ?;";
		JSONObject environments = new JSONObject();
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
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				environments.append("environments", obj);
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
		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "List Environment by Environment Type", notes = "Display Environments by Environment Type for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getEnvironmentByType/{gotsid}/{environment_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironmentByType(@PathParam("gotsid") String gotsid,
			@PathParam("environment_type") String environment_type) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select environment_id, environment_name, cluster_name, cluster_url, username, password, environment_type from camundabpmajsc6.environments where gots_id = ? and environment_type = ?;";
		JSONObject environments = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, environment_type);
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
				environments.append("environments", obj);
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
		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "List Environment by Environment ID", notes = "Display Environment by Environment ID for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID") })
	@GET
	@Path("/getEnvironmentById/{environment_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEnvironmentById(@PathParam("environment_id") String environment_id) throws JSONException {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select gots_id, environment_id, environment_name, cluster_name, cluster_url, username, password, environment_type from camundabpmajsc6.environments where environment_id = ?;";
		JSONObject environments = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, environment_id);
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
				environments.append("environments", obj);
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
		String gotsid = "";
		JSONArray system_properties = environments.getJSONArray("environments");
		for (int i = 0; i < system_properties.length(); i++) {
			JSONObject item = system_properties.getJSONObject(i);
			gotsid = item.getString("gots_id");
		}
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}

		return Response.status(200).entity(environments.toString()).build();
	}

	@ApiOperation(value = "Update Environment Parameters", notes = "Update Environment Parameters Environment ID for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "The Environment name already exists.") })
	@POST
	@Path("/updateEnvironment/{gotsid}/{environment_id}/{name}/{cluster_name}/{cluster_url}/{username}/{password}/{environment_type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateEnvironment(@PathParam("gotsid") String gotsid,
			@PathParam("environment_id") String environment_id, @PathParam("name") String name,
			@PathParam("cluster_name") String cluster_name, @PathParam("cluster_url") String cluster_url,
			@PathParam("username") String username, @PathParam("password") String password,
			@PathParam("environment_type") String environment_type) throws Exception {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select environment_name from camundabpmajsc6.environments where gots_id=? and environment_name=? and environment_id <> ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, environment_id);
			rs = ps.executeQuery();
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

		if (!rs.next()) {
			String encryptedPassword = EncryptDecryptService.encrypt(password);
			con = Database.getConnection();
			ps = null;
			sql = "update camundabpmajsc6.environments set environment_name = ?, cluster_name = ?, cluster_url = ?, username = ?, password = ?, environment_type = ? where environment_id = ?";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, name);
				ps.setString(2, cluster_name);
				ps.setString(3, cluster_url);
				ps.setString(4, username);
				ps.setString(5, encryptedPassword);
				ps.setString(6, environment_type);
				ps.setString(7, environment_id);
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
			return Response.status(200).build();
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(400);
			configAlreadyExistsException.setMessage("The Environment name '" + name + "' already exists.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
	}

	@ApiOperation(value = "Delete Environment for a GOTS ID", notes = "Delete Environment for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "This environment cannot be deleted because it is being used by pipeline."),
			@ApiResponse(code = 400, message = "This environment cannot be deleted because it is being used by a pipeline flow.") })
	@DELETE
	@Path("/deleteEnvironment/{gotsid}/{environment_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEnvironment(@PathParam("environment_id") String environment_id,
			@PathParam("gotsid") String gotsid) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "select pf.name as flowName, dc.name as configName from deployment_config dc "
				+ "join pipeline_flow pf on pf.pipeline_flow_id=dc.pipeline_flow_id"
				+ " where dc.gots_id=? and dc.environment_id = ? limit 1;";
		String flowName = "", flowConfigName = "";
		boolean flowFound = false;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, environment_id);
			rs = ps.executeQuery();
			if (rs.next()) {
				flowName = rs.getString("flowName");
				flowConfigName = rs.getString("configName");
				flowFound = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!flowFound) {
			sql = "select pmc.name, pc.attribute_name, pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
					+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and attribute_name='environmentDropDown' and attribute_value=? order by pc.config_order;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, environment_id);
				rs = ps.executeQuery();
				boolean found = false;
				String value = "", name = "";
				while (rs.next() && found == false) {
					if (!(rs.getString("attribute_value") == null)) {
						value = rs.getString("attribute_value");
						name = rs.getString("name");
						if (value.equals(environment_id)) {
							found = true;
						}
					}
				}
				if (!found) {
					Database.deleteEnvironment(environment_id);
					return Response.status(200).build();
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage(
							"This environment cannot be deleted because it is being used by pipeline '" + name + "'");
					return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(401);
			configAlreadyExistsException
					.setMessage("This environment cannot be deleted because it is being used by the pipeline flow '"
							+ flowName + "', configuration '" + flowConfigName + "'");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}

	private void generateConfig(String gotsid, String name, String pipeline_name) {
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		String sql = "INSERT INTO `camundabpmajsc6`.`pipeline_gots_config`(`pipeline_config_id`, `gots_id`, `name`) "
				+ "select pipeline_config_id , ?, ? from camundabpmajsc6.pipeline_config where pipeline_name = ? "
				+ "and (config_type<>'array' or config_type is null) and pipeline_config_id not in (select pipeline_config_id "
				+ "from camundabpmajsc6.pipeline_gots_config where gots_id=? and name=?)";
		try {

			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, name);
			ps.setString(3, pipeline_name);
			ps.setString(4, gotsid);
			ps.setString(5, name);
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

	@ApiOperation(value = "Add new Pipeline Flow for a GOTS ID", notes = "Add new Pipeline Flow for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The Pipeline Flow name already exists"),
			@ApiResponse(code = 400, message = "The Pipeline Flow name already exists") })
	@POST
	@Path("/savePipelineFlow")
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePipelineFlow(@ApiParam PipelineFlowDetail payload) {
		/**************************************
		 * Parse payload and retrieve gots_id, name, and description
		 **************************************/
		String gotsid = payload.getGotsid();
		String name = payload.getName();
		String description = payload.getDescription();
		String id = "";

		/**************************************
		 * Retrieve current user and ensure they are an admin
		 **************************************/
		String user = request.getUserPrincipal().getName();
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			/**************************************
			 * Insert new record into pipeline_flow table if it does not already
			 * exist.
			 **************************************/
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String sql = "Insert into pipeline_flow (gots_id, name, created_date, creator, description) values (?, ?, NOW(), ?, ?);";

			try {
				ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, gotsid);
				ps.setString(2, name);
				ps.setString(3, user);
				ps.setString(4, description);
				ps.executeQuery();
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					id = rs.getString("pipeline_flow_id");
				}
			} catch (Exception e) {
				/**************************************
				 * Throw an error if the gots_id and name combination already
				 * exists.
				 **************************************/
				e.printStackTrace();
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException.setMessage("The Pipeline Flow (" + name + ") already exists.");
				return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			/**************************************
			 * Throw an error if the user is not an administrator for the gots
			 * id.
			 **************************************/
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException
					.setMessage("You are not an admin for GOTS ID (" + gotsid + ") and cannot create a Pipeline Flow.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(201).entity(id).build();
	}

	@ApiOperation(value = "Get all Pipeline Flows for a GOTS ID", notes = "Get all Pipeline Flows for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getPipelineFlows/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineFlows(@ApiParam(required = true) @PathParam("gotsid") String gotsid) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select * from pipeline_flow where gots_id=?;";
		JSONObject pipelineFlows = new JSONObject();
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
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				pipelineFlows.append("pipelineFlows", obj);
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
		return Response.status(200).entity(pipelineFlows.toString()).build();
	}

	@ApiOperation(value = "Get a Pipeline Flow by ID", notes = "Get a Pipeline Flow by ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getPipelineFlowByID/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineFlowByID(
			@ApiParam(value = "The pipeline_flow_id", required = true) @PathParam("id") String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select pipeline_flow_id, gots_id, name, description from pipeline_flow where pipeline_flow_id=?;";
		JSONObject obj = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
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
		return Response.status(200).entity(obj.toString()).build();
	}

	@ApiOperation(value = "Add new Pipeline Flow for a GOTS ID", notes = "Add new Pipeline Flow for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The Pipeline Flow name already exists"),
			@ApiResponse(code = 400, message = "The Pipeline Flow name already exists") })
	@PUT
	@Path("/updatePipelineFlow/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePipelineFlow(
			@ApiParam(value = "pipeline_flow_id to be updated", required = true) @PathParam("id") String id,
			@ApiParam PipelineFlowDetail pdc) {
		/**************************************
		 * Parse payload and retrieve pipeline_flow_id, gots_id, name, and
		 * description
		 **************************************/
		String gotsid = pdc.getGotsid();
		String name = pdc.getName();
		String description = pdc.getDescription();

		/**************************************
		 * Retrieve current user and ensure they are an admin
		 **************************************/
		String user = request.getUserPrincipal().getName();
		int endOfUID = user.indexOf('@');
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			/**************************************
			 * Update record int pipeline_flow table if new name does not
			 * already exist.
			 **************************************/
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "Update pipeline_flow set name=?, updated_date=NOW(), modifier=?, description=? where pipeline_flow_id=?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, name);
				ps.setString(2, user);
				ps.setString(3, description);
				ps.setString(4, id);
				ps.executeQuery();
			} catch (Exception e) {
				/**************************************
				 * Throw an error if the gots_id and name combination already
				 * exists.
				 **************************************/
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException.setMessage("The Pipeline Flow (" + name + ") already exists.");
				return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			/**************************************
			 * Throw an error if the user is not an administrator for the gots
			 * id.
			 **************************************/
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException
					.setMessage("You are not an admin for GOTS ID (" + gotsid + ") and cannot create a Pipeline Flow.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();
	}

	@ApiOperation(value = "Delete PipelineFlow for a GOTS ID", notes = "Delete Pipeline Flow for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "This Pipeline Flow cannot be deleted because it is being used by pipeline.") })
	@DELETE
	@Path("/deletePipelineFlow/{gotsid}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePipelineFlow(
			@ApiParam(value = "pipeline_flow_id to be deleted", required = true) @PathParam("id") String id,
			@ApiParam(required = true) @PathParam("gotsid") String gotsid) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			Connection con = Database.getConnection();
			ResultSet rs = null;
			PreparedStatement ps = null;
			String sql = "select pmc.name, pc.attribute_name, pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
					+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and attribute_name='pipelineFlowDropDown' and attribute_value=? order by pc.config_order;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, id);
				rs = ps.executeQuery();
				boolean found = false;
				String value = "", name = "";
				while (rs.next() && found == false) {
					if (!(rs.getString("attribute_value") == null)) {
						value = rs.getString("attribute_value");
						name = rs.getString("name");
						if (value.equals(id)) {
							found = true;
						}
					}
				}
				if (!found) {
					Database.deletePipelineFlow(id);
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage(
							"This pipeline flow cannot be deleted because it is being used by pipeline '" + name + "'");
					return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}

	private boolean checkGotsIntegrity(String gotsid, String build_server_id, String configuration_repository_id,
			String environment_id) {
		boolean buildServerMatches = true, environmentMatches = true, configRepoMatches = true;
		if (build_server_id != null) {
			try {
				Map<String, Object> buildServer = jdbcTemplate.queryForMap(
						"select gots_id from build_server_environments where build_server_environment_id=?;",
						build_server_id);
				if (!buildServer.get("gots_id").toString().equals(gotsid)) {
					buildServerMatches = false;
				}
			} catch (Exception e) {
				buildServerMatches = false;
			}
		}
		if (environment_id != null) {
			try {
				Map<String, Object> environment = jdbcTemplate
						.queryForMap("select gots_id from environments where environment_id=?;", environment_id);
				if (!environment.get("gots_id").toString().equals(gotsid)) {
					environmentMatches = false;
				}
			} catch (Exception e) {
				environmentMatches = false;
			}
		}
		if (configuration_repository_id != null) {
			try {
				Map<String, Object> configRepo = jdbcTemplate.queryForMap(
						"select gots_id from configuration_repositories where configuration_repository_id=?;",
						configuration_repository_id);
				if (!configRepo.get("gots_id").toString().equals(gotsid)) {
					configRepoMatches = false;
				}
			} catch (Exception e) {
				configRepoMatches = false;
			}
		}

		return buildServerMatches && environmentMatches && configRepoMatches;
	}

	@ApiOperation(value = "Add new Deployment Configuration for a Pipeline Flow", notes = "Add new Deployment Configuration for a Pipeline Flow")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "The deployment name already exists"),
			@ApiResponse(code = 102, message = "THe user is not an admin for that gots"),
			@ApiResponse(code = 103, message = "A production environment cannot be configured with a temporary application."),
			@ApiResponse(code = 104, message = "Please select a Build Server Configuration from the drop down below."),
			@ApiResponse(code = 105, message = "The build server and/or environment selected is not affiliated with the GOTS ID being used."),
			@ApiResponse(code = 400, message = "The Pipeline Flow name already exists") })
	@POST
	@Path("/saveDeploymentConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveDeploymentConfiguration(@ApiParam DeploymentConfigurationDetail dcd) {
		/**************************************
		 * Parse payload and retrieve relevant information
		 **************************************/
		String pipeline_flow_id = dcd.getPipeline_flow_id();
		String build_server_id = dcd.getBuild_server_id();
		String environment_id = dcd.getEnvironment_id();
		String configuration_repository_id = dcd.getConfiguration_repository_id();
		String name = dcd.getName();
		String gotsid = dcd.getGotsid();
		String description = dcd.getDescription();
		boolean enable_notification = dcd.getEnable_notification();
		String notification_list = dcd.getNotification_list();
		boolean enable_approval = dcd.getEnable_approval();
		String approval_list = dcd.getApproval_list();
		String pipeline_name = dcd.getPipeline_name();
		String id = "", environmentType = "", environmentGots = "";
		boolean matches = checkGotsIntegrity(gotsid, build_server_id, configuration_repository_id, environment_id);
		if (matches) {
			if (build_server_id != null) {
				Map<String, Object> buildServerGots = null;
				try {
					buildServerGots = jdbcTemplate.queryForMap(
							// join asf_gots and get associated gots.
							"select gots_id from build_server_environments where build_server_environment_id = ?;",
							build_server_id);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (environment_id != null) {
					Map environmentInfo = Database.getEnvironmentByID(environment_id);
					if (!environmentInfo.isEmpty()) {
						environmentType = environmentInfo.get("environment_type").toString().toUpperCase();
						environmentGots = environmentInfo.get("gots_id").toString();
					}
				}
				// if associated gots, pass
				boolean associated = false;
				try {
					Map l = jdbcTemplate.queryForMap("select associated_gots_id from asf_gots where gots_id=?;",
							gotsid);
					if (l.get("associated_gots_id") != null) {
						associated = true;
					}					
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!(associated == false && environmentType.equals("PROD"))) {
					/**************************************
					 * Retrieve current user and ensure they are an admin
					 **************************************/
					String user = request.getUserPrincipal().getName();
					if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
						/**************************************
						 * Retrieve the highest deployment order number
						 **************************************/
						int max = getMaxDeploymentNum(pipeline_flow_id);
						max++;
						/**************************************
						 * Insert new record into deployment_config table if it
						 * does not already exist.
						 **************************************/
						Connection con = Database.getConnection();
						PreparedStatement ps = null;
						ResultSet rs = null;
						String sql = "Insert into deployment_config (build_server_id, environment_id, configuration_repository_id, name, gots_id, pipeline_name, created_date, creator,"
								+ " description, enable_notification, notification_list, enable_approval, approval_list, pipeline_flow_id, deployment_order) "
								+ "values (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?);";
						try {
							ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
							ps.setString(1, build_server_id);
							ps.setString(2, environment_id);
							ps.setString(3, configuration_repository_id);
							ps.setString(4, name);
							ps.setString(5, gotsid);
							ps.setString(6, pipeline_name);
							ps.setString(7, user);
							ps.setString(8, description);
							ps.setBoolean(9, enable_notification);
							ps.setString(10, notification_list);
							ps.setBoolean(11, enable_approval);
							ps.setString(12, approval_list);
							ps.setString(13, pipeline_flow_id);
							ps.setInt(14, max);
							ps.executeUpdate();
							rs = ps.getGeneratedKeys();
							if (rs.next()) {
								id = rs.getString("deployment_config_id");
							}
						} catch (Exception e) {
							/**************************************
							 * Throw an error if the gots_id, name, and
							 * pipeline_flow_id combination already exists.
							 **************************************/
							e.printStackTrace();
							PiplelineException configAlreadyExistsException = new PiplelineException();
							configAlreadyExistsException.setCode(101);
							configAlreadyExistsException
									.setMessage("The Deployment Name (" + name + ") already exists.");
							return Response.status(400).entity(configAlreadyExistsException).build();
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
					} else {
						/**************************************
						 * Throw an error if the user is not an administrator
						 * for the gots id.
						 **************************************/
						PiplelineException configAlreadyExistsException = new PiplelineException();
						configAlreadyExistsException.setCode(102);
						configAlreadyExistsException.setMessage(
								"You are not an admin for GOTS ID (" + gotsid + ") and cannot create a Pipeline Flow.");
						return Response.status(400).entity(configAlreadyExistsException).build();
					}
				} else {
					/**
					 * Throw an error if the user is trying to save a prod
					 * configuration with a temporary gots
					 * 
					 * (unreachable while Database.isAssociated(String gotsid) always returns true. 
					 *  See the method comment for details)
					 */
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(103);
					configAlreadyExistsException.setMessage(
							"A production environment cannot be configured with a temporary application. Please associate a GOTS ID to configure a production environment");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				/**
				 * Throw an error if the user does not select a build server
				 */
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(104);
				configAlreadyExistsException
						.setMessage("Please select a Build Server Configuration from the drop down below.");
				return Response.status(400).entity(configAlreadyExistsException).build();
			}
		} else {
			/**
			 * Throw an error if the gots id doesn't match
			 */
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(104);
			configAlreadyExistsException.setMessage("Permission Denied");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(201).build();
	}

	@ApiOperation(value = "Get all Deployment Configurations for a Pipeline Flow and GOTS", notes = "Get all Deployment Configurations for a Pipeline Flow and GOTS")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getDeploymentConfigurations/{gotsid}/{pipelineFlowID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeploymentConfigurations(@ApiParam(required = true) @PathParam("gotsid") String gotsid,
			@ApiParam(value = "The ID for the pipeline flow", required = true) @PathParam("pipelineFlowID") String pipelienFlowID) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select * from deployment_config where"
				+ " gots_id=? and pipeline_flow_id=? order by deployment_order;";
		JSONObject deploymentConfigurations = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, gotsid);
			ps.setString(2, pipelienFlowID);
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
				deploymentConfigurations.append("deploymentConfigurations", obj);
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
		return Response.status(200).entity(deploymentConfigurations.toString()).build();
	}

	@ApiOperation(value = "Get a Deployment Configuration by ID", notes = "Get a Deployment Configuration by ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getDeploymentConfigurationByID/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeploymentConfigurationByID(
			@ApiParam(value = "The deployment configuration id", required = true) @PathParam("id") String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select deployment_config_id, pipeline_flow_id, build_server_id, environment_id, configuration_repository_id, "
				+ "name, gots_id, "
				+ "pipeline_name, description, enable_notification, notification_list, enable_approval, approval_list "
				+ "from deployment_config where deployment_config_id=?;";
		JSONObject obj = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			rsmd = rs.getMetaData();
			while (rs.next()) {
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
		return Response.status(200).entity(obj.toString()).build();
	}

	@ApiOperation(value = "Update a Deployment Configuration for a Pipeline Flow", notes = "Update a Deployment Configuration for a Pipeline Flow")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The Pipeline Flow name already exists"),
			@ApiResponse(code = 103, message = "Please select a Build Server Configuration from the drop down below."),
			@ApiResponse(code = 104, message = "A build server must be selected."),
			@ApiResponse(code = 105, message = "The build server and/or environment selected is not affiliated with the GOTS ID being used."),
			@ApiResponse(code = 400, message = "The Pipeline Flow name already exists") })
	@PUT
	@Path("/updateDeploymentConfig/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDeploymentConfig(
			@ApiParam(value = "The deployment configuration id", required = true) @PathParam("id") String id,
			@ApiParam DeploymentConfigurationDetail dcd) {
		/**************************************
		 * Parse payload and retrieve relevant information
		 **************************************/
		String build_server_id = dcd.getBuild_server_id();
		String environment_id = dcd.getEnvironment_id();
		String configuration_repository_id = dcd.getConfiguration_repository_id();
		String name = dcd.getName();
		String gotsid = dcd.getGotsid();
		String description = dcd.getDescription();
		boolean enable_notification = dcd.getEnable_notification();
		String notification_list = dcd.getNotification_list();
		boolean enable_approval = dcd.getEnable_approval();
		String approval_list = dcd.getApproval_list();
		String environmentType = "";
		boolean matches = checkGotsIntegrity(gotsid, build_server_id, configuration_repository_id, environment_id);
		if (matches) {
			if (build_server_id != null) {
				if (environment_id != null) {
					Map environmentInfo = Database.getEnvironmentByID(environment_id);
					if (!environmentInfo.isEmpty()) {
						environmentType = environmentInfo.get("environment_type").toString().toUpperCase();
					}
				}
				// query for associated gots, if exists let through
				boolean associated = false;
				try {
					Map l = jdbcTemplate.queryForMap("select associated_gots_id from asf_gots where gots_id=?;",
							gotsid);
					if (l.get("associated_gots_id") != null) {
						associated = true;
					}					
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!(associated == false && environmentType.equals("PROD"))) {
					/**************************************
					 * Retrieve current user and ensure they are an admin
					 **************************************/
					String user = request.getUserPrincipal().getName();
					if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
						/**************************************
						 * Update record in deployment_config table if new name
						 * does not already exist.
						 **************************************/
						Connection con = Database.getConnection();
						PreparedStatement ps = null;
						String sql = "Update deployment_config set build_server_id=?, environment_id=?, name=?, updated_date=NOW(), "
								+ "modifier=?, description=?, enable_notification=?, notification_list=?, enable_approval=?, "
								+ "approval_list=?, configuration_repository_id=? where deployment_config_id=?;";
						try {
							ps = con.prepareStatement(sql);
							ps.setString(1, build_server_id);
							ps.setString(2, environment_id);
							ps.setString(3, name);
							ps.setString(4, user);
							ps.setString(5, description);
							ps.setBoolean(6, enable_notification);
							ps.setString(7, notification_list);
							ps.setBoolean(8, enable_approval);
							ps.setString(9, approval_list);
							ps.setString(10, configuration_repository_id);
							ps.setString(11, id);
							ps.executeQuery();
						} catch (Exception e) {
							/**************************************
							 * Throw an error if the gots_id, name, and
							 * pipeline_flow_id combination already exists.
							 **************************************/
							PiplelineException configAlreadyExistsException = new PiplelineException();
							configAlreadyExistsException.setCode(102);
							configAlreadyExistsException
									.setMessage("The Deployment Configuration (" + name + ") already exists.");
							return Response.status(400).entity(configAlreadyExistsException).build();
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
					} else {
						/**************************************
						 * Throw an error if the user is not an administrator
						 * for the gots id.
						 **************************************/
						PiplelineException configAlreadyExistsException = new PiplelineException();
						configAlreadyExistsException.setCode(101);
						configAlreadyExistsException.setMessage(
								"You are not an admin for GOTS ID (" + gotsid + ") and cannot create a Pipeline Flow.");
						return Response.status(400).entity(configAlreadyExistsException).build();
					}
				} else {
					/**
					 * Throw an error if the user is trying to save a prod
					 * configuration with a temporary gots
					 * 
					 * (unreachable while Database.isAssociated(String gotsid) always returns true. 
					 *  See the method comment for details)
					 */
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(102);
					configAlreadyExistsException.setMessage(
							"A production environment cannot be configured with a temporary application. Please associate a GOTS ID to configure a production environment");
					return Response.status(400).entity(configAlreadyExistsException).build();
				}
			} else {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(103);
				configAlreadyExistsException
						.setMessage("Please select a Build Server Configuration from the drop down below.");
				return Response.status(400).entity(configAlreadyExistsException).build();
			}
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(103);
			configAlreadyExistsException.setMessage("Permission Denied");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();
	}

	private int getMaxDeploymentNum(String pipeline_flow_id) {
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

	@ApiOperation(value = "Delete Deployment Configuration for a Pipeline Flow and GOTS ID", notes = "Delete Deployment Configuration for a Pipeline Flow and GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "This Deployment Configuration cannot be deleted because it is being used by pipeline.") })
	@DELETE
	@Path("/deleteDeploymentConfiguration/{gotsid}/{pipelineFlowID}/{id}/{deployment_order}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteDeploymentConfiguration(
			@ApiParam(value = "The deployment configuration id to be deleted", required = true) @PathParam("id") String id,
			@ApiParam(required = true) @PathParam("gotsid") String gotsid,
			@ApiParam(value = "The id for the pipeline flow", required = true) @PathParam("pipelineFlowID") String pipelineFlowID,
			@ApiParam(value = "The number position of the deployment configuration", required = true) @PathParam("deployment_order") int deployment_order) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			Connection con = Database.getConnection();
			ResultSet rs = null;
			PreparedStatement ps = null;
			String sql = "select pmc.name, pc.attribute_name, pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
					+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
					+ "where gots_id=? and attribute_name='pipelineFlowDropDown' and attribute_value=? order by pc.config_order;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, pipelineFlowID);
				rs = ps.executeQuery();
				boolean found = false;
				String value = "", name = "";
				while (rs.next() && found == false) {
					if (!(rs.getString("attribute_value") == null)) {
						value = rs.getString("attribute_value");
						name = rs.getString("name");
						if (value.equals(id)) {
							found = true;
						}
					}
				}
				if (!found) {
					Database.deleteDeploymentConfiguration(id, deployment_order, pipelineFlowID);
				} else {
					PiplelineException configAlreadyExistsException = new PiplelineException();
					configAlreadyExistsException.setCode(400);
					configAlreadyExistsException.setMessage(
							"This deployment configuration cannot be deleted because it is being used by pipeline '"
									+ name + "'");
					return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}

	@ApiOperation(value = "Move deployment configuration up in the list for a Pipeline Flow", notes = "Move deployment configuration up in the list for a Pipeline Flow")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The pipeline configuration cannot be placed higher in the queue") })
	@GET
	@Path("/moveUpDeploymentConfiguration/{gotsid}/{pipelineFlowID}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveUpDeploymentConfiguration(@ApiParam(required = true) @PathParam("gotsid") String gotsid,
			@ApiParam(value = "The id for the pipeline flow", required = true) @PathParam("pipelineFlowID") String pipelineFlowID,
			@ApiParam(value = "The name for the deployment configuration") @PathParam("id") String id) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			int deployment_order = Integer.MAX_VALUE;
			try {
				Map l = jdbcTemplate.queryForMap(
						"select * from deployment_config where gots_id=? and pipeline_flow_id=? and deployment_config_id=?;",
						gotsid, pipelineFlowID, id);
				deployment_order = Integer.valueOf(l.get("deployment_order").toString());
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(400).build();
			}
			if (deployment_order != 1) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "CALL `camundabpmajsc6`.`moveUpDeploymentConfig`(?, ?, ?);";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, pipelineFlowID);
					ps.setString(2, id);
					ps.setString(3, gotsid);
					ps.executeUpdate();
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
			} else {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException
						.setMessage("This deployment configuration cannot be placed any higher in the queue.");
				return Response.status(400).entity(configAlreadyExistsException).build();
			}
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}

	@ApiOperation(value = "Move deployment configuration down in the list for a Pipeline Flow", notes = "Move deployment configuration down in the list for a Pipeline Flow")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The pipeline configuration cannot be placed any lower in the queue") })
	@GET
	@Path("/moveDownDeploymentConfiguration/{gotsid}/{pipelineFlowID}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveDownDeploymentConfiguration(@ApiParam(required = true) @PathParam("gotsid") String gotsid,
			@ApiParam(value = "The id for the pipeline flow", required = true) @PathParam("pipelineFlowID") String pipelineFlowID,
			@ApiParam(value = "The name of the deployment configuration", required = true) @PathParam("id") String id) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			int max = getMaxDeploymentNum(pipelineFlowID);
			int deployment_order = Integer.MAX_VALUE;
			try {
				Map l = jdbcTemplate.queryForMap(
						"select * from deployment_config where gots_id=? and pipeline_flow_id=? and deployment_config_id=?;",
						gotsid, pipelineFlowID, id);
				deployment_order = Integer.valueOf(l.get("deployment_order").toString());
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(400).build();
			}
			if (deployment_order != max) {
				Connection con = Database.getConnection();
				ResultSet rs = null;
				PreparedStatement ps = null;
				String sql = "CALL `camundabpmajsc6`.`moveDownDeploymentConfig`(?, ?, ?);";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, pipelineFlowID);
					ps.setString(2, id);
					ps.setString(3, gotsid);
					ps.executeUpdate();
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
			} else {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException
						.setMessage("The pipeline configuration cannot be placed any lower in the queue");
				return Response.status(400).entity(configAlreadyExistsException).build();
			}
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}

	private String generatePipelineDeploymentConfig(int deployment_config_id, String gotsid, String name)
			throws SQLException {
		PreparedStatement ps = null;
		Connection con = Database.getConnection();
		ResultSet rs = null;
		String id = "";
		String sql = "Insert into pipeline_deployment_config (deployment_config_id, gots_id, name, jenkins_job, jenkins_params) "
				+ "values (?, ?, ?, '', '');";
		try {
			ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, deployment_config_id);
			ps.setString(2, gotsid);
			ps.setString(3, name);
			ps.executeQuery();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getString("pipeline_deployment_config_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
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
		return id;
	}

	@ApiOperation(value = "Get a Deployment Pipeline configuration by Pipeline Flow ID", notes = "Get a Deployment Pipeline configuration by Pipeline Flow ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "The pipeline flow selected is not associated with GOTS ID"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getPipelineFlowConfig/{pipeline_flow_id}/{name}/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineFlowConfig(
			@ApiParam(value = "The pipeline flow id", required = true) @PathParam("pipeline_flow_id") String pipeline_flow_id,
			@ApiParam(value = "The name of the pipeline", required = true) @PathParam("name") String name,
			@ApiParam(required = true) @PathParam("gotsid") String gotsid) throws JSONException {
		/****************
		 * Look for pipeline flow config by pipeline flow id and name of the
		 * pipeline
		 */
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		JSONObject pipelineFlowConfig = new JSONObject();
		try {
			l = jdbcTemplate.queryForList(
					"select dc.deployment_config_id, pdc.pipeline_deployment_config_id, dc.gots_id, pipeline_flow_id, dc.name, pdc.jenkins_job, pdc.jenkins_params from deployment_config dc "
							+ "left join pipeline_deployment_config pdc on pdc.deployment_config_id = dc.deployment_config_id  and pdc.name=? and pdc.gots_id=? "
							+ "where dc.pipeline_flow_id=? and pipeline_deployment_config_id is null;",
					name, gotsid, pipeline_flow_id);
			for (int i = 0; i < l.size(); i++) {
				Map<String, Object> config = l.get(i);
				generatePipelineDeploymentConfig(Integer.valueOf(config.get("deployment_config_id").toString()), gotsid,
						name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			l = jdbcTemplate.queryForList(
					"select dc.deployment_config_id, pdc.pipeline_deployment_config_id, pipeline_flow_id, dc.name, pdc.jenkins_job, pdc.jenkins_params, "
							+ "dc.deployment_order from deployment_config dc "
							+ "join pipeline_deployment_config pdc on pdc.deployment_config_id = dc.deployment_config_id "
							+ "where dc.pipeline_flow_id=? and pdc.name=? and pdc.gots_id=? order by dc.deployment_order;",
					pipeline_flow_id, name, gotsid);
			for (int i = 0; i < l.size(); i++) {
				Map<String, Object> config = l.get(i);
				JSONObject obj = new JSONObject();
				for (Entry<String, Object> e : config.entrySet()) {
					obj.put(e.getKey(), e.getValue());
				}
				pipelineFlowConfig.append("pipelineFlowConfigs", obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(pipelineFlowConfig.toString()).build();
	}

	@ApiOperation(value = "Save a Deployment Pipeline configuration by Pipeline Flow ID", notes = "Save a Deployment Pipeline configuration by Pipeline Flow ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@POST
	@Path("/savePipelineFlowConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePipelineFlowConfig() {
		/**************************************
		 * Parse payload and retrieve relevant information
		 **************************************/
		String jsonString = "";
		CamundaConnector c = new CamundaConnector();
		try {
			jsonString = IOUtils.toString(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList object = c.jsonToArrayList(jsonString);
		for (int i = 0; i < object.size(); i++) {
			String pipeline_deployment_config_id = "", jenkinsJob = "", jenkinsParams = "", name = "";
			HashMap obj = (HashMap) object.get(i);
			pipeline_deployment_config_id = (String) obj.get("pipeline_deployment_config_id");
			jenkinsJob = (String) obj.get("jenkins_job");
			jenkinsParams = (String) obj.get("jenkins_params");
			name = (String) obj.get("name");
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "UPDATE `camundabpmajsc6`.`pipeline_deployment_config` SET `jenkins_job` = ?, `jenkins_params` = ?, `name` = ? WHERE `pipeline_deployment_config_id` = ?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, jenkinsJob);
				ps.setString(2, jenkinsParams);
				ps.setString(3, name);
				ps.setString(4, pipeline_deployment_config_id);
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
		return Response.status(200).build();
	}

	@ApiOperation(value = "Save a configuration repository to be used in a pipeline deployment configuration", notes = "Save a configuration repository to be used in a pipeline deployment configuration")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@POST
	@Path("/saveConfigurationRepository")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveConfigurationRepository(@ApiParam ConfigurationRepositoryDetail crd) throws Exception {
		String user = request.getUserPrincipal().getName();
		String name = crd.getName();
		String gotsid = crd.getGots_id();
		String url = crd.getUrl();
		String username = crd.getUsername();
		String password = crd.getPassword();
		String encryptedPassword = EncryptDecryptService.encrypt(password);
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "Insert into configuration_repositories (gots_id, name, url, username, password, created_by, created_date) values (?, ?, ?, ?, ?, ?, NOW());";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, name);
				ps.setString(3, url);
				ps.setString(4, username);
				ps.setString(5, encryptedPassword);
				ps.setString(6, user);
				ps.executeQuery();
			} catch (Exception e) {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException
						.setMessage("The Configuration Repository Name (" + name + ") already exists.");
				return Response.status(400).entity(configAlreadyExistsException).build();
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
			return Response.status(201).build();
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
	}

	@ApiOperation(value = "Get all Configuration Repositories for a GOTS", notes = "Get all Configuration Repositories for a GOTS")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getConfigurationRepositories/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationRepositories(@ApiParam(required = true) @PathParam("gotsid") String gotsid) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select configuration_repository_id, gots_id, name, url, username, password from configuration_repositories where gots_id = ?;";
		JSONObject configurationRepositories = new JSONObject();
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
					if (column_name.equals("password")) {
						column_value = EncryptDecryptService.decrypt(rs.getString(i));
					}
					if (column_value == null || column_value.equals("")) {
						column_value = "";
					}
					obj.put(column_name, column_value);
				}
				configurationRepositories.append("configurationRepositories", obj);
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
		return Response.status(200).entity(configurationRepositories.toString()).build();
	}

	@ApiOperation(value = "Get all Configuration Repositories for a GOTS by id", notes = "Get all Configuration Repositories for a GOTS by id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@GET
	@Path("/getConfigurationRepositoriesById/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationRepositoriesById(@ApiParam(required = true) @PathParam("id") String id) {
		Connection con = Database.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		String sql = "select configuration_repository_id, gots_id, name, url, username, password from configuration_repositories where configuration_repository_id = ?;";
		JSONObject configurationRepositories = new JSONObject();
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
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
				configurationRepositories.append("configurationRepositories", obj);
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
		return Response.status(200).entity(configurationRepositories.toString()).build();
	}

	@ApiOperation(value = "Update a configuration repository entry", notes = "Update a configuration repository entry")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 102, message = "The Pipeline Flow name already exists"),
			@ApiResponse(code = 400, message = "The Pipeline Flow name already exists") })
	@PUT
	@Path("/updateConfigurationRepository/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateConfigurationRepository(
			@ApiParam(value = "The configuration repository id", required = true) @PathParam("id") String id,
			@ApiParam ConfigurationRepositoryDetail crd) {
		/**************************************
		 * Parse payload and retrieve relevant information
		 **************************************/
		String name = crd.getName();
		String url = crd.getUrl();
		String username = crd.getUsername();
		String password = crd.getPassword();
		String gotsid = crd.getGots_id();

		/**************************************
		 * Retrieve current user and ensure they are an admin
		 **************************************/
		String user = request.getUserPrincipal().getName();
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			/**************************************
			 * Update record in deployment_config table if new name does not
			 * already exist.
			 **************************************/
			Connection con = Database.getConnection();
			PreparedStatement ps = null;
			String sql = "Update configuration_repositories set name=?, url=?, username=?, password=?, modifier=?, updated_date=NOW() where configuration_repository_id=?;";
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, name);
				ps.setString(2, url);
				ps.setString(3, username);
				ps.setString(4, EncryptDecryptService.encrypt(password));
				ps.setString(5, user);
				ps.setString(6, id);
				ps.executeQuery();
			} catch (Exception e) {
				/**************************************
				 * Throw an error if the gots_id, name, and pipeline_flow_id
				 * combination already exists.
				 **************************************/
				e.printStackTrace();
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(102);
				configAlreadyExistsException.setMessage("The Configuration Repository (" + name + ") already exists.");
				return Response.status(400).entity(configAlreadyExistsException).build();
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
		} else {
			/**************************************
			 * Throw an error if the user is not an administrator for the gots
			 * id.
			 **************************************/
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException
					.setMessage("You are not an admin for GOTS ID (" + gotsid + ") and cannot create a Pipeline Flow.");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();
	}

	@ApiOperation(value = "Delete a configuration repository for a GOTS ID", notes = "Delete a configuration repository for a GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You are not an admin for GOTS ID"),
			@ApiResponse(code = 400, message = "This Configuration Repository cannot be deleted because it is being used by pipeline.") })
	@DELETE
	@Path("/deleteConfigurationRepository/{id}/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteConfigurationRepository(
			@ApiParam(value = "The configuration repository id to be deleted", required = true) @PathParam("id") String id,
			@ApiParam(required = true) @PathParam("gotsid") String gotsid) {
		if (request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			Connection con = Database.getConnection();
			ResultSet rs = null;
			PreparedStatement ps = null;
			String sql = "select pf.name as flowName, dc.name as configName from deployment_config dc "
					+ "join pipeline_flow pf on pf.pipeline_flow_id=dc.pipeline_flow_id"
					+ " where dc.gots_id=? and dc.configuration_repository_id = ? limit 1;";
			String flowName = "", flowConfigName = "";
			boolean flowFound = false;
			try {
				ps = con.prepareStatement(sql);
				ps.setString(1, gotsid);
				ps.setString(2, id);
				rs = ps.executeQuery();
				if (rs.next()) {
					flowName = rs.getString("flowName");
					flowConfigName = rs.getString("configName");
					flowFound = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!flowFound) {
				sql = "select pmc.name, pc.attribute_name, pmc.attribute_value from camundabpmajsc6.pipeline_config pc "
						+ "join camundabpmajsc6.pipeline_gots_config pmc on pc.pipeline_config_id = pmc.pipeline_config_id "
						+ "where gots_id=? and attribute_name='configurationRepositoryDropDown' and attribute_value=? order by pc.config_order;";
				try {
					ps = con.prepareStatement(sql);
					ps.setString(1, gotsid);
					ps.setString(2, id);
					rs = ps.executeQuery();
					boolean found = false;
					String value = "", name = "";
					while (rs.next() && found == false) {
						if (!(rs.getString("attribute_value") == null)) {
							value = rs.getString("attribute_value");
							name = rs.getString("name");
							if (value.equals(id)) {
								found = true;
							}
						}
					}
					if (!found) {
						Database.deleteConfigurationRepository(id);
					} else {
						PiplelineException configAlreadyExistsException = new PiplelineException();
						configAlreadyExistsException.setCode(400);
						configAlreadyExistsException.setMessage(
								"This configuration repository cannot be deleted because it is being used by pipeline '"
										+ name + "'");
						return Response.status(400).entity(configAlreadyExistsException).build();
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
			} else {
				PiplelineException configAlreadyExistsException = new PiplelineException();
				configAlreadyExistsException.setCode(401);
				configAlreadyExistsException.setMessage(
						"This Configuration Repository cannot be deleted because it is being used by the pipeline flow '"
								+ flowName + "', configuration '" + flowConfigName + "'");
				return Response.status(400).entity(configAlreadyExistsException).build();
			}
		} else {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You are not an admin for GOTS ID (" + gotsid + ").");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).build();

	}
}
