/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of openIEC61850.
 * For more information visit http://www.openiec61850.org or http://www.openmuc.org
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
//package org.openiec61850.server.report;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.DataObject;
//import org.openiec61850.common.model.report.DataTriggerable;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.DataSource;
//
//public class DataMonitor implements Runnable {
//
//	/**
//	 * Wrapper for {@link DataObject}s whose {@link DataSource} is not a
//	 * {@link TriggeringDataSource}. These {@link DataObject}s are to be
//	 * monitored actively.
//	 * 
//	 * @author bertram
//	 */
//	private class DataCheck {
//		DataContainer dataObject;
//		AccessPoint accessPoint;
//		DataTriggerable triggerable;
//		String triggerId;
//		TriggerConditions triggerCondition;
//
//		public DataCheck(String triggerId, TriggerConditions trgOps, AccessPoint accessPoint, DataContainer dataObject,
//				DataTriggerable triggerable) {
//			this.triggerId = triggerId;
//			this.triggerCondition = trgOps;
//			this.accessPoint = accessPoint;
//			this.dataObject = dataObject;
//			this.triggerable = triggerable;
//		}
//	}
//
//	private static Logger logger = LoggerFactory.getLogger(DataMonitor.class);
//	private static DataMonitor instance = new DataMonitor();
//	private final List<DataCheck> dataChecks = new ArrayList<DataCheck>();
//	private long nextTriggerCheck = 0L;
//
//	protected DataMonitor() {
//		MonitorThread.theThread().addService(this);
//	}
//
//	public static DataMonitor theMonitor() {
//		return instance;
//	}
//
//	/**
//	 * Checks {@link DataObject} to be reported that does not implement
//	 * {@link TriggeringDataSource} by calling
//	 * {@link DataSource#readValues(DataObject)} on that object and checks if
//	 * {@link TriggerConditions} match.
//	 * 
//	 * @throws ConfigurationException
//	 */
//	private void checkForTrigger(DataCheck reportCheck) throws ServiceError, ConfigurationException {
//		DataContainer originalDataObject = reportCheck.dataObject;
//		TriggerConditions trigger = new DataSourceConnection().handleDataContainer(reportCheck.accessPoint,
//				originalDataObject, null, false);
//		if (trigger.matches(reportCheck.triggerCondition)) {
//			reportCheck.triggerable.dataTrigger(reportCheck.triggerId, trigger, originalDataObject);
//		}
//		else if (reportCheck.triggerCondition.isDataUpdate()) {
//			reportCheck.triggerable.dataTrigger(reportCheck.triggerId, TriggerConditions.DATA_UPDATE,
//					originalDataObject);
//		}
//	}
//
//	public void addMonitor(String triggerId, TriggerConditions trgOps, AccessPoint accessPoint,
//			DataContainer dataObject, DataTriggerable triggerable) {
//		dataChecks.add(new DataCheck(triggerId, trgOps, accessPoint, dataObject, triggerable));
//	}
//
//	public void run() {
//		if (nextTriggerCheck < System.currentTimeMillis()) {
//			for (DataCheck dataCheck : dataChecks) {
//				try {
//					checkForTrigger(dataCheck);
//				} catch (ServiceError e) {
//					logger.error("Error monitoring data for {}", dataCheck.triggerId);
//				} catch (ConfigurationException e) {
//					logger.error("Error monitoring data for {}", dataCheck.triggerId);
//				}
//			}
//			nextTriggerCheck = System.currentTimeMillis();
//		}
//	}
//
// }
