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
package org.openiec61850.scl;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author mzillgit
 * 
 *         SCL-Parser: LNodeType parser and representation
 */

class LnType extends AbstractType {

	private String lnClass = null;
	private String iedType = null;
	private Vector<Do> dataObjects = null;

	private static Logger logger = LoggerFactory.getLogger(LnType.class);

	public LnType(String id, String desc) {
		super(id, desc);
	}

	public LnType(Node xmlNode) throws SclParseException {

		super(xmlNode);

		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("lnClass")) {
					this.lnClass = node.getNodeValue();
				}
				else if (nodeName.equals("iedType")) {
					this.iedType = node.getNodeValue();
				}
			}
		}
		else {
			logger.error("LNodeType(): attributes = NULL!");
		}

		if (this.lnClass == null) {
			throw new SclParseException("Required attribute \"lnClass\" not found!");
		}

		/* Parse data objects */
		NodeList elements = xmlNode.getChildNodes();

		if (elements != null) {
			this.dataObjects = new Vector<Do>();

			for (int i = 0; i < elements.getLength(); i++) {
				node = elements.item(i);

				if (node.getNodeName().equals("DO")) {
					this.dataObjects.add(new Do(node));
				}

			}
		}

	}

	public String getLnClass() {
		return lnClass;
	}

	public String getIedType() {
		return iedType;
	}

	public Vector<Do> getDataObjects() {
		return dataObjects;
	}

}
