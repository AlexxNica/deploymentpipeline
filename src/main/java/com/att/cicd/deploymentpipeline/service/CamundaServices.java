/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

import com.att.cicd.deploymentpipeline.util.CamundaConnector;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.ApplicationLogger;
import com.att.cicd.deploymentpipeline.workflow.dataaccess.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Api(value = "/camunda")
@Path("/camundaServices")
public class CamundaServices {

	public static void main(String[] args) throws ParseException, IOException {
		CamundaServices s = new CamundaServices();
		// s.getPipelineFinishedProcessByName("25316", "1610Client",
		// "application-delivery-workflow-v1-0-2");
		// s.startPipelineProcess("25316", "test",
		// "application-delivery-workflow-v1-0-2");
		// Response r = (rest.onboardPipeline("15"));
		
		
		
		/**
		Response r = s.getTodoList();
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<Object> obj = mapper.readValue(mapper.writeValueAsString(r.getEntity()), mapper.getTypeFactory().constructCollectionType(ArrayList.class, Object.class));
		for (int i = 0; i < obj.size(); i++) {
			Map m = (Map) (obj.get(i));
			s.stopPipelineProcess(m.get("id").toString());
		}
		**/
	}

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;
	
	@ApiOperation(value = "List all the Pipeline assigned to the user which require approval", notes = "List all the Pipeline details assigned to the user for all pipeline type and GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getRequireApprovalList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRequireApprovalList() throws ParseException, JsonProcessingException {
		String user = request.getUserPrincipal().getName();
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = (c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%25" + user
				+ "%25,todo_eq_true,requireApproval_eq_true", cookie, true));
		ArrayList instances = c.jsonToArrayList(response);
		ArrayList payloadList = new ArrayList();
		Response r = (getAllPipelineNames());
		ObjectMapper mapper = new ObjectMapper();
		String pipelineNames = mapper.writeValueAsString(r.getEntity());
		ArrayList icons = c.jsonToArrayList(pipelineNames);
		for (int i = 0; i < instances.size(); i++) {
			Map payload = new HashMap();
			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
			// payload.put("definitionId", c.getJsonString((Map)
			// instances.get(i), "definitionId"));
			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
			String id = c.getJsonString((Map) instances.get(i), "id");
			// payload.put("id", id);
			payload.put("gotsid", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("status", "In Progress");
			String responseid = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables",
					cookie, true));
			Map items = c.jsonToMap(responseid);
			String pipeline_name = c.getJsonString(c.getJsonMap(items, "pipeline_name"), "value");
			payload.put("pipeline_name", pipeline_name);
			payload.put("acronym", c.getJsonString(c.getJsonMap(items, "acronym"), "value"));
			responseid = c.getJsonString(c.getJsonMap(items, "pipeline_id"), "value");
			if (responseid != null && !responseid.equals("")) {
				payload.put("id", responseid);
				r = (getPipelineProcessCurrentStatus(responseid));
				mapper = new ObjectMapper();
				String currentStatus = mapper.writeValueAsString(r.getEntity());
				Map info = c.jsonToMap(currentStatus);
				payload.put("name", c.getJsonString(info, "name"));
				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = null;
				try {
					date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
					payload.put("pipelineStartTime", outputFormat.format(date));
					date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
					payload.put("taskStartTime", outputFormat.format(date));
				} catch (Exception e) {
					payload.put("pipelineStartTime", "");
					payload.put("taskStartTime", "");
				}
				payload.put("assignee", c.getJsonString(info, "assignee"));
				String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
				String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
				payload.put("currentStep", (activityName1 + " > " + activityName2));
				for (int j = 0; j < icons.size(); j++) {
					if (c.getJsonString((Map) icons.get(j), "name").equals(pipeline_name)) {
						payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
					}
				}
			}
			payloadList.add(payload);
		}
		return Response.status(200).entity(payloadList).build();
	}

	@ApiOperation(value = "List all the Pipeline assigned to the user", notes = "List all the Pipeline details assigned to the user for all pipeline type and GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getTodoList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTodoList() throws ParseException, JsonProcessingException {
		String user = request.getUserPrincipal().getName();
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = (c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%25" + user
				+ "%25,todo_eq_true", cookie, true));
//		String response = (c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%" + uuid
//				+ "%&processDefinitionKey=review-submodule", cookie, true));
		ArrayList instances = c.jsonToArrayList(response);
		ArrayList payloadList = new ArrayList();
		Response r = (getAllPipelineNames());
		ObjectMapper mapper = new ObjectMapper();
		String pipelineNames = mapper.writeValueAsString(r.getEntity());
		ArrayList icons = c.jsonToArrayList(pipelineNames);
		for (int i = 0; i < instances.size(); i++) {
			Map payload = new HashMap();
			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
			// payload.put("definitionId", c.getJsonString((Map)
			// instances.get(i), "definitionId"));
			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
			String id = c.getJsonString((Map) instances.get(i), "id");
			// payload.put("id", id);
			payload.put("gotsid", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("status", "In Progress");
			String responseid = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables",
					cookie, true));
			Map items = c.jsonToMap(responseid);
			String pipeline_name = c.getJsonString(c.getJsonMap(items, "pipeline_name"), "value");
			payload.put("pipeline_name", pipeline_name);
			payload.put("acronym", c.getJsonString(c.getJsonMap(items, "acronym"), "value"));
			responseid = c.getJsonString(c.getJsonMap(items, "pipeline_id"), "value");
			if (responseid != null && !responseid.equals("")) {
				payload.put("id", responseid);
				r = (getPipelineProcessCurrentStatus(responseid));
				mapper = new ObjectMapper();
				String currentStatus = mapper.writeValueAsString(r.getEntity());
				Map info = c.jsonToMap(currentStatus);
				payload.put("name", c.getJsonString(info, "name"));
				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = null;
				try {
					date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
					payload.put("pipelineStartTime", outputFormat.format(date));
					date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
					payload.put("taskStartTime", outputFormat.format(date));
				} catch (Exception e) {
					payload.put("pipelineStartTime", "");
					payload.put("taskStartTime", "");
				}
				payload.put("assignee", c.getJsonString(info, "assignee"));
				String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
				String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
				payload.put("currentStep", (activityName1 + " > " + activityName2));
				for (int j = 0; j < icons.size(); j++) {
					if (c.getJsonString((Map) icons.get(j), "name").equals(pipeline_name)) {
						payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
					}
				}
			}
			payloadList.add(payload);
		}
//		// for other submodule
//		response = c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%" + uuid
//				+ "%&processDefinitionKey=pipeline-submodule", cookie, true);
//		instances = c.jsonToArrayList(response);
//		for (int i = 0; i < instances.size(); i++) {
//			Map payload = new HashMap();
//			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
//			// payload.put("definitionId", c.getJsonString((Map)
//			// instances.get(i), "definitionId"));
//			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
//			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
//			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
//			String id = c.getJsonString((Map) instances.get(i), "id");
//			// payload.put("id", id);
//			payload.put("gotsid", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("status", "In Progress");
//			String responseid = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables",
//					cookie, true));
//			Map items = c.jsonToMap(responseid);
//			String pipeline_name = c.getJsonString(c.getJsonMap(items, "pipeline_name"), "value");
//			payload.put("pipeline_name", pipeline_name);
//			payload.put("acronym", c.getJsonString(c.getJsonMap(items, "acronym"), "value"));
//			responseid = c.getJsonString(c.getJsonMap(items, "pipeline_id"), "value");
//			if (responseid != null && !responseid.equals("")) {
//				payload.put("id", responseid);
//				r = (getPipelineProcessCurrentStatus(responseid));
//				mapper = new ObjectMapper();
//				String currentStatus = mapper.writeValueAsString(r.getEntity());
//				Map info = c.jsonToMap(currentStatus);
//				payload.put("name", c.getJsonString(info, "name"));
//				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
//				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//				Date date = null;
//				try {
//					date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
//					payload.put("pipelineStartTime", outputFormat.format(date));
//					date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
//					payload.put("taskStartTime", outputFormat.format(date));
//				} catch (Exception e) {
//					payload.put("pipelineStartTime", "");
//					payload.put("taskStartTime", "");
//				}
//				payload.put("assignee", c.getJsonString(info, "assignee"));
//				String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
//				String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
//				payload.put("currentStep", (activityName1 + " > " + activityName2));
//				for (int j = 0; j < icons.size(); j++) {
//					if (c.getJsonString((Map) icons.get(j), "name").equals(pipeline_name)) {
//						payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
//					}
//				}
//			}
//			payloadList.add(payload);
//		}
//		// for jenkins submodule
//		response = c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%" + uuid
//				+ "%&processDefinitionKey=jenkins-review-submodule", cookie, true);
//		instances = c.jsonToArrayList(response);
//		for (int i = 0; i < instances.size(); i++) {
//			Map payload = new HashMap();
//			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
//			// payload.put("definitionId", c.getJsonString((Map)
//			// instances.get(i), "definitionId"));
//			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
//			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
//			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
//			String id = c.getJsonString((Map) instances.get(i), "id");
//			// payload.put("id", id);
//			payload.put("gotsid", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("status", "In Progress");
//			String responseid = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables",
//					cookie, true));
//			Map items = c.jsonToMap(responseid);
//			String pipeline_name = c.getJsonString(c.getJsonMap(items, "pipeline_name"), "value");
//			payload.put("pipeline_name", pipeline_name);
//			payload.put("acronym", c.getJsonString(c.getJsonMap(items, "acronym"), "value"));
//			responseid = c.getJsonString(c.getJsonMap(items, "pipeline_id"), "value");
//			if (responseid != null && !responseid.equals("")) {
//				payload.put("id", responseid);
//				r = (getPipelineProcessCurrentStatus(responseid));
//				mapper = new ObjectMapper();
//				String currentStatus = mapper.writeValueAsString(r.getEntity());
//				Map info = c.jsonToMap(currentStatus);
//				payload.put("name", c.getJsonString(info, "name"));
//				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
//				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//				Date date = null;
//				try {
//					date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
//					payload.put("pipelineStartTime", outputFormat.format(date));
//					date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
//					payload.put("taskStartTime", outputFormat.format(date));
//				} catch (Exception e) {
//					payload.put("pipelineStartTime", "");
//					payload.put("taskStartTime", "");
//				}
//				payload.put("assignee", c.getJsonString(info, "assignee"));
//				String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
//				String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
//				payload.put("currentStep", (activityName1 + " > " + activityName2));
//				for (int j = 0; j < icons.size(); j++) {
//					if (c.getJsonString((Map) icons.get(j), "name").equals(pipeline_name)) {
//						payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
//					}
//				}
//			}
//			payloadList.add(payload);
//		}
//		// for other submodule
//		response = c.get("camunda/api/engine/engine/default/process-instance?variables=assignTo_like_%" + uuid
//				+ "%&processDefinitionKey=jenkins-pipeline-submodule", cookie, true);
//		instances = c.jsonToArrayList(response);
//		for (int i = 0; i < instances.size(); i++) {
//			Map payload = new HashMap();
//			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
//			// payload.put("definitionId", c.getJsonString((Map)
//			// instances.get(i), "definitionId"));
//			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
//			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
//			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
//			String id = c.getJsonString((Map) instances.get(i), "id");
//			// payload.put("id", id);
//			payload.put("gotsid", c.getJsonString((Map) instances.get(i), "businessKey"));
//			payload.put("status", "In Progress");
//			String responseid = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables",
//					cookie, true));
//			Map items = c.jsonToMap(responseid);
//			String pipeline_name = c.getJsonString(c.getJsonMap(items, "pipeline_name"), "value");
//			payload.put("pipeline_name", pipeline_name);
//			payload.put("acronym", c.getJsonString(c.getJsonMap(items, "acronym"), "value"));
//			responseid = c.getJsonString(c.getJsonMap(items, "pipeline_id"), "value");
//			if (responseid != null && !responseid.equals("")) {
//				payload.put("id", responseid);
//				r = (getPipelineProcessCurrentStatus(responseid));
//				mapper = new ObjectMapper();
//				String currentStatus = mapper.writeValueAsString(r.getEntity());
//				Map info = c.jsonToMap(currentStatus);
//				payload.put("name", c.getJsonString(info, "name"));
//				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
//				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//				Date date = null;
//				try {
//					date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
//					payload.put("pipelineStartTime", outputFormat.format(date));
//					date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
//					payload.put("taskStartTime", outputFormat.format(date));
//				} catch (Exception e) {
//					payload.put("pipelineStartTime", "");
//					payload.put("taskStartTime", "");
//				}
//				payload.put("assignee", c.getJsonString(info, "assignee"));
//				String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
//				String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
//				payload.put("currentStep", (activityName1 + " > " + activityName2));
//				for (int j = 0; j < icons.size(); j++) {
//					if (c.getJsonString((Map) icons.get(j), "name").equals(pipeline_name)) {
//						payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
//					}
//				}
//			}
//			payloadList.add(payload);
//		}
		return Response.status(200).entity(payloadList).build();
	}

	@ApiOperation(value = "List all the Pipeline Names assigned to the user", notes = "List all the Pipeline Names assigned to the user for all pipeline type and GOTS ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getAllPipelineNames")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPipelineNames() {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = (c.get("camunda/api/engine/engine/default/process-definition", cookie, true));
		ArrayList items = c.jsonToArrayList(response);
		ArrayList responseArray = new ArrayList();
		Map<String, Integer> newestVersions = new HashMap<String, Integer>();
		for (int i = 0; i < items.size(); i++) {
			String key = c.getJsonString((Map) items.get(i), "key");
			int value = Integer.parseInt(c.getJsonInteger((Map) items.get(i), "version"));
			if (key.contains("-workflow") != false) {
				if (newestVersions.get(key) != null && Integer.valueOf(value) > newestVersions.get(key)) {
					newestVersions.put(key, Integer.valueOf(value));
				} else {
					newestVersions.put(key, Integer.valueOf(value));
				}
			}
		}
		
		for (int i = 0; i < items.size(); i++) {
			Map responseItem = new HashMap();
			String key = c.getJsonString((Map) items.get(i), "key");
			int value = Integer.parseInt(c.getJsonInteger((Map) items.get(i), "version"));
			if (key.contains("-workflow") != false && value == newestVersions.get(key)) {
				responseItem.put("label", c.getJsonString((Map) items.get(i), "name"));
				responseItem.put("name", key);
				String descriptionWithIcon = c.getJsonString((Map) items.get(i), "description");
				String description = descriptionWithIcon.substring(0, descriptionWithIcon.indexOf('|')).trim();
				String icon = descriptionWithIcon
						.substring(descriptionWithIcon.indexOf('|') + 1, descriptionWithIcon.length()).trim();
				responseItem.put("description", description);
				responseItem.put("icon", icon);
				responseItem.put("version", newestVersions.get(key));
				responseArray.add(responseItem);
			}
		}
//		
//		int newestVersion = 0;
//		for (int i = 0; i < items.size(); i++) {
//			Map responseItem = new HashMap();
//			int version = Integer.parseInt(c.getJsonInteger((Map) items.get(i), "version"));
//			if (version >= newestVersion) {
//				newestVersion = version;
//				String key = c.getJsonString((Map) items.get(i), "key");
//				if (key.contains("-workflow") != false) {
//					responseItem.put("label", c.getJsonString((Map) items.get(i), "name"));
//					responseItem.put("name", key);
//					String descriptionWithIcon = c.getJsonString((Map) items.get(i), "description");
//					String description = descriptionWithIcon.substring(0, descriptionWithIcon.indexOf('|')).trim();
//					String icon = descriptionWithIcon
//							.substring(descriptionWithIcon.indexOf('|') + 1, descriptionWithIcon.length()).trim();
//					responseItem.put("description", description);
//					responseItem.put("icon", icon);
//					responseItem.put("version", newestVersion);
//					responseArray.add(responseItem);
//				}
//			}
//		}
//		for (int i = 0; i < responseArray.size(); i++) {
//			Map item = (Map) responseArray.get(i);
//			if (Integer.valueOf(item.get("version").toString()) < newestVersion) {
//				responseArray.remove(i);
//			}
//		}
		return Response.status(200).entity(responseArray).build();
	}

	@ApiOperation(value = "Retrieve current status of the pipeline process assigned to the user", notes = "Retrieve current status of the pipeline process by id assigned to the user for all pipeline type")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "You do not have access to this api") })
	@GET
	@Path("/getPipelineProcessCurrentStatus/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineProcessCurrentStatus(@PathParam("id") String id) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = (c.get("camunda/api/engine/engine/default/process-instance/" + id + "/activity-instances",
				cookie, true));
		Map items = c.jsonToMap(response);
		HashMap status = new HashMap();
		List<Map> list = new ArrayList();
		HashMap item = new HashMap();
		item.put("processname", c.getJsonString(items, "activityName"));
		ArrayList childActivityInstances = c.getJsonArray(items, "childActivityInstances");
		String activityId = "", activityName = "";
		for (int i = 0; i < childActivityInstances.size(); i++) {
			activityId = c.getJsonString((Map) childActivityInstances.get(i), "activityId");
			activityName = c.getJsonString((Map) childActivityInstances.get(i), "activityName");
			item.put("status", activityId);
			item.put("activityName", activityName);
		}
		if (activityId == null || activityId.equals("")) {
			ArrayList childTransitionInstances = c.getJsonArray(items, "childTransitionInstances");
			for (int i = 0; i < childTransitionInstances.size(); i++) {
				activityId = c.getJsonString((Map) childTransitionInstances.get(i), "activityId");
				activityName = c.getJsonString((Map) childTransitionInstances.get(i), "activityName");
				item.put("status", activityId);
				item.put("activityName", activityName);
			}
		}
		status.put("0", item);
		// list.add(item);

		// Sub Process

		response = (c.get(
				"camunda/api/cockpit/plugin/base/default/process-instance/" + id + "/called-process-instances", cookie,
				true));
		ArrayList sub = c.jsonToArrayList(response);
		String subprocessid = "";
		for (int i = 0; i < sub.size(); i++) {
			subprocessid = c.getJsonString((Map) sub.get(i), "id");
		}
		if (subprocessid.equals("")) {
			subprocessid = "ERROR";
		}
		response = c.get("camunda/api/engine/engine/default/process-instance/" + subprocessid + "/activity-instances",
				cookie, true);

		items = c.jsonToMap(response);
		HashMap item2 = new HashMap();
		item2.put("processname", c.getJsonString(items, "activityName"));
		childActivityInstances = c.getJsonArray(items, "childActivityInstances");
		for (int i = 0; i < childActivityInstances.size(); i++) {
			activityId = c.getJsonString((Map) childActivityInstances.get(i), "activityId");
			activityName = c.getJsonString((Map) childActivityInstances.get(i), "activityName");
			item2.put("status", activityId);
			item2.put("activityName", activityName);
		}
		if (activityId == null || activityId.equals("")) {
			ArrayList childTransitionInstances = c.getJsonArray(items, "childTransitionInstances");
			for (int i = 0; i < childTransitionInstances.size(); i++) {
				activityId = c.getJsonString((Map) childTransitionInstances.get(i), "activityId");
				activityName = c.getJsonString((Map) childTransitionInstances.get(i), "activityName");
				item2.put("status", activityId);
				item2.put("activityName", activityName);
			}
		}

		// Sub Sub Process
		// get Sub Sub Process
		response = c.get("camunda/api/cockpit/plugin/base/default/process-instance/" + subprocessid
				+ "/called-process-instances", cookie, true);

		// Get Activity Time
		ArrayList subsub = c.jsonToArrayList(response);
		String callActivityInstanceId = "";
		for (int i = 0; i < subsub.size(); i++) {
			callActivityInstanceId = c.getJsonString((Map) subsub.get(i), "callActivityInstanceId");
		}
		String time = c.get("camunda/api/engine/engine/default/history/activity-instance?activityInstanceId="
				+ callActivityInstanceId, cookie, true);
		// Sub Sub Process Status
		String subsubprocessid = "";
		for (int i = 0; i < subsub.size(); i++) {
			subsubprocessid = c.getJsonString((Map) subsub.get(i), "id");
		}
		response = c.get(
				"camunda/api/engine/engine/default/process-instance/" + subsubprocessid + "/activity-instances", cookie,
				true);
		status.put("1", item2);
		// list.add(item2);
		HashMap item3 = new HashMap();
		items = c.jsonToMap(response);
		item3.put("processname", c.getJsonString(items, "activityName"));
		item3.put("submoduleID", subsubprocessid);
		childActivityInstances = c.getJsonArray(items, "childActivityInstances");
		for (int i = 0; i < childActivityInstances.size(); i++) {
			activityId = c.getJsonString((Map) childActivityInstances.get(i), "activityId");
			activityName = c.getJsonString((Map) childActivityInstances.get(i), "activityName");
			item3.put("status", activityId);
			item3.put("activityName", activityName);
		}
		if (activityId == null || activityId.equals("")) {
			ArrayList childTransitionInstances = c.getJsonArray(items, "childTransitionInstances");
			for (int i = 0; i < childTransitionInstances.size(); i++) {
				activityId = c.getJsonString((Map) childTransitionInstances.get(i), "activityId");
				activityName = c.getJsonString((Map) childTransitionInstances.get(i), "activityName");
				item3.put("status", activityId);
				item3.put("activityName", activityName);
			}
		}

		// list.add(item3);
		// get sub sub process attribute for description
		// Get Variables
		// TODO make sure this api returns the new attribute you set after that,
		// status.put the attribute in it
		// status.put("jenkinsStatus", "In P");
		status.put("2", item3);
		String descriptionresponse = c.get(
				"camunda/api/engine/engine/default/process-instance/" + subsubprocessid + "/variables", cookie, true);
		Map object1 = c.jsonToMap(descriptionresponse);
		// String taskStatus = "Jenkins: " +
		// c.getJsonString(c.getJsonMap(object1, "taskStatus"), "value");
		String taskStatus = c.getJsonString(c.getJsonMap(object1, "taskStatus"), "value");
		//if((taskStatus.equalsIgnoreCase("Checkout"))||(taskStatus.equalsIgnoreCase("Compile"))||(taskStatus.equalsIgnoreCase("Unit Test"))||(taskStatus.equalsIgnoreCase("Package"))||(taskStatus.equalsIgnoreCase("Verify"))||(taskStatus.equalsIgnoreCase("Quality Scan"))||(taskStatus.equalsIgnoreCase("Publish Artifact")))
		//taskStatus = "Jenkins : " + taskStatus;
		status.put("taskStatus", taskStatus);
		status.put("description", c.getJsonString(c.getJsonMap(object1, "processDescription"), "value"));
		// end get sub sub process attribute for description
		// end Sub Sub Process

		// Get Task
		items = c.jsonToMap(response);
		String responseid = c.getJsonString(items, "id");
		try {
			response = c.get("camunda/api/engine/engine/default/task?processInstanceId=" + responseid, cookie, true);
			ArrayList statusArray = c.jsonToArrayList(response);
			for (int i = 0; i < statusArray.size(); i++) {
				status.put("taskID", c.getJsonString((Map) statusArray.get(i), "id"));
				status.put("assignee", c.getJsonString((Map) statusArray.get(i), "assignee"));
				status.put("description", c.getJsonString((Map) statusArray.get(i), "description"));
			}
		} catch (Exception e) {
		}
		// Get Variables
		response = c.get("camunda/api/engine/engine/default/process-instance/" + id + "/variables", cookie, true);
		Map object = c.jsonToMap(response);
		String gotsid = c.getJsonString(c.getJsonMap(object, "gotsid"), "value");
		status.put("gotsid", gotsid);
		status.put("acronym", c.getJsonString(c.getJsonMap(object, "acronym"), "value"));
		status.put("status", c.getJsonString(c.getJsonMap(object, "status"), "value"));
		ArrayList statusArray1 = c.jsonToArrayList(time);
		for (int i = 0; i < statusArray1.size(); i++) {
			status.put("taskStartTime", c.getJsonString((Map) statusArray1.get(i), "startTime"));
		}
		status.put("name", c.getJsonString(c.getJsonMap(object, "name"), "value"));
		// Get Instance Start Time
		response = c.get("camunda/api/engine/engine/default/history/process-instance/" + id, cookie, true);
		object = c.jsonToMap(response);
		status.put("pipelineStartTime", c.getJsonString(object, "startTime"));
		if (c.getJsonString(object, "endTime").equals("") && !c.getJsonString(object, "startTime").equals("")) {
			status.put("complete", "false");
		} else {
			status.put("complete", "true");
		}

		return Response.status(200).entity(status).build();

	}

	@ApiOperation(value = "List the current process of the pipeline by name", notes = "Retrieve current status of the pipeline process assigned to the user by name")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 400, message = "You do not have access to this api") })
	@GET
	@Path("/getPipelineCurrentProcessByName/{gotsid}/{name}/{pipelineName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineCurrentProcessByName(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipelineName") String pipelineName)
			throws JsonProcessingException, ParseException, SQLException {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.get("camunda/api/engine/engine/default/process-instance?processDefinitionKey="
				+ pipelineName + "&businessKey=" + gotsid + "&variables=name_eq_" + name, cookie, true);
		ArrayList instances = c.jsonToArrayList(response);
		ArrayList payloadList = new ArrayList();
		Response r = (getAllPipelineNames());
		ObjectMapper mapper = new ObjectMapper();
		String pipelineNames = mapper.writeValueAsString(r.getEntity());
		ArrayList icons = c.jsonToArrayList(pipelineNames);
		for (int i = 0; i < instances.size(); i++) {
			Map payload = new HashMap();
			r = (getPipelineProcessCurrentStatus(c.getJsonString((Map) instances.get(i), "id")));
			mapper = new ObjectMapper();
			String currentStatus = mapper.writeValueAsString(r.getEntity());
			Map info = c.jsonToMap(currentStatus);
			String id = c.getJsonString((Map) instances.get(i), "id");
			payload.put("id", id);
			payload.put("links", c.getJsonArray((Map) instances.get(i), "links"));
			payload.put("definitionId", c.getJsonString((Map) instances.get(i), "definitionId"));
			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
			payload.put("ended", c.getJsonBoolean((Map) instances.get(i), "ended"));
			payload.put("suspended", c.getJsonBoolean((Map) instances.get(i), "suspended"));
			payload.put("name", c.getJsonString((Map) instances.get(i), "name"));
			// ******** Make the call to get Status of the Jenkins
			// Pipeline**************
			// payload.put("status", "In Progress");
			payload.put("status", info.get("taskStatus"));
			payload.put("submoduleID", c.getJsonString((Map) c.getJsonMap(info, "2"), "submoduleID"));
			try {
				SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
				SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = null;
				date = inputFormat.parse(c.getJsonString(info, "pipelineStartTime"));
				payload.put("pipelineStartTime", outputFormat.format(date));
				date = inputFormat.parse(c.getJsonString(info, "taskStartTime"));
				payload.put("taskStartTime", outputFormat.format(date));
			} catch (Exception e) {
				payload.put("pipelineStartTime", "01/01/11 01:01:01");
				payload.put("taskStartTime", "01/01/11 01:01:01");
			}
			payload.put("assignee", c.getJsonString(info, "assignee"));
			payload.put("gotsid", gotsid);
			payload.put("pipelineName", name);
			payload.put("acronym", c.getJsonString(info, "acronym"));
			String activityName1 = c.getJsonString((Map) c.getJsonMap(info, "0"), "activityName");
			String activityName2 = c.getJsonString((Map) c.getJsonMap(info, "1"), "activityName");
			payload.put("currentStep", activityName1 + " > " + activityName2);
			for (int j = 0; j < icons.size(); j++) {
				if (c.getJsonString((Map) icons.get(j), "name").equals(pipelineName)) {
					payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
				}
			}
			Map pipelineInformation = new HashMap();
			r = getPipelineCurrentProcessInfo(id);
			payload.put("Pipeline Information", r.getEntity());

			payloadList.add(payload);
		}
		return Response.status(200).entity(payloadList).build();
	}

	@ApiOperation(value = "List all the finished processes of the pipeline", notes = "Retrieve all the finished processes of the pipeline by gotsid, name and pipelineName.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api") })
	@GET
	@Path("/getPipelineFinishedProcessByName/{gotsid}/{name}/{pipelineName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineFinishedProcessByName(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipelineName") String pipelineName) throws JsonProcessingException, ParseException {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.get(
				"camunda/api/engine/engine/default/history/process-instance?processDefinitionKey=" + pipelineName
						+ "&finished=true&processInstanceBusinessKey=" + gotsid + "&variables=name_eq_" + name,
				cookie, true);
		ArrayList instances = c.jsonToArrayList(response);
		ArrayList payloadList = new ArrayList();
		for (int i = 0; i < instances.size(); i++) {
			Map payload = new HashMap();
			name = c.get("camunda/api/engine/engine/default/history/variable-instance?processInstanceId="
					+ c.getJsonString((Map) instances.get(i), "id") + "&variableName=name", cookie, true);
			ArrayList names = c.jsonToArrayList(name);
			payload.put("id", c.getJsonString((Map) instances.get(i), "id"));
			payload.put("businessKey", c.getJsonString((Map) instances.get(i), "businessKey"));
			payload.put("processDefinitionId", c.getJsonString((Map) instances.get(i), "processDefinitionId"));
			payload.put("processDefinitionKey", c.getJsonString((Map) instances.get(i), "processDefinitionKey"));
			SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
			SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			try {
				Date date = null;
				date = inputFormat.parse(c.getJsonString((Map) instances.get(i), "startTime"));
				payload.put("startTime", outputFormat.format(date));
				date = inputFormat.parse(c.getJsonString((Map) instances.get(i), "endTime"));
				payload.put("endTime", outputFormat.format(date));
			} catch (Exception e) {
				payload.put("startTime", "");
				payload.put("endTime", "");
			}
			payload.put("durationInMillis", c.getJsonInteger((Map) instances.get(i), "durationInMillis"));
			payload.put("startUserId", c.getJsonString((Map) instances.get(i), "startUserId"));
			payload.put("startActivityId", c.getJsonString((Map) instances.get(i), "startActivityId"));
			payload.put("deleteReason", c.getJsonString((Map) instances.get(i), "deleteReason"));
			payload.put("superPorcessInstanceId", c.getJsonString((Map) instances.get(i), "superProcessInstanceId"));
			payload.put("superCaseInstanceId", c.getJsonString((Map) instances.get(i), "superCaseInstanceId"));
			payload.put("caseInstanceId", c.getJsonString((Map) instances.get(i), "caseInstanceId"));
			payload.put("name", c.getJsonString((Map) names.get(0), "value"));
			payload.put("status", "Completed");
			try {
				Date endTime = inputFormat.parse(c.getJsonString((Map) instances.get(i), "endTime"));
				Date startTime = inputFormat.parse(c.getJsonString((Map) instances.get(i), "startTime"));
				long diff = endTime.getTime() - startTime.getTime();
				long second = (diff / 1000) % 60;
				long minute = (diff / (1000 * 60)) % 60;
				long hour = (diff / (1000 * 60 * 60)) % 24;
				long days = (diff / (1000 * 60 * 60 * 24)) % 365;
				payload.put("duration", String.format("%dd %dh %dm %ds", days, hour, minute, second));
			} catch (Exception e) {
				payload.put("duration", "");
			}
			Response r = (getAllPipelineNames());
			ObjectMapper mapper = new ObjectMapper();
			String pipelineNames = mapper.writeValueAsString(r.getEntity());
			ArrayList icons = c.jsonToArrayList(pipelineNames);
			for (int j = 0; j < icons.size(); j++) {
				if (c.getJsonString((Map) icons.get(j), "name").equals(pipelineName)) {
					payload.put("icon", c.getJsonString((Map) icons.get(j), "icon"));
				}
			}
			payloadList.add(payload);
		}
		if (gotsid == null || gotsid.equals("")
				|| (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")
						&& !request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|user"))) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}

		return Response.status(200).entity(payloadList).build();
	}

	@ApiOperation(value = "Start pipeline process", notes = "Start/Instantiate the process definition.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error"),
			@ApiResponse(code = 101, message = "You do not have access to this api") })
	@POST
	@Path("/startPipelineProcess/{gotsid}/{name}/{pipelineName}/{acronym}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPipelineProcess(@PathParam("gotsid") String gotsid, @PathParam("name") String name,
			@PathParam("pipelineName") String pipelineName, @PathParam("acronym") String acronym)
			throws JsonProcessingException {
		String info = "If support from the AT&T Eco team is needed, please provide the following information. GOTS ID: "
				+ gotsid + " | Pipeline Name: " + name + " | Pipeline Type: " + pipelineName;
		int id = ApplicationLogger.callLog(gotsid, name, "", pipelineName, "", "", "CamundaServices",
				"startPipelineProcess", info, true);
		String user = request.getUserPrincipal().getName();
		info = user + " has started a pipeline instance for the pipeline (" + name + ").";
		int id2 = ApplicationLogger.callLog(gotsid, name, "", pipelineName, "", "", "CamundaServices",
				"startPipelineProcess", info, true);
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		Map body = new HashMap();
		Map variables = new HashMap();
		Map values = new HashMap();
		values.put("value", gotsid);
		values.put("type", "String");
		variables.put("gotsid", values);
		Map namevalues = new HashMap();
		namevalues.put("value", name);
		namevalues.put("type", "String");
		variables.put("name", namevalues);
		Map acronymvalues = new HashMap();
		acronymvalues.put("value", acronym);
		acronymvalues.put("type", "String");
		variables.put("acronym", acronymvalues);
		body.put("businessKey", gotsid);
		body.put("variables", variables);
		ObjectMapper mapper = new ObjectMapper();
		String postBody = mapper.writeValueAsString(body);
		String response = c.post("camunda/api/engine/engine/default/process-definition/key/" + pipelineName + "/start",
				postBody, cookie, true);
		String pipeline_id = c.getJsonString(c.jsonToMap(response), "id");
		ApplicationLogger.updatePipelineId(id, pipeline_id);
		ApplicationLogger.updatePipelineId(id2, pipeline_id);
		if (gotsid == null || gotsid.equals("")
				|| (!request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|admin")
						&& !request.isUserInRole(((String) System.getProperty("aafnamespace"))+".gots|" + gotsid + "|user"))) {
			PiplelineException configAlreadyExistsException = new PiplelineException();
			configAlreadyExistsException.setCode(101);
			configAlreadyExistsException.setMessage("You do not have access to this api");
			return Response.status(400).entity(configAlreadyExistsException).build();
		}
		return Response.status(200).entity(response).build();
	}

	@ApiOperation(value = "Delete process instance", notes = "Delete an running process instance.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/stopPipelineProcess/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopPipelineProcess(@PathParam("id") String id) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.delete("camunda/api/engine/engine/default/process-instance/" + id, cookie, true);
		return Response.status(200).entity(response).build();
	}

	@ApiOperation(value = "Get XML for the process definition", notes = "Retrieves the BPMN 2.0 XML for the process definition.")

	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getXMLDiagram/{processName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getXMLDiagram(@PathParam("processName") String processName) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.get("camunda/api/engine/engine/default/process-definition/key/" + processName + "/xml",
				cookie, true);
		// response = c.getJsonString(c.jsonToMap(response), "bpmn20Xml");
		return Response.status(200).entity(response).build();
	}

	@ApiOperation(value = "Retrieves the rendered form for a task", notes = "Retrieves the rendered form for a task")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getTaskForm/{id}")
	@Produces(MediaType.APPLICATION_XHTML_XML)
	public Response getTaskForm(@PathParam("id") String id) {
		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.get("camunda/api/engine/engine/default/task/" + id + "/rendered-form", cookie, false);
		return Response.status(200).entity(response).build();
	}

	@ApiOperation(value = "Complete a task", notes = "Complete a task and update process variables.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@POST
	@Path("/completeTask/{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response completeTask(@PathParam("taskID") String taskID, String jsonVariables)
			throws JsonProcessingException {
		CamundaConnector c = new CamundaConnector();
		String user = request.getUserPrincipal().getName();
		Map topObject = c.jsonToMap(jsonVariables);
		Map variables = c.getJsonMap(topObject, "variables");
		Map item = new HashMap();
		item.put("value", user);
		item.put("type", "String");
		variables.put("approver", item);
		ObjectMapper mapper = new ObjectMapper();
		String postBody = mapper.writeValueAsString(topObject);
		String cookie = c.getAuth();
		String response = c.post("camunda/api/engine/engine/default/task/" + taskID + "/complete", postBody, cookie,
				true);
		return Response.status(200).entity(response).build();
	}

	@ApiOperation(value = "Show current Info for the pipeline", notes = "Shows the current process info and links for the running pipeline")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getPipelineCurrentProcessInfo/{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineCurrentProcessInfo(@PathParam("taskID") String taskID) throws SQLException {
		Map pipelineInformation = new HashMap();

		CamundaConnector c = new CamundaConnector();
		String cookie = c.getAuth();
		String response = c.get("camunda/api/engine/engine/default/execution/" + taskID + "/localVariables", cookie,
				true);
		Map topObject = c.jsonToMap(response);

		/*
		 * SYSTEM CONNECTIONS INFORMATION
		 */
		Map systemConnectionsInfo = new HashMap();

		// Retrieve Repository URL
		String sourceCodeURL = "";
		Connection con = Database.getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = "Select * from pipeline_seed_info where name=? and gots_id =?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, c.getJsonString(c.getJsonMap(topObject, "name"), "value"));
			ps.setString(2, c.getJsonString(c.getJsonMap(topObject, "gotsid"), "value"));
			rs = ps.executeQuery();
			if (rs.next()) {
				sourceCodeURL = rs.getString("source_code_url");
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
		if (!sourceCodeURL.equals("")) {
			systemConnectionsInfo.put("Source Code", sourceCodeURL);
		}
		// ---------------------------------

		// Retrieve Jenkins Dashboard URL
		String buildServer = "", buildServerURL = "";
		if (c.getJsonString(c.getJsonMap(topObject, "jenkinsLoginFailed"), "value").equals("false")) {
			String jenkinsJob = c.getJsonString(c.getJsonMap(topObject, "jobName"), "value");
			buildServer = c.getJsonString(c.getJsonMap(topObject, "jenkinsURL"), "value");
			buildServerURL = buildServer + "/jenkins/job/" + jenkinsJob;
		}
		if (!buildServer.equals("")) {
			systemConnectionsInfo.put("Build Server (Jenkins Dashboard)", buildServerURL);
		}
		// ---------------------------------

		// Retrieve Artifact Repository (Docker Dashboard URL)
		// try {
		// String artifactURL = c.getJsonString(c.getJsonMap(topObject,
		// "artifactURL"), "value");
		// systemConnectionsInfo.put("Artifact Repository (Docker Dashboard)",
		// "testurl");
		// } catch (Exception e) {
		// }

		// Retrieve Environment (Kubernetes Dashboard URL)
		String environmentURL = c.getJsonString(c.getJsonMap(topObject, "environmentURL"), "value");
		if (!environmentURL.equals("")) {
			systemConnectionsInfo.put("Environment (Kubernetes Dashboard)", environmentURL);
		}
		if (systemConnectionsInfo.size() != 0) {
			pipelineInformation.put("System Connections Information", systemConnectionsInfo);
		}

		/*
		 * Seed Template Information
		 */
		Map seedTemplateInfo = new HashMap();
		// Retrieve Seed Template Documentation

		if (seedTemplateInfo.size() != 0) {
			pipelineInformation.put("Seed Template Information", seedTemplateInfo);
		}

		/*
		 * Logs
		 */
		// Retrieve AT&T Eco Logs
		Map logs = new HashMap();
		String ecoLogs = "/uui/ecologs/" + taskID;
		logs.put("AT&T Eco Logs", ecoLogs);

		// Retrieve Build Server Logs
		if (!buildServer.equals("")) {
			logs.put("Build Server (Jenkins) Logs",
					c.getJsonString(c.getJsonMap(topObject, "executeID"), "value") + "console");
		}

		if (logs.size() != 0) {
			pipelineInformation.put("Logs", logs);
		}

		return Response.status(200).entity(pipelineInformation).build();
	}

	@ApiOperation(value = "Shows the user friendly Eco logs", notes = "Shows all of the logs to help the user debug without support of eco team")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Service not available"),
			@ApiResponse(code = 500, message = "Unexpected Runtime error") })
	@GET
	@Path("/getPipelineCurrentProcessLogs/{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPipelineCurrentProcessLogs(@PathParam("taskID") String taskID) {
		Map logs = new HashMap();
		ArrayList ecoLogs = Database.getPipelineInstanceLogs(taskID);
		logs.put("AT&T Eco Logs", ecoLogs);
		return Response.status(200).entity(logs).build();
	}
}
