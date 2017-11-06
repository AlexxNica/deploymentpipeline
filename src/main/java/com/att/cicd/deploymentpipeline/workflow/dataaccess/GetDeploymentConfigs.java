/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.workflow.dataaccess;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class GetDeploymentConfigs implements JavaDelegate {

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
		String info = "Start";
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
				"GetDeploymentConfigs", "execute", info, false);
		
		String pipeline_flow_id = Database.getVariable(gotsid, name, pipeline_name, submodule_name, "global", "pipelineFlowDropDown");
		Map results = Database.getDeploymentConfigInfo(pipeline_flow_id);
		
		String initialDeployment = results.get("initialDeployment").toString().trim();
		String numConfigs = results.get("numConfigs").toString().trim();
		
		ex.setVariable("pipeline_flow_id", pipeline_flow_id);
		ex.setVariable("deployment_indexer", initialDeployment);
		ex.setVariable("numConfigs", numConfigs);
		if (Integer.parseInt(numConfigs) != -1) {
			info = "The pipeline is currently in the " + submodule_name + " stage.";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetDeploymentConfigs", "execute", info, true);
			info = "This pipeline instance has been configured with " + numConfigs + " Pipeline Flow Deployment Configuration(s).";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetDeploymentConfigs", "execute", info, true);
		} else {
			info = "There are no deployment configurations for the " + Database.getSubmoduleNameLabel(submodule_name) + " stage of this pipeline. Therefore it has been skipped.";
			ApplicationLogger.callLog(gotsid, process_name, pipelineId, pipeline_name, submodule_name, process_name,
					"GetDeploymentConfigs", "execute", info, true);
		}
		info = "Setting deployment_indexer = " + initialDeployment + " and numConfigs = " + numConfigs;
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name,
				"GetDeploymentConfigs", "execute", info);
		ApplicationLogger.callLog(gotsid, name, pipelineId, pipeline_name, submodule_name, process_name, "GetDeploymentConfigs",
				"execute", "End");
	}
}
