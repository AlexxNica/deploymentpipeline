/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline;

import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.att.cicd.deploymentpipeline.service.AdminServices;
import com.att.cicd.deploymentpipeline.service.CamundaServices;
import com.att.cicd.deploymentpipeline.service.DeploymentPipelineOnboardingService;
import com.att.cicd.deploymentpipeline.service.DeploymentPipelineReportingService;
import com.att.cicd.deploymentpipeline.service.DeploymentPipelineRestService;
import com.att.cicd.deploymentpipeline.service.LoginService;
import com.att.cicd.deploymentpipeline.service.PipelineNotificationService;
import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@ApplicationPath("/")
public class JerseyConfiguration extends ResourceConfig {
	private static final Logger log = Logger.getLogger(JerseyConfiguration.class.getName());

	@Autowired
	public JerseyConfiguration(ObjectMapper lrf) {
//		packages("com.att.cicd.deploymentpipeline.service");
		register(CamundaServices.class);
		register(DeploymentPipelineOnboardingService.class);
		register(DeploymentPipelineReportingService.class);
		register(DeploymentPipelineRestService.class);
		register(PipelineNotificationService.class);
		register(AdminServices.class);
		register(LoginService.class);
		register(UserInfoService.class);
		  // register endpoints
//	    packages("com.shengwang.demo");
	    // register jackson for json 
	    register(new ObjectMapperContextResolver(lrf));
	  }
	 
	  @Provider
	  public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
	 
	    private final ObjectMapper mapper;
	 
	    public ObjectMapperContextResolver(ObjectMapper mapper) {
	      this.mapper = mapper;
	    }
	 
	    @Override
	    public ObjectMapper getContext(Class<?> type) {
	      return mapper;
	    }
	  }
}