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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ObjectReference syntax: LDName/LNName.DOName[.Name[. ...]]
 * 
 */
public final class ObjectReference implements Iterable<String> {

	private final String objectReference;
	private List<String> nodeNames = null;

	// if the ObjectReference contains an array index this variable will save
	// its position in the nodeNames List
	private int arrayIndexPosition = -1;

	public ObjectReference(String objectReference) {
		if (objectReference == null || objectReference == "") {
			throw new IllegalArgumentException();
		}
		this.objectReference = objectReference;
	}

	/**
	 * @return Returns name part of the reference.
	 */
	public String getName() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return nodeNames.get(nodeNames.size() - 1);
	}

	@Override
	public String toString() {
		return objectReference;
	}

	public boolean isLogicalDeviceRef() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return (nodeNames.size() == 1);
	}

	public boolean isLogicalNodeRef() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return (nodeNames.size() == 2);
	}

	public boolean isDataRef() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return (nodeNames.size() > 2);
	}

	int getArrayIndexPosition() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return arrayIndexPosition;
	}

	public Iterator<String> iterator() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return nodeNames.iterator();
	}

	public String get(int i) {
		if (nodeNames == null) {
			parseForNameList();
		}
		return nodeNames.get(i);
	}

	public int size() {
		if (nodeNames == null) {
			parseForNameList();
		}
		return nodeNames.size();
	}

	private void parseForNameList() {

		nodeNames = new LinkedList<String>();

		int lastDelim = -1;
		int nextDelim = objectReference.indexOf('/');
		if (nextDelim == -1) {
			nodeNames.add(objectReference.substring(lastDelim + 1, objectReference.length()));
			return;
		}

		nodeNames.add(objectReference.substring(lastDelim + 1, nextDelim));

		int dotIndex = -1;
		int openingbracketIndex = -1;
		int closingbracketIndex = -1;
		while (true) {
			lastDelim = nextDelim;
			if (dotIndex == -1) {
				dotIndex = objectReference.indexOf('.', lastDelim + 1);
				if (dotIndex == -1) {
					dotIndex = objectReference.length();
				}
			}
			if (openingbracketIndex == -1) {
				openingbracketIndex = objectReference.indexOf('(', lastDelim + 1);
				if (openingbracketIndex == -1) {
					openingbracketIndex = objectReference.length();
				}
			}
			if (closingbracketIndex == -1) {
				closingbracketIndex = objectReference.indexOf(')', lastDelim + 1);
				if (closingbracketIndex == -1) {
					closingbracketIndex = objectReference.length();
				}
			}

			if (dotIndex == openingbracketIndex && dotIndex == closingbracketIndex) {
				nodeNames.add(objectReference.substring(lastDelim + 1, objectReference.length()));
				return;
			}

			if (dotIndex < openingbracketIndex && dotIndex < closingbracketIndex) {
				nextDelim = dotIndex;
				dotIndex = -1;
			}
			else if (openingbracketIndex < dotIndex && openingbracketIndex < closingbracketIndex) {
				nextDelim = openingbracketIndex;
				openingbracketIndex = -1;
				arrayIndexPosition = nodeNames.size() + 1;
			}
			else if (closingbracketIndex < dotIndex && closingbracketIndex < openingbracketIndex) {
				if (closingbracketIndex == (objectReference.length() - 1)) {
					nodeNames.add(objectReference.substring(lastDelim + 1, closingbracketIndex));
					return;
				}
				nextDelim = closingbracketIndex;
				closingbracketIndex = -1;
			}
			nodeNames.add(objectReference.substring(lastDelim + 1, nextDelim));
		}
	}
}
