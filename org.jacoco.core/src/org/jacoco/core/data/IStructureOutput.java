/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of the internal class structure. This interface is
 * meant to be implemented by parties that want to retrieve data from the class
 * analyzing process.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IStructureOutput {

	/**
	 * Provides structural information about a class as collected during
	 * instrumentation.
	 * 
	 * @param id
	 *            unique id for the class
	 * @param name
	 *            VM name of the class
	 * @param bundle
	 *            optional bundle identifier this class belongs to
	 * @return call-back for structure details about the class
	 * 
	 */
	public IClassStructureOutput classStructure(long id, String name,
			String bundle);

}
