/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

public class GotsDetail  {
	String gotsid;
	String acronym;
	String applicationName;
	String description;
	String role;
	String pipelineName;
	String appContact;
	String IT_LTM;
	String namespace;
	String associated_gots_id;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getAppContact() {
		return appContact;
	}

	public void setAppContact(String appContact) {
		this.appContact = appContact;
	}

	public String getIT_LTM() {
		return IT_LTM;
	}

	public void setIT_LTM(String iT_LTM) {
		IT_LTM = iT_LTM;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGotsid() {
		return gotsid;
	}

	public void setGotsid(String gotsid) {
		this.gotsid = gotsid;
	}

	public String getAcronym() {
		return acronym;
	} 

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getAssociated_gots_id() {
		return associated_gots_id;
	}

	public void setAssociated_gots_id(String associated_gots_id) {
		this.associated_gots_id = associated_gots_id;
	}

}
