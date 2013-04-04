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
package org.openiec61850.server.data;

import java.util.List;
import java.util.Set;

import org.openiec61850.AccessPoint;
import org.openiec61850.BasicDataAttribute;
import org.openiec61850.ServiceError;

/**
 * The IEC server AccessPoint reads and writes data from and to so called
 * DataSources. These data sources are external to the openIEC61850 library
 * stack and are accessed via this interface. For every AccessPoint there must
 * be an implementation of this interface. These implementations are not part of
 * the openIEC61850 stack but of the concrete applications that make use of the
 * stack.
 * 
 * @author bertram
 * @author Stefan Feuerhahn
 */
public interface DataSource extends Runnable {

	/**
	 * Inserts the proper values in the given BasicDataAttributes using
	 * setValue(). This function is called by the openIEC61850 server library
	 * whenever a GetDataValues or GetDataSetValues service request comes in.
	 * 
	 */
	public void readValues(List<BasicDataAttribute> basicDataAttributes) throws ServiceError;

	/**
	 * Takes the values from the given list of BasicDataAttributes.
	 * 
	 */
	public void writeValues(List<BasicDataAttribute> basicDataAttributes) throws ServiceError;

	/**
	 * Initiates the DataSource with a reference back to the Access point and
	 * all triggerable nodes
	 */

	public void initialize(AccessPoint ac, Set<String> triggerDchg, Set<String> triggerQchg, Set<String> triggerDupd);

}
