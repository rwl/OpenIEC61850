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

import org.openiec61850.DataSet;
import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;

/**
 * see 61850-7-2 Chapter 16.2.2
 * 
 * @author bertram
 */
public class BufferedReportControlBlock extends ReportControlBlock {
	/**
	 * purge buffer - The attribute PurgeBuf shall indicate the request to
	 * discard buffered events.
	 * 
	 * TODO: implement handling
	 */

	/*
	 * See state machine on page 95 of section 7-2 Only in "enabled" are reports
	 * sent
	 */
	private enum State {
		disabled, resync, enabled
	};

	private State currentState = State.disabled;

	private DataSet dataSet;

	// private boolean purgeBuf;
	/**
	 * entry identifier
	 * 
	 * TODO: implement handling
	 */

	// private String entryId;

	// private Date timeOfEntry;

	/**
	 * reservation time
	 * 
	 * ???
	 */
	// private int resvTms;

	// public BufferedReportControlBlock(BufferedReportControlBlock master) {
	// super(master);
	// purgeBuf = master.purgeBuf;
	// entryId = master.entryId;
	// timeOfEntry = (Date) master.timeOfEntry.clone();
	// resvTms = master.resvTms;
	//
	// }

	public BufferedReportControlBlock(ObjectReference objectReference, List<ModelNode> children) {
		super(objectReference);
		for (ModelNode child : children) {
			this.children.put(child.getNodeName(), child);
		}

	}

	// @Override
	// public BufferedReportControlBlock copyDeep() {
	// return new BufferedReportControlBlock(this);
	// }

	@Override
	public ModelNode copy() {
		List<ModelNode> childCopies = new ArrayList<ModelNode>(children.size());
		for (ModelNode childNode : children.values()) {
			childCopies.add(childNode.copy());
		}
		return new BufferedReportControlBlock(objectReference, childCopies);
	}

	@Override
	public DataSet getDataSet() {

		return dataSet;
	}

	@Override
	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}
}
