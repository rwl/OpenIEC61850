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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openiec61850.jmms.mms.asn1.AlternateAccess;
import org.openiec61850.jmms.mms.asn1.ObjectName;
import org.openiec61850.jmms.mms.asn1.ObjectName.SubSeq_domain_specific;
import org.openiec61850.jmms.mms.asn1.VariableDef;

public final class ServerModel extends ModelNode {

	// private Map<String, ReportControlBlock> rcbs = new HashMap<String,
	// ReportControlBlock>();
	private Map<String, DataSet> dataSets = new LinkedHashMap<String, DataSet>();

	public ServerModel(List<LogicalDevice> logicalDevices) {
		this(logicalDevices, null);
	}

	public ServerModel(List<LogicalDevice> logicalDevices, Collection<DataSet> dataSets) {
		children = new LinkedHashMap<String, ModelNode>();
		objectReference = null;
		for (LogicalDevice logicalDevice : logicalDevices) {
			this.children.put(logicalDevice.getReference().getName(), logicalDevice);
		}
		// populateRcbDS();

		if (dataSets != null) {
			addDataSets(dataSets);
		}

	}

	@Override
	public ServerModel copy() {
		List<LogicalDevice> childCopies = new ArrayList<LogicalDevice>(children.size());
		for (ModelNode childNode : children.values()) {
			childCopies.add((LogicalDevice) childNode.copy());
		}
		return new ServerModel(childCopies);
	}

	public DataSet getDataSet(String dataSetReference) {
		return dataSets.get(dataSetReference);
	}

	void addDataSet(DataSet dataSet) {
		// TODO check if DataSet has correct Reference
		dataSets.put(dataSet.getReferenceStr(), dataSet);
	}

	void addDataSets(Collection<DataSet> dataSets) {
		for (DataSet dataSet : dataSets) {
			addDataSet(dataSet);
		}
	}

	List<String> getDataSetNames(String ldName) {
		// TODO make thread save
		List<String> dataSetNames = new LinkedList<String>();
		for (String dataSetRef : dataSets.keySet()) {
			if (dataSetRef.startsWith(ldName)) {
				dataSetNames.add(dataSetRef.substring(dataSetRef.indexOf('/') + 1).replace('.', '$'));
			}
		}
		return dataSetNames;
	}

	public Collection<DataSet> getDataSets() {
		return dataSets.values();
	}

	/**
	 * 
	 * @param dataSetReference
	 * @return returns the DataSet that was removed, null otherwise
	 */
	DataSet removeDataSet(String dataSetReference) {
		return dataSets.remove(dataSetReference);
	}

	// public ReportControlBlock getRCB(String rcbRef) {
	// return rcbs.get(rcbRef);
	// }
	//
	// public Collection<ReportControlBlock> getRCBs() {
	// for (ReportControlBlock rcb : rcbs.values()) {
	// System.out.println("rcb: " + rcb.getReference().toString());
	// }
	// return rcbs.values();
	// }
	//
	// public void setRCBs(ReportControlBlock rcb) {
	// rcbs.put(rcb.getReference().toString(), rcb);
	// }

	@Override
	public String toString() {
		return "Server";
	}

	// private void populateRcbDS() {
	// for (ModelNode ld : children.values()) {
	// for (ModelNode ln : ld.getChildren()) {
	// for (DataSet ds : ((LogicalNode) ln).getDataSets()) {
	// dataSets.put(ds.getReference().toString(), ds);
	// }
	// for (ReportControlBlock rcb : ((LogicalNode)
	// ln).getReportControlBlocks()) {
	// rcbs.put(rcb.getReference().toString(), rcb);
	// }
	// }
	// }
	// }

	/**
	 * Returns the subModelNode that is referenced by the given VariableDef.
	 * Return null in case the referenced ModelNode is not found.
	 * 
	 * @param variableDef
	 * @return
	 * @throws ServiceError
	 */
	FcModelNode getNodeFromVariableDef(VariableDef variableDef) throws ServiceError {

		ObjectName objectName = variableDef.variableSpecification.name;

		if (objectName == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"name in objectName is not selected");
		}

		SubSeq_domain_specific domainSpecific = objectName.domain_specific;

		if (domainSpecific == null) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
					"domain_specific in name is not selected");
		}

		ModelNode modelNode = getChild(domainSpecific.domainId.toString());

		if (modelNode == null) {
			return null;
		}

		String mmsItemId = domainSpecific.itemId.toString();
		int index1 = mmsItemId.indexOf('$');

		if (index1 == -1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "invalid mms item id");
		}

		LogicalNode ln = (LogicalNode) modelNode.getChild(mmsItemId.substring(0, index1));

		if (ln == null) {
			return null;
		}

		int index2 = mmsItemId.indexOf('$', index1 + 1);

		if (index2 == -1) {
			throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "invalid mms item id");
		}

		FunctionalConstraint fc = FunctionalConstraint.fromString(mmsItemId.substring(index1 + 1, index2));

		index1 = index2;

		index2 = mmsItemId.indexOf('$', index1 + 1);

		if (index2 == -1) {
			return (FcModelNode) ln.getChild(mmsItemId.substring(index1 + 1), fc);
		}

		modelNode = ln.getChild(mmsItemId.substring(index1 + 1, index2), fc);

		index1 = index2;
		index2 = mmsItemId.indexOf('$', index1 + 1);
		while (index2 != -1) {
			modelNode = modelNode.getChild(mmsItemId.substring(index1 + 1, index2));
			index1 = index2;
			index2 = mmsItemId.indexOf('$', index1 + 1);
		}

		modelNode = modelNode.getChild(mmsItemId.substring(index1 + 1));

		if (variableDef.alternateAccess == null) {
			return (FcModelNode) modelNode;
		}

		AlternateAccess.SubChoice altAccIt = variableDef.alternateAccess.seqOf.get(0);

		if (altAccIt.selectAlternateAccess != null) {
			modelNode = ((Array) modelNode).getChild((int) altAccIt.selectAlternateAccess.accessSelection.index.val);

			String mmsSubArrayItemId = altAccIt.selectAlternateAccess.alternateAccess.seqOf.get(0).component.toString();
			index1 = -1;
			index2 = mmsSubArrayItemId.indexOf('$');
			while (index2 != -1) {
				modelNode = modelNode.getChild(mmsSubArrayItemId.substring(index1 + 1, index2));
				index1 = index2;
				index2 = mmsItemId.indexOf('$', index1 + 1);
			}

			return (FcModelNode) modelNode.getChild(mmsSubArrayItemId.substring(index1 + 1));
		}
		else {
			return (FcModelNode) ((Array) modelNode).getChild((int) altAccIt.index.val);
		}

	}
}
