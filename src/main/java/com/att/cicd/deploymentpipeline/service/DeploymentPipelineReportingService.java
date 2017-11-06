/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ReportBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Api(value = "/reports")
@Path("/report")
public class DeploymentPipelineReportingService {

	private static final Logger LOGGER = Logger.getLogger(DeploymentPipelineReportingService.class);

	public static void main(String[] args) throws JsonProcessingException {
//		DeploymentPipelineReportingService rest = new DeploymentPipelineReportingService();
//		Response r = (rest.getPipelineNamesWithGotsid("252"));
		// Response r = (rest.getPipelineNames());
//		ObjectMapper mapper = new ObjectMapper();
//		System.out.println("TEST RESPONSECODE IS:" + (r.getStatus()));
//		System.out.println("TEST RESPONSE IS:" + mapper.writeValueAsString(r.getEntity()));
	}

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/addToServicesReports/{gotsid}/{feature_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addToServicesReports(@PathParam("gotsid") String gotsid,
			@PathParam("feature_name") String feature_name) {
		String user = request.getUserPrincipal().getName();
		Database.addToServicesReports(user, gotsid, feature_name);
		return Response.status(200).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api")})
	@GET
	@Path("/addToReport/{gotsid}/{name}/{pipeline_name}/{submodule_name}/{process_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addToReport(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("submodule_name") String submodule_name,
			@PathParam("process_name") String process_name) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		} else {
			Database.addToReport(gotsid, name, pipeline_name, submodule_name, process_name, "", false, "true", "",
					false, "jenkinsPipeline");
			return Response.status(200).build();
		}
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api")})
	@GET
	@Path("/update/{gotsid}/{name}/{pipeline_name}/{submodule_name}/{process_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipeline_name") String pipeline_name, @PathParam("submodule_name") String submodule_name,
			@PathParam("process_name") String process_name) {
		if (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		} else {
			Database.update(gotsid, name, pipeline_name, submodule_name, process_name, "", false, "true", "", "true");
			return Response.status(200).build();
		}
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getPipelineNames")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineNames() {
		JSONObject response = ReportBuilder.getPipelineNameList();
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getPipelineNames/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineNamesWithGotsid(@PathParam("gotsid") String gotsid) {
		JSONObject response = ReportBuilder.getPipelineNameListWithGotsid(gotsid);
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getSummaryReport/{pipelineName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSummaryReport(@PathParam("pipelineName") String pipelineName) {
		JSONObject response = ReportBuilder.getSummaryReport(pipelineName);
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getGotsReport/{pipelineName}/{ltm}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGotsReport(@PathParam("pipelineName") String pipelineName, @PathParam("ltm") String ltm) {
		JSONObject response = ReportBuilder.getGotsReport(pipelineName, ltm);
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getGotsDetailSummaryReport/{pipelineName}/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGotsDetailSummaryReport(@PathParam("pipelineName") String pipelineName,
			@PathParam("gotsid") String gotsid) {
		JSONObject response = ReportBuilder.getGotsDetailSummaryReport(pipelineName, gotsid);
		return Response.status(200).entity(response.toString()).build();
	}

	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"), 
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getGotsDetailReport/{pipelineName}/{gotsid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGotsDetailReport(@PathParam("pipelineName") String pipelineName,
			@PathParam("gotsid") String gotsid) {
		JSONObject response = ReportBuilder.getGotsDetailReport(pipelineName, gotsid);
		return Response.status(200).entity(response.toString()).build();
	}
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"), 
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getServicesReport")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServicesReport() {
		JSONObject response = ReportBuilder.getServicesReport();
		return Response.status(200).entity(response.toString()).build();
	}
	
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"), 
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getServicesReportsCountFeatures")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServicesReportsCountFeatures() throws JSONException {
		JSONObject response = ReportBuilder.getServicesReportsCountFeatures();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"), 
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/servicesReportsCountUUID")
	@Produces(MediaType.APPLICATION_JSON)
	public Response servicesReportsCountUUID() throws JSONException {
		JSONObject response = ReportBuilder.servicesReportsCountUUID();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),  
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/servicesReportsCountGOTS")
	@Produces(MediaType.APPLICATION_JSON)
	public Response servicesReportsCountGOTS() throws JSONException {
		JSONObject response = ReportBuilder.servicesReportsCountGOTS();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),  
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/servicesReportsShowGOTS")
	@Produces(MediaType.APPLICATION_JSON)
	public Response servicesReportsShowGOTS() throws JSONException {
		JSONObject response = ReportBuilder.servicesReportsShowGOTS();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"), 
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/servicesReportsCountByGotsAndFeature")
	@Produces(MediaType.APPLICATION_JSON)
	public Response servicesReportsCountByGotsAndFeature() throws JSONException {
		JSONObject response = ReportBuilder.servicesReportsCountByGotsAndFeature();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiOperation(value = "List out all the names of the reports", 
			notes = "Displays all the name and details of report queries.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getAllReportNames")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllReportNames() throws JSONException {
		JSONObject response = ReportBuilder.showAllReportOptions();
		JSONArray responseArray = response.getJSONArray("report");
		return Response.status(200).entity(responseArray.toString()).build();
	}
	
	@ApiOperation(value = "Display Report by Report ID", 
			notes = "Display Report by Report ID")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message= "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error")})
	@GET
	@Path("/getReportByID/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReportById(@PathParam("id") String id) throws JSONException {
		JSONArray response = ReportBuilder.getReportById(id);
		JSONObject responseArray = response.getJSONObject(0);
		return Response.status(200).entity(responseArray.toString()).build();
	}
}
