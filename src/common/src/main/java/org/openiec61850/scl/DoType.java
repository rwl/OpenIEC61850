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
 */

class DoType extends AbstractType {

	private String cdc = null;
	private String iedType = null; /* optional */
	private Vector<Da> dAttributes;
	private Vector<Sdo> sdos;

	private static Logger logger = LoggerFactory.getLogger(DoType.class);

	// public DOType(String id, String desc, String cdc) {
	// super(id, desc);
	// this.cdc = cdc;
	// }

	public DoType(Node xmlNode) throws SclParseException {
		/* Parse attributes */
		super(xmlNode);

		NamedNodeMap attributes = xmlNode.getAttributes();
		Node node;

		if (attributes != null) {
			String nodeName;

			for (int i = 0; i < attributes.getLength(); i++) {
				node = attributes.item(i);
				nodeName = node.getNodeName();

				if (nodeName.equals("cdc")) {
					this.cdc = node.getNodeValue();
				}
				else if (nodeName.equals("iedType")) {
					this.iedType = node.getNodeValue();
				}
			}
		}
		else {
			logger.error("AbstractElement(): attributes = NULL!");
		}

		if (this.cdc == null) {
			throw new SclParseException("Required attribute \"cdc\" not found!");
		}

		/* Parse contained SDOs and DAs */
		NodeList elements = xmlNode.getChildNodes();

		if (elements != null) {
			this.dAttributes = new Vector<Da>();
			this.sdos = new Vector<Sdo>();

			for (int i = 0; i < elements.getLength(); i++) {
				node = elements.item(i);

				if (node.getNodeName().equals("SDO")) {
					// System.out.println("SDO:");
					this.sdos.add(new Sdo(node));
				}

				if (node.getNodeName().equals("DA")) {
					// System.out.println("DA:");
					this.dAttributes.add(new Da(node));
				}
			}
		}

	} /* DOType(Node xmlNode) */

	public String getCdc() {
		return cdc;
	}

	public String getIedType() {
		return iedType;
	}

	public Vector<Da> getdAttributes() {
		return dAttributes;
	}

	public Vector<Sdo> getSdos() {
		return sdos;
	}

}
