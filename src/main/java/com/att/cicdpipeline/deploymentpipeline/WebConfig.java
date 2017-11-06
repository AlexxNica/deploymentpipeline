/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.att.cadi.filter.CadiFilter;
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(CamundaBpmAutoConfiguration.class)
public class WebConfig {

	@Autowired
	ServletContext servletContext;

	@Value("${com.att.ajsc.camunda.contextPath:/camunda}")
	private String CAMUNDA_SUFFIX;

	private static final EnumSet<DispatcherType> DISPATCHER_TYPES = EnumSet.of(DispatcherType.REQUEST);

	@Bean
	public WebMvcConfigurerAdapter forwardToIndex() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addViewControllers(ViewControllerRegistry registry) {
				registry.addViewController("/swagger").setViewName("redirect:/icd/index.html");
				registry.addViewController("/icd/").setViewName("redirect:/icd/index.html");
			}
		};
	}

	@Bean
	public FilterRegistrationBean filterRegistration() {
		registerFilter("Authentication Filter", AuthenticationFilter.class, CAMUNDA_SUFFIX+"/*");


		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(ecoFilter());
		registration.addUrlPatterns("/v1/*");
		registration.setName("ecoFilter");
		registration.setOrder(0);
		return registration;

	}

	@Bean(name = "ecoFilter")
	public Filter ecoFilter() {

		return new EcoFilter();

	}


	private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final String... urlPatterns) {
		return registerFilter(filterName, filterClass, null, urlPatterns);
	}

	private FilterRegistration registerFilter(final String filterName, final Class<? extends Filter> filterClass, final Map<String, String> initParameters,
			final String... urlPatterns) {
		FilterRegistration filterRegistration = servletContext.getFilterRegistration(filterName);

		if (filterRegistration == null) {
			filterRegistration = servletContext.addFilter(filterName, filterClass);
			filterRegistration.addMappingForUrlPatterns(DISPATCHER_TYPES, true, urlPatterns);

			if (initParameters != null) {
				filterRegistration.setInitParameters(initParameters);
			}

		}

		return filterRegistration;
	}
}