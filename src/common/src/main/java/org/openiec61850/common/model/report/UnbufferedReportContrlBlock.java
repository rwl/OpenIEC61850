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
 * see 61850-7-2 Chapter 16.2.4
 * 
 * @author bertram
 */
public class UnbufferedReportContrlBlock extends ReportControlBlock {

	/**
	 * reserve URCB - The attribute Resv (if set to TRUE) shall indicate that
	 * the URCB is currently exclusively reserved for the client that has set
	 * the value to TRUE. Other clients shall not be allowed to set any
	 * attribute of that URCB.
	 * 
	 * TODO: implement handling
	 */
	// private boolean resv;

	/**
	 * Deep copy constructor
	 * 
	 * @param master
	 */
	// public UnbufferedReportContrlBlock(UnbufferedReportContrlBlock master) {
	// super(master);
	// resv = master.resv;
	// }

	private DataSet dataSet;

	public UnbufferedReportContrlBlock(ObjectReference objectReference, List<ModelNode> children) {
		super(objectReference);
		for (ModelNode child : children) {
			this.children.put(child.getNodeName(), child);
			// if(child instanceof DataSet){
			// dataSetRef = child.getNodeName();
			// }
		}
	}

	// @Override
	// public UnbufferedReportContrlBlock copyDeep() {
	// return new UnbufferedReportContrlBlock(this);
	// }

	@Override
	public ModelNode copy() {
		List<ModelNode> childCopies = new ArrayList<ModelNode>(children.size());
		for (ModelNode childNode : children.values()) {
			childCopies.add(childNode.copy());
		}
		return new UnbufferedReportContrlBlock(objectReference, childCopies);
	}

	@Override
	public DataSet getDataSet() {
		return dataSet;
	}

	@Override
	public void setDataSet(DataSet dataSet2) {
		this.dataSet = dataSet2;
	}

	@Override
	public ModelNode getChild(String childName) {

		// if(childName.equals("DatSet")){
		// return super.getChild(dataSetRef, null);
		// }
		// else{
		return super.getChild(childName, null);
		// }
	}

	// public void addChild(ModelNode child){
	// children.put(child.getNodeName(), child);
	// if(child instanceof DataSet){
	// dataSetRef = child.getNodeName();
	// }
	// }
}
