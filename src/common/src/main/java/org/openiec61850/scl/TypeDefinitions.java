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

class TypeDefinitions {
	private final Vector<LnType> lnodeTypes = new Vector<LnType>();
	private final Vector<DoType> doTypes = new Vector<DoType>();
	private final Vector<DaType> daTypes = new Vector<DaType>();
	private final Vector<EnumType> enumTypes = new Vector<EnumType>();

	public TypeDefinitions() {
	}

	void putLNodeType(LnType lnodeType) {
		lnodeTypes.add(lnodeType);
	}

	void putDOType(DoType doType) {
		doTypes.add(doType);
	}

	void putDAType(DaType daType) {
		daTypes.add(daType);
	}

	void putEnumType(EnumType enumType) {
		enumTypes.add(enumType);
	}

	public DaType getDAType(String daType) {
		for (DaType datype : daTypes) {
			if (datype.getId().equals(daType)) {
				return datype;
			}
		}

		return null;
	}

	public DoType getDOType(String doType) {
		for (DoType dotype : doTypes) {
			if (dotype.getId().equals(doType)) {
				return dotype;
			}
		}

		return null;
	}

	public LnType getLNodeType(String lnType) {

		for (LnType ntype : lnodeTypes) {
			if (ntype.getId().equals(lnType)) {
				return ntype;
			}
		}

		return null;
	}

	public EnumType getEnumType(String enumTypeRef) {
		for (EnumType enumType : enumTypes) {
			if (enumType.getId().equals(enumTypeRef)) {
				return enumType;
			}
		}

		return null;
	}

}
