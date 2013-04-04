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

class EnumVal {
	private String id = null;
	private Integer ord = null;

	private static Logger logger = LoggerFactory.getLogger(EnumVal.class);

	public EnumVal(String id, int ord) {
		this.id = id;
		this.ord = ord;
	}

	public EnumVal(Node xmlNode) throws SclParseException {
		this.id = xmlNode.getTextContent();

		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("ord")) {
					this.ord = Integer.parseInt(node.getNodeValue());
				}
			}
		}
		else {
			logger.error("EnumVal(): attributes = NULL!");
		}

		if (this.ord == null) {
			throw new SclParseException("Required attribute \"ord\" not found!");
		}
	}

	public String getId() {
		return id;
	}

	public int getOrd() {
		return ord;
	}
}
