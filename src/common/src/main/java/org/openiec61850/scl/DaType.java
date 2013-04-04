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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class DaType extends AbstractType {

	private Vector<Bda> bdas;

	public DaType(String id, String desc) {
		super(id, desc);
	}

	public DaType(Node xmlNode) throws SclParseException {
		super(xmlNode);

		/* Simply ignore iedType attribute */

		/* Parse BDAs */
		NodeList elements = xmlNode.getChildNodes();

		if (elements != null) {
			Node node;

			this.bdas = new Vector<Bda>();

			for (int i = 0; i < elements.getLength(); i++) {
				node = elements.item(i);

				if (node.getNodeName().equals("BDA")) {
					this.bdas.add(new Bda(node));
				}

			}
		}
	} /* DAType(Node) */

	public Vector<Bda> getBdas() {
		return bdas;
	}
}
