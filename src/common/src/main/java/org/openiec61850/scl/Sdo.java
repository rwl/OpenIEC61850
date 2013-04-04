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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

class Sdo extends AbstractElement {

	private String type = null;

	private static Logger logger = LoggerFactory.getLogger(Sdo.class);

	public Sdo(String name, String desc, DoType type) {
		super(name, desc);
	}

	public Sdo(Node xmlNode) throws SclParseException {
		super(xmlNode);

		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("type")) {
					this.type = node.getNodeValue();
					// System.out.println("type = " + this.type);
				}
			}
		}
		else {
			logger.error("SDO(): attributes = NULL!");
		}

		if (this.type == null) {
			throw new SclParseException("Required attribute \"type\" not found!");
		}
	}

	public String getType() {
		return type;
	}
}
