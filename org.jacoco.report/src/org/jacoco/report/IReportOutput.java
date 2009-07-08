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
package org.jacoco.report;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface to emit binary files.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IReportOutput {

	/**
	 * Creates a file at the given local path. The returned {@link OutputStream}
	 * has to be closed before the next document is created.
	 * 
	 * @param path
	 *            local path to the new document
	 * @return output for the content
	 * @throws IOException
	 *             if the creation fails
	 */
	public OutputStream createFile(String path) throws IOException;

}
