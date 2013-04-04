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
import org.openmuc.jasn1.ber.types.BerBoolean;
import org.openmuc.jasn1.ber.types.BerOctetString;

public class DaEntryTime extends BasicDataAttribute {

	private byte[] value;

	public DaEntryTime(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.basicType = DaType.TIMESTAMP;
		this.value = value;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (byte[]) value;
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	/**
	 * Sets EntryTime to byte[6] with all zeros
	 */
	@Override
	public void setDefault() {
		value = new byte[6];
	}

	@Override
	public DaEntryTime copy() {
		return new DaEntryTime(objectReference, fc, sAddr, value, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, null, null, null, new BerOctetString(value), null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.binary_time == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: binary_time/EntryTime");
		}
		value = data.binary_time.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, null, null, null, new BerBoolean(true), null,
				null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + value;
	}

}
