/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

public class PipelineConfigDetail {
	public String getGots_id() {
		return gots_id;
	}

	public void setGots_id(String gots_id) {
		this.gots_id = gots_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPipeline_name() {
		return pipeline_name;
	}

	public void setPipeline_name(String pipeline_name) {
		this.pipeline_name = pipeline_name;
	}

	public String getSubmodule_name() {
		return submodule_name;
	}

	public void setSubmodule_name(String submodule_name) {
		this.submodule_name = submodule_name;
	}

	public String getProcess_name() {
		return process_name;
	}

	public void setProcess_name(String process_name) {
		this.process_name = process_name;
	}

	public String getProcess_name_label() {
		return process_name_label;
	}

	public void setProcess_name_label(String process_name_label) {
		this.process_name_label = process_name_label;
	}

	public String getAttribute_name() {
		return attribute_name;
	}

	public void setAttribute_name(String attribute_name) {
		this.attribute_name = attribute_name;
	}

	public String getAttribute_value() {
		return attribute_value;
	}

	public void setAttribute_value(String attribute_value) {
		this.attribute_value = attribute_value;
	}

	public String getAttribute_type() {
		return attribute_type;
	}

	public void setAttribute_type(String attribute_type) {
		this.attribute_type = attribute_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSubmodule_label() {
		return submodule_label;
	}

	public void setSubmodule_label(String submodule_label) {
		this.submodule_label = submodule_label;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}
	
	public String getConfig_identifier() {
		return config_identifier;
	}

	public void setConfig_identifier(String config_identifier) {
		this.config_identifier = config_identifier;
	}

	String gots_id;
	String name;
	String pipeline_name;
	String submodule_name;
	String process_name;
	String process_name_label;
	String attribute_name;
	String attribute_value;
	String attribute_type;
	String description;
	String label;
	String submodule_label;
	String validation;
	String config_identifier;
	

}
