/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class GetIndexInformation implements JavaDelegate {

	public void execute(DelegateExecution ex) throws Exception {
		String gotsid = getVar(ex, "gotsid");
		String name = getVar(ex, "name");
		String pipeline_name = getVar(ex, "pipeline_name");
		String submodule_name = getVar(ex, "submodule_name");
		String process_name = getVar(ex, "processName");
		String pipelineId = getVar(ex, "pipeline_id");
		String info = "Beginning";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name, "GetConfig",
				"execute", info);

		Map results = Database.getIndexInformation(gotsid, name, pipeline_name, submodule_name, process_name);

		String initialIndex = results.get("initialIndex").toString().trim();
		String numDeployments = results.get("numDeployments").toString().trim();

		ex.setVariable("array_indexer", initialIndex);
		ex.setVariable("numDeployments", numDeployments);
		if (Integer.parseInt(numDeployments) != -1) {
			info = "The pipeline is currently in the " + Database.getSubmoduleNameLabel(submodule_name) + " stage.";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetIndexInformation", "execute", info, true);
			info = "This pipeline instance has been configured with " + numDeployments + " deployment(s).";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetIndexInformation", "execute", info, true);
		} else {
			info = "There are no deployment configurations for the " + Database.getSubmoduleNameLabel(submodule_name) + " stage of this pipeline. Therefore it has been skipped.";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetIndexInformation", "execute", info, true);
		}
		info = "Setting array_indexer = " + initialIndex + " and numDeployments = " + numDeployments;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
				"GetIndexInformation", "execute", info);
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

