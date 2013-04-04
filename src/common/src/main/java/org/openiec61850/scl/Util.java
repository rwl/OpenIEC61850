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
package org.openiec61850.scl;

class Util {
	static boolean parseBooleanValue(String value) throws SclParseException {
		if (value.equals("true")) {
			return true;
		}
		else if (value.equals("false")) {
			return false;
		}
		else {
			throw new SclParseException("Not a boolean value");
		}
	}

	static boolean isBasicType(String bType) throws SclParseException {
		if (bType.equals("BOOLEAN")) {
			return true;
		}
		if (bType.equals("INT8")) {
			return true;
		}
		if (bType.equals("INT16")) {
			return true;
		}
		if (bType.equals("INT32")) {
			return true;
		}
		if (bType.equals("INT64")) {
			return true;
		}
		if (bType.equals("INT8U")) {
			return true;
		}
		if (bType.equals("INT16U")) {
			return true;
		}
		if (bType.equals("INT32U")) {
			return true;
		}
		if (bType.equals("FLOAT32")) {
			return true;
		}
		if (bType.equals("FLOAT64")) {
			return true;
		}
		if (bType.equals("Timestamp")) {
			return true;
		}
		if (bType.equals("VisString32")) {
			return true;
		}
		if (bType.equals("VisString64")) {
			return true;
		}
		if (bType.equals("VisString65")) {
			return true;
		}
		if (bType.equals("VisString129")) {
			return true;
		}
		if (bType.equals("VisString255")) {
			return true;
		}
		if (bType.equals("Unicode255")) {
			return true;
		}
		if (bType.equals("Octet64")) {
			return true;
		}
		if (bType.equals("Struct")) {
			return false;
		}
		if (bType.equals("Enum")) {
			return true;
		}
		if (bType.equals("Quality")) {
			return true;
		}
		if (bType.equals("Check")) {
			return true;
		}
		if (bType.equals("Dbpos")) {
			return true;
		}
		if (bType.equals("Tcmd")) {
			return true;
		}

		throw new SclParseException("Invalid bType: " + bType);
	}

}
