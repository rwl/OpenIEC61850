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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openiec61850.common.model.report.OptFields;
import org.openiec61850.common.model.report.Report;
import org.openiec61850.common.model.report.ReportControlBlock;
import org.openiec61850.common.model.report.ReportEntryData;
import org.openiec61850.common.model.report.ReportEntryData.ReasonCode;
import org.openiec61850.common.model.report.UnbufferedReportContrlBlock;
import org.openiec61850.server.data.ConfigurationException;
import org.openiec61850.server.data.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AccessPoint {

	final ServerModel serverModel;
	private final String name;
	DataSource dataSource;
	private Thread dataSourceThread;
	private final Map<Long, Map<String, DataSet>> nonPersistentDS = new LinkedHashMap<Long, Map<String, DataSet>>();

	// TODO is SqNum for both buffered and unbuffered reports?
	private int reportSqNum = 0;
	private int reportSubSqNum = 0;

	// TODO synchronize these lists?
	private final Map<String, List<DataSet>> triggerDchg = new LinkedHashMap<String, List<DataSet>>();
	private final Map<String, List<DataSet>> triggerDupd = new LinkedHashMap<String, List<DataSet>>();
	private final Map<String, List<DataSet>> triggerQchg = new LinkedHashMap<String, List<DataSet>>();

	// DataSet references to report control blocks
	private final Map<DataSet, List<ReportControlBlock>> dsRCB = new LinkedHashMap<DataSet, List<ReportControlBlock>>();

	// lock for data set related functionality
	private Lock dsLock = new ReentrantLock();

	private static Logger logger = LoggerFactory.getLogger(AccessPoint.class);

	public AccessPoint(String name, ServerModel server, ServiceSupport serviceSupport) {
		this.name = name;
		this.serverModel = server;

		// generate trigger table for reports
		// TODO commented out:
		// populateTriggers();

		// this.associations = new ArrayList<Association>();

	}

	public ModelNode getRCB(ObjectReference objRef) throws ServiceError {

		ModelNode lnNode = serverModel.findSubNode(new ObjectReference(objRef.get(1) + "/" + objRef.get(2)), null);

		if (!(lnNode instanceof LogicalNode)) {
			throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "invalid object reference " + objRef);
		}

		Collection<ReportControlBlock> rcbs = ((LogicalNode) lnNode).getReportControlBlocks();

		for (ReportControlBlock rcb : rcbs) {
			if (rcb.getReference().toString().equals(objRef.toString())) {
				return rcb;
			}
		}

		return null;
	}

	// public ModelNode getRCB(Association association, String objRef,
	// FunctionalConstraint fc) throws ServiceError {
	//
	// ModelNode node = serverModel.findSubNode(new ObjectReference(objRef),
	// fc);
	// if (node == null) {
	// throw new ServiceError(ServiceError.INSTANCE_NOT_AVAILABLE,
	// "object not found: " + objRef);
	// }
	//
	// if (!(node instanceof ReportControlBlock)) {
	// throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
	// "invalid object reference " + objRef);
	// }
	//
	// return node.copy();
	//
	// }

	public void initDataSource(String dataSourceClassName) throws ConfigurationException {
		Object instance = null;
		try {
			instance = Class.forName(dataSourceClassName).newInstance();
		} catch (Exception e) {
			throw new ConfigurationException("Exception instantiating DataSource: " + dataSourceClassName + e);
		}
		if (!(instance instanceof DataSource)) {
			throw new ConfigurationException(instance.getClass().getName() + " does not implement the "
					+ DataSource.class.getName() + " interface");
		}

		dataSource = (DataSource) instance;

		/*
		 * provide a link to the AccessPoint and pass in the shared memory (i.e.
		 * rcbs, list of associations, etc)
		 */
		dataSource.initialize(this, triggerDchg.keySet(), triggerQchg.keySet(), triggerDupd.keySet());
		dataSourceThread = new Thread(dataSource);
		dataSourceThread.start();

	}

	public void selectEditSG(String ref, int sGN) throws ServiceError {
		// TODO Auto-generated method stub

	}

	// TODO commented out:
	// public void setURCBValues(UnbufferedReportContrlBlock urcb, ModelNode
	// value) throws ServiceError {
	//
	// ModelNode resv = urcb.getChild("Resv");
	// if (((BOOLEAN) resv).getValue() == true) {// only the client that
	// // reserved it can make changes
	// // need to check if it is the client that reserved it
	//
	// // TODO handle how?
	// // if (urcb.getOwner() != association.getIPAddress()) {
	// // throw new ServiceError(ServiceError.ACCESS_VIOLATION,
	// // "setURCBValues: this client cannot set values");
	// // }
	//
	// }
	//
	// ModelNode child = urcb.getChild(value.getNodeName());
	// if (child == null) {
	// throw new ServiceError(ServiceError.PARAMETER_VALUE_INCONSISTENT,
	// "setURCBValues: report attribute is not valid");
	// }
	//
	// if (value.getNodeName().equals("RptEna")) {
	//
	// if (((BOOLEAN) value).getValue() == true) {
	//
	// if (((BOOLEAN) child).getValue() == false) {
	// // resetting the value of sqNum to 0 each time it has been
	// // enabled from idle
	// INT8U sqNum = (INT8U) urcb.getChild("SqNum");
	// sqNum.setValue((short) 0);
	// }
	//
	// // if(((BOOLEAN)urcb.getChild("Resv")).getValue() == false){
	// ((BOOLEAN) urcb.getChild("Resv")).setValue(true);
	// // }
	//
	// // if(((OCTET_STRING_64)urcb.getChild("Owner")).getValue() ==
	// // null){
	// //
	// ((OCTET_STRING_64)urcb.getChild("Owner")).setValue(association.getIPAddress());
	// // }
	//
	// // TODO
	// // urcb.setOwner(association.getIPAddress());
	//
	// }
	// else {
	// // reset the reservation and owner of the rcb
	// // if(((BOOLEAN)urcb.getChild("Resv")).getValue() == true){
	// ((BOOLEAN) urcb.getChild("Resv")).setValue(false);
	// // }
	//
	// // if(urcb.getChild("Owner") != null ||
	// // ((OCTET_STRING_64)urcb.getChild("Owner")).getValue() !=
	// // null){
	// // ((OCTET_STRING_64)urcb.getChild("Owner")).setValue(new
	// // byte[0]);
	// // }
	// urcb.setOwner(null);
	// }
	//
	// ((BOOLEAN) child).setValue(((BOOLEAN) value).getValue());
	//
	// }
	//
	// else if (value.getNodeName().equals("Resv")) {
	// ((BOOLEAN) child).setValue(((BOOLEAN) value).getValue());
	//
	// if (((BOOLEAN) value).getValue() == true) {
	// // urcb.addChild(new OCTET_STRING_64(new ObjectReference(value
	// // .getReference().toString()
	// // + ".Owner"), null, "", association
	// // .getIPAddress(), false, false));
	// //
	// ((OCTET_STRING_64)urcb.getChild("Owner")).setValue(association.getIPAddress());
	//
	// // TODO
	// // urcb.setOwner(association.getIPAddress());
	// }
	// }
	//
	// else if (value.getNodeName().equals("ConfRev")) {
	// // ((INT32U)child).setValue(((INT32U)value).getValue());
	// // not writable but IEDScout sends a write request
	// // TODO return an error?
	// return;
	// }
	//
	// else if (value.getNodeName().equals("SqNum")) {
	// // not writable but IEDScout sends a write request
	// // TODO return an error?
	// return;
	// }
	//
	// else if (value.getNodeName().equals("IntgPd")) {
	// if (((TriggerConditions) urcb.getChild("TrgOps")).isIntegrity()) {
	// ((INT64) child).setValue(((INT64) value).getValue());
	//
	// // TODO implement setup of integrity reports
	// }
	//
	// else {
	// throw new ServiceError(ServiceError.ACCESS_VIOLATION);
	// }
	// }
	//
	// // Can be set with RptEna = false or true
	// else if (value.getNodeName().equals("GI")) {
	//
	// if (((TriggerConditions)
	// urcb.getChild("TrgOps")).isGeneralInterrogation()) {
	// ((BOOLEAN) child).setValue(((BOOLEAN) value).getValue());
	// // TODO start process of sending GI report: after setting to
	// // true, the GI process shall be started.
	// // After initializing this process, the value of GI shall be set
	// // back to false (see iec61850-7-2, page 103
	// }
	// else {
	// throw new ServiceError(ServiceError.ACCESS_VIOLATION);
	// }
	//
	// }
	//
	// else {
	// // these can only be set when rptEna = false
	// if (((BOOLEAN) urcb.getChild("RptEna")).getValue() == false) {
	//
	// if (value.getNodeName().equals("RptID")) {
	// ((VISIBLE_STRING) child).setValue(((VISIBLE_STRING) value).getValue());
	// }
	// // else if (value instanceof DataSet) {
	// else if (value.getNodeName().equals("DatSet")) {
	// // update trigger information
	//
	// //
	// if(!(urcb.getChild(urcb.getDataSetRef()).getReference().toString().equals(value.getReference().toString()))){
	// if (((VISIBLE_STRING) urcb.getChild("DatSet")).getValue().equals(
	// ((VISIBLE_STRING) value).getValue())) {
	// INT64 confRev = (INT64) urcb.getChild("ConfRev");
	// confRev.setValue(confRev.getValue() + 1);
	//
	// // TODO was commented out:
	// // DataSet ds = (DataSet)
	// // getDataSetDirectory(((VISIBLE_STRING)
	// // value).getValue().toString());
	// DataSet ds = null;
	//
	// updateTriggers((DataSet) urcb.getDataSet(), ds, urcb);
	//
	// urcb.setDataSet(ds);
	// }
	// }
	// else if (value.getNodeName().equals("OptFlds")) {
	// ((OptFields) urcb.getChild("OptFlds")).setValue(((OptFields)
	// value).getValue());
	//
	// }
	//
	// else if (value.getNodeName().equals("BufTm")) {
	// ((INT32U) child).setValue(((INT32U) value).getValue());
	// }
	//
	// else if (value.getNodeName().equals("TrgOps")) {
	// ((TriggerConditions)
	// urcb.getChild("TrgOps")).setValue(((TriggerConditions)
	// value).getValue());
	// }
	// else {
	// throw new ServiceError(ServiceError.PARAMETER_VALUE_INAPPROPRIATE);
	// }
	// }
	//
	// else {// trying to set value when rptEna is true not allowed
	// throw new ServiceError(ServiceError.ACCESS_VIOLATION);
	// }
	// }
	//
	// // TODO check if integrity is set in trigger options and the integrity
	// // period is set to start the sending of integrity periods
	//
	// // TODO handle GI requests if GI has been written to true. Reset the
	// // value back to false
	// }

	// TODO commented out:
	// public void notificationIndication(ModelNode node, ReasonCode rc) {
	// // TODO figure out if it is a buffered/unbuffered/log - call appropriate
	// // functions
	// List<DataSet> datSets = null;
	// List<ReportControlBlock> rcbs = null;
	//
	// if (rc == ReasonCode.DCHG) {
	// datSets = triggerDchg.get(node.getReference().toString());
	// }
	// else if (rc == ReasonCode.QCHG) {
	// datSets = triggerQchg.get(node.getReference().toString());
	// }
	// else if (rc == ReasonCode.DUPD) {
	// datSets = triggerDupd.get(node.getReference().toString());
	// }
	// // TODO handle integrity and GI cases - handled in a separate
	// // method/thread?
	//
	// // TODO need to update values of DO or DA so that it can be included
	// // into report
	//
	// for (DataSet ds : datSets) {
	//
	// // need to see what node is a member of the data set (i.e. DO, DA)
	// // The report is sent for the member
	// ModelNode reportNode = null;
	// for (int i = node.getReference().size() - 1; i > 0; i--) {
	// reportNode = ds.getChild(node.getReference().get(i));
	// if (reportNode != null
	// && (reportNode instanceof FcDataObject || reportNode instanceof
	// BasicDataAttribute || reportNode instanceof ConstructedDataAttribute)) {
	// break;
	// }
	// }
	//
	// // update the members of the node to be reported
	// if
	// (reportNode.getReference().toString().equals(node.getReference().toString()))
	// {
	// reportNode = node; // node should have updated value
	// }
	//
	// else if (reportNode instanceof FcDataObject) {
	// // need to update the basic data attributes of the data object
	// // which will be reported
	// reportNode = reportNode.copy();
	// try {
	// dataSource.readValues(reportNode.getBasicDataAttributes());
	// } catch (ServiceError e) {
	// logger.warn("problem during reading member of data set for report");
	// }
	// }
	//
	// rcbs = dsRCB.get(ds);
	// for (ReportControlBlock rcb : rcbs) {
	// if (((BOOLEAN) rcb.getChild("RptEna")).getValue() == true) {
	// if (rcb instanceof UnbufferedReportContrlBlock) {
	// generateURCBReport(reportNode, rc, (UnbufferedReportContrlBlock) rcb);
	// }
	// else if (rcb instanceof BufferedReportControlBlock) {
	// // TODO add to buffered report queue
	// }
	// }
	// }
	//
	// // TODO do the same thing for logs
	//
	// }
	//
	// }

	private void generateURCBReport(ModelNode node, ReasonCode rc, UnbufferedReportContrlBlock urcb) {
		System.out.println("UnbufferedReport is going to be generated");

		// byte[] owner = ((OCTET_STRING_64) urcb.getChild("Owner")).getValue();
		byte[] owner = urcb.getOwner();
		ConnectionHandler connectionHandler = null;

		// for (Association association : associations) {
		// // TODO
		// // if (association.getIPAddress() == owner) {
		// connectionHandler = association.getConnectionHandler();
		// // }
		// }

		if (connectionHandler != null) {

			OptFields optFlds = (OptFields) urcb.getChild("OptFlds");

			Report report = new Report();

			// TODO commented out:
			// report.setRptId(((VISIBLE_STRING)
			// urcb.getChild("RptID")).getValue());
			report.setOptFlds(optFlds);

			if (optFlds.isSeqNum()) { // SeqNum determined here
				report.setSqNum(reportSqNum);
				reportSqNum++;
				// TODO subSqNum and MoreSegmentsFollow
			}

			if (optFlds.isDataSet()) {
				// report.setDataSet(urcb.getChild("DatSet").getReference()
				// .toString());
				report.setDataSetRef(urcb.getDataSet().getReferenceStr());
			}

			if (optFlds.isConfigRef()) {
				report.setConvRev(((DaInt64) urcb.getChild("ConfRev")).getValue());
			}

			if (optFlds.isTimeStamp()) {
				// TODO commented out:
				// report.setTimeOfEntry(new Timestamp(null, null, "", new
				// Date(), false, false));
			}

			ReportEntryData rptEntryData = new ReportEntryData();

			if (optFlds.isDataRef()) {
				rptEntryData.setDataRef(node.getReference().toString());
			}

			rptEntryData.setValue(node);

			if (optFlds.isReasonCode()) {
				rptEntryData.setReasonCode(rc);
			}

			report.addEntryData(rptEntryData);

			byte[] inclusionBitString = new byte[1];
			inclusionBitString[0] = 0x01;
			report.setInclusionBitString(inclusionBitString);

			try {
				connectionHandler.sendReport(report);
			} catch (IOException e) {
				logger.warn("generateURCBReport got an IOException");
			}

			// increment the sequence number after report has been sent

			DaInt8u sqNum = (DaInt8u) urcb.getChild("SqNum");

			if (sqNum.getValue().intValue() < 256) {
				// TODO commented out:
				// sqNum.setValue(new Byte((byte) (sqNum.getValue() + 1)));
			}

			else {
				// TODO commented out:
				// sqNum.setValue(new Byte((byte) 0));
			}
		}

	}

	/**
	 * TODO update logs as well
	 */

	// TODO commmented out:
	// private void populateTriggers() {
	// // initial update
	// for (ModelNode ld : serverModel.getChildren()) {
	// for (ModelNode ln : ld.getChildren()) {
	// for (ReportControlBlock rcb : ((LogicalNode)
	// ln).getReportControlBlocks()) {
	// DataSet ds = (DataSet) rcb.getDataSet();
	//
	// TriggerConditions trgOps = (TriggerConditions) rcb.getChild("TrgOps");
	//
	// if (ds != null) {
	// if (dsRCB.get(ds) == null) {
	// dsRCB.put(ds, new ArrayList<ReportControlBlock>());
	// }
	// dsRCB.get(ds).add(rcb);
	//
	// for (BasicDataAttribute mem : ds.getBasicDataAttributes()) {
	//
	// if (mem.getDchg() && trgOps.isDataChange()) {
	// if (triggerDchg.get(mem.getReference().toString()) == null) {
	// triggerDchg.put(mem.getReference().toString(), new ArrayList<DataSet>());
	// }
	//
	// triggerDchg.get(mem.getReference().toString()).add(ds);
	// }
	//
	// else if (mem.getQchg() && trgOps.isQualityChange()) {
	// if (triggerQchg.get(mem.getReference().toString()) == null) {
	// triggerQchg.put(mem.getReference().toString(), new ArrayList<DataSet>());
	// }
	//
	// triggerQchg.get(mem.getReference().toString()).add(ds);
	// }
	//
	// else if (mem.getDupd() && trgOps.isDataUpdate()) {
	// if (triggerDupd.get(mem.getReference().toString()) == null) {
	// triggerDupd.put(mem.getReference().toString(), new ArrayList<DataSet>());
	// }
	//
	// triggerDupd.get(mem.getReference().toString()).add(ds);
	// }
	//
	// }
	// }
	//
	// }
	// }
	// }
	//
	// }

	/**
	 * This method should be called when reports/logs point to a new data set so
	 * that the members to be monitored are updated TODO implement the update
	 * for logs
	 * 
	 */

	// TODO commented out:
	// private void updateTriggers(DataSet oldDS, DataSet newDS,
	// ReportControlBlock rcb) {
	//
	// // remove old data set reference
	// for (ReportControlBlock reportCtrl : dsRCB.get(oldDS)) {
	// if
	// (reportCtrl.getReference().toString().equals(rcb.getReference().toString()))
	// {
	// dsRCB.get(oldDS).remove(reportCtrl);
	// break;
	// }
	// }
	//
	// // add new data set reference
	// if (dsRCB.get(newDS) == null) {
	// dsRCB.put(newDS, new ArrayList<ReportControlBlock>());
	// }
	//
	// dsRCB.get(newDS).add(rcb);
	//
	// // remove members of old data set reference from trigger
	// for (BasicDataAttribute mem : oldDS.getBasicDataAttributes()) {
	// if (mem.getDchg()) {
	// for (DataSet datSet : triggerDchg.get(mem.getReference().toString())) {
	// if
	// (datSet.getReference().toString().equals(oldDS.getReference().toString()))
	// {
	// triggerDchg.remove(datSet);
	// break;
	// }
	// }
	// }
	//
	// else if (mem.getQchg()) {
	// for (DataSet datSet : triggerQchg.get(mem.getReference().toString())) {
	// if
	// (datSet.getReference().toString().equals(oldDS.getReference().toString()))
	// {
	// triggerQchg.remove(datSet);
	// break;
	// }
	// }
	// }
	//
	// else if (mem.getDchg()) {
	// for (DataSet datSet : triggerDupd.get(mem.getReference().toString())) {
	// if
	// (datSet.getReference().toString().equals(oldDS.getReference().toString()))
	// {
	// triggerDupd.remove(datSet);
	// break;
	// }
	// }
	// }
	// }
	//
	// // add members of new data set reference
	//
	// for (BasicDataAttribute mem : newDS.getBasicDataAttributes()) {
	// TriggerConditions trgOps = (TriggerConditions) rcb.getChild("TrgOps");
	//
	// String memRef = mem.getReference().toString();
	//
	// if (mem.getDchg() && trgOps.isDataChange()) {
	// List<DataSet> datSets = triggerDchg.get(memRef);
	//
	// if (datSets == null) {
	// triggerDchg.put(memRef, new ArrayList<DataSet>());
	// triggerDchg.get(memRef).add(newDS);
	// return;
	//
	// }
	//
	// for (DataSet datSet : datSets) {
	// if
	// (datSet.getReference().toString().equals(newDS.getReference().toString()))
	// {
	// return; // already added
	// }
	// }
	//
	// // otherwise not added yet
	// triggerDchg.get(memRef).add(newDS);
	// }
	//
	// else if (mem.getQchg() && trgOps.isQualityChange()) {
	// List<DataSet> datSets = triggerQchg.get(memRef);
	//
	// if (datSets == null) {
	// triggerQchg.put(memRef, new ArrayList<DataSet>());
	// triggerQchg.get(memRef).add(newDS);
	// return;
	//
	// }
	//
	// for (DataSet datSet : datSets) {
	// if
	// (datSet.getReference().toString().equals(newDS.getReference().toString()))
	// {
	// return; // already added
	// }
	// }
	//
	// // otherwise not added yet
	// triggerQchg.get(memRef).add(newDS);
	// }
	//
	// else if (mem.getDupd() && trgOps.isDataUpdate()) {
	// List<DataSet> datSets = triggerDupd.get(memRef);
	//
	// if (datSets == null) {
	// triggerDupd.put(memRef, new ArrayList<DataSet>());
	// triggerDupd.get(memRef).add(newDS);
	// return;
	//
	// }
	//
	// for (DataSet datSet : datSets) {
	// if
	// (datSet.getReference().toString().equals(newDS.getReference().toString()))
	// {
	// return; // already added
	// }
	// }
	//
	// // otherwise not added yet
	// triggerDupd.get(memRef).add(newDS);
	// }
	// }
	// }

	public String getName() {
		return name;
	}

	void addNonPersistentDataSet(DataSet dataSet, ConnectionHandler connectionHandler) {
		// TODO Auto-generated method stub

	}
}
