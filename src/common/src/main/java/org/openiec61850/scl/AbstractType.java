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

/**
 * 
 * @author mzillgit
 * 
 */

abstract class AbstractType {

	private String id = null;
	private String desc = null;

	private static Logger logger = LoggerFactory.getLogger(AbstractType.class);

	public AbstractType(String id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	public AbstractType(Node xmlNode) throws SclParseException {
		NamedNodeMap attributes = xmlNode.getAttributes();

		if (attributes != null) {
			Node node;
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("id")) {
					this.id = node.getNodeValue();
				}
				else if (nodeName.equals("desc")) {
					this.desc = node.getNodeValue();
				}
			}
		}
		else {
			logger.error("AbstractType(): attributes = NULL!");
		}

		if (this.id == null) {
			throw new SclParseException("Required attribute \"id\" nod found!");
		}
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}
}
