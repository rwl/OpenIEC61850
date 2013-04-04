/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2012
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

import java.nio.ByteBuffer;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_floating_point;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerOctetString;

public final class DaFloat64 extends BasicDataAttribute {

	private byte[] value;

	public DaFloat64(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.basicType = DaType.FLOAT64;
		this.value = value;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	public void setValue(byte[] value) {
		if (value != null && value.length != 9) {
			throw new IllegalArgumentException("value does not have length 9");
		}
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		setValue((byte[]) value);
	}

	public void setDouble(Double value) {
		this.value = ByteBuffer.allocate(1 + 8).put((byte) 11).putDouble(value).array();
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public Double getDouble() {
		if (value == null) {
			return null;
		}
		return Double.longBitsToDouble(((0xffL & ((long) value[1])) << 56) | ((0xffL & ((long) value[2])) << 48)
				| ((0xffL & ((long) value[3])) << 40) | ((0xffL & ((long) value[4])) << 32)
				| ((0xffL & ((long) value[5])) << 24) | ((0xffL & ((long) value[6])) << 16)
				| ((0xffL & ((long) value[7])) << 8) | ((0xffL & ((long) value[8])) << 0));
	}

	@Override
	public void setDefault() {
		value = new byte[9];
	}

	@Override
	public DaFloat64 copy() {
		return new DaFloat64(objectReference, fc, sAddr, value, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, new BerOctetString(), null, null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.floating_point == null || data.floating_point.octetString.length != 9) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT,
					"expected type: floating_point as an octet string of size 9");
		}
		value = data.floating_point.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, new SubSeq_floating_point(new BerInteger(64),
				new BerInteger(11)), null, null, null, null, null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + getDouble();
	}

}
