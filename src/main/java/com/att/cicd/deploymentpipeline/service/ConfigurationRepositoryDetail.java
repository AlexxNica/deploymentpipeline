/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ConfigurationRepositoryDetail {
	String name;
	String gots_id;
	String url;
	String username;
	String password;
	String type;
	@ApiModelProperty(position=1, required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ApiModelProperty(position=2, required=true)
	public String getGots_id() {
		return gots_id;
	}
	public void setGots_id(String gots_id) {
		this.gots_id = gots_id;
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

}
