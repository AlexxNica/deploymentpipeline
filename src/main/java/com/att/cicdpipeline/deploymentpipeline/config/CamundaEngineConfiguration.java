/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline.config;

import javax.sql.DataSource;

import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.att.ajsc.camunda.util.CamundaHistoryEventHandler;

@Configuration
public class CamundaEngineConfiguration {

	 /* 
	 * Camunda Identity databse DataSource configuration
	 */
	  @Primary
	  @Bean(name="camundabpmajsc6DataSource")
	  @ConfigurationProperties(prefix="spring.datasource")
	  public DataSource dataSource() {
		  return DataSourceBuilder
			        .create()
			        .build();
	  }
	  
	  @Bean(name="historyEventHandler")
	  public static HistoryEventHandler HistoryEventHandlerConfiguration() {	    
		  return new CamundaHistoryEventHandler();
	  }
	  
	  @Bean(name="jobExecutor") 
	  public static JobExecutor DefaultJobExecutor() { 
		  JobExecutor je = new DefaultJobExecutor(); 
		  je.setLockTimeInMillis(300000);
		  je.setWaitTimeInMillis(5000);
		  je.setMaxWait(10000);
		  return je; 
	  }
	 
	  
	  /*@Bean	 
	  public static CamundaHistoryConfiguration camundaHistoryConfiguration() {
	    //return new DefaultHistoryConfiguration();
		  return new CustomHistoryConfiguration();
	  }
	 */
	  
	  /* 
	   * configure your additional secondary database 
	   */
	
	  /*@ConfigurationProperties(prefix="datasource.secondary")
	  public DataSource secondaryDataSource() {
	    return DataSourceBuilder.create().build();
	  }*/
		 
	  /*You just have to provide one or more beans implementing the 
	   * org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin interface 
	   * (or extend from org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration). 
	   * So if you want that your configuration is applied before the default configurations add a @Order(Ordering.DEFAULT_ORDER - 1)
	   * If you want that your configuration is applied after the default configurations add a @Order(Ordering.DEFAULT_ORDER + 1) annotation to your class.
	   */
	 /* @Bean
      @Order(Ordering.DEFAULT_ORDER + 1)
      public static ProcessEnginePlugin myCustomConfiguration() {
              return new MyCustomConfiguration();
      }*/
}