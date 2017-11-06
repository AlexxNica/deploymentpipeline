/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

public class JenkinsPipelineConfig {
	String gotsid;
	String name;
	String url;
	String username;
	String password;
	String devJobName;
	String devJenkinsParams;
	String qcJobName;
	String qcParams;
	String clientJobName;
	String clientParams;
	String prodJobName;
	String prodParams;
	
	//Seed Info------------------------
	
	String seed_id;		
	String seed_owner;	
	String build_server_url;
	String source_code_url;
  //-----------------------
	public String getGotsid() {
		return gotsid;
	}
	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDevJobName() {
		return devJobName;
	}
	public void setDevJobName(String devJobName) {
		this.devJobName = devJobName;
	}
	public String getDevJenkinsParams() {
		return devJenkinsParams;
	}
	public void setDevJenkinsParams(String devJenkinsParams) {
		this.devJenkinsParams = devJenkinsParams;
	}
	public String getQcJobName() {
		return qcJobName;
	}
	public void setQcJobName(String qcJobName) {
		this.qcJobName = qcJobName;
	}
	public String getQcParams() {
		return qcParams;
	}
	public void setQcParams(String qcParams) {
		this.qcParams = qcParams;
	}
	public String getClientJobName() {
		return clientJobName;
	}
	public void setClientJobName(String clientJobName) {
		this.clientJobName = clientJobName;
	}
	public String getClientParams() {
		return clientParams;
	}
	public void setClientParams(String clientParams) {
		this.clientParams = clientParams;
	}
	public String getProdJobName() {
		return prodJobName;
	}
	public void setProdJobName(String prodJobName) {
		this.prodJobName = prodJobName;
	}
	public String getProdParams() {
		return prodParams;
	}
	public void setProdParams(String prodParams) {
		this.prodParams = prodParams;
	}
	
	//-------------------------------------
	
	public String getSeed_id() {
		return seed_id;
	}
	public void setSeed_id(String seed_id) {
		this.seed_id = seed_id;
	}	
	public String getSeed_owner() {
		return seed_owner;
	}
	public void setSeed_owner(String seed_owner) {
		this.seed_owner = seed_owner;
	}
	public String getBuild_server_url() {
		return build_server_url;
	}
	public void setBuild_server_url(String build_server_url) {
		this.build_server_url = build_server_url;
	}
	public String getSource_code_url() {
		return source_code_url;
	}
	public void setSource_code_url(String source_code_url) {
		this.source_code_url = source_code_url;
	}
	
	//----------------------------------------------
	
}
