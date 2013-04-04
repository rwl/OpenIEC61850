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
import org.openmuc.jasn1.ber.types.string.BerUTF8String;

public class DaUnicodeString extends BasicDataAttribute {

	private byte[] value;
	private int maxLength;

	public DaUnicodeString(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			int maxLength, boolean dchg, boolean dupd) {
		if (value != null && value.length > maxLength) {
			throw new IllegalArgumentException("max_length less than value.length().");
		}
		this.objectReference = objectReference;
		this.fc = fc;
		this.basicType = DaType.UNICODE_STRING;
		this.sAddr = sAddr;
		this.value = value;
		this.dchg = dchg;
		this.dupd = dupd;
		this.maxLength = maxLength;
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		if (value.length > maxLength) {
			throw new IllegalArgumentException("UNICODE_STRING value size exceeds maxLength of " + maxLength);
		}
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		setValue((byte[]) value);
	}

	public int getMaxLength() {
		return maxLength;
	}

	@Override
	public void setDefault() {
		value = new byte[maxLength];
	}

	@Override
	public DaUnicodeString copy() {
		return new DaUnicodeString(objectReference, fc, sAddr, value, maxLength, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, null, null, null, null, new BerUTF8String(value), null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.mms_string == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: mms_string/unicode_string");
		}
		value = data.mms_string.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, null, null, null, null, new BerInteger(
				maxLength * -1), null);
	}

	@Override
	public String toString() {
		if (value == null) {
			return getReference().toString() + ": null";
		}
		return getReference().toString() + ": " + new String(value);
	}

}
