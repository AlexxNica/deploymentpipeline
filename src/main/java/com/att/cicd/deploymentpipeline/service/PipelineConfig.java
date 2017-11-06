/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.service;

public class PipelineConfig  {
	String name;
	String pipeline_name;
	String seed_display_name;
	String seed_icon;
	String description;
	
	
	public String getSeed_display_name() {
		return seed_display_name;
	}

	public void setSeed_display_name(String seed_display_name) {
		this.seed_display_name = seed_display_name;
	}

	public String getSeed_icon() {
		return seed_icon;
	}

	public void setSeed_icon(String seed_icon) {
		this.seed_icon = seed_icon;
	}

	public String getPipeline_name() {
		return pipeline_name;
	}

	public void setPipeline_name(String pipeline_name) {
		this.pipeline_name = pipeline_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
