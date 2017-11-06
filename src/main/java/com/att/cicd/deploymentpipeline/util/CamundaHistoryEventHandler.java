/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.att.ajsc.camunda.core.AttCamundaService;


public class CamundaHistoryEventHandler extends DbHistoryEventHandler implements HistoryEventHandler {
	
	private static final Logger LOGGER = Logger.getLogger(CamundaHistoryEventHandler.class);

	 
	List<HistoricActivityInstanceEventEntity> historyEventList = new ArrayList<HistoricActivityInstanceEventEntity>();
	  
	  @Override
	  public void handleEvent(HistoryEvent historyEvent) 
	  {
	    // create db entry
	    super.handleEvent(historyEvent);
	    
	    // Create log Entry
	    HistoricActivityInstanceEventEntity activityEvent = null;
	    
	    Date startDate = null;
    	Date endDate = null;
	    if(historyEvent != null && historyEvent instanceof HistoricActivityInstanceEventEntity )
	    {
	    	activityEvent = (HistoricActivityInstanceEventEntity) historyEvent;
	    	 if(activityEvent != null)
	 	    {
	 	    	 startDate = activityEvent.getStartTime();
	 	    	 endDate = activityEvent.getEndTime();
	 	    	
	 	    	 if(activityEvent.getActivityType() != null && (activityEvent.getActivityType().equalsIgnoreCase("manualTask")
	 	    			  || activityEvent.getActivityType().equalsIgnoreCase("userTask")))
	 	    	 {
	 	    		historyEventList.clear();
	 	    	 }
	 	    	if(startDate != null && endDate != null)
		 	    {
	 	    		LOGGER.info(activityEvent + ":" + activityEvent.getProcessInstanceId()+ ":" +  activityEvent.getActivityType() );
		 	    	if(activityEvent != null && activityEvent.getProcessInstanceId() != null)
		 	    	{
		 	    		historyEventList.add(activityEvent);
	 	    			if( activityEvent.getActivityType() != null && activityEvent.getActivityType().equalsIgnoreCase("noneEndEvent"))
	 	    			{
	 	    				if(AttCamundaService.getHttpRequest() != null)
 			 	    		{
 	    					  for(HistoricActivityInstanceEventEntity actiEvent : historyEventList)
			 	    		    {
 	    						 int activityInstanceState = actiEvent.getActivityInstanceState();
		 	    		    	 String activityState = getActivityInstanceState(activityInstanceState);
 	    			 	    	 //  resolve null pointer exception if actiEvent.getActivityName()
 	    			 	    	 String serviceName = actiEvent.getActivityName();
 	    			 	    	 if ( serviceName == null ) {
 	    			 	    		serviceName = "UNKNOWN";
 	    			 	    	 }
			 	    		    }
 	    					 LOGGER.info("Call performacne is success");
 			 	    		}
	 	    				else
 			 	    		{
			 	    		    RestTemplate restTemplate = new RestTemplate();
			 	    		    String histEvents = "";
			 	    		    for(HistoricActivityInstanceEventEntity actiEvent : historyEventList)
			 	    		    {
			 	    		    	 int activityInstanceState = actiEvent.getActivityInstanceState();
			 	    		    	 String activityState = getActivityInstanceState(activityInstanceState);
			 	    		    	histEvents = histEvents + actiEvent.getActivityName() + ":" + String.valueOf(actiEvent.getStartTime().getTime() )+ ":" 
			 	    		    			+ String.valueOf(actiEvent.getEndTime().getTime()) + ":" + activityState + "," ;
			 	    		    	LOGGER.info(actiEvent);
			 	    		    }
			 	    			Map<String,String> restValues = new HashMap<String,String>();
			 	    			restValues.put("procInstId", activityEvent.getProcessInstanceId());
			 	    			restValues.put("histEventList",histEvents);
			 	    			// TODO: this should be fixed - put in temporary solution with existing sysprop and vars - why are we calling our own service?
			 	    			String port = System.getProperty("server.port");
			 	    			try
			 	    			{
			 	    				String host = (String) System.getProperty("uihost");
			 	    				ResponseEntity<String> resp = restTemplate.getForEntity(host+":" + port + "/services/deploymentpipeline3/v1/jaxrsExample/log/createLogHist/{procInstId}/{histEventList}", String.class, restValues);
			 	    			}
			 	    			catch(Exception e)
			 	    			{
			 	    				LOGGER.info("Not able to call restService");
			 	    			}
 			 	    		}
	 	    				historyEventList.clear();
	 	    			}
		 	    	}
		 	    }
	 	    }
	    }
	  }


	  private String getActivityInstanceState(int activityInstanceState)
	  {
		   String activityState = "Default";
		  if(activityInstanceState == 1)
	    	 {
	    		 activityState = "Complete";
	    	 }
	    	 else if(activityInstanceState == 2)
	    	 {
	    		 activityState = "Cancelled";
	    	 }
		  return activityState;
	  }

	}