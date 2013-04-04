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
package org.openiec61850.common.model.report;

import java.util.ArrayList;
import java.util.List;

import org.openiec61850.DaEntryTime;
import org.openiec61850.DataSet;
import org.openiec61850.common.model.report.ReportEntryData.ReasonCode;

/**
 * See IEC 61850-7-2, chapter "ReportFormat Syntax"
 * 
 * @author bertram
 * 
 */
public class Report {
	/**
	 * report ID, see {@link ReportControlBlock#rptId}
	 */
	private String rptId = null;
	/**
	 * optional fields, see {@link ReportControlBlock#optFlds}
	 */
	private OptFields optFlds = null;
	/**
	 * Sequence numberThe parameter MoreSegmentsFollow indicates that more
	 * report segments with the same sequence number follow, counted up for
	 * every {@code Report} instance generated
	 */
	private int sqNum = -1;
	/**
	 * For the case of long reports that do not fit into one message, a single
	 * report shall be divided into subreports. Each segment – of one report –
	 * shall be numbered with the same sequence number and a unique SubSqNum.
	 */
	private int subSqNum = -1;
	/**
	 * The parameter MoreSegmentsFollow indicates that more report segments with
	 * the same sequence number follow
	 */
	private boolean moreSegmentsFollow = false;
	/**
	 * Reference of the {@link DataSet} in {@link ReportControlBlock#dataSet}
	 */
	private String dataSetRef = null;
	/**
	 * The parameter BufOvfl shall indicate to the client that entries within
	 * the buffer may have been lost. The detection of possible loss of
	 * information occurs when a client requests a resynchronization to a
	 * non-existent entry or to the first entry in the queue.
	 */
	private boolean bufOvfl = false;
	/**
	 * see {@link ReportControlBlock#convRev}
	 */
	private long convRev = -1;
	/**
	 * The parameter TimeOfEntry shall specify the time when the EntryID was
	 * created
	 */
	private DaEntryTime timeOfEntry = null;
	/**
	 * Unique Id of this Report
	 */
	private long entryId = -1;

	/**
	 * Indicator of data set members included in the report
	 */
	private byte[] inclusionBitString = null;

	/**
	 * Reason for the inclusion
	 */
	private List<ReasonCode> reasonCodes = null;

	/**
	 * Data set reference - this is an updated copy
	 */
	private DataSet dataSet = null;

	private List<ReportEntryData> entryData = new ArrayList<ReportEntryData>();

	public Report() {

	}

	public String getRptId() {
		return rptId;
	}

	public void setRptId(String rptId) {
		this.rptId = rptId;
	}

	public OptFields getOptFlds() {
		return optFlds;
	}

	public void setOptFlds(OptFields optFlds) {
		this.optFlds = optFlds;
	}

	public int getSqNum() {
		return sqNum;
	}

	public void setSqNum(int sqNum) {
		this.sqNum = sqNum;
	}

	public int getSubSqNum() {
		return subSqNum;
	}

	public void setSubSqNum(int subSqNum) {
		this.subSqNum = subSqNum;
	}

	public boolean isMoreSegmentsFollow() {
		return moreSegmentsFollow;
	}

	public void setMoreSegmentsFollow(boolean moreSegmentsFollow) {
		this.moreSegmentsFollow = moreSegmentsFollow;
	}

	public boolean getMoreSegmentsFollow() {
		return moreSegmentsFollow;
	}

	public String getDataSetRef() {
		return dataSetRef;
	}

	public void setDataSetRef(String dataSetRef) {
		this.dataSetRef = dataSetRef;
	}

	public boolean isBufOvfl() {
		return bufOvfl;
	}

	public void setBufOvfl(boolean bufOvfl) {
		this.bufOvfl = bufOvfl;
	}

	public boolean getBufOvfl() {
		return bufOvfl;
	}

	public long getConvRev() {
		return convRev;
	}

	public void setConvRev(long convRev) {
		this.convRev = convRev;
	}

	public DaEntryTime getTimeOfEntry() {
		return timeOfEntry;
	}

	public void setTimeOfEntry(DaEntryTime time) {
		this.timeOfEntry = time;
	}

	public long getEntryId() {
		return entryId;
	}

	public void setEntryId(long entryId) {
		this.entryId = entryId;
	}

	public List<ReportEntryData> getEntryData() {
		return entryData;
	}

	public void setEntryData(List<ReportEntryData> entryData) {
		this.entryData = entryData;
	}

	public void addEntryData(ReportEntryData red) {
		red.setReport(this);
		getEntryData().add(red);
	}

	public void setInclusionBitString(byte[] value) {
		inclusionBitString = value;
	}

	public byte[] getInclusionBitString() {
		return inclusionBitString;
	}

	public void setReasonCode(List<ReasonCode> reasonCode) {
		this.reasonCodes = reasonCode;
	}

	public List<ReasonCode> getReasonCode() {
		return reasonCodes;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

}
