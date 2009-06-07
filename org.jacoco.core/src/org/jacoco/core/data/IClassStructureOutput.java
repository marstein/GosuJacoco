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
 * Interface for data output of the internal structure of a single class. This
 * interface is meant to be implemented by parties that want to retrieve data
 * from the instrumentation process.
 */
public interface IClassStructureOutput {

	/**
	 * The source file name might be reported through this method call.
	 * 
	 * @param name
	 *            name of the corresponding source file
	 */
	public void sourceFile(String name);

	/**
	 * Called for every instrumented method.
	 * 
	 * @param id
	 *            identifier of this method within the class
	 * @param name
	 *            name of the method
	 * @param desc
	 *            parameter and return value description
	 * @param signature
	 *            generic signature or <code>null</code>
	 * @return call-back for structure details about the method
	 */
	public IMethodStructureOutput methodStructure(int id, String name,
			String desc, String signature);

	/**
	 * Called after all information for this class has been emitted.
	 */
	public void end();

}
