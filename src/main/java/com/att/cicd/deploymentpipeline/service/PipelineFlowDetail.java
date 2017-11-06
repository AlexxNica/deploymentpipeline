/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class PipelineFlowDetail {
	String gotsid;
	String name;
	String description;
	
	@ApiModelProperty(position = 1, required = true)
	public String getGotsid() {
		return gotsid;
	}
	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}
	@ApiModelProperty(position = 2, required = true, value = "the name of the pipeline flow")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(position = 3, value = "the description of the pipeline flow")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
