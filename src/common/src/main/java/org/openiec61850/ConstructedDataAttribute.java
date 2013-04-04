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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.DataSequence;

public final class ConstructedDataAttribute extends FcModelNode {

	/**
	 * 
	 * @param objectReference
	 * @param fc
	 * @param children
	 *            can be of type CONSTRUCTED_DATA_ATTRIBUTE and/or
	 *            BASIC_DATA_ATTRIBUTE
	 */
	public ConstructedDataAttribute(ObjectReference objectReference, FunctionalConstraint fc, List<FcModelNode> children) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.children = new LinkedHashMap<String, ModelNode>();
		for (ModelNode child : children) {
			this.children.put(child.getNodeName(), child);
		}
	}

	@Override
	public ConstructedDataAttribute copy() {
		List<FcModelNode> subDataAttributesCopy = new ArrayList<FcModelNode>();
		for (ModelNode subDA : children.values()) {
			subDataAttributesCopy.add((FcModelNode) subDA.copy());
		}
		return new ConstructedDataAttribute(this.getReference(), this.fc, subDataAttributesCopy);
	}

	@Override
	Data getMmsDataObj() {
		ArrayList<Data> seq = new ArrayList<Data>(children.size());
		for (ModelNode modelNode : getChildren()) {
			Data child = modelNode.getMmsDataObj();
			if (child == null) {
				throw new IllegalArgumentException("Unable to convert Child: " + modelNode.objectReference
						+ " to MMS Data Object.");
			}
			seq.add(child);
		}
		if (seq.size() == 0) {
			throw new IllegalArgumentException("Converting ModelNode: " + objectReference
					+ " to MMS Data Object resulted in Sequence of size zero.");
		}

		return new Data(null, new DataSequence(seq), null, null, null, null, null, null, null, null, null, null);

	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.structure == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: structure");
		}
		if (data.structure.seqOf.size() != children.size()) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: structure with " + children.size()
					+ " elements");
		}

		Iterator<Data> iterator = data.structure.seqOf.iterator();
		for (ModelNode child : children.values()) {
			child.setValueFromMmsDataObj(iterator.next());
		}
	}

}
