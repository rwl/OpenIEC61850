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

abstract class AbstractDataAttribute extends AbstractElement {

	private String sAddr = null; /* optional - short address */
	private String bType = null; /* mandatory - basic type */
	private final int valKind = 0; /*
									 * optional/conditional - how to interpret
									 * value if one is given
									 */
	private String type = null; /* conditional - if bType = "Enum" or "Struct" */
	private int count = 0; /* optional - number of array elements */
	private Vector<Value> values = null;

	private static Logger logger = LoggerFactory.getLogger(AbstractDataAttribute.class);

	public AbstractDataAttribute(Node xmlNode) throws SclParseException {
		super(xmlNode);

		/* Parse attributes */
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
				else if (nodeName.equals("sAddr")) {
					this.sAddr = node.getNodeValue();
				}
				else if (nodeName.equals("bType")) {
					this.bType = node.getNodeValue();
				}
				else if (nodeName.equals("count")) {
					this.count = Integer.parseInt(node.getNodeValue());
				}
			}
		}
		else {
			logger.error("AbstractDataAttribute(): attributes = NULL!");
		}

		if (this.bType == null) {
			throw new SclParseException("Required attribute \"bType\" not found!");
		}

		/* Parse Val elements */
		NodeList elements = xmlNode.getChildNodes();

		if (elements != null) {
			this.values = new Vector<Value>();

			for (int i = 0; i < elements.getLength(); i++) {
				node = elements.item(i);

				if (node.getNodeName().equals("Val")) {
					throw new SclParseException("AbstractDataAttribute(): Val not implemented!");
					// this.sdos.add(new SDO(node));
				}
			}
		}

	}

	public String getsAddr() {
		return sAddr;
	}

	public String getbType() {
		return bType;
	}

	public int getValKind() {
		return valKind;
	}

	public String getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public Vector<Value> getValues() {
		return values;
	}

}
