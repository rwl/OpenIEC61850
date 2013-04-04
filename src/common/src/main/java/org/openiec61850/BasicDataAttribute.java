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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

abstract public class BasicDataAttribute extends FcModelNode {

	/** attribute value type */
	protected DaType basicType = null;

	/** short address, can be used by SCSM and for local data mapping */
	protected String sAddr = null;

	protected boolean dchg;
	protected boolean qchg;
	protected boolean dupd;

	public boolean getDchg() {
		return dchg;
	}

	public boolean getDupd() {
		return dupd;
	}

	public boolean getQchg() {
		return dupd;
	}

	public DaType getBasicType() {
		return basicType;
	}

	public String getSAddr() {
		return sAddr;
	}

	@Override
	public ModelNode getChild(String childName, FunctionalConstraint fc) {
		return null;
	}

	@Override
	public ModelNode getChild(String childName) {
		return null;
	}

	@Override
	public Collection<ModelNode> getChildren() {
		return null;
	}

	@Override
	public Iterator<ModelNode> iterator() {
		return Collections.<ModelNode> emptyList().iterator();
	}

	abstract public void setDefault();

	@Override
	public List<BasicDataAttribute> getBasicDataAttributes() {
		List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<BasicDataAttribute>();
		subBasicDataAttributes.add(this);
		return subBasicDataAttributes;
	}

	abstract public void setValue(Object value);

	abstract public Object getValue();

}
