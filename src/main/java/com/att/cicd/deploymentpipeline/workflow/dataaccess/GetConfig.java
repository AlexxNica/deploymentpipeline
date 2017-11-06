/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.CamundaConnector;

public class GetConfig implements JavaDelegate {

	public void execute(DelegateExecution ex) throws Exception {
		// Save information passed from upper layer workflows.
		String gotsid = getVar(ex, "gotsid");
		String name = getVar(ex, "name");
		String pipeline_name = getVar(ex, "pipeline_name");
		String submodule_name = getVar(ex, "submodule_name");
		String process_name = getVar(ex, "processName");
		String pipelineId = getVar(ex, "pipeline_id");
		String index = getVar(ex, "array_indexer");
		ex.setVariable("reviewSubmodule", "false");
		ex.setVariable("refreshRate", Database.getSystemConfig("jenkinsRefreshRate").toString());
		ex.setVariable("associated", Database.isAssociated(gotsid));
		String info = "Beginning";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name, "GetConfig",
				"execute", info);
		String deployName = Database.getVariableWithIndex(gotsid, name, pipeline_name, submodule_name, "global",
				"deploymentName", index);

		// Get the config attributes and values by index
		Map results = Database.getConfigAttributesByIndex(gotsid, name, pipeline_name, submodule_name, process_name,
				index);
		if (!process_name.equals("RequestBuild")) {
			info = "The \"" + Database.getProcessNameLabel(process_name) + "\" phase for the deployment (" + deployName + ") in the "
					+ Database.getSubmoduleNameLabel(submodule_name) + " stage has started.";
			ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetConfig", "execute", info, true);
		}

		// Set each attribute's value during execution.
		Iterator it = results.entrySet().iterator();
		boolean automatic = false;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String attribute_name = pair.getKey().toString().trim();
			String attribute_value = pair.getValue().toString().trim();
			if (attribute_name.equals("assignTo") && (attribute_value == null || attribute_value.isEmpty())) {
				ex.setVariable("assignTo", AAFConnector.getAdminList(gotsid));
			} else if (attribute_name.equals("enableApproval")
					&& (attribute_value == null || attribute_value.isEmpty() || attribute_value.equals("false"))) {
				ex.setVariable("isApproved", true);
				ex.setVariable(attribute_name, "false");
				info = "The \"" + Database.getProcessNameLabel(process_name) + "\" approval process has been disabled in the "
						+ Database.getSubmoduleNameLabel(submodule_name) + " stage. This step has been skipped.";
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
						"GetConfig", "execute", info, true);
				info = "Setting " + attribute_name + " = " + attribute_value;
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
						"GetConfig", "execute", info);
			} else {
				if (attribute_name.equals("enableApproval")) {
					info = "The \"" + Database.getProcessNameLabel(process_name) + "\" approval process has been enabled in the "
							+ Database.getSubmoduleNameLabel(submodule_name) + " stage for the deployment configuration (" + deployName + ").";
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
							"GetConfig", "execute", info, true);
				}
				ex.setVariable(attribute_name, attribute_value);
				info = "Setting " + attribute_name + " = " + attribute_value;
				if (attribute_name.equals("automatic") && attribute_value.equals("true")) {
					automatic = true;
					info = "The " + Database.getSubmoduleNameLabel(submodule_name) + " phase for the deployment (" + deployName
							+ ") has been configured to be handled automatically. Starting the automatic build and/or deployment process. Please wait...";
					ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
							"GetConfig", "execute", info, true);
				}
				ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
						"GetConfig", "execute", info);
			}
		}

		// Set process description
		if (automatic) {
			String description = Database.getDescription("descriptionAutomatic", pipeline_name, submodule_name,
					process_name);
			String deployname = Database.getVariableWithIndex(gotsid, name, pipeline_name, submodule_name, "global",
					"deploymentName", index);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployname);
			ex.setVariable("processDescription", newDescription);
		} else {
			String description = Database.getDescription("description", pipeline_name, submodule_name, process_name);
			String deployname = Database.getVariableWithIndex(gotsid, name, pipeline_name, submodule_name, "global",
					"deploymentName", index);
			String newDescription = description.replaceAll(Pattern.quote("((deployname))"), deployname);
			ex.setVariable("processDescription", newDescription);
		}
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name, "GetConfig",
				"execute", "End");
	}

	public String getVar(DelegateExecution execution, String varName) {
		String var = (String) execution.getVariable(varName);
		if (var == null) {
			var = "";
		}
		return var;
	}

}
