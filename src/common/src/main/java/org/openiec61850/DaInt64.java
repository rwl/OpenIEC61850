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

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openmuc.jasn1.ber.types.BerInteger;

public final class DaInt64 extends BasicDataAttribute {

	private Long value;

	public DaInt64(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, Long value, boolean dchg,
			boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.basicType = DaType.INT64;
		this.value = value;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (Long) value;
	}

	@Override
	public Long getValue() {
		return value;
	}

	@Override
	public void setDefault() {
		value = new Long(0);
	}

	@Override
	public DaInt64 copy() {
		return new DaInt64(objectReference, fc, sAddr, value, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, new BerInteger(value), null, null, null, null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.integer == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: integer");
		}
		value = data.integer.val;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, new BerInteger(64), null, null, null, null, null, null,
				null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + value;
	}

}
