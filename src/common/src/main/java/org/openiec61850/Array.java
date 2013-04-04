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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.DataSequence;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_array;
import org.openmuc.jasn1.ber.types.BerInteger;

public final class Array extends FcModelNode {

	private final List<ModelNode> items;

	/**
	 * An Array can contain up to n instances of one and the same DataObject,
	 * ConstructedDataAttribute, or BasicDataAttribute. The children of the
	 * array have the name that equals the index in the array (e.g. "0","1"
	 * etc.)
	 * 
	 * @param objectReference
	 * @param fc
	 * @param childNodes
	 */
	public Array(ObjectReference objectReference, FunctionalConstraint fc, List<FcModelNode> childNodes) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.items = new ArrayList<ModelNode>(childNodes.size());
		for (ModelNode childNode : childNodes) {
			this.items.add(childNode);
		}
	}

	@Override
	public Collection<ModelNode> getChildren() {
		return new ArrayList<ModelNode>(items);
	}

	@Override
	public Iterator<ModelNode> iterator() {
		return items.iterator();
	}

	@Override
	public ModelNode getChild(String childName, FunctionalConstraint fc) {
		return items.get(Integer.parseInt(childName));
	}

	public ModelNode getChild(int index) {
		return items.get(index);
	}

	@Override
	public ModelNode copy() {
		List<FcModelNode> itemsCopy = new ArrayList<FcModelNode>(items.size());
		for (ModelNode item : items) {
			itemsCopy.add((FcModelNode) item);
		}
		return new Array(objectReference, fc, itemsCopy);
	}

	@Override
	public List<BasicDataAttribute> getBasicDataAttributes() {
		List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<BasicDataAttribute>();
		for (ModelNode item : items) {
			subBasicDataAttributes.addAll(item.getBasicDataAttributes());
		}
		return subBasicDataAttributes;
	}

	// public List<Array> getArraysOfFCData() {
	//
	// List<Array> arraysOfFCDO = new LinkedList<Array>();
	//
	// if (fc != null) {
	// arraysOfFCDO.add(this);
	// return arraysOfFCDO;
	// }
	//
	// Map<FunctionalConstraint, List<ModelNode>> subFCDataMap = new
	// EnumMap<FunctionalConstraint, List<ModelNode>>(
	// FunctionalConstraint.class);
	//
	// for (FunctionalConstraint fc : FunctionalConstraint.values()) {
	// subFCDataMap.put(fc, new LinkedList<ModelNode>());
	// }
	//
	// for (ModelNode childNode : items) {
	//
	// if (childNode.getFunctionalConstraint() == null) {
	// for (ModelNode fcChildNode : ((FcDataObject)
	// childNode).getFCDataObjects()) {
	// subFCDataMap.get(fcChildNode.getFunctionalConstraint()).add(fcChildNode);
	// }
	// }
	// else {
	// subFCDataMap.get(childNode.getFunctionalConstraint()).add(childNode);
	// }
	//
	// }
	//
	// for (FunctionalConstraint fc : FunctionalConstraint.values()) {
	// if (subFCDataMap.get(fc).size() > 0) {
	// // arraysOfFCDO.add(new DataObject(objectReference,
	// // subFCDataMap.get(fc), cdcName, fc));
	// arraysOfFCDO.add(new Array(objectReference, fc, subFCDataMap.get(fc)));
	// }
	// }
	//
	// return arraysOfFCDO;
	//
	// }

	public int size() {
		return items.size();
	}

	@Override
	Data getMmsDataObj() {
		ArrayList<Data> seq = new ArrayList<Data>(items.size());
		for (ModelNode modelNode : items) {
			Data mmsArrayItem = modelNode.getMmsDataObj();
			if (mmsArrayItem == null) {
				throw new IllegalArgumentException("Unable to convert Child: " + modelNode.objectReference
						+ " to MMS Data Object.");
			}
			seq.add(mmsArrayItem);
		}

		if (seq.size() == 0) {
			throw new IllegalArgumentException("Converting ModelNode: " + objectReference
					+ " to MMS Data Object resulted in Sequence of size zero.");
		}

		return new Data(new DataSequence(seq), null, null, null, null, null, null, null, null, null, null, null);

	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.array.seqOf == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: structure");
		}
		if (data.array.seqOf.size() != items.size()) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: structure with " + children.size()
					+ " elements");
		}

		Iterator<Data> iterator = data.array.seqOf.iterator();
		for (ModelNode child : items) {
			child.setValueFromMmsDataObj(iterator.next());
		}
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(
				new SubSeq_array(null, new BerInteger(items.size()), items.get(0).getMmsTypeSpec()), null, null, null,
				null, null, null, null, null, null, null, null);
	}
}
