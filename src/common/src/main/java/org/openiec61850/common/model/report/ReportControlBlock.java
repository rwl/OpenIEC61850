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

import java.util.LinkedHashMap;

import org.openiec61850.DataSet;
import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;

public abstract class ReportControlBlock extends ModelNode {

	/**
	 * The attribute RptID shall be the client-specified report identifier of
	 * the BRCB that has caused the generation of the report. If the report
	 * identifier value of the BRCB is NULL, then the reference of the BRCB
	 * shall be reported as the report identifier.
	 */
	// private String rptID;

	/**
	 * report enable
	 * 
	 * TODO: implement handling
	 */
	// private boolean rptEna;

	/**
	 * configuration revision The attribute ConfRev shall represent a count of
	 * the number of times that the configuration of the DATA-SET referenced by
	 * DatSet has been changed. Changes that shall be counted are:
	 * <ul>
	 * <li>any deletion of a member of the DATA-SET;</li>
	 * <li>the reordering of members of the DATA-SET; and</li>
	 * <li>Successful SetBRCBValues of the DatSet attribute where the DatSet
	 * attribute value changes.</li>
	 * </ul>
	 * The counter shall be incremented when the configuration changes. At
	 * configuration time, the configuration tool will be responsible for
	 * incrementing/maintaining the ConfRev value. When configuration changes
	 * occur due to SetBRCBValues, the IED shall be responsible for incrementing
	 * the value of ConfRev.
	 * 
	 * TODO: implement handling
	 */
	// private long convRef = 1L;

	/**
	 * buffer time - The attribute BufTm (see Figure 27) shall specify the time
	 * interval in milliseconds for the buffering of internal notifications
	 * caused by data-change (dchg), quality-change (qchg), data update (dupd)
	 * by the BRCB for inclusion into a single report.
	 * 
	 * TODO: implement handling
	 */
	// private long bufTm;

	/**
	 * sequence number - The attribute SqNum shall specify the sequence number
	 * for each BRCB that has report enable set to TRUE. This number is to be
	 * incremented by the BRCB for each report generated and sent. The increment
	 * shall occur once the BRCB has formatted the report and requested for
	 * transmission.
	 * 
	 * TODO: implement handling
	 */
	// private int sqNum;

	/**
	 * general-interrogation - The attribute GI shall indicate the request to
	 * start the general-interrogation process.
	 * 
	 * TODO: implement handling
	 */
	// private boolean gi;

	/**
	 * This is for the owner of the rcb (i.e. client who reserves/enables it)
	 * Can also be added as a child, but it is optional
	 */

	private byte[] owner = null;

	/**
	 * Default constructor
	 */
	// public ReportControlBlock() {
	// }

	private String name;

	public ReportControlBlock(ObjectReference objectReference) {
		this.objectReference = objectReference;
		this.children = new LinkedHashMap<String, ModelNode>();
	}

	public void setChild(String name, ModelNode node) {
		children.put(name, node);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(byte[] owner) {
		this.owner = owner;
	}

	public byte[] getOwner() {
		return owner;
	}

	public abstract DataSet getDataSet();

	public abstract void setDataSet(DataSet dataSet);

	/**
	 * Deep-copy constructor
	 * 
	 * @param master
	 */
	// public ReportControlBlock(ReportControlBlock master) {
	// super(master);
	// rptID = master.rptID;
	// rptEna = master.rptEna;
	// convRef = master.convRef;
	// bufTm = master.bufTm;
	// sqNum = master.sqNum;
	// gi = master.gi;
	// }

	// public String getRptID() {
	// return rptID;
	// }
	//
	// public void setRptID(String rptID) {
	// this.rptID = rptID;
	// }
	//
	// public boolean isRptEna() {
	// return rptEna;
	// }
	//
	// public void setRptEna(boolean rptEna) {
	// this.rptEna = rptEna;
	// }
	//
	// public long getConvRef() {
	// return convRef;
	// }
	//
	// public void setConvRef(long convRef) {
	// this.convRef = convRef;
	// }
	//
	// public long getBufTm() {
	// return bufTm;
	// }
	//
	// public void setBufTm(long bufTm) {
	// this.bufTm = bufTm;
	// }
	//
	// public int getSqNum() {
	// return sqNum;
	// }
	//
	// public void setSqNum(int sqNum) {
	// this.sqNum = sqNum;
	// }
	//
	// public boolean isGi() {
	// return gi;
	// }
	//
	// public void setGi(boolean gi) {
	// this.gi = gi;
	// }
	//
	// @Override
	// public String getId() {
	// return getRptID();
	// }
	//
	// public ReportControlBlock copyDeep() {
	// return new ReportControlBlock(this);
	// }

	// @Override
	// public ModelNode copy() {
	// // TODO Auto-generated method stub
	// return null;
	// }

}
