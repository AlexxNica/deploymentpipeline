/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicdpipeline.deploymentpipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.att.cicd.deploymentpipeline.service.UserInfoService;
import com.att.cicd.deploymentpipeline.util.AAFConnector;
import com.att.cicd.deploymentpipeline.util.CamundaConnector;

public class EcoFilter implements Filter{

	/**
	 * Name of the cookie that holds the username
	 */
	private static final String USER_COOKIE_NAME = "username";
	private static final String EMAIL_COOKIE_NAME = "email";

	private static final Logger LOGGER = Logger.getLogger(EcoFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if(request instanceof RequestWrapper) {
			//do nothing. request has already been filtered
			return;
		} else if(request instanceof HttpServletRequest) {
			HttpServletRequest httpReq = (HttpServletRequest) request;
			Cookie[] cookies = httpReq.getCookies();
			String user = "";
			String email = "";
			if(cookies != null)
			{
				for (int i = 0; i < cookies.length; i++) {
					switch(cookies[i].getName()) {
					case USER_COOKIE_NAME:
						user = cookies[i].getValue();
						break;
					case EMAIL_COOKIE_NAME:
						email = cookies[i].getValue();
						break;
					}
				}
				if(user.length()==0) {
					user = UserInfoService.getUsername(email);
					if(user.equals(UserInfoService.NO_NAME)) {
						user = "";
					}
				}
			}

			List<String> roles = null;
			if(user.length()>0) {
				String permPath = "/authz/perms/user/" + UserInfoService.fullyQualify(user);
				String perms = AAFConnector.get(permPath);
				roles = parsePerms(perms);
			} else {
				LOGGER.warn("User not found");
				roles = new ArrayList<String>();
			}
			chain.doFilter(new RequestWrapper(user, roles, httpReq), response);
			return; //nothing else to do with this request and response
		}
		//this filter isn't applicable to the request. Use the next filter if one exists.
		chain.doFilter(request, response);	
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	private static List<String> parsePerms(String perms) {
		List<String> roles = new ArrayList<String>();

		//CamundaConnector.java has some static json parsing methods.
		//This code here has nothing to do with Camunda.
		Map top = CamundaConnector.jsonToMap(perms);
		ArrayList perm = CamundaConnector.getJsonArray(top, "perm");
		for(int i=0; i<perm.size(); i++) {
			String type = CamundaConnector.getJsonString((Map) perm.get(i), "type");
			String instance = CamundaConnector.getJsonString((Map) perm.get(i), "instance");
			String action = CamundaConnector.getJsonString((Map) perm.get(i), "action");
			roles.add(type+"|"+instance+"|"+action);
		}
		return roles;
	}

}
