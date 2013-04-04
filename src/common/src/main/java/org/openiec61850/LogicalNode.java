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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openiec61850.common.model.report.ReportControlBlock;

public final class LogicalNode extends ModelNode {

	private final Map<String, ReportControlBlock> reportControlBlocks = new HashMap<String, ReportControlBlock>();
	// private final List<LogControlBlock> logControlBlocks = new
	// ArrayList<LogControlBlock>();
	private final Map<FunctionalConstraint, Map<String, FcDataObject>> fcDataObjects = new EnumMap<FunctionalConstraint, Map<String, FcDataObject>>(
			FunctionalConstraint.class);

	public LogicalNode(ObjectReference objectReference, List<FcDataObject> fcDataObjects) {
		children = new LinkedHashMap<String, ModelNode>();
		for (FunctionalConstraint myfc : FunctionalConstraint.values()) {
			this.fcDataObjects.put(myfc, new LinkedHashMap<String, FcDataObject>());
		}

		this.objectReference = objectReference;
		// if (dataObjects != null) {
		// for (DataObject dataObject : dataObjects) {
		// this.children.put(dataObject.getReference().getName(), dataObject);
		// }
		// }

		for (FcDataObject fcDataObject : fcDataObjects) {
			this.children.put(fcDataObject.getReference().getName() + fcDataObject.fc.toString(), fcDataObject);
			this.fcDataObjects.get(fcDataObject.getFunctionalConstraint()).put(fcDataObject.getReference().getName(),
					fcDataObject);
		}

		// }

		// if (dataSets != null) {
		// for (DataSet ds : dataSets) {
		// this.dataSets.put(ds.getReference().getName(), ds);
		// }
		// }
	}

	@Override
	public LogicalNode copy() {

		List<FcDataObject> dataObjectsCopy = new ArrayList<FcDataObject>();
		for (ModelNode obj : children.values()) {
			dataObjectsCopy.add((FcDataObject) obj.copy());
		}

		// List<DataSet> dataSetsCopy = new ArrayList<DataSet>();
		// for (DataSet dataSet : dataSets.values()) {
		// dataSetsCopy.add(dataSet.copy());
		// }

		// for (ReportControlBlock rcb : reportControlBlocks) {
		// addReportControlBlock(rcb.copyDeep());
		// }
		// for (LogControlBlock lcb : logControlBlocks) {
		// addLogControlBlock(lcb.copyDeep());
		// }

		LogicalNode copy = new LogicalNode(objectReference, dataObjectsCopy);
		return copy;
	}

	public void addReportControlBlock(ReportControlBlock rcb) {
		reportControlBlocks.put(rcb.getReference().getName(), rcb);
	}

	public Collection<ReportControlBlock> getReportControlBlocks() {
		return reportControlBlocks.values();
	}

	public ReportControlBlock getReportControlBlock(String rcbName) {
		return reportControlBlocks.get(rcbName);
	}

	// public void addLogControlBlock(LogControlBlock lcb) {
	// logControlBlocks.add(lcb);
	// }
	//
	// public List<LogControlBlock> getLogControlBlocks() {
	// return logControlBlocks;
	// }

	public List<FcDataObject> getChildren(FunctionalConstraint fc) {
		Collection<FcDataObject> fcChildren = fcDataObjects.get(fc).values();
		if (fcChildren.size() == 0) {
			return null;
		}
		else {
			return new ArrayList<FcDataObject>(fcChildren);
		}
	}

	@Override
	public ModelNode getChild(String childName, FunctionalConstraint fc) {
		if (fc != null) {
			return fcDataObjects.get(fc).get(childName);
		}
		for (ModelNode child : children.values()) {
			if (child.getNodeName().equals(childName)) {
				return child;
			}
		}
		return null;
	}
}
