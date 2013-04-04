/*
 * Copyright Fraunhofer ISE, energy & meteo Systems GmbH, and other contributors 2011
 *
 * This file is part of jMMS.
 * For more information visit http://www.openmuc.org 
 *
 * jMMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jMMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jMMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openiec61850.jmms.iso.acse;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface AcseAssociationListener {

	public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer data);

	public void serverStoppedListeningIndication(IOException e);
}
