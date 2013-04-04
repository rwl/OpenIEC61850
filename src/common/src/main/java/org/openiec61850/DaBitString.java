package org.openiec61850;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerInteger;

public final class DaBitString extends BasicDataAttribute {

	private byte[] value;
	private int maxNumBits;
	private BitStringType bitStringType;

	public DaBitString(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			int maxNumBits, BitStringType bitStringType, boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.basicType = DaType.BIT_STRING;
		this.sAddr = sAddr;
		this.value = value;
		this.maxNumBits = maxNumBits;
		this.bitStringType = bitStringType;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (byte[]) value;
	}

	public int getMaxNumBits() {
		return maxNumBits;
	}

	public BitStringType getBitStringType() {
		return bitStringType;
	}

	/**
	 * Initializes BIT_STRING with all zeros
	 */
	@Override
	public void setDefault() {
		value = new byte[(maxNumBits / 8 + (((maxNumBits % 8) > 0) ? 1 : 0))];
	}

	@Override
	public DaBitString copy() {
		return new DaBitString(objectReference, fc, sAddr, value, maxNumBits, bitStringType, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, new BerBitString(value, maxNumBits), null, null, null, null, null, null,
				null, null);
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.bit_string == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: bit_string");
		}
		if (data.bit_string.numBits > maxNumBits) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, objectReference
					+ ": bit_string is bigger than maxNumBits: " + data.bit_string.numBits + ">" + maxNumBits);
		}
		value = data.bit_string.bitString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, new BerInteger(maxNumBits * -1), null, null, null, null, null,
				null, null, null);
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + value;
	}

}
