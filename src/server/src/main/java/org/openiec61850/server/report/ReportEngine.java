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
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.openiec61850.common.ServiceError;
//import org.openiec61850.common.model.DataContainer;
//import org.openiec61850.common.model.DataSet;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.basictypes.Timestamp;
//import org.openiec61850.common.model.report.BufferedReportControlBlock;
//import org.openiec61850.common.model.report.ClientReportState;
//import org.openiec61850.common.model.report.ControlBlock;
//import org.openiec61850.common.model.report.Report;
//import org.openiec61850.common.model.report.ReportControlBlock;
//import org.openiec61850.common.model.report.ReportEntryData;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.DataSource;
//import org.openiec61850.server.data.TriggeringDataSource;
//import org.openiec61850.server.log.DataStorage;
//import org.openiec61850.server.log.DataStorageFactory;
//import org.openiec61850.server.scsm.SCSMConnectionHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * The {@link ReportEngine} handles the reporting functionality. It is started
// * calling the {@link #run()}-Method. It looks for {@link ReportControlBlock}s
// * configured in the SCL file and sets up connections to {@link DataSource}s for
// * all {@link DataSet}s referenced in the {@link ReportControlBlock}s.
// * 
// * There are two types of {@link DataSource}s. For simple {@link DataSource}s,
// * the {@link ReportEngine} starts a background {@link Thread} in its
// * {@link #run}-Method to monitor the configured {@link TriggerConditions}. For
// * {@link TriggeringDataSource}s the {@link ReportEngine} registers all reports
// * at the {@link TriggeringDataSource} and just waits for triggers. In this case
// * the {@link TriggeringDataSource} controls the monitoring and triggering
// * process.
// * 
// * If a client wants to receive {@link Report}s, the client connection must
// * register by using the method
// * {@link #registerClient(SCSMConnectionHandler, String)}.
// * 
// * @author bertram
// */
//public class ReportEngine extends ControlBlockHandler {
//	private static Logger logger = LoggerFactory.getLogger(ReportEngine.class);
//	private long unbufferedEntryId = 0L;
//	/**
//	 * Map of registered clients.
//	 */
//	private final Map<SCSMConnectionHandler, Map<String, ClientReportState>> clients = new HashMap<SCSMConnectionHandler, Map<String, ClientReportState>>();
//
//	public ReportEngine(List<AccessPoint> accessPoints) {
//		super(accessPoints);
//	}
//
//	@Override
//	protected List<? extends ControlBlock> getControlBlocks(LogicalNode logicalNode) {
//		return logicalNode.getReportControlBlocks();
//	}
//
//	/**
//	 * Data values have been changed/updated and need to be appended to a report
//	 */
//	public void dataTrigger(String reportId, TriggerConditions reason, DataContainer dataObject) {
//		ReportControlBlock rcb = (ReportControlBlock) getControlBlock(reportId);
//		if (rcb == null) {
//			logger.error("dataTrigger called for invalid report id: {}", reportId);
//			return;
//		}
//		Report report = generateReport(rcb, reason, dataObject);
//		DataStorage dataStorage = DataStorageFactory.theFactory().getDataStorage();
//		if (rcb instanceof BufferedReportControlBlock) {
//			dataStorage.writeReport(report);
//		}
//		else {
//			report.setEntryId(unbufferedEntryId++);
//		}
//		List<ClientReportState> persistentStates = dataStorage.getClientReportStates();
//		for (SCSMConnectionHandler client : clients.keySet()) {
//			Map<String, ClientReportState> clientMap = clients.get(client);
//			if (clientMap.containsKey(reportId)) {
//				ClientReportState state = null;
//				if (rcb instanceof BufferedReportControlBlock) {
//					state = findClientReportState(reportId, persistentStates, client);
//				}
//				else {
//					state = clientMap.get(reportId);
//				}
//				sendReport(report, client, state);
//			}
//		}
//		dataStorage.writeClientReportStates(persistentStates);
//	}
//
//	private ClientReportState findClientReportState(String reportId, List<ClientReportState> persistentStates,
//			SCSMConnectionHandler client) {
//		for (ClientReportState searchState : persistentStates) {
//			if (searchState.matches(reportId, client.getClientId())) {
//				return searchState;
//			}
//		}
//		ClientReportState state = new ClientReportState(reportId, client.getClientId());
//		persistentStates.add(state);
//		return state;
//	}
//
//	private void sendReport(Report report, SCSMConnectionHandler client, ClientReportState state) {
//		try {
//			report.setSqNum(state.getSqNum());
//			client.sendReport(report);
//			state.setSqNum(state.getSqNum() + 1);
//			state.setReportEntryId(report.getEntryId());
//		} catch (ServiceError e) {
//			logger.warn("cannot send report to client {}: {}", client.getClientId(), e.getMessage());
//		}
//	}
//
//	private Report generateReport(ReportControlBlock rcb, TriggerConditions reason, DataContainer dataObject) {
//		Report report = new Report();
//		report.setRptId(rcb.getRptID());
//		report.setConvRev(rcb.getConvRef());
//		report.setDataSet(rcb.getDataSet().getReference().toString());
//		report.setOptFlds(rcb.getOptFlds());
//		report.setTimeOfEntry(new Timestamp());
//		for (DataContainer da : dataObject.recursive()) {
//			ReportEntryData entryData = new ReportEntryData();
//			entryData.setDataRef(da.getReference().toString());
//			entryData.setReasonCode(reason);
//			if (da.getValue() == null) {
//				entryData.setValue(null);
//			}
//			else {
//				entryData.setValue(da.getValue().getValue());
//			}
//			report.getEntryData().add(entryData);
//		}
//		return report;
//	}
//
//	public void registerClient(SCSMConnectionHandler client, String reportId) throws ServiceError {
//		ControlBlock rcb = getControlBlock(reportId);
//		if (rcb == null) {
//			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "invalid report id " + reportId);
//		}
//		Map<String, ClientReportState> clientMap = clients.get(client);
//		if (clientMap == null) {
//			clientMap = new HashMap<String, ClientReportState>();
//			clients.put(client, clientMap);
//		}
//		clientMap.put(reportId, new ClientReportState(reportId, client.getClientId()));
//		if (rcb instanceof BufferedReportControlBlock) {
//			sendBufferedReports(reportId, client);
//		}
//	}
//
//	public void deregisterClient(SCSMConnectionHandler client, String reportId) {
//		Map<String, ClientReportState> clientMap = clients.get(client);
//		clientMap.remove(reportId);
//	}
//
//	private void sendBufferedReports(String reportId, SCSMConnectionHandler client) {
//		DataStorage dataStorage = DataStorageFactory.theFactory().getDataStorage();
//		List<ClientReportState> persistentStates = dataStorage.getClientReportStates();
//		ClientReportState state = findClientReportState(reportId, persistentStates, client);
//		for (Report report : dataStorage.readReport(reportId, state.getReportEntryId())) {
//			sendReport(report, client, state);
//		}
//		dataStorage.writeClientReportStates(persistentStates);
//	}
// }
