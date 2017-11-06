/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class CDPWorkflowConfig {
	String gotsid;
	String name;
	String url;
	String username;
	String password;
	String seed_id;
	String seed_owner;
	String source_code_url;
	String devJobName;
	String devJenkinsParams;
	@ApiModelProperty(position=1, required=true)
	public String getGotsid() {
		return gotsid;
	}
	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}
	@ApiModelProperty(position=2, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(position=3, required=true)
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@ApiModelProperty(position=4, required=true)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@ApiModelProperty(position=5, required=true)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@ApiModelProperty(position=6, required=true)
	public String getSeed_id() {
		return seed_id;
	}
	public void setSeed_id(String seed_id) {
		this.seed_id = seed_id;
	}
	@ApiModelProperty(position=7, required=true)
	public String getSeed_owner() {
		return seed_owner;
	}
	public void setSeed_owner(String seed_owner) {
		this.seed_owner = seed_owner;
	}
	@ApiModelProperty(position=8, required=true)
	public String getSource_code_url() {
		return source_code_url;
	}
	public void setSource_code_url(String source_code_url) {
		this.source_code_url = source_code_url;
	}
	@ApiModelProperty(position=9, required=true)
	public String getDevJobName() {
		return devJobName;
	}
	public void setDevJobName(String devJobName) {
		this.devJobName = devJobName;
	}
	@ApiModelProperty(position=10, required=true)
	public String getDevJenkinsParams() {
		return devJenkinsParams;
	}
	public void setDevJenkinsParams(String devJenkinsParams) {
		this.devJenkinsParams = devJenkinsParams;
	}
	

}
