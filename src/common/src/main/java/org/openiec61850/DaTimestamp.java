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

import java.util.Calendar;
import java.util.Date;

import org.openiec61850.jmms.mms.asn1.Data;
import org.openiec61850.jmms.mms.asn1.TypeSpecification;
import org.openmuc.jasn1.ber.types.BerNull;
import org.openmuc.jasn1.ber.types.BerOctetString;

public class DaTimestamp extends BasicDataAttribute {

	private byte[] value;

	/**
	 * The SecondSinceEpoch shall be the interval in seconds continuously
	 * counted from the epoch 1970-01-01 00:00:00 UTC
	 */
	public long getSecondsSinceEpoch() {
		return ((0xffL & value[0]) << 24 | (0xffL & value[1]) << 16 | (0xffL & value[2]) << 8 | (0xffL & value[3]));
	}

	/**
	 * The attribute FractionOfSecond shall be the fraction of the current
	 * second when the value of the TimeStamp has been determined. The fraction
	 * of second shall be calculated as
	 * <code>(SUM from I = 0 to 23 of bi*2**–(I+1) s).</code>
	 * 
	 * NOTE 1 The resolution is the smallest unit by which the time stamp is
	 * updated. The 24 bits of the integer provides 1 out of 16777216 counts as
	 * the smallest unit; calculated by 1/2**24 which equals approximately 60
	 * ns.
	 * 
	 * NOTE 2 The resolution of a time stamp may be 1/2**1 (= 0,5 s) if only the
	 * first bit is used; or may be 1/2**2 (= 0,25 s) if the first two bits are
	 * used; or may be approximately 60 ns if all 24 bits are used. The
	 * resolution provided by an IED is outside the scope of this standard.
	 */
	public int getFractionOfSecond() {
		return ((0xff & value[4]) << 16 | (0xff & value[5]) << 8 | (0xff & value[6]));
	}

	public DaTimestamp(ObjectReference objectReference, FunctionalConstraint fc, String sAddr, byte[] value,
			boolean dchg, boolean dupd) {
		this.objectReference = objectReference;
		this.fc = fc;
		this.sAddr = sAddr;
		this.basicType = DaType.TIMESTAMP;
		this.value = value;
		this.dchg = dchg;
		this.dupd = dupd;
	}

	public void setDate(Date date) {

		int secondsSinceEpoch = (int) (date.getTime() / 1000L);
		int fractionOfSecond = (int) ((date.getTime() % 1000L) / 1000.0 * (1 << 24));

		// 0x8a = time accuracy of 10 and LeapSecondsKnown = true, ClockFailure
		// = false, ClockNotSynchronized = false
		value = new byte[] { (byte) ((secondsSinceEpoch >> 24) & 0xff), (byte) ((secondsSinceEpoch >> 16) & 0xff),
				(byte) ((secondsSinceEpoch >> 8) & 0xff), (byte) (secondsSinceEpoch & 0xff),
				(byte) ((fractionOfSecond >> 16) & 0xff), (byte) ((fractionOfSecond >> 8) & 0xff),
				(byte) (fractionOfSecond & 0xff), (byte) 0x8a };

	}

	public void setDate(Date date, boolean leapSecondsKnown, boolean clockFailure, boolean clockNotSynchronized,
			int timeAccuracy) {

		int secondsSinceEpoch = (int) (date.getTime() / 1000L);
		int fractionOfSecond = (int) ((date.getTime() % 1000L) / 1000.0 * (1 << 24));

		int timeQuality = timeAccuracy & 0x1f;
		if (leapSecondsKnown) {
			timeQuality = timeQuality | 0x80;
		}
		if (clockFailure) {
			timeQuality = timeQuality | 0x40;
		}
		if (clockNotSynchronized) {
			timeQuality = timeQuality | 0x20;
		}

		value = new byte[] { (byte) ((secondsSinceEpoch >> 24) & 0xff), (byte) ((secondsSinceEpoch >> 16) & 0xff),
				(byte) ((secondsSinceEpoch >> 8) & 0xff), (byte) (secondsSinceEpoch & 0xff),
				(byte) ((fractionOfSecond >> 16) & 0xff), (byte) ((fractionOfSecond >> 8) & 0xff),
				(byte) (fractionOfSecond & 0xff), (byte) timeQuality };

	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (byte[]) value;
	}

	public Date getDate() {
		if (value == null) {
			return null;
		}
		long time = getSecondsSinceEpoch() * 1000L + (long) (((float) getFractionOfSecond()) / (1 << 24) * 1000 + 0.5);
		return new Date(time);
	}

	@Override
	public byte[] getValue() {
		return value;
	}

	/**
	 * The value TRUE of the attribute LeapSecondsKnown shall indicate that the
	 * value for SecondSinceEpoch takes into account all leap seconds occurred.
	 * If it is FALSE then the value does not take into account the leap seconds
	 * that occurred before the initialization of the time source of the device.
	 * 
	 * Java {@link Date} and {@link Calendar} objects do handle leap seconds, so
	 * this is usually true.
	 */
	public boolean getLeapSecondsKnown() {
		return ((value[7] & 0x80) != 0);
	}

	/**
	 * The attribute clockFailure shall indicate that the time source of the
	 * sending device is unreliable. The value of the TimeStamp shall be
	 * ignored.
	 */
	public boolean getClockFailure() {
		return ((value[7] & 0x40) != 0);
	}

	/**
	 * The attribute clockNotSynchronized shall indicate that the time source of
	 * the sending device is not synchronized with the external UTC time.
	 */
	public boolean getClockNotSynchronized() {
		return ((value[7] & 0x20) != 0);
	}

	/**
	 * The attribute TimeAccuracy shall represent the time accuracy class of the
	 * time source of the sending device relative to the external UTC time. The
	 * timeAccuracy classes shall represent the number of significant bits in
	 * the FractionOfSecond
	 * 
	 * If the time is set via Java {@link Date} objects, the accuracy is 1 ms,
	 * that is a timeAccuracy value of 10.
	 */
	public int getTimeAccuracy() {
		return ((value[7] & 0x1f));
	}

	/**
	 * Sets Timestamp to 8Byte with all zeros
	 */
	@Override
	public void setDefault() {
		value = new byte[8];
	}

	/**
	 * Sets Timestamp to current time
	 */
	public void setCurrentTime() {
		setDate(new Date());
	}

	@Override
	public DaTimestamp copy() {
		return new DaTimestamp(objectReference, fc, sAddr, value, dchg, dupd);
	}

	@Override
	Data getMmsDataObj() {
		if (value == null) {
			return null;
		}
		return new Data(null, null, null, null, null, null, null, null, null, null, null, new BerOctetString(value));
	}

	@Override
	void setValueFromMmsDataObj(Data data) throws ServiceError {
		if (data.utc_time == null) {
			throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: utc_time/timestamp");
		}
		value = data.utc_time.octetString;
	}

	@Override
	TypeSpecification getMmsTypeSpec() {
		return new TypeSpecification(null, null, null, null, null, null, null, null, null, null, null, new BerNull());
	}

	@Override
	public String toString() {
		return getReference().toString() + ": " + getDate();
	}

}
