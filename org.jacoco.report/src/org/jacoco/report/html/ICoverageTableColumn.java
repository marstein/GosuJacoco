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
package org.jacoco.report.html;

import java.io.IOException;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;

/**
 * Renderer for a single column of a coverage table. The methods are always
 * called in the sequence <code>init header footer item*</code>. Implementations
 * might be stateful.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface ICoverageTableColumn {

	/**
	 * Initializes the column before any output method is called.
	 * 
	 * @param items
	 *            all items that will be displayed in the table
	 * @param total
	 *            the summary of all coverage data items in the table
	 */
	public void init(List<ICoverageTableItem> items, ICoverageNode total);

	/**
	 * Renders the header for this column.
	 * 
	 * @param tr
	 *            the parent table row
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	public void header(HTMLElement tr, Resources resources,
			ReportOutputFolder base) throws IOException;

	/**
	 * Renders the footer for this column.
	 * 
	 * @param tr
	 *            the parent table row
	 * @param total
	 *            the summary of all coverage data items in the table
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	public void footer(HTMLElement tr, ICoverageNode total,
			Resources resources, ReportOutputFolder base) throws IOException;

	/**
	 * Renders a single item in this column.
	 * 
	 * @param tr
	 *            the parent table row
	 * @param item
	 *            the item to display
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	public void item(HTMLElement tr, ICoverageTableItem item,
			Resources resources, ReportOutputFolder base) throws IOException;

}
