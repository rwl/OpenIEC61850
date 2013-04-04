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
//import java.util.Collection;
//import java.util.List;
//
//import org.openiec61850.common.model.FunctionalConstraint;
//import org.openiec61850.common.model.LogicalNode;
//import org.openiec61850.common.model.ModelNode;
//import org.openiec61850.common.model.ObjectReference;
//import org.openiec61850.common.model.basictypes.Timestamp;
//import org.openiec61850.common.model.log.Log;
//import org.openiec61850.common.model.log.LogControlBlock;
//import org.openiec61850.common.model.log.LogEntry;
//import org.openiec61850.common.model.log.LogEntryData;
//import org.openiec61850.common.model.report.ControlBlock;
//import org.openiec61850.common.model.report.TriggerConditions;
//import org.openiec61850.server.AccessPoint;
//import org.openiec61850.server.data.ConfigurationException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class LogEngine extends ControlBlockHandler {
//
//	private static Logger logger = LoggerFactory.getLogger(LogEngine.class);
//
//	public LogEngine(List<AccessPoint> accessPoints) {
//		super(accessPoints);
//	}
//
//	@Override
//	protected List<? extends ControlBlock> getControlBlocks(LogicalNode logicalNode) {
//		return logicalNode.getLogControlBlocks();
//	}
//
//	public void dataTrigger(String triggerId, TriggerConditions reason, DataContainer dataObject) {
//		try {
//			LogControlBlock lcb = (LogControlBlock) getControlBlock(triggerId);
//			LogEntry log = generateLogEntry(lcb, reason, dataObject);
//			writeLogEntry(log);
//		} catch (ConfigurationException e) {
//			logger.error("error writing log {}", e.getMessage());
//		}
//	}
//
//	private LogEntry generateLogEntry(LogControlBlock lcb, TriggerConditions trgOps, DataContainer dataAttributes) {
//		LogEntry logEntry = new LogEntry();
//		logEntry.setLogRef(lcb.getLogRef());
//		// logEntry.setEntryTime(new Timestamp());
//		for (DataContainer da : dataAttributes.recursive()) {
//			LogEntryData led = new LogEntryData();
//			led.setDataRef(da.getReference().toString());
//			led.setReasonCode(trgOps);
//			led.setValue(da.getValue().getValue());
//			logEntry.addLogEntryData(led);
//		}
//		return logEntry;
//	}
//
//	private void writeLogEntry(LogEntry log) throws ConfigurationException {
//		DataStorageFactory.theFactory().getDataStorage().writeLogEntry(log);
//	}
//
//	public Log queryLogByTime(ObjectReference logReference, Timestamp rangeStartTime, Timestamp rangeStopTime)
//			throws ConfigurationException {
//		Collection<LogEntry> entries = DataStorageFactory.theFactory().getDataStorage()
//				.queryLogByTime(logReference, rangeStartTime, rangeStopTime);
//		return createLog(logReference, entries);
//	}
//
//	private Log createLog(ObjectReference logReference, Collection<LogEntry> entries) {
//		Log log = new Log();
//		log.setLogName(logReference.getName());
//		log.setLogRef(logReference);
//		log.getEntries().addAll(entries);
//		for (LogEntry le : entries) {
//			if (log.getNewEntr() == 0L || log.getNewEntr() > le.getEntryId()) {
//				log.setNewEntr(le.getEntryId());
//				log.setNewEntrTm(le.getEntryTime());
//			}
//			if (log.getOldEntr() == 0L || log.getOldEntr() < le.getEntryId()) {
//				log.setOldEntr(le.getEntryId());
//				log.setOldEntrTm(le.getEntryTime());
//			}
//		}
//		return log;
//	}
//
//	public Log queryLogAfter(ObjectReference logReference, Timestamp rangeStartTime, long startEntry)
//			throws ConfigurationException {
//		Collection<LogEntry> entries = DataStorageFactory.theFactory().getDataStorage()
//				.queryLogAfter(logReference, rangeStartTime, startEntry);
//		return createLog(logReference, entries);
//	}
//
//	/**
//	 * Not yet implemented
//	 */
//	public LogStatusValues getLogStatusValues(ObjectReference logReference, FunctionalConstraint fc) {
//		throw new UnsupportedOperationException("Not yet implemented");
//	}
//
//	public void dataTrigger(String triggerId, TriggerConditions reason, ModelNode dataObject) {
//		// TODO Auto-generated method stub
//
//	}
// }

