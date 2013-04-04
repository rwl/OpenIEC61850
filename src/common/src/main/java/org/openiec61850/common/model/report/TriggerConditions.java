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
package org.openiec61850.common.model.report;

import org.openiec61850.ModelNode;
import org.openiec61850.ObjectReference;

public class TriggerConditions extends ModelNode {

	// public static final TriggerConditions DATA_CHANGE = new
	// TriggerConditions(true, false, false, false);
	// public static final TriggerConditions DATA_UPDATE = new
	// TriggerConditions(false, false, true, false);
	// public static final TriggerConditions QUALITY_CHANGE = new
	// TriggerConditions(false, true, false, false);
	// public static final TriggerConditions NO_TRIGGER = new
	// TriggerConditions(false, false, false, false);
	// public static final TriggerConditions INTEGRITY = new
	// TriggerConditions(false, false, false, true);

	// private String typeID = "TriggerConditions";

	private boolean dataChange = false;
	private boolean qualityChange = false;
	private boolean dataUpdate = false;
	private boolean integrity = false;
	/* default of GI is true for backwards compatibility IEC 61850-6 sec. 9.3.8 */
	private boolean generalInterrogation = true;

	// private BitSet value;
	private byte[] value = new byte[1];

	public TriggerConditions() {

	}

	public TriggerConditions(ObjectReference objectReference) {
		this.objectReference = objectReference;
		// this.fc = null;
		/* can also make the members BasicDataAttributes and add to children */
		this.children = null;
		// value = new BitSet(6);
		// value.set(5, false);
		value[0] = (byte) (value[0] & ~(1 << 7)); // reserved and set to 0. See
													// iec61850-8-1 sec. 8.1.3.9
	}

	// public TriggerConditions(boolean dchg, boolean qchg, boolean dupd,
	// boolean integrity) {
	// this.dataChange = dchg;
	// this.qualityChange = qchg;
	// this.dataUpdate = dupd;
	// this.integrity = integrity;
	// }

	public boolean isDataChange() {
		return dataChange;
	}

	public boolean isQualityChange() {
		return qualityChange;
	}

	public boolean isDataUpdate() {
		return dataUpdate;
	}

	public boolean isIntegrity() {
		return integrity;
	}

	public boolean isGeneralInterrogation() {
		return generalInterrogation;
	}

	public void setDataChange(boolean dataChange) {
		this.dataChange = dataChange;
		// value.set(4, dataChange);
		if (dataChange) {
			value[0] = (byte) (value[0] | (1 << 6));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 6));
		}

	}

	public void setQualityChange(boolean qualityChange) {
		this.qualityChange = qualityChange;
		// value.set(3, qualityChange);
		if (qualityChange) {
			value[0] = (byte) (value[0] | (1 << 5));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 5));
		}
	}

	public void setDataUpdate(boolean dataUpdate) {
		this.dataUpdate = dataUpdate;
		// value.set(2, dataUpdate);
		if (dataUpdate) {
			value[0] = (byte) (value[0] | (1 << 4));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 4));
		}
	}

	public void setIntegrity(boolean integrity) {
		this.integrity = integrity;
		// value.set(1, integrity);
		if (integrity) {
			value[0] = (byte) (value[0] | (1 << 3));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 3));
		}
	}

	public void setGeneralInterrogation(boolean generalInterrogation) {
		this.generalInterrogation = generalInterrogation;
		// value.set(0, generalInterrogation);
		if (generalInterrogation) {
			value[0] = (byte) (value[0] | (1 << 2));
		}
		else {
			value[0] = (byte) (value[0] & ~(1 << 2));
		}
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] data) {
		value = data;

		if ((value[0] & 0x04) == 0x04) {
			generalInterrogation = true;
		}
		else {
			generalInterrogation = false;
		}

		if ((value[0] & 0x08) == 0x08) {
			integrity = true;
		}
		else {
			integrity = false;
		}

		if ((value[0] & 0x10) == 0x10) {
			dataUpdate = true;
		}
		else {
			dataUpdate = false;
		}

		if ((value[0] & 0x20) == 0x20) {
			qualityChange = true;
		}
		else {
			qualityChange = false;
		}

		if ((value[0] & 0x04) == 0x04) {
			dataChange = true;
		}
		else {
			dataChange = false;
		}
	}

	// public boolean isAnySet() {
	// return dataChange || dataUpdate || generalInterrogation || integrity ||
	// qualityChange;
	// }

	// public boolean matches(TriggerConditions mask) {
	// return (isDataChange() && mask.isDataChange()) || (isQualityChange() &&
	// mask.isQualityChange())
	// || (isDataUpdate() && mask.isDataUpdate())
	// || (isGeneralInterrogation() && mask.isGeneralInterrogation()) ||
	// (isIntegrity() && mask.isIntegrity());
	// }

	// public TriggerConditions combine(TriggerConditions other) {
	// if (other == null) {
	// return this;
	// }
	// TriggerConditions newTC = new TriggerConditions();
	// newTC.setDataChange(dataChange || other.dataChange);
	// newTC.setDataUpdate(dataUpdate || other.dataUpdate);
	// newTC.setGeneralInterrogation(generalInterrogation ||
	// other.generalInterrogation);
	// newTC.setIntegrity(integrity || other.integrity);
	// newTC.setQualityChange(qualityChange || other.qualityChange);
	// return newTC;
	// }

	// public TriggerConditions copyDeep() {
	// try {
	// return (TriggerConditions) this.clone();
	// } catch (CloneNotSupportedException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public void setDefault() {
		dataChange = false;
		qualityChange = false;
		dataUpdate = false;
		integrity = false;
		/* default of GI is true for backwards compatibility IEC 61850-6 */
		generalInterrogation = true;

	}

	@Override
	public ModelNode copy() {
		// try {
		// return (TriggerConditions) this.clone();
		// } catch (CloneNotSupportedException e) {
		// e.printStackTrace();
		// }
		// return null;

		TriggerConditions copy = new TriggerConditions(this.objectReference);
		copy.setDataChange(this.dataChange);
		copy.setDataUpdate(this.dataUpdate);
		copy.setQualityChange(this.qualityChange);
		copy.setIntegrity(this.integrity);
		copy.setGeneralInterrogation(this.generalInterrogation);

		return copy;

	}

}
