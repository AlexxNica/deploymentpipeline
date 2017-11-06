/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline;

import javax.servlet.Servlet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.log4j.BasicConfigurator;
import org.camunda.bpm.spring.boot.starter.webapp.CamundaBpmWebappAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.webapp.CamundaBpmWebappInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

import com.att.cicd.deploymentpipeline.util.SystemPropertiesLoader;


@SpringBootApplication
@ComponentScan(basePackages = {"com"})
@EnableAutoConfiguration(exclude = {CamundaBpmWebappAutoConfiguration.class, CamundaBpmWebappInitializer.class,HibernateJpaAutoConfiguration.class,JpaRepositoriesAutoConfiguration.class, SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class })
@EnableAsync
public class Application extends SpringBootServletInitializer {

//	private static final String CAMEL_SERVLET_NAME = "CamelServlet";
//	private static final String CAMEL_URL_MAPPING = "/restservices/deploymentpipeline/v1/*";

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		SystemPropertiesLoader.addSystemProperties(); 
		SpringApplication.run(Application.class, args);
	}

//	@Bean
//	public ServletRegistrationBean servletRegistrationBean() {
//		ServletRegistrationBean registration = new ServletRegistrationBean();
//		registration.setName(CAMEL_SERVLET_NAME);
//		registration.setServlet((Servlet) new CamelHttpTransportServlet());
//		Collection<String> urlMappings = new ArrayList<String>();
//		urlMappings.add(CAMEL_URL_MAPPING);
//		registration.setUrlMappings(urlMappings);
//		return registration;
//	}
//
//	@Bean
//	public Client restClient() {
//		return ClientBuilder.newClient();
//	}

}