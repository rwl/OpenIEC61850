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
//package org.openiec61850.server.report;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.openiec61850.common.ServiceError;
////import org.openiec61850.common.model.DataContainer;
//import org.openiec61850.common.model.DataObject;
//import org.openiec61850.common.model.DataSet;
//import org.openiec61850.common.model.LogicalDevice;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.report.ControlBlock;
//import org.openiec61850.common.model.report.DataTriggerable;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.DataSource;
//import org.openiec61850.server.data.DataSourceFactory;
//import org.openiec61850.server.data.TriggeringDataSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Abstract superclass of {@link org.openiec61850.server.report.ReportEngine}
// * and {@link org.openiec61850.server.log.LogEngine} that contains code common
// * to both engines
// * 
// * @author bertram
// */
//public abstract class ControlBlockHandler implements DataTriggerable {
//
//	/**
//	 * Integrity Reports/Logs are created, when
//	 * {@link TriggerConditions#integrity} is set and
//	 * {@link ControlBlock#intgPd} is not 0. {@link ControlBlock#intgPd}
//	 * determines the interval when the complete {@link DataSet} of the
//	 * {@link ControlBlock} is reported/logged
//	 * 
//	 * @author bertram
//	 */
//	public class IntegrityReport {
//		ControlBlock rcb;
//		AccessPoint accessPoint;
//		long nextReport = System.currentTimeMillis();
//
//		public IntegrityReport(ControlBlock rcb, AccessPoint accessPoint) {
//			this.rcb = rcb;
//			this.accessPoint = accessPoint;
//		}
//
//		public ControlBlock getControlBlock() {
//			return rcb;
//		}
//
//		public AccessPoint getAccessPoint() {
//			return accessPoint;
//		}
//
//		public long getNextIntegrityReport() {
//			return nextReport;
//		}
//
//		public void setNextIntegrityReport(long l) {
//			nextReport = l;
//		}
//	}
//
//	protected List<IntegrityReport> integrityReports = new ArrayList<IntegrityReport>();
//	private final List<AccessPoint> accessPoints;
//	private static Logger logger = LoggerFactory.getLogger(IntegrityReport.class);
//	private final Map<String, ControlBlock> controlBlocks = new HashMap<String, ControlBlock>();
//
//	public ControlBlockHandler(List<AccessPoint> accessPoints) {
//		this.accessPoints = accessPoints;
//	}
//
//	/**
//	 * Look for {@link ControlBlock} in all {@link LogicalNode} and setup
//	 * reporting/logging
//	 * 
//	 * @param accessPoints
//	 */
//	private void setup(List<AccessPoint> accessPoints) {
//		for (AccessPoint accessPoint : accessPoints) {
//			for (LogicalDevice logicalDevice : accessPoint.getServer().getLDevices()) {
//				for (LogicalNode logicalNode : logicalDevice.getLNodes()) {
//					for (ControlBlock lcb : getControlBlocks(logicalNode)) {
//						try {
//							setupControlBlock(accessPoint, lcb);
//						} catch (ConfigurationException e) {
//							logger.error("Error setting up {} {}", lcb.getClass().getSimpleName(), lcb.getId());
//						}
//					}
//				}
//			}
//		}
//		// Start thread for creating integrity reports/logs periodically
//		MonitorThread.theThread().addService(new Runnable() {
//
//			public void run() {
//				try {
//					checkIntegrityReports();
//				} catch (ConfigurationException e) {
//					logger.error("Error creating integrity report");
//				} catch (ServiceError e) {
//					logger.error("Error creating integrity report");
//				}
//			}
//		});
//	}
//
//	protected void setupControlBlock(AccessPoint accessPoint, ControlBlock rcb) throws IllegalArgumentException,
//			ConfigurationException {
//		controlBlocks.put(rcb.getId(), rcb);
//		if (rcb.getTrgOps().isIntegrity()) {
//			integrityReports.add(new IntegrityReport(rcb, accessPoint));
//		}
//		else {
//			for (DataContainer dataObject : rcb.getDataSet().getDataObjects()) {
//				DataSource dataSource = DataSourceFactory.theFactory().getDataSource(accessPoint,
//						dataObject.getReference().toString());
//				if (dataSource instanceof TriggeringDataSource) {
//					// The data source will tell us when trigger conditions
//					// occur
//					((TriggeringDataSource) dataSource).addTrigger(this, rcb.getId(), rcb.getTrgOps(), dataObject);
//				}
//				else {
//					// The data source does not support triggers. Start a
//					// monitor thread
//					// that monitors the data ad acts as the trigger
//					DataMonitor.theMonitor().addMonitor(rcb.getId(), rcb.getTrgOps(), accessPoint, dataObject, this);
//				}
//			}
//		}
//	}
//
//	protected ControlBlock getControlBlock(String id) {
//		return controlBlocks.get(id);
//	}
//
//	/**
//	 * Starts a background {@link Thread} to monitor all {@link DataObject}s of
//	 * all {@link DataSource}es that are not {@link TriggeringDataSource}. For
//	 * {@link TriggeringDataSource} this is not neccessary, the data source will
//	 * care for monitoring and call {@link #dataTrigger(String, DataObject)} on
//	 * value changes.
//	 */
//	public void run() {
//		setup(accessPoints);
//	}
//
//	/**
//	 * Check if integrity reports are to be executed
//	 */
//	protected void checkIntegrityReports() throws ConfigurationException, ServiceError {
//		DataSourceConnection sourceConnection = new DataSourceConnection();
//		for (IntegrityReport integrityReport : integrityReports) {
//			ControlBlock controlBlock = integrityReport.getControlBlock();
//			if (controlBlock.getIntgPd() > 0L) {
//				if (integrityReport.getNextIntegrityReport() < System.currentTimeMillis()) {
//					for (DataContainer dataObject : controlBlock.getDataSet().getDataObjects()) {
//						sourceConnection.handleDataContainer(integrityReport.accessPoint, dataObject, null, false);
//					}
//					dataTrigger(controlBlock.getId(), TriggerConditions.INTEGRITY, controlBlock.getDataSet());
//				}
//				integrityReport.setNextIntegrityReport(System.currentTimeMillis() + controlBlock.getIntgPd());
//			}
//		}
//	}
//
//	/**
//	 * get all {@link ControlBlock} from the {@link LogicalNode} relevant for
//	 * this handler
//	 */
//	protected abstract List<? extends ControlBlock> getControlBlocks(LogicalNode logicalNode);
//
// }