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
package org.openiec61850;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openiec61850.scl.SclParseException;
import org.openiec61850.scl.SclParser;
import org.openiec61850.server.data.ConfigurationException;
import org.openiec61850.server.data.PropertiesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneServer implements ServerStopListener {

	private static Logger logger = LoggerFactory.getLogger(StandaloneServer.class);

	private List<ServerSAP> serverSAPs = new LinkedList<ServerSAP>();

	public static void main(String[] args) throws IOException, ConfigurationException {
		if (args.length != 1) {
			System.out.println("usage: org.openiec61850.server.Server <properties file>");
			return;
		}

		new StandaloneServer(args[0]);
	}

	public StandaloneServer(String propFilePath) throws IOException, ConfigurationException {

		// TODO use ShutdownHook?
		// Runtime.getRuntime().addShutdownHook(new Thread() {
		// public void run() {
		// if (scsmServerSAPs != null) {
		// for (SCSMServerSAP scsmServerSAP : scsmServerSAPs) {
		// scsmServerSAP.stopListening();
		// }
		// }
		// logger.error("Server was killed.");
		//
		// }
		// });

		PropertiesParser propertiesParser = new PropertiesParser();
		try {
			propertiesParser.parse(propFilePath);
		} catch (Exception e) {
			System.err.println("Error parsing properties file: " + e.getMessage());
			return;
		}

		logger.debug(System.getProperty("java.vm.name"));

		List<AccessPoint> accessPoints = null;
		try {
			accessPoints = SclParser.parse(propertiesParser.getSclFilePath());
		} catch (SclParseException e) {
			System.err.println("Error parsing ICD file: " + e.getMessage());
			return;
		}

		List<AccessPoint> initializedAccessPoints = new LinkedList<AccessPoint>();
		for (AccessPoint accessPoint : accessPoints) {
			String dataSourceClassName = propertiesParser.getDataSourceClassName(accessPoint.getName());
			if (dataSourceClassName == null) {
				System.err.println("No DataSource has been configured for the AccessPoint \"" + accessPoint.getName()
						+ "\" in the properties file.");
			}
			else {
				try {
					accessPoint.initDataSource(propertiesParser.getDataSourceClassName(accessPoint.getName()));
					initializedAccessPoints.add(accessPoint);
				} catch (ConfigurationException e) {
					System.out.println("Error initializing DataSource: " + e.getMessage());
					e.printStackTrace();
					return;
				}
			}
		}

		if (initializedAccessPoints.size() == 0) {
			System.err.println("No AccessPoint could be connected to a DataSource. Will exit.");
			return;
		}

		for (String sapPropName : propertiesParser.sapNames) {
			logger.info("configuring SAP: " + sapPropName);
			ServerSAP serverSAP = new ServerSAP();

			boolean foundAccessPointForSAP = false;
			for (AccessPoint accessPoint : initializedAccessPoints) {
				if (accessPoint.getName().equals(propertiesParser.getAPName(sapPropName))) {
					serverSAP.init(accessPoint, this, propertiesParser.properties, sapPropName);
					foundAccessPointForSAP = true;
				}
			}

			if (foundAccessPointForSAP == false) {
				logger.error("not AccessPoint found");
				return;
			}

			serverSAPs.add(serverSAP);

		}

		for (ServerSAP serverSap : serverSAPs) {
			serverSap.startListening();
		}

		logger.info("IED Server is running");
	}

	public void serverStoppedListening(ServerSAP serverSap) {
		logger.error("An SAP stopped listening");

	}

}
