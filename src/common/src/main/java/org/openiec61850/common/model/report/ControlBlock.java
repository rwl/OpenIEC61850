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

import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;

public abstract class ControlBlock extends ModelNode {

	private ObjectReference reference;
	private String name;

	// private String ref;

	/**
	 * Reference to DataSet to be monitored
	 */
	// private DataSet dataSet;
	// private OptFields optFlds;

	/**
	 * trigger options
	 */
	// private TriggerConditions trgOps;
	/**
	 * integrity period - If TrgOps includes a setting indicating integrity, the
	 * attribute IntgPd shall indicate the period in milliseconds used for
	 * generating an integrity report. An integrity report shall report the
	 * values of all members of the related DATA-SET. BufTm shall have no effect
	 * when this change issues a report.
	 */
	// private long intgPd;

	public ControlBlock(ObjectReference objectReference) {
		this.objectReference = objectReference;
		this.children = new LinkedHashMap<String, ModelNode>();
	}

	// public ControlBlock(ControlBlock master) {
	// reference = master.reference; // TODO removed copyDeep
	// dataSet = master.dataSet.copy();
	// optFlds = master.optFlds.copyDeep();
	// trgOps = master.trgOps.copyDeep();
	// intgPd = master.intgPd;
	// }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public ObjectReference getReference() {
	// return reference;
	// }

	// public void setRef(String ref) {
	// this.ref = ref;
	// }

	// public void setReference(String reference) {
	// this.reference = new ObjectReference(reference);
	// }
	//
	// public DataSet getDataSet() {
	// return dataSet;
	// }
	//
	// public void setDataSet(DataSet dataSet) {
	// this.dataSet = dataSet;
	// }
	//
	// public TriggerConditions getTrgOps() {
	// return trgOps;
	// }
	//
	// public void setTrgOps(TriggerConditions trgOps) {
	// this.trgOps = trgOps;
	// }
	//
	// public long getIntgPd() {
	// return intgPd;
	// }
	//
	// public void setIntgPd(long intgPd) {
	// this.intgPd = intgPd;
	// }
	//
	// public void setOptFlds(OptFields optFlds) {
	// this.optFlds = optFlds;
	// }
	//
	// public OptFields getOptFlds() {
	// return optFlds;
	// }
	//
	// public abstract String getId();
}
