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

package org.openiec61850.bitstringhelper;

/**
 * 
 * Check packed list according to 61850-7-2
 * 
 * @author sfeuerhahn
 * 
 */
public class Check {

	public static boolean getSynchrocheck(byte[] bitStringValue) {
		return ((bitStringValue[0] & 0x80) == 0x80);
	}

	public static boolean getInterlockCheck(byte[] bitStringValue) {
		return ((bitStringValue[0] & 0x40) == 0x40);
	}

	public static byte[] getBitStringValue(boolean synchrocheck, boolean interlockCheck) {
		if (synchrocheck) {
			if (interlockCheck) {
				return new byte[] { (byte) 0xc0 };
			}
			else {
				return new byte[] { (byte) 0x80 };
			}
		}
		else {
			if (interlockCheck) {
				return new byte[] { (byte) 0x40 };
			}
			else {
				return new byte[] { 0 };
			}
		}
	}

}
