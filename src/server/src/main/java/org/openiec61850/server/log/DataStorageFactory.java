///*
// * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
// *
// * This file is part of openIEC61850.
// * For more information visit http://www.openmuc.org 
// *
// * openIEC61850 is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 2.1 of the License, or
// * (at your option) any later version.
// *
// * openIEC61850 is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with openIEC61850.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//package org.openiec61850.server.log;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Properties;
//import java.util.logging.Logger;
//
//import org.openiec61850.server.data.ConfigurationException;
//
///**
// * TODO: put instantiation of classes into a delegate to be able to work with
// * OSGi
// * 
// * @author bertram
// */
//public class DataStorageFactory {
//
//	private static DataStorageFactory instance = new DataStorageFactory();
//	private static final String configFileName = "iec61850storage.properties";
//	private DataStorage dataStorage;
//	private Properties config;
//	private static Logger logger = LoggerFactory.getLogger(DataStorageFactory.class);
//
//	public static DataStorageFactory theFactory() {
//		return instance;
//	}
//
//	/**
//	 * For testing
//	 */
//	public void setConfig(Properties config) {
//		this.config = config;
//	}
//
//	protected Properties readConfig() throws ConfigurationException {
//		try {
//			InputStream is = getClass().getResourceAsStream(configFileName);
//			if (is == null) {
//				throw new ConfigurationException("Cannot find " + configFileName);
//			}
//			Properties props = new Properties();
//			props.load(is);
//			return props;
//		} catch (IOException e) {
//			throw new ConfigurationException("Error reading " + configFileName, e);
//		}
//	}
//
//	protected void createDataStorage() throws ConfigurationException {
//		if (config == null) {
//			config = readConfig();
//		}
//		String classname = config.getProperty("class");
//		if (classname == null) {
//			throw new ConfigurationException(configFileName + " does not contain \"class\"-Property");
//		}
//		try {
//			Object object = Class.forName(classname).newInstance();
//			if (object instanceof DataStorage) {
//				dataStorage = (DataStorage) object;
//				dataStorage.init();
//			}
//			else {
//				throw new ConfigurationException(classname + " does not implement " + DataStorage.class.getName());
//			}
//		} catch (InstantiationException e) {
//			throw new ConfigurationException("Error instantiating " + classname, e);
//		} catch (IllegalAccessException e) {
//			throw new ConfigurationException("Error instantiating " + classname, e);
//		} catch (ClassNotFoundException e) {
//			throw new ConfigurationException("Error instantiating " + classname, e);
//		}
//	}
//
//	public DataStorage getDataStorage() {
//		if (dataStorage == null) {
//			try {
//				createDataStorage();
//			} catch (ConfigurationException exc) {
//				logger.debug("error creating DataStorage {}, using default {}", exc.getMessage(),
//						InMemoryDataStorage.class.getSimpleName());
//				dataStorage = new InMemoryDataStorage();
//			}
//		}
//		return dataStorage;
//	}
// }
