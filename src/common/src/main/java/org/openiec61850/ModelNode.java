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
import java.util.Map;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.StructComponent;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_structure;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_structure.SubSeqOf_components;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;

public abstract class ModelNode implements Iterable<ModelNode> {

	protected ObjectReference objectReference;
	protected Map<String, ModelNode> children;

	/**
	 * Copies the whole node with all of its children. Creates new
	 * BasicDataAttribute values but reuses ObjectReferences,
	 * FunctionalConstraints.
	 */
	public abstract ModelNode copy();

	public ModelNode getChild(String childName) {
		return getChild(childName, null);
	}

	public ModelNode getChild(String childName, FunctionalConstraint fc) {
		return children.get(childName);
	}

	@SuppressWarnings("unchecked")
	public Collection<ModelNode> getChildren() {
		if (children == null) {
			return null;
		}
		return (Collection<ModelNode>) ((Collection<?>) children.values());
	}

	protected Iterator<Iterator<? extends ModelNode>> getIterators() {
		List<Iterator<? extends ModelNode>> iterators = new ArrayList<Iterator<? extends ModelNode>>();
		if (children != null) {
			iterators.add(children.values().iterator());
		}
		return iterators.iterator();
	}

	public ModelNode findSubNode(ObjectReference objectReference, FunctionalConstraint fc) {

		ModelNode currentNode = this;

		Iterator<String> searchedNodeReferenceIterator = objectReference.iterator();

		if (this.objectReference != null) {
			Iterator<String> thisNodeReferenceIterator = this.objectReference.iterator();
			while (thisNodeReferenceIterator.hasNext()) {
				if (!searchedNodeReferenceIterator.hasNext()
						|| !searchedNodeReferenceIterator.next().equals(thisNodeReferenceIterator.next())) {
					return null;
				}
			}
		}

		while (searchedNodeReferenceIterator.hasNext()) {
			currentNode = currentNode.getChild(searchedNodeReferenceIterator.next(), fc);
			if (currentNode == null) {
				return null;
			}

		}

		return currentNode;
	}

	public ModelNode findSubNode(ObjectReference objectReference) {
		return findSubNode(objectReference, null);
	}

	public ModelNode findSubNode(String objectReference, FunctionalConstraint fc) {
		return findSubNode(new ObjectReference(objectReference), fc);
	}

	public ModelNode findSubNode(String objectReference) {
		return findSubNode(new ObjectReference(objectReference), null);
	}

	public ObjectReference getReference() {
		return objectReference;
	}

	public String getNodeName() {
		return objectReference.getName();
	}

	public Iterator<ModelNode> iterator() {
		return children.values().iterator();
	}

	public List<BasicDataAttribute> getBasicDataAttributes() {
		List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<BasicDataAttribute>();
		for (ModelNode child : children.values()) {
			subBasicDataAttributes.addAll(child.getBasicDataAttributes());
		}
		return subBasicDataAttributes;
	}

	@Override
	public String toString() {
		return getReference().toString();
	}

	Data getMmsDataObj() {
		return null;
	}

	void setValueFromMmsDataObj(Data data) throws ServiceError {
	}

	TypeSpecification getMmsTypeSpec() {
		List<StructComponent> structComponents = new LinkedList<StructComponent>();
		for (ModelNode child : children.values()) {
			structComponents.add(new StructComponent(new BerVisibleString(child.getNodeName().getBytes()), child
					.getMmsTypeSpec()));
		}
		SubSeqOf_components componentsSequenceType = new SubSeqOf_components(structComponents);
		SubSeq_structure structure = new SubSeq_structure(null, componentsSequenceType);

		return new TypeSpecification(null, structure, null, null, null, null, null, null, null, null, null, null);
	}

}
