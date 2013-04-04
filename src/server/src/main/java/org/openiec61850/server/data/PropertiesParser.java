/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of openIEC61850.
 * For more information visit http://www.openmuc.org 
 *
 * openIEC61850 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * openIEC61850 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openiec61850.server.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

public class PropertiesParser {

	Map<String, String> dataSourceClassNamesByAccessPoint = null;
	String generalDataSource = null;
	String sclFilePath = null;
	public String[] sapNames = null;
	public Properties properties;

	public void parse(String propFilePath) throws IOException, ConfigurationException {

		loadProperties(propFilePath);

		generalDataSource = properties.getProperty("openIEC61850.accessPoints.dataSource");

		sclFilePath = properties.getProperty("openIEC61850.sclFile");
		if (sclFilePath == null) {
			throw new ConfigurationException("sclFile not configured");
		}

		buildSourceMap();

	}

	private void loadProperties(String propFilePath) throws FileNotFoundException, IOException {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propFilePath);
		} catch (FileNotFoundException e) {
			// file was not found in the file system now check if file can be
			// found in the classpath
			inputStream = getClass().getClassLoader().getResourceAsStream(propFilePath);
			if (inputStream == null) {
				throw e;
			}
		}
		try {
			properties = new Properties();
			properties.load(inputStream);
		} finally {
			inputStream.close();
		}
	}

	private void buildSourceMap() throws ConfigurationException {

		dataSourceClassNamesByAccessPoint = new HashMap<String, String>();
		Enumeration<?> propNames = properties.propertyNames();

		while (propNames.hasMoreElements()) {
			String propertyKey = (String) propNames.nextElement();

			if (propertyKey.startsWith("openIEC61850.accessPoint.") && propertyKey.endsWith(".dataSource")
					&& propertyKey.length() > 36) {
				String accessPointName = propertyKey.substring(25, propertyKey.length() - 11);

				if (accessPointName == "") {
					throw new ConfigurationException("no source configured");
				}

				dataSourceClassNamesByAccessPoint.put(accessPointName, properties.getProperty(propertyKey));
			}
			else if (propertyKey.startsWith("openIEC61850.serverSAPs")) {
				String sapNamesString = properties.getProperty(propertyKey);
				if (sapNamesString == null || sapNamesString.equals("")) {
					throw new ConfigurationException("openIEC61850.serverSAPs are not configured");
				}
				sapNames = properties.getProperty(propertyKey).split("\\.");
			}
		}
	}

	/**
	 * Will return the name of the DataSource class configured for the given
	 * AccessPoint. If no DataSource was configured it will return null.
	 * 
	 * @param AccessPointName
	 */
	public String getDataSourceClassName(String AccessPointName) {
		String dataSourceClassName = dataSourceClassNamesByAccessPoint.get(AccessPointName);

		for (String dsc : dataSourceClassNamesByAccessPoint.keySet()) {
			System.out.println("dsc: " + dsc);
		}

		if (dataSourceClassName == null) {
			return generalDataSource;
		}
		return dataSourceClassName;
	}

	public String getAPName(String sapPropName) {
		return properties.getProperty("openIEC61850.serverSAP." + sapPropName + ".accessPoint");
	}

	public String getSclFilePath() {
		return sclFilePath;
	}

}
