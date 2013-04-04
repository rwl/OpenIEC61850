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

import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;

/**
 * see 61850-7-2, chapter 14.2.2.8
 * 
 */
public class OptFields extends ModelNode {
	/**
	 * SqNum shall be included in the report
	 */
	private boolean seqNum;
	/**
	 * TimeOfEntry shall be included in the report
	 */
	private boolean timeStamp;
	/**
	 * DataSet shall be included in the report
	 */
	private boolean dataSet;
	/**
	 * ReasonCode shall be included in the report
	 */
	private boolean reasonCode;
	/**
	 * DataRef or DataAttributeReference shall be included in the report
	 */
	private boolean dataRef;
	/**
	 * BufOvfl shall be included in the report
	 */
	private boolean bufOvfl;
	/**
	 * EntryID shall be included in the report
	 */
	private boolean entryId;
	/**
	 * ConfRev shall be included in the report
	 */
	private boolean configRef;
	/**
	 * ???
	 */
	/* segmentation is depreciated and not used */
	private boolean segmentation;

	// private BitSet value;

	private byte[] value = new byte[2];

	public OptFields(ObjectReference objectReference) {
		this.objectReference = objectReference;
		// fc = null;

		/* can also make the members BasicDataAttributes and add to children */
		children = null;

		// value = new BitSet(9);
		// value.set(8, false); //reserved see iec61850-8-1 Table 62
		value[0] = (byte) (value[0] & ~(1 << 7));
	}

	public boolean isSeqNum() {
		return seqNum;
	}

	public void setSeqNum(boolean seqNum) {
		this.seqNum = seqNum;
		// value.set(7, seqNum);
		if (seqNum) {
			value[0] = (byte) (value[0] | (1 << 6));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 6));
		}
	}

	public boolean isTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(boolean timeStamp) {
		this.timeStamp = timeStamp;
		// value.set(6, timeStamp);
		if (timeStamp) {
			value[0] = (byte) (value[0] | (1 << 5));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 5));
		}
	}

	public boolean isDataSet() {
		return dataSet;
	}

	public void setDataSet(boolean dataSet) {
		this.dataSet = dataSet;
		// value.set(4, dataSet);
		if (dataSet) {
			value[0] = (byte) (value[0] | (1 << 3));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 3));
		}
	}

	public boolean isReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(boolean reasonCode) {
		this.reasonCode = reasonCode;
		// value.set(5, reasonCode);
		if (reasonCode) {
			value[0] = (byte) (value[0] | (1 << 4));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 4));
		}
	}

	public boolean isDataRef() {
		return dataRef;
	}

	public void setDataRef(boolean dataRef) {
		this.dataRef = dataRef;
		// value.set(3, dataRef);
		if (dataRef) {
			value[0] = (byte) (value[0] | (1 << 2));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 2));
		}
	}

	public boolean isBufOvfl() {
		return bufOvfl;
	}

	public void setBufOvfl(boolean bufOvfl) {
		this.bufOvfl = bufOvfl;
		// value.set(2, bufOvfl);
		if (bufOvfl) {
			value[0] = (byte) (value[0] | (1 << 1));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 1));
		}
	}

	public boolean isEntryId() {
		return entryId;
	}

	public void setEntryId(boolean entryId) {
		this.entryId = entryId;
		// value.set(1, entryId);
		if (entryId) {
			value[0] = (byte) (value[0] | (1));
		}
		else {
			value[0] = (byte) (value[0] & ~(1));
		}
	}

	public boolean isConfigRef() {
		return configRef;
	}

	public void setConfigRef(boolean configRef) {
		this.configRef = configRef;
		// value.set(0, configRef);
		if (configRef) {
			value[1] = (byte) (value[1] | (1 << 7));
		}
		else {
			value[1] = (byte) (value[1] & ~(1 << 7));
		}
	}

	public boolean isSegmentation() {
		return segmentation;
	}

	public void setSegmentation(boolean segmentation) {
		this.segmentation = segmentation;
	}

	// public OptFields copyDeep() {
	// try {
	// return (OptFields) this.clone();
	// } catch (CloneNotSupportedException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public void setValue(byte[] data) {
		value = data;

		if ((value[1] & 0x80) == 0x80) {
			configRef = true;
		}
		else {
			configRef = false;
		}

		if ((value[1] & 0x40) == 0x40) {
			segmentation = true;
		}
		else {
			segmentation = false;
		}

		if ((value[0] & 0x01) == 0x01) {
			entryId = true;
		}
		else {
			entryId = false;
		}

		if ((value[0] & 0x02) == 0x02) {
			bufOvfl = true;
		}
		else {
			bufOvfl = false;
		}

		if ((value[0] & 0x04) == 0x04) {
			dataRef = true;
		}
		else {
			dataRef = false;
		}

		if ((value[0] & 0x08) == 0x08) {
			dataSet = true;
		}
		else {
			dataSet = false;
		}

		if ((value[0] & 0x10) == 0x10) {
			reasonCode = true;
		}
		else {
			reasonCode = false;
		}

		if ((value[0] & 0x20) == 0x20) {
			timeStamp = true;
		}
		else {
			timeStamp = false;
		}

		if ((value[0] & 0x40) == 0x40) {
			seqNum = true;
		}
		else {
			seqNum = false;
		}
	}

	public byte[] getValue() {
		return value;
	}

	public void setDefault() {

		seqNum = false;
		timeStamp = false;
		dataSet = false;
		reasonCode = false;
		dataRef = false;
		bufOvfl = false;
		entryId = false;
		configRef = false;
		// segmentation = false;

	}

	@Override
	public ModelNode copy() {
		// try {
		// return (OptFields) this.clone();
		// } catch (CloneNotSupportedException e) {
		// e.printStackTrace();
		// }
		// return null;

		OptFields copy = new OptFields(this.objectReference);
		copy.setSeqNum(this.seqNum);
		copy.setTimeStamp(this.timeStamp);
		copy.setDataSet(this.dataSet);
		copy.setReasonCode(this.reasonCode);
		copy.setDataRef(this.dataRef);
		copy.setBufOvfl(this.bufOvfl);
		copy.setEntryId(this.entryId);
		copy.setConfigRef(this.configRef);

		return copy;
	}

}