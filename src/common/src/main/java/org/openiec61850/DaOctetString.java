package org.openiec61850;

import java.util.Arrays;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerOctetString;

public class DaOctetString extends BasicDataAttribute {

	private byte[] value;
	private int maxLength;

	public DaOctetString(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			int maxLength, boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.basicType = DaType.OCTET_STRING;
		this.sAddr = sAddr;
		this.value = value;
		this.maxLength = maxLength;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		if (value.length > maxLength) {
			throw new IllegalArgumentException("OCTET_STRING value size exceeds maxLength of " + maxLength);
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
	public DaOctetString copy() {
		return new DaOctetString(objectReference, fc, sAddr, value, maxLength, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, null, new BerOctetString(value), null, null, null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.octet_string == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: octet_string");
		}
		value = data.octet_string.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, null, new BerInteger(maxLength * -1), null,
				null, null, null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + Arrays.toString(value);
	}

}
