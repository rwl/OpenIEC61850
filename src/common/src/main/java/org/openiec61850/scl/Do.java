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
class Do extends AbstractElement {
	private String type;
	private boolean trans; /* ??? transient flag */
	private String accessControl; /* ??? */

	private static Logger logger = LoggerFactory.getLogger(Do.class);

	public Do(String name, String desc, String type) {
		super(name, desc);
	}

	/**
	 * 
	 * @param xmlNode
	 * @throws SclParseException
	 */
	public Do(Node xmlNode) throws SclParseException {
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
				}
				else if (nodeName.equals("accessControl")) {
					this.accessControl = node.getNodeValue();
				}
				else if (nodeName.equals("transient")) {
					this.trans = Util.parseBooleanValue(node.getNodeValue());
				}
			}
		}
		else {
			logger.error("DO(): attributes = NULL!");
		}

		if (this.type == null) {
			throw new SclParseException("Required attribute \"type\" not found!");
		}
	}

	public String getType() {
		return type;
	}

	public boolean isTrans() {
		return trans;
	}

	public String getAccessControl() {
		return accessControl;
	}

}
