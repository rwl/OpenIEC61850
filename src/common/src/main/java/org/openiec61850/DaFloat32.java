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

import java.nio.ByteBuffer;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openiec61850.jmms.mms.asn1.TypeSpecification.SubSeq_floating_point;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerOctetString;

public final class DaFloat32 extends BasicDataAttribute {

	private byte[] value;

	public DaFloat32(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.basicType = DaType.FLOAT32;
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

	public void setFloat(Float value) {
		this.value = ByteBuffer.allocate(1 + 4).put((byte) 8).putFloat(value).array();
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public Float getFloat() {
		if (value == null) {
			return null;
		}
		return Float.intBitsToFloat(((0xff & value[1]) << 24) | ((0xff & value[2]) << 16) | ((0xff & value[3]) << 8)
				| ((0xff & value[4]) << 0));
	}

	@Override
	public void setDefault() {
		value = new byte[5];
	}

	@Override
	public DaFloat32 copy() {
		return new DaFloat32(objectReference, fc, sAddr, value, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, new BerOctetString(value), null, null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.floating_point == null || data.floating_point.octetString.length != 5) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT,
					"expected type: floating_point as an octet string of size 5");
		}
		value = data.floating_point.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, new SubSeq_floating_point(new BerInteger(32),
				new BerInteger(8)), null, null, null, null, null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + getFloat();
	}

}
