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

abstract class AbstractElement {
	private String name = null;
	private String desc = null;

	private static Logger logger = LoggerFactory.getLogger(AbstractElement.class);

	public AbstractElement(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public AbstractElement(Node xmlNode) throws SclParseException {
		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("name")) {
					this.name = node.getNodeValue();
				}
				else if (nodeName.equals("desc")) {
					this.desc = node.getNodeValue();
				}
			}
		}
		else {
			logger.error("AbstractElement(): attributes = NULL!");
		}

		if (this.name == null) {
			throw new SclParseException("Required attribute \"name\" not found!");
		}
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}
}
