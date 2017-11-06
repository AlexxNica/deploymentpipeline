/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
/**
 * 
 */
package com.att.cicd.deploymentpipeline.util;

/**
 *
 */


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class SystemPropertiesLoader {

	public static void addSystemProperties() {

		InputStream input = null;
		Properties properties = new Properties();

		try {

			if (System.getenv("system_properties_path") != null) {
				input = new FileInputStream(System.getenv("system_properties_path"));
			} else {
				input = new SystemPropertiesLoader().getClass().getResourceAsStream("/system.properties");
			}
			properties.load(input);

			Enumeration<Object> keys = properties.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = properties.getProperty(key);
				System.setProperty(key, value);
			}
		}

		catch (Exception e) {

			if (System.getenv("system_properties_path") != null) {
				System.out.println("error loading the properties from the system properties path "
						+ System.getenv("system_properties_path"));
			} else {
				System.out.println("error loading the properties from system.properties ");
			}

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					System.out.println("error loading the properties from system.properties ");
				}
			}
		}

		if (System.getenv("com_att_eelf_logging_path") != null) {
			System.setProperty("com.att.eelf.logging.path", System.getenv("com_att_eelf_logging_path"));
			System.setProperty("logging.config", System.getenv("com_att_eelf_logging_path") + "/logback.xml");
		}

	}

}

