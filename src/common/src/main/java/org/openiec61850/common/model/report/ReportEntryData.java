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

import org.openiec61850.DataSet;
import org.openiec61850.ModelNode;

public class ReportEntryData {

	public static enum ReasonCode {
		DCHG, QCHG, DUPD, INTEGRITY, GI, APPTRIGGER
	};

	/**
	 * Not specified in IEC61850 but useful for data persistence
	 */
	private long id;
	/**
	 * Reference to to {@link DataSet}-member
	 */
	private String dataRef;
	/**
	 * Attribute value to be reported
	 */
	private ModelNode value;
	/**
	 * Trigger that caused the data to be put into the report
	 */
	// private TriggerConditions reasonCode;
	private ReasonCode reasonCode;
	/**
	 * Backreference to report
	 */
	private Report report;

	public String getDataRef() {
		return dataRef;
	}

	public void setDataRef(String dataRef) {
		this.dataRef = dataRef;
	}

	public ModelNode getValue() {
		return value;
	}

	public void setValue(ModelNode value) {
		this.value = value;
	}

	public ReasonCode getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(ReasonCode reasonCode) {
		this.reasonCode = reasonCode;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

}