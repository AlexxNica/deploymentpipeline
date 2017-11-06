/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class DeploymentConfigurationDetail {
	String pipeline_flow_id;
	String build_server_id;
	String environment_id;
	String configuration_repository_id;
	String name;
	String gotsid;
	String description;
	boolean enable_notification;
	String notification_list;
	boolean enable_approval;
	String approval_list;
	String pipeline_name;
	@ApiModelProperty(position=1, required=true)
	public String getPipeline_flow_id() {
		return pipeline_flow_id;
	}
	public void setPipeline_flow_id(String pipeline_flow_id) {
		this.pipeline_flow_id = pipeline_flow_id;
	}
	@ApiModelProperty(position=2, required=true)
	public String getBuild_server_id() {
		return build_server_id;
	}
	public void setBuild_server_id(String build_server_id) {
		this.build_server_id = build_server_id;
	}
	@ApiModelProperty(position=3, required=true)
	public String getConfiguration_repository_id() {
		return configuration_repository_id;
	}
	public void setConfiguration_repository_id(String configuration_repository_id) {
		this.configuration_repository_id = configuration_repository_id;
	}
	@ApiModelProperty(position=4, required=true)
	public String getEnvironment_id() {
		return environment_id;
	}
	public void setEnvironment_id(String environment_id) {
		this.environment_id = environment_id;
	}
	@ApiModelProperty(position=5, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(position=6, required=true)
	public String getGotsid() {
		return gotsid;
	}
	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}
	@ApiModelProperty(position=7)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@ApiModelProperty(position=8, required=true)
	public boolean getEnable_notification() {
		return enable_notification;
	}
	public void setEnable_notification(boolean enable_notification) {
		this.enable_notification = enable_notification;
	}
	@ApiModelProperty(position=9, required=true)
	public String getNotification_list() {
		return notification_list;
	}
	public void setNotification_list(String notification_list) {
		this.notification_list = notification_list;
	}
	@ApiModelProperty(position=10, required=true)
	public boolean getEnable_approval() {
		return enable_approval;
	}
	public void setEnable_approval(boolean enable_approval) {
		this.enable_approval = enable_approval;
	}
	@ApiModelProperty(position=11, required=true)
	public String getApproval_list() {
		return approval_list;
	}
	public void setApproval_list(String approval_list) {
		this.approval_list = approval_list;
	}
	@ApiModelProperty(value="The name given to the pipeline by the user", position=12, required=true)
	public String getPipeline_name() {
		return pipeline_name;
	}
	public void setPipeline_name(String pipeline_name) {
		this.pipeline_name = pipeline_name;
	}
}
