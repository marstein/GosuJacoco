/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Locator that searches source files in multiple {@link ISourceFileLocator}
 * instances. For each lookup request the first locator that returns a
 * {@link Reader} for source content is selected.
 */
public class MultiSourceFileLocator implements ISourceFileLocator {

	private final int tabWidth;

	private final List<ISourceFileLocator> delegates;

	/**
	 * Creates a new empty locator.
	 * 
	 * @param tabWidth
	 *            tab width in source files as number of blanks used for all
	 *            source files
	 */
	public MultiSourceFileLocator(final int tabWidth) {
		this.tabWidth = tabWidth;
		this.delegates = new ArrayList<ISourceFileLocator>();
	}

	/**
	 * Adds the given locator. Locators are queried in the sequence they have
	 * been added.
	 * 
	 * @param locator
	 *            Additional locator to query
	 */
	public void add(final ISourceFileLocator locator) {
		delegates.add(locator);
	}

	public Reader getSourceFile(final String packageName, final String fileName)
			throws IOException {
		for (final ISourceFileLocator d : delegates) {
			final Reader reader = d.getSourceFile(packageName, fileName);
			if (reader != null) {
				return reader;
			}
		}
		return null;
	}

	public int getTabWidth() {
		return tabWidth;
	}

}
