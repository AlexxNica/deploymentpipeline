/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.EncryptDecryptService;
import com.att.cicd.deploymentpipeline.workflow.notification.EmailNotification;

public class GetPipelineFlowConfig implements JavaDelegate {

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}

	public void execute(DelegateExecution ex) throws Exception {
		String gotsid = getVar(ex, "gotsid");
		String name = getVar(ex, "name");
		String pipeline_name = getVar(ex, "pipeline_name");
		String submodule_name = getVar(ex, "submodule_name");
		String process_name = getVar(ex, "processName");
		String pipelineId = getVar(ex, "pipeline_id");
		String pipeline_flow_id = getVar(ex, "pipeline_flow_id");
		String deployment_indexer = getVar(ex, "deployment_indexer");
		String acronym = getVar(ex, "acronym");
		String numConfigs = String.valueOf(Database.getMaxDeploymentNum(pipeline_flow_id));
		ex.setVariable("numConfigs", numConfigs);
		ex.setVariable("reviewSubmodule", "false");
		ex.setVariable("refreshRate", Database.getSystemConfig("jenkinsRefreshRate").toString());
		ex.setVariable("associated", Database.isAssociated(gotsid));
		String info = "Beginning";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
				"GetPipelineFlowConfig", "execute", info);
		String deployConfigName = Database.getDeploymentConfigName(pipeline_flow_id, deployment_indexer);
		ex.setVariable("deployConfigName", deployConfigName);
		Map results = Database.getDeploymentConfigAttributesByIndex(pipeline_flow_id, gotsid, deployment_indexer);

		Iterator it = results.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String attribute_name = pair.getKey().toString().trim();
			String attribute_value = "";
			if (pair.getValue() != null) {
				attribute_value = pair.getValue().toString().trim();
			}
			if (attribute_name.equals("approval_list") && (attribute_value == null || attribute_value.isEmpty())) {
				ex.setVariable("assignTo", AAFConnector.getAdminList(gotsid));
			} else if ((attribute_name.equals("enable_approval") && process_name.equals("Deployment Approval"))
					&& (attribute_value == null || attribute_value.isEmpty() || attribute_value.equals("0"))) {
				ex.setVariable("isApproved", true);
				ex.setVariable(attribute_name, "false");
				info = "The \"" + process_name + "\" approval process has been disabled in the " + deployConfigName
						+ " configuration. This step has been skipped.";
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
						"GetPipelineFlowConfig", "execute", info, true);
				info = "Setting " + attribute_name + " = " + attribute_value;
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
						"GetPipelineFlowConfig", "execute", info);
			} else if ((attribute_name.equals("environment_id"))) {
				Map environment = Database.getEnvironmentByID(attribute_value);
				if (!environment.isEmpty()) {
					ex.setVariable("environment_type", environment.get("environment_type"));
				} else {
					ex.setVariable("environment_type", "");
				}
				ex.setVariable(attribute_name, attribute_value);
			} else {
				if (attribute_name.equals("enable_approval") && process_name.equals("Deployment Approval")) {
					ex.setVariable("isApproved", false);
					info = "The approval process has been enabled in the (" + deployConfigName + ") configuration.";
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
							"GetPipelineFlowConfig", "execute", info, true);
				}
				if (attribute_name.equals("approval_list")) {
					ex.setVariable("assignTo", attribute_value);
				} else {
					ex.setVariable(attribute_name, attribute_value);
				}
				info = "Setting " + attribute_name + " = " + attribute_value;
			}
		}
		// Set necessary variables
		ex.setVariable("automatic", "true");
		if (!process_name.equals("Deployment Approval")) {
			info = deployConfigName + " is starting the automatic build and/or deployment process. Please wait...";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetPipelineFlowConfig", "execute", info, true);
		}
		if (process_name.equals("Deployment Approval")) {
			String description = Database.getDescription("description", pipeline_name, submodule_name, process_name);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployConfigName);
			ex.setVariable("processDescription", newDescription);
		} else {
			String description = Database.getDescription("descriptionAutomatic", pipeline_name, submodule_name,
					process_name);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployConfigName);
			ex.setVariable("processDescription", newDescription);
		}

		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name, "GetConfig",
				"GetPipelineFlowConfig", "End");
	}

}
